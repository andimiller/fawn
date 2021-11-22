package com.meltwater.fawn.codec

import cats.Eq
import cats.laws.discipline.DeprecatedEqInstances.catsLawsEqForFn1
import org.scalacheck.{Arbitrary, Cogen}

import scala.annotation.nowarn

object TestInstances {

  implicit def arbDecoder[T: Arbitrary]: Arbitrary[FawnDecoder[T]] =
    Arbitrary(
      Arbitrary
        .arbFunction1[String, Either[Throwable, T]]
        .arbitrary
        .map { f => (s: String) => f(s) }
    )

  implicit def arbEncoder[T: Cogen]: Arbitrary[FawnEncoder[T]] =
    Arbitrary(
      Arbitrary
        .arbFunction1[T, String]
        .arbitrary
        .map { f => (t: T) => f(t) }
    )

  implicit val throwableEq: Eq[Throwable] = Eq.fromUniversalEquals[Throwable]

  @nowarn implicit def decoderEq[T: Eq: Arbitrary]: Eq[FawnDecoder[T]] = {
    implicit val fnEq: Eq[String => Either[Throwable, T]] =
      catsLawsEqForFn1[String, Either[Throwable, T]]
    Eq.by(e => e.decode(_))
  }

  @nowarn implicit def encoderEq[T: Eq: Arbitrary]: Eq[FawnEncoder[T]] = {
    implicit val fnEq: Eq[T => String] = catsLawsEqForFn1[T, String]
    Eq.by(e => e.encode(_))
  }

}
