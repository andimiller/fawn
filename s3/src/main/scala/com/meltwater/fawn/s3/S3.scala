package com.meltwater.fawn.s3

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import com.meltwater.fawn.auth.V4Middleware
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion, AWSService}
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.scalaxml.xml

import scala.util.control.NoStackTrace

trait S3[F[_]] {
  def listBuckets(): F[ListAllMyBucketsResponse]
  def listObjectsV2(bucket: String): F[ListBucketResponse]
  def putObject[T](bucket: String, key: String, t: T, optHeaders: Option[Headers] = None)(implicit enc: EntityEncoder[F, T]): F[UploadFileResponse]
  def getObject[T](bucket: String, key: String)(implicit dec: EntityDecoder[F, T]): F[DownloadFileResponse[T]]
  def deleteObject(bucket: String, key: String): F[DeleteObjectResponse]
  def getBucketAcl(bucket: String): F[GetBucketAclResponse]

  //Multipart Uploads
  def createMultipartUpload(bucket: String, key: String, headers: Option[Headers] = None): F[CreateMultipartUploadResponse]
  def listMultipartUploads(bucket: String, optHeaders: Option[Headers] = None): F[ListMultipartUploadsResponse]
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

  case class S3Error(code: Status, headers: Headers, body: String) extends Throwable with NoStackTrace {
    override def getMessage: String =
      s"S3Error(code = $code, headers = $headers, body = \"$body\")"
  }

  def apply[F[_]: Sync: Clock](baseClient: Client[F],
                  credentials: AWSCredentials,
                  awsRegion: AWSRegion): S3[F] = new S3[F] {

    private val client: Client[F] = V4Middleware[F](credentials, awsRegion, AWSService.s3).apply(baseClient)

    private val region: String = awsRegion.value

    private def handleError(r: Response[F]): F[Throwable] =
      r.bodyText.compile.foldMonoid.map { b =>
        S3Error(r.status, r.headers, b)
      }

    override def listBuckets(): F[ListAllMyBucketsResponse] = client.expectOr(
      Request[F](
        Method.GET,
        Uri.fromString(s"https://s3.$region.amazonaws.com").toOption.get
      )
    )(handleError)

    override def listObjectsV2(bucket: String): F[ListBucketResponse] = client.expectOr(
      Request[F](
        Method.GET,
        (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
          .toOption
          .get)
          .withQueryParams(Map(
           "list-type"    -> 2.toString.some, //sets the request to use the v2 version
           "Content-Type" -> "text/plain".some
        ).flattenOption)
      )
    )(handleError)

    case class HeaderError(name: String, headers: Headers) extends Throwable(s"Could not find header $name") with NoStackTrace {
      override def toString = s"HeaderError(name = $name)"
    }

    private def getHeader(key: String, headers: Headers): F[Header] = Sync[F].fromEither(headers.get(key.ci).toRight(HeaderError(key, headers)))

    override def putObject[T](
        bucket: String,
        key: String,
        t: T,
        optHeaders: Option[Headers] = None)(implicit enc: EntityEncoder[F, T]): F[UploadFileResponse] =
      client.run(
        Request[F](
          Method.PUT,
          (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
            .toOption
            .get / key),
          headers = optHeaders.getOrElse(Headers())
        ).withEntity(t)
      ).use { resp => {
        {
          for {
            requestId <- getHeader("x-amz-request-id", resp.headers)
            etag <- getHeader("ETag", resp.headers)
          } yield UploadFileResponse(requestId.value, etag.value, resp.headers.filter(_ != etag).filter(_ != requestId))
        }
      }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
    }

    override def getObject[T](
       bucket: String,
       key: String)(implicit dec: EntityDecoder[F, T]): F[DownloadFileResponse[T]] =
      client.run(
        Request[F](
          Method.GET,
          Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
            .toOption
            .get / key
        )
      ).use { resp => {
        {
          for {
            requestId <- getHeader("x-amz-request-id", resp.headers)
            etag <- getHeader("ETag", resp.headers)
            body <- resp.as[T]
          } yield DownloadFileResponse(requestId.value, etag.value, resp.headers.filter(_ != etag).filter(_ != requestId) ,body)
        }
      }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
    }

    override def deleteObject(bucket: String, key: String): F[DeleteObjectResponse] =
      client.run(
        Request[F](
          Method.DELETE,
          Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
           .toOption
           .get / key
        )
      ).use { resp => {
        {
         for {
           requestId <- getHeader("x-amz-request-id", resp.headers)
         } yield DeleteObjectResponse(requestId.value)
        }
      }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
    }

    override def getBucketAcl(bucket: String): F[GetBucketAclResponse] =
      client.expectOr(
        Request[F](
          Method.GET,
          Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
            .toOption
            .get
            .withQueryParam("acl","")
        )
      )(handleError)

    override def createMultipartUpload(bucket: String, key: String, optHeaders: Option[Headers]): F[CreateMultipartUploadResponse] =
      client.expectOr(
        Request[F](
          Method.POST,
          (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
            .toOption
            .get / key)
            .withQueryParam("uploads",""),
          headers = optHeaders.getOrElse(Headers())
        )
      )(handleError)

    override def listMultipartUploads(bucket: String, optHeaders: Option[Headers]): F[ListMultipartUploadsResponse] =
      client.expectOr(
        Request[F](
          Method.GET,
          (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
            .toOption
            .get)
            .withQueryParam("uploads","")
        )
      )(handleError)

  }

}