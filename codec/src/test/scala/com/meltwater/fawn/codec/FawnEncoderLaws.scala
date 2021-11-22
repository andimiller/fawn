package com.meltwater.fawn.codec

import cats.implicits._
import net.andimiller.munit.cats.effect.styles.WordIOSpec
import cats.laws.discipline.ContravariantTests
import TestInstances._

class FawnEncoderLaws extends WordIOSpec {

  "FawnEncoder" should {
    val tests = ContravariantTests[FawnEncoder]
    List(
      tests.contravariant[String, Int, Long].props,
      tests.invariant[String, Int, Long].props
    ).flatten.foreach { case (name, prop) =>
      name in prop
    }
  }

}
