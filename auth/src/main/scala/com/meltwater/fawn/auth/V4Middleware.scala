package com.meltwater.fawn.auth

import cats.effect.{Clock, Resource, Sync}
import cats.implicits._
import fs2.RaiseThrowable
import org.http4s.headers.Host
import org.http4s._
import org.http4s.client.{Client, Middleware}
import scodec.bits.ByteVector

import java.security.MessageDigest
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField._
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.concurrent.TimeUnit

import com.meltwater.fawn.common._

object V4Middleware {
  protected[auth] def encodePath(path: String): String = if (path == "") {
    "/"
  } else {
    Uri.encode(path)
  }

  protected[auth] def canonicalQuery(q: Query): String = q.toVector
    .sortBy(_._1)
    .map { case (k, v) =>
      val encodedKey   = Uri.encode(k, toSkip = AwsEncodings.unreserved)
      val encodedValue = v.map(Uri.encode(_, toSkip = AwsEncodings.unreserved)).getOrElse("")
      s"$encodedKey=$encodedValue"
    }
    .mkString("&")

  protected[auth] def canonicalHeaders(headers: Headers): String =
    headers.toList
      .sortBy(_.name.value)
      .map { h =>
        h.name.value.toLowerCase + ":" + h.value.trim.replaceAll(" +", " ")
      }
      .mkString("\n") + "\n"

  protected[auth] def sha256hash(b: String): String =
    ByteVector.view(MessageDigest.getInstance("SHA-256").digest(b.getBytes)).toHex

  protected[auth] val DATE: DateTimeFormatter = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendValue(YEAR, 4)
    .appendValue(MONTH_OF_YEAR, 2)
    .appendValue(DAY_OF_MONTH, 2)
    .toFormatter

}

case class V4Middleware[F[_]: Sync: RaiseThrowable: Clock](
    credentials: AWSCredentials,
    region: AWSRegion,
    service: AWSService)
    extends Middleware[F] {
  import V4Middleware._

  val nowZoned: F[ZonedDateTime] = Clock[F]
    .realTime(TimeUnit.SECONDS)
    .map(Instant.ofEpochSecond)
    .map(ZonedDateTime.ofInstant(_, ZoneOffset.UTC))

  def credentialScope(now: ZonedDateTime): String =
    now.format(DATE) + "/" + region.value + "/" + service.value + "/aws4_request"

  def stringToBeSigned(datetime: ZonedDateTime, hashedCanonical: String): String =
    List(
      "AWS4-HMAC-SHA256",
      AmzDate.renderZDT(datetime),
      credentialScope(datetime),
      hashedCanonical
    ).mkString("\n")

  def canonicalRequest(
      req: Request[F],
      datetime: ZonedDateTime): F[(Request[F], List[String], String)] = {
    val body = req.bodyText.compile.foldMonoid
    body.map { b =>
      val bodyHash                    = sha256hash(b)
      val amazonHeaders: List[Header] =
        List(AmzDate(datetime), Header("x-amz-content-sha256", bodyHash))
      val headers                     = req.headers ++ Headers.of(
        req.uri.host.map[Header] { h => Host(h.value, req.uri.port) }.toList: _*) ++ Headers.of(
        amazonHeaders: _*)
      val canonical                   = List(
        req.method.name,
        encodePath(req.uri.path),
        canonicalQuery(req.uri.query),
        canonicalHeaders(headers),
        headers.toList.sortBy(_.name.value).iterator.map(_.name.value.toLowerCase).mkString(";"),
        bodyHash
      ).mkString("\n")
      val hashedCanonical             = sha256hash(canonical)
      val newReq                      = req.putHeaders(amazonHeaders: _*)
      (newReq, headers.toList.map(_.name.value.toLowerCase), hashedCanonical)
    }

  }

  def hmac(key: ByteVector, data: ByteVector): ByteVector = {
    val algo = "HmacSHA256"
    val hmac = javax.crypto.Mac.getInstance(algo)

    hmac.init(new javax.crypto.spec.SecretKeySpec(key.toArray, algo))
    ByteVector(hmac.doFinal(data.toArray))
  }

  def createKey(secret: ByteVector, now: ZonedDateTime) = {
    val dateSigned    = hmac(
      ByteVector("AWS4".getBytes) ++ secret,
      ByteVector(now.withZoneSameInstant(ZoneOffset.UTC).format(DATE).getBytes))
    val regionSigned  = hmac(dateSigned, ByteVector(region.value.getBytes))
    val serviceSigned = hmac(regionSigned, ByteVector(service.value.getBytes))
    hmac(serviceSigned, ByteVector("aws4_request".getBytes))
  }

  def sign(
      req: Request[F],
      now: ZonedDateTime,
      secret: ByteVector): F[(Request[F], List[String], ByteVector)] = {
    canonicalRequest(req, now).map { case (req, headers, reqhash) =>
      val str    = stringToBeSigned(now, reqhash)
      val key    = createKey(secret, now)
      val signed = hmac(key, ByteVector(str.getBytes))
      (req, headers, signed)
    }
  }

  def signRequest(req: Request[F]): F[Request[F]] =
    nowZoned.flatMap(now =>
      sign(req, now, ByteVector(credentials.secretAccessKey.getBytes)).map {
        case (req2, headers, signature) =>
          val auth = AWS4Authorization(
            "AWS4-HMAC-SHA256",
            credentials.accessKeyId,
            credentialScope(now),
            headers,
            signature.toHex
          )
          req2.putHeaders(auth)
      })

  // and finally implement the middleware
  override def apply(v1: Client[F]): Client[F] = Client { req =>
    Resource.suspend(
      signRequest(req).map(v1.run)
    )
  }
}
