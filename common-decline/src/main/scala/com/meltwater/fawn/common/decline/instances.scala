package com.meltwater.fawn.common.decline

import cats.implicits._
import cats.data.ValidatedNel
import com.meltwater.fawn.common.AWSRegion
import com.monovore.decline.Argument

/** Provides Decline instances for Fawn's core types
  */
object instances {
  implicit val strictRegionArgument: Argument[AWSRegion] = new Argument[AWSRegion] {
    override def read(string: String): ValidatedNel[String, AWSRegion] =
      AWSRegion.regions
        .find(_.value == string)
        .toValidNel(s"'$string' is not a recognised AWS Region")
    override def defaultMetavar: String                                = "eu-west-1"
  }
}
