package com.meltwater.fawn.s3

import cats.effect.{Clock, Sync}
import cats.implicits._
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import com.meltwater.fawn.auth.V4Middleware
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion, AWSService}
import org.http4s._
import org.http4s.client.Client
import org.http4s.scalaxml.xml
import org.http4s.implicits._

import scala.util.control.NoStackTrace

trait S3[F[_]] {
  def listBuckets(): F[ListAllMyBucketsResult]
  def listObjectsV2(bucket: String): F[ListBucketResult]
  def putObject[T](bucket: String, key: String, acl: AWSAccessControlList, storageClass: AWSStorageClass, t: T)(implicit enc: EntityEncoder[F, T]): F[UploadFileResponse]
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

    override def listBuckets(): F[ListAllMyBucketsResult] = client.expectOr(
      Request[F](
        Method.GET,
        Uri.fromString(s"https://s3.$region.amazonaws.com").toOption.get
      )
    )(handleError)

    override def listObjectsV2(bucket: String): F[ListBucketResult] = client.expectOr(
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

    case class HeaderError(name: String) extends Throwable(s"Could not find header $name") with NoStackTrace {
      override def toString = s"HeaderError(name = $name)"
    }

    private def getHeader(key: String, headers: Headers): F[Header] = Sync[F].fromEither(headers.get(key.ci).toRight(HeaderError(key)))

    override def putObject[T](
        bucket: String,
        key: String,
        acl: AWSAccessControlList,
        storageClass: AWSStorageClass,
        t: T)(implicit enc: EntityEncoder[F, T]): F[UploadFileResponse] =
      client.run(
        Request[F](
          Method.PUT,
          (Uri.fromString(s"https://$bucket.s3.$region.amazonaws.com")
            .toOption
            .get / key),
          headers = Headers(
            Header("x-amz-acl",acl.value),
            Header("x-amz-storage-class",storageClass.value)
          )
        ).withEntity(t)
      ).use { resp => {
        {
          for {
            header <- getHeader("ETag", resp.headers)
          } yield UploadFileResponse(eTag = header.toString())
        }
      }.recoverWith(_ => handleError(resp).flatMap(Sync[F].raiseError))
    }
  }

}