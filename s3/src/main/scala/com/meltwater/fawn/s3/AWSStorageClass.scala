package com.meltwater.fawn.s3

case class AWSStorageClass(value: String)
object AWSStorageClass {
  val `STANDARD`: AWSStorageClass            = AWSStorageClass("STANDARD")
  val `REDUCED-REDUNDANCY`: AWSStorageClass  = AWSStorageClass("REDUCED-REDUNDANCY")
  val `STANDARD-IA`: AWSStorageClass         = AWSStorageClass("STANDARD-IA")
  val `ONEZONE-IA`: AWSStorageClass          = AWSStorageClass("ONEZONE-IA")
  val `INTELLIGENT_TIERING`: AWSStorageClass = AWSStorageClass("INTELLIGENT-TIERING")
  val `GLACIER`: AWSStorageClass             = AWSStorageClass("GLACIER")
  val `DEEP_ARCHIVE`: AWSStorageClass        = AWSStorageClass("DEEP-ARCHIVE")
  val `OUTPOSTS`: AWSStorageClass            = AWSStorageClass("OUTPOSTS")
  val `GLACIER-IR`: AWSStorageClass          = AWSStorageClass("GLACIER-IR")
}