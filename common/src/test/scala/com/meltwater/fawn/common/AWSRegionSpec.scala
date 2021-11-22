package com.meltwater.fawn.common

import cats.effect.IO
import net.andimiller.munit.cats.effect.styles.WordIOSpec

class AWSRegionSpec extends WordIOSpec {
  "Provided regions" should {
    "have unique names" in {
      IO {
        AWSRegion.regions.distinct
      }.assertEquals(
        AWSRegion.regions
      )
    }
  }
}
