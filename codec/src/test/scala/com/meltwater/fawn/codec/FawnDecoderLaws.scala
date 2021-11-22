package com.meltwater.fawn.codec

import cats.implicits._
import cats.laws.discipline.ApplicativeErrorTests
import net.andimiller.munit.cats.effect.styles.WordIOSpec
import TestInstances._

class FawnDecoderLaws extends WordIOSpec {

  "FawnDecoder" should {
    val tests = ApplicativeErrorTests[FawnDecoder, Throwable]

    List(
      tests.applicativeError[String, Int, Long].props,
      tests.applicative[String, Int, Long].props,
      tests.semigroupal[String, Int, Long].props,
      tests.apply[String, Int, Long].props,
      tests.invariant[String, Int, Long].props,
      tests.functor[String, Int, Long].props
    ).flatten.foreach { case (name, prop) =>
      name in prop
    }
  }

}
