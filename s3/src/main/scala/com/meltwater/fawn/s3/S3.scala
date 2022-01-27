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
  def createBucket(bucket: String, optHeaders: Option[Headers] = None): F[CreateBucketResponse]
  def deleteBucket(bucket: String, optHeaders: Option[Headers] = None): F[GenericResponse]
  def listBuckets(): F[ListBucketsResponse]

  //Object Interaction
  def listObjectsV2(bucket: String, optHeaders: Option[Headers] = None): F[ListObjectsResponse]
  def putObject[T](bucket: String, key: String, t: T, optHeaders: Option[Headers] = None)(implicit
      enc: EntityEncoder[F, T]): F[UploadFileResponse]
  def getObject[T](bucket: String, key: String, optHeaders: Option[Headers] = None)(implicit
      dec: EntityDecoder[F, T]): F[DownloadFileResponse[T]]
  def deleteObject(
      bucket: String,
      key: String,
      optHeaders: Option[Headers] = None): F[GenericResponse]
  def copyObject(
      bucket: String,
      key: String,
      copySource: String,
      optHeaders: Option[Headers] = None): F[CopyObjectResponse]
  def headObject(
      bucket: String,
      key: String,
      optHeaders: Option[Headers] = None): F[HeadObjectResponse]

  //Multipart Uploads
  def createMultipartUpload(
      bucket: String,
      key: String,
      optHeaders: Option[Headers] = None): F[CreateMultipartUploadResponse]
  def abortMultipartUpload(
      bucket: String,
      key: String,
      uploadId: String,
      optHeaders: Option[Headers] = None): F[GenericResponse]
  def listMultipartUploads(
      bucket: String,
      optHeaders: Option[Headers] = None): F[ListMultipartUploadsResponse]
  def listParts(
      bucket: String,
      key: String,
      uploadId: String,
      optHeaders: Option[Headers] = None): F[ListPartsResponse]
  def uploadPart[T](
      bucket: String,
      key: String,
      partNumber: Int,
      uploadId: String,
      t: T,
      optHeaders: Option[Headers] = None)(implicit enc: EntityEncoder[F, T]): F[UploadPartResponse]
  def completeMultipartUpload(
      bucket: String,
      key: String,
      uploadId: String,
      parts: List[String],
      optHeaders: Option[Headers] = None): F[CompleteMultipartUploadResponse]
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
      awsRegion: AWSRegion): S3[F] = new S3[F] {

    private val client: Client[F] =
      V4Middleware[F](credentials, awsRegion, AWSService.s3).apply(baseClient)

    private val region: String = awsRegion.value

    private def handleError(r: Response[F]): F[Throwable] =
      r.bodyText.compile.foldMonoid.map { b =>
        S3Error(r.status, r.headers, b)
      }

    private def getHeader(key: String, headers: Headers): F[Header] =
      Sync[F].fromEither(headers.get(key.ci).toRight(HeaderError(key, headers)))

    private def createBucketBody(): Elem =
      <CreateBucketConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
        <LocationConstraint>{region}</LocationConstraint>
      </CreateBucketConfiguration>

    override def createBucket(
        bucket: String,
        optHeaders: Option[Headers]): F[CreateBucketResponse] =
      client
        .run(
          Request[F](
            Method.PUT,
            Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get,
            headers = optHeaders.getOrElse(Headers.empty)
          ).withEntity(createBucketBody())
        )
        .use { resp =>
          {
            {
              for {
                location  <- getHeader("Location", resp.headers)
                requestId <- getHeader("x-amz-request-id", resp.headers)
              } yield CreateBucketResponse(
                location.value,
                GenericResponse(requestId.value, resp.headers))
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def deleteBucket(bucket: String, optHeaders: Option[Headers]): F[GenericResponse] =
      client
        .run(
          Request[F](
            Method.DELETE,
            Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get,
            headers = optHeaders.getOrElse(Headers.empty)
          )
        )
        .use { resp =>
          {
            {
              for {
                requestId <- getHeader("x-amz-request-id", resp.headers)
              } yield GenericResponse(requestId.value, resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def listBuckets(): F[ListBucketsResponse] = client.expectOr(
      Request[F](
        Method.GET,
        Uri.fromString(s"https://s3.$region.amazonaws.com").toOption.get
      )
    )(handleError)

    override def listObjectsV2(
        bucket: String,
        optHeaders: Option[Headers] = None): F[ListObjectsResponse] = client.expectOr(
      Request[F](
        Method.GET,
        Uri
          .fromString(s"https://$bucket.s3.$region.amazonaws.com")
          .toOption
          .get
          .withQueryParams(
            Map(
              "list-type"    -> 2.toString.some, //sets the request to use the v2 version
              "Content-Type" -> "text/plain".some
            ).flattenOption),
        headers = optHeaders.getOrElse(Headers.empty)
      )
    )(handleError)

    override def putObject[T](
        bucket: String,
        key: String,
        t: T,
        optHeaders: Option[Headers] = None)(implicit
        enc: EntityEncoder[F, T]): F[UploadFileResponse] =
      client
        .run(
          Request[F](
            Method.PUT,
            Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key,
            headers = optHeaders.getOrElse(Headers())
          ).withEntity(t)
        )
        .use { resp =>
          {
            {
              for {
                requestId <- getHeader("x-amz-request-id", resp.headers)
                etag      <- getHeader("ETag", resp.headers)
              } yield UploadFileResponse(etag.value, GenericResponse(requestId.value, resp.headers))
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def getObject[T](bucket: String, key: String, optHeaders: Option[Headers] = None)(
        implicit dec: EntityDecoder[F, T]): F[DownloadFileResponse[T]] =
      client
        .run(
          Request[F](
            Method.GET,
            Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key,
            headers = optHeaders.getOrElse(Headers.empty)
          )
        )
        .use { resp =>
          {
            {
              for {
                requestId <- getHeader("x-amz-request-id", resp.headers)
                etag      <- getHeader("ETag", resp.headers)
                body      <- resp.as[T]
              } yield DownloadFileResponse(
                etag.value,
                body,
                GenericResponse(requestId.value, resp.headers))
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def deleteObject(
        bucket: String,
        key: String,
        optHeaders: Option[Headers] = None): F[GenericResponse] =
      client
        .run(
          Request[F](
            Method.DELETE,
            Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key,
            headers = optHeaders.getOrElse(Headers.empty)
          )
        )
        .use { resp =>
          {
            {
              for {
                requestId <- getHeader("x-amz-request-id", resp.headers)
              } yield GenericResponse(requestId.value, resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def copyObject(
        bucket: String,
        key: String,
        copySource: String,
        optHeaders: Option[Headers] = None): F[CopyObjectResponse] =
      client.expectOr(
        Request[F](
          Method.PUT,
          Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key,
          headers = Headers(Header("x-amz-copy-source", copySource)) ++
            optHeaders.getOrElse(Headers.empty)
        )
      )(handleError)

    override def headObject(
        bucket: String,
        key: String,
        optHeaders: Option[Headers]): F[HeadObjectResponse] =
      client
        .run(
          Request[F](
            Method.HEAD,
            Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key,
            headers = optHeaders.getOrElse(Headers.empty)
          )
        )
        .use { resp =>
          {
            {
              for {
                requestId     <- getHeader("x-amz-request-id", resp.headers)
                eTag          <- getHeader("ETag", resp.headers)
                contentLength <- getHeader("Content-Length", resp.headers)
                contentType   <- getHeader("Content-Type", resp.headers)
              } yield HeadObjectResponse(
                eTag.value,
                contentLength.value.toInt,
                contentType.value,
                GenericResponse(requestId.value, resp.headers))
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def createMultipartUpload(
        bucket: String,
        key: String,
        optHeaders: Option[Headers] = None): F[CreateMultipartUploadResponse] =
      client.expectOr(
        Request[F](
          Method.POST,
          (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key)
            .withQueryParam("uploads", ""),
          headers = optHeaders.getOrElse(Headers.empty)
        )
      )(handleError)

    override def abortMultipartUpload(
        bucket: String,
        key: String,
        uploadId: String,
        optHeaders: Option[Headers] = None): F[GenericResponse] =
      client
        .run(
          Request[F](
            Method.DELETE,
            (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key)
              .withQueryParam("uploadId", uploadId),
            headers = optHeaders.getOrElse(Headers.empty)
          )
        )
        .use { resp =>
          {
            {
              for {
                requestId <- getHeader("x-amz-request-id", resp.headers)
              } yield GenericResponse(requestId.value, resp.headers)
            }
          }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
        }

    override def listMultipartUploads(
        bucket: String,
        optHeaders: Option[Headers] = None): F[ListMultipartUploadsResponse] =
      client.expectOr(
        Request[F](
          Method.GET,
          Uri
            .fromString(s"https://$bucket.s3.$region.amazonaws.com")
            .toOption
            .get
            .withQueryParam("uploads", ""),
          headers = optHeaders.getOrElse(Headers.empty)
        )
      )(handleError)

    override def listParts(
        bucket: String,
        key: String,
        uploadId: String,
        optHeaders: Option[Headers] = None): F[ListPartsResponse] =
      client.expectOr(
        Request[F](
          Method.GET,
          (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key)
            .withQueryParam("uploadId", uploadId),
          headers = optHeaders.getOrElse(Headers.empty)
        )
      )(handleError)

    override def uploadPart[T](
        bucket: String,
        key: String,
        partNumber: Int,
        uploadId: String,
        body: T,
        optHeaders: Option[Headers] = None)(implicit
        enc: EntityEncoder[F, T]): F[UploadPartResponse] =
      client
        .run(
          Request[F](
            Method.PUT,
            (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key)
              .withQueryParams(
                Map(
                  "partNumber" -> partNumber.toString.some,
                  "uploadId"   -> uploadId.some
                ).flattenOption),
            headers = optHeaders.getOrElse(Headers.empty)
          ).withEntity(body)
        )
        .use { resp =>
          {
            {
              for {
                requestId <- getHeader("x-amz-request-id", resp.headers)
                eTag      <- getHeader("ETag", resp.headers)
              } yield UploadPartResponse(eTag.value, GenericResponse(requestId.value, resp.headers))
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
        optHeaders: Option[Headers]): F[CompleteMultipartUploadResponse] =
      client.expectOr(
        Request[F](
          Method.POST,
          (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com").toOption.get / key)
            .withQueryParam("uploadId", uploadId),
          headers = optHeaders.getOrElse(Headers.empty)
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
