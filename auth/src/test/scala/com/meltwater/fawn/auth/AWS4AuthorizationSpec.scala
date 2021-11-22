package com.meltwater.fawn.auth

import cats.effect.IO
import cats.implicits._
import net.andimiller.munit.cats.effect.styles.WordIOSpec

class AWS4AuthorizationSpec extends WordIOSpec {
  "AWS4Authorization" should {
    "parse the example AWS give" in {
      IO {
        AWS4Authorization.parse(
          "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7")
      }.assertEquals(
        AWS4Authorization(
          "AWS4-HMAC-SHA256",
          "AKIDEXAMPLE",
          "20150830/us-east-1/iam/aws4_request",
          List("content-type", "host", "x-amz-date"),
          "5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7"
        ).asRight
      )
    }
    "render the example" in {
      IO {
        AWS4Authorization(
          "AWS4-HMAC-SHA256",
          "AKIDEXAMPLE",
          "20150830/us-east-1/iam/aws4_request",
          List("content-type", "host", "x-amz-date"),
          "5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7"
        ).value
      }.assertEquals(
        "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7"
      )
    }
  }
}
