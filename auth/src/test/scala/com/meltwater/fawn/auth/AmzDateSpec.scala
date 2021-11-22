package com.meltwater.fawn.auth

import cats.implicits.catsSyntaxEitherId
import net.andimiller.munit.cats.effect.styles.WordIOSpec
import org.http4s.ParseFailure
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll

import java.time.{LocalDateTime, ZoneOffset}

class AmzDateSpec extends WordIOSpec {
  implicit val ldt: Arbitrary[LocalDateTime] = Arbitrary(for {
    year       <- Gen.choose(1970, 3000)
    month      <- Gen.choose(1, 12)
    dayOfMonth <- Gen.choose(1, 28) // just go with the valid ones for now
    hour       <- Gen.choose(0, 23)
    minute     <- Gen.choose(0, 59)
    second     <- Gen.choose(0, 59)
  } yield LocalDateTime.of(year, month, dayOfMonth, hour, minute, second))
  "AmzDate" should {
    "roundtrip with local date time" in {
      forAll { ldt: LocalDateTime =>
        val d = AmzDate(ldt.atZone(ZoneOffset.UTC))
        AmzDate.parse(d.value) == d.asRight[ParseFailure]
      }
    }
  }

}
