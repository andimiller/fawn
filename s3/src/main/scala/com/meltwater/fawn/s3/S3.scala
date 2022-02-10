package com.meltwater.fawn.s3

import cats.effect.concurrent.Ref
import cats.effect.{Clock, ExitCase, Resource, Sync}
import cats.implicits._
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import com.meltwater.fawn.auth.V4Middleware
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion, AWSService}
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.scalaxml._
import org.typelevel.log4cats.Logger

import scala.util.control.NoStackTrace
import scala.xml.Elem

/** Provides methods that correspond to AWS endpoints allowing interaction with S3 on AWS
  */
trait S3[F[_]] {
  //Bucket Interaction
  def createBucket(bucket: String, optHeaders: Headers = Headers.empty): F[CreateBucketResponse]
  def deleteBucket(bucket: String, optHeaders: Headers = Headers.empty): F[Headers]
  def listBuckets(): F[ListBucketsResponse]

  //Object Interaction
  def listObjectsV2(bucket: String, optHeaders: Headers = Headers.empty): F[ListObjectsResponse]
  def putObject[T](bucket: String, key: String, t: T, optHeaders: Headers = Headers.empty)(implicit
      enc: EntityEncoder[F, T]): F[UploadFileResponse]
  def getObject[T](bucket: String, key: String, optHeaders: Headers = Headers.empty)(implicit
      dec: EntityDecoder[F, T]): F[DownloadFileResponse[T]]
  def deleteObject(bucket: String, key: String, optHeaders: Headers = Headers.empty): F[Headers]
  def copyObject(
      bucket: String,
      key: String,
      copySource: String,
      optHeaders: Headers = Headers.empty): F[CopyObjectResponse]
  def headObject(
      bucket: String,
      key: String,
      optHeaders: Headers = Headers.empty): F[HeadObjectResponse]

  //Multipart Uploads
  def createMultipartUpload(
      bucket: String,
      key: String,
      optHeaders: Headers = Headers.empty): F[CreateMultipartUploadResponse]
  def abortMultipartUpload(
      bucket: String,
      key: String,
      uploadId: String,
      optHeaders: Headers = Headers.empty): F[Headers]
  def listMultipartUploads(
      bucket: String,
      optHeaders: Headers = Headers.empty): F[ListMultipartUploadsResponse]
  def listParts(
      bucket: String,
      key: String,
      uploadId: String,
      optHeaders: Headers = Headers.empty): F[ListPartsResponse]
  def uploadPart[T](
      bucket: String,
      key: String,
      partNumber: Int,
      uploadId: String,
      t: T,
      optHeaders: Headers = Headers.empty)(implicit enc: EntityEncoder[F, T]): F[UploadPartResponse]
  def completeMultipartUpload(
      bucket: String,
      key: String,
      uploadId: String,
      parts: List[String],
      optHeaders: Headers = Headers.empty): F[CompleteMultipartUploadResponse]
  def startMultipartUpload(bucket: String, key: String): Resource[F, MultipartUpload[F]]
}

object S3 {

  implicit def xtractDecoder[F[_]: Sync, T: XmlReader]: EntityDecoder[F, T] = xml[F].transform {
    r =>
      r.flatMap { elem =>
        XmlReader.of[T].read(elem) match {
          case ParseSuccess(get)              => get.asRight
          case PartialParseSuccess(_, errors) =>
            MalformedMessageBodyFailure(s"Unable to parse body: $errors").asLeft
          case ParseFailure(errors)           =>
            MalformedMessageBodyFailure(s"Unable to parse body: $errors").asLeft
        }
      }
  }

  case class S3Error(code: Status, headers: Headers, body: String)
      extends Throwable
      with NoStackTrace {
    override def getMessage: String =
      s"S3Error(code = $code, headers = $headers, body = \"$body\")"
  }

  case class HeaderError(name: String, headers: Headers)
      extends Throwable(s"Could not find header $name")
      with NoStackTrace {
    override def toString = s"HeaderError(name = $name)"
  }

  def apply[F[_]: Sync: Clock: Logger](
      baseClient: Client[F],
      credentials: AWSCredentials,
      awsRegion: AWSRegion,
      endpoint: Option[Uri] = None): S3[F] = new S3[F] {

    private val region: String = awsRegion.value

    //Default to amazons endpoint if none is provided
    private val endpointHost =
      endpoint.getOrElse(Uri.unsafeFromString(s"https://s3.$region.amazonaws.com"))

    def insertBucketToUri(host: Uri, bucket: String): Uri = host.copy(authority =
      Some(Uri.Authority(host = Uri.RegName(bucket + "." + region + "." + host.host.get))))

    private val client: Client[F] =
      V4Middleware[F](credentials, awsRegion, AWSService.s3).apply(baseClient)

    private def handleError(r: Response[F]): F[Throwable] =
      r.bodyText.compile.foldMonoid.map { b =>
        S3Error(r.status, r.headers, b)
      }

    private def getHeader(key: String, headers: Headers): F[Header] =
      Sync[F].fromEither(headers.get(key.ci).toRight(HeaderError(key, headers)))

    private def createBucketBody: Elem =
      <CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
        <LocationConstraint>{region}</LocationConstraint>
      </CreateBucketConfiguration>

    override def createBucket(
        bucket: String,
        optHeaders: Headers = Headers.empty): F[CreateBucketResponse] =
      client
        .run(
          Request[F](
            Method.PUT,
            insertBucketToUri(endpointHost, bucket),
            headers = optHeaders
          ).withEntity(createBucketBody)
        )
        .use { resp =>
          {
            {
              for {
                location <- getHeader("Location", resp.headers)
              } yield CreateBucketResponse(location.value, resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def deleteBucket(bucket: String, optHeaders: Headers = Headers.empty): F[Headers] =
      client
        .run(
          Request[F](
            Method.DELETE,
            insertBucketToUri(endpointHost, bucket),
            headers = optHeaders
          )
        )
        .use { resp =>
          { resp.headers.pure[F] }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def listBuckets(): F[ListBucketsResponse] = client.expectOr(
      Request[F](
        Method.GET,
        endpointHost
      )
    )(handleError)

    override def listObjectsV2(
        bucket: String,
        optHeaders: Headers = Headers.empty): F[ListObjectsResponse] = client.expectOr(
      Request[F](
        Method.GET,
        insertBucketToUri(endpointHost, bucket)
          .withQueryParams(
            Map(
              "list-type"    -> 2.toString.some, //sets the request to use the v2 version
              "Content-Type" -> "text/plain".some
            ).flattenOption),
        headers = optHeaders
      )
    )(handleError)

    override def putObject[T](
        bucket: String,
        key: String,
        t: T,
        optHeaders: Headers = Headers.empty)(implicit
        enc: EntityEncoder[F, T]): F[UploadFileResponse] =
      client
        .run(
          Request[F](
            Method.PUT,
            insertBucketToUri(endpointHost, bucket) / key,
            headers = optHeaders
          ).withEntity(t)
        )
        .use { resp =>
          {
            {
              for {
                etag <- getHeader("ETag", resp.headers)
              } yield UploadFileResponse(etag.value, resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def getObject[T](bucket: String, key: String, optHeaders: Headers = Headers.empty)(
        implicit dec: EntityDecoder[F, T]): F[DownloadFileResponse[T]] =
      client
        .run(
          Request[F](
            Method.GET,
            insertBucketToUri(endpointHost, bucket) / key,
            headers = optHeaders
          )
        )
        .use { resp =>
          {
            {
              for {
                etag <- getHeader("ETag", resp.headers)
                body <- resp.as[T]
              } yield DownloadFileResponse(etag.value, body, resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def deleteObject(
        bucket: String,
        key: String,
        optHeaders: Headers = Headers.empty): F[Headers] =
      client
        .run(
          Request[F](
            Method.DELETE,
            insertBucketToUri(endpointHost, bucket) / key,
            headers = optHeaders
          )
        )
        .use { resp =>
          { resp.headers.pure[F] }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def copyObject(
        bucket: String,
        key: String,
        copySource: String,
        optHeaders: Headers = Headers.empty): F[CopyObjectResponse] =
      client.expectOr(
        Request[F](
          Method.PUT,
          insertBucketToUri(endpointHost, bucket) / key,
          headers = Headers(Header("x-amz-copy-source", copySource)) ++
            optHeaders
        )
      )(handleError)

    override def headObject(
        bucket: String,
        key: String,
        optHeaders: Headers): F[HeadObjectResponse] =
      client
        .run(
          Request[F](
            Method.HEAD,
            insertBucketToUri(endpointHost, bucket) / key,
            headers = optHeaders
          )
        )
        .use { resp =>
          {
            {
              for {
                eTag          <- getHeader("ETag", resp.headers)
                contentLength <- getHeader("Content-Length", resp.headers)
                contentType   <- getHeader("Content-Type", resp.headers)
              } yield HeadObjectResponse(
                eTag.value,
                contentLength.value.toInt,
                contentType.value,
                resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def createMultipartUpload(
        bucket: String,
        key: String,
        optHeaders: Headers = Headers.empty): F[CreateMultipartUploadResponse] =
      client.expectOr(
        Request[F](
          Method.POST,
          (insertBucketToUri(endpointHost, bucket) / key)
            .withQueryParam("uploads", ""),
          headers = optHeaders
        )
      )(handleError)

    override def abortMultipartUpload(
        bucket: String,
        key: String,
        uploadId: String,
        optHeaders: Headers = Headers.empty): F[Headers] =
      client
        .run(
          Request[F](
            Method.DELETE,
            (insertBucketToUri(endpointHost, bucket) / key)
              .withQueryParam("uploadId", uploadId),
            headers = optHeaders
          )
        )
        .use { resp =>
          { resp.headers.pure[F] }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def listMultipartUploads(
        bucket: String,
        optHeaders: Headers = Headers.empty): F[ListMultipartUploadsResponse] =
      client.expectOr(
        Request[F](
          Method.GET,
          Uri
            .unsafeFromString(s"https://$bucket.$endpoint")
            .withQueryParam("uploads", ""),
          headers = optHeaders
        )
      )(handleError)

    override def listParts(
        bucket: String,
        key: String,
        uploadId: String,
        optHeaders: Headers = Headers.empty): F[ListPartsResponse] =
      client.expectOr(
        Request[F](
          Method.GET,
          (insertBucketToUri(endpointHost, bucket) / key)
            .withQueryParam("uploadId", uploadId),
          headers = optHeaders
        )
      )(handleError)

    override def uploadPart[T](
        bucket: String,
        key: String,
        partNumber: Int,
        uploadId: String,
        body: T,
        optHeaders: Headers = Headers.empty)(implicit
        enc: EntityEncoder[F, T]): F[UploadPartResponse] =
      client
        .run(
          Request[F](
            Method.PUT,
            (insertBucketToUri(endpointHost, bucket) / key)
              .withQueryParams(
                Map(
                  "partNumber" -> partNumber.toString.some,
                  "uploadId"   -> uploadId.some
                ).flattenOption),
            headers = optHeaders
          ).withEntity(body)
        )
        .use { resp =>
          {
            {
              for {
                eTag <- getHeader("ETag", resp.headers)
              } yield UploadPartResponse(eTag.value, resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    private def part(num: Int, eTag: String): Elem =
      <Part><PartNumber>{num}</PartNumber><ETag>{eTag}</ETag></Part>

    private def completeMultipartUploadBody(parts: List[String]): Elem =
      <CompleteMultipartUpload>{
        parts.zip(LazyList from 1).map { case (eTag, num) => part(num, eTag) }
      }</CompleteMultipartUpload>

    override def completeMultipartUpload(
        bucket: String,
        key: String,
        uploadId: String,
        parts: List[String],
        optHeaders: Headers): F[CompleteMultipartUploadResponse] =
      client.expectOr(
        Request[F](
          Method.POST,
          (insertBucketToUri(endpointHost, bucket) / key)
            .withQueryParam("uploadId", uploadId),
          headers = optHeaders
        ).withEntity(completeMultipartUploadBody(parts))
      )(handleError)

    override def startMultipartUpload(
        bucket: String,
        key: String): Resource[F, MultipartUpload[F]] = {
      val create = for {
        etags  <- Ref.of[F, List[String]](List.empty)
        upload <- createMultipartUpload(bucket, key)
      } yield MultipartUpload(this, bucket, key, upload.uploadId, etags)
      Resource.makeCase(create) { case (mpu, exitCase) =>
        exitCase match {
          case ExitCase.Completed => mpu.close
          case ExitCase.Error(e)  =>
            Logger[F].error(e)("Aborting S3 Multipart upload due to error") *> mpu.abort
          case ExitCase.Canceled  =>
            Logger[F].error("Aborting S3 Multipart upload due to cancellation") *> mpu.abort
        }
      }
    }
  }
}

case class MultipartUpload[F[_]: Sync](
    client: S3[F],
    bucket: String,
    key: String,
    uploadId: String,
    etags: Ref[F, List[String]]) {

  def sendPart[T](t: T)(implicit enc: EntityEncoder[F, T]): F[Unit] = for {
    nextPartNum <- etags.get.map(_.length) //get the next part number
    uploaded    <- client.uploadPart(bucket, key, nextPartNum, uploadId, t)(enc)
    _           <- etags.update(_.appended(uploaded.eTag))
  } yield ()

  protected[s3] def close: F[Unit] = for {
    finalEtags <- etags.get
    _           = client.completeMultipartUpload(bucket, key, uploadId, finalEtags)
  } yield ()

  protected[s3] def abort: F[Unit] = client.abortMultipartUpload(bucket, key, uploadId).void
}
