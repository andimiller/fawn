package com.meltwater.fawn.codec

import cats.effect.IO
import cats.implicits._
import com.meltwater.fawn.codec.basic.basicStringDecoder
import net.andimiller.munit.cats.effect.styles.WordIOSpec

class FawnDecoderSpec extends WordIOSpec {
  implicit val decoderLong: FawnDecoder[Long] = new FawnDecoder[Long] {
    override def decode(s: String): Either[Throwable, Long] =
      s.toLongOption.toRight(new Throwable("not a valid Long"))
  }

  "FawnDecoder" should {
    "have an orElse" in {
      val d = FawnDecoder[Long]
        .map(_.asRight[String])
        .orElse(
          FawnDecoder[String].map(_.asLeft[Long])
        )
      IO {
        d.decode("12345")
      }.assertEquals(12345L.asRight.asRight) *>
        IO {
          d.decode("hello world")
        }.assertEquals("hello world".asLeft.asRight)
    }
  }
}
