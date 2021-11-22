package com.meltwater.fawn.auth

import cats.effect.{Clock, IO}
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion, AWSService}
import net.andimiller.munit.cats.effect.styles.FlatIOSpec
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Header, Headers, Method, Request}
import scodec.bits.{ByteVector, HexStringSyntax}

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.duration.TimeUnit

class V4MiddlewareSpec extends FlatIOSpec {

  implicit val clock: Clock[IO] = new Clock[IO] {
    val now                                          = ZonedDateTime.of(2015, 8, 30, 12, 36, 0, 0, ZoneOffset.UTC)
    override def realTime(unit: TimeUnit): IO[Long]  = IO.pure(now.toInstant.getEpochSecond)
    override def monotonic(unit: TimeUnit): IO[Long] = IO.pure(now.toInstant.getEpochSecond)
  }

  "hash" should "work the way the docs say" in {
    val input = """GET
      |/
      |Action=ListUsers&Version=2010-05-08
      |content-type:application/x-www-form-urlencoded; charset=utf-8
      |host:iam.amazonaws.com
      |x-amz-date:20150830T123600Z
      |
      |content-type;host;x-amz-date
      |e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855""".stripMargin
    IO {
      V4Middleware.sha256hash(input)
    }.assertEquals("f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59")
  }

  "hash" should "also work with hashes with leading zeroes" in {
    IO { V4Middleware.sha256hash("example string 2\n") }
      .assertEquals("0939173aa96acbcfa7394a1f4c8cc76b9d8f72500a547047775494708965b881")
  }

  "stringToBeSigned" should "create the right string to sign" in {
    IO {
      new V4Middleware[IO](AWSCredentials("keyid", "secret"), AWSRegion.`us-east-1`, AWSService.iam)
        .stringToBeSigned(
          ZonedDateTime.of(2015, 8, 30, 12, 36, 0, 0, ZoneOffset.UTC),
          "f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59"
        )
    }.assertEquals(
      """AWS4-HMAC-SHA256
        |20150830T123600Z
        |20150830/us-east-1/iam/aws4_request
        |f536975d06c0309214f805bb90ccff089219ecd68b2577efef23edd43b7e1a59""".stripMargin
    )
  }

  "createKey" should "create the key from the examples" in {
    // https://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
    IO {
      new V4Middleware[IO](AWSCredentials("keyid", "secret"), AWSRegion.`us-east-1`, AWSService.iam)
        .createKey(
          ByteVector("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY".getBytes),
          ZonedDateTime.of(2015, 8, 30, 12, 36, 0, 0, ZoneOffset.UTC)
        )
    }.assertEquals(
      hex"c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9"
    )
  }

  "sign" should "do the whole example" in {
    new V4Middleware[IO](
      AWSCredentials("keyid", "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"),
      AWSRegion.`us-east-1`,
      AWSService.iam)
      .sign(
        Request[IO](
          Method.GET,
          uri"https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08"
        ).withHeaders(
          Headers.of(
            Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
          )
        ),
        ZonedDateTime.of(2015, 8, 30, 12, 36, 0, 0, ZoneOffset.UTC),
        ByteVector("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY".getBytes)
      )
      .map(_._3)
      .assertEquals(
        hex"dd479fa8a80364edf2119ec24bebde66712ee9c9cb2b0d92eb3ab9ccdc0c3947"
      )
  }

  "signRequest" should "transform a full request" in {
    val r = Request[IO](
      Method.GET,
      uri"https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08"
    ).withHeaders(
      Headers.of(
        Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
      )
    )
    new V4Middleware[IO](
      AWSCredentials(
        "AKIDEXAMPLE",
        "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
      ),
      AWSRegion.`us-east-1`,
      AWSService.iam)
      .signRequest(
        r
      )
      .assertEquals(
        r.withHeaders(
          Headers.of(
            Header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"),
            AmzDate(ZonedDateTime.of(2015, 8, 30, 12, 36, 0, 0, ZoneOffset.UTC)),
            Header(
              "x-amz-content-sha256",
              "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
            AWS4Authorization(
              "AWS4-HMAC-SHA256",
              "AKIDEXAMPLE",
              "20150830/us-east-1/iam/aws4_request",
              List("content-type", "host", "x-amz-date", "x-amz-content-sha256"),
              "dd479fa8a80364edf2119ec24bebde66712ee9c9cb2b0d92eb3ab9ccdc0c3947"
            )
          )
        )
      )
  }

}
