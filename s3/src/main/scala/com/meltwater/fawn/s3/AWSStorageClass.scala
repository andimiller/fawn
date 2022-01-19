package com.meltwater.fawn.s3

case class AWSStorageClass(value: String)
object AWSStorageClass {
  val `standard`: AWSStorageClass            = AWSStorageClass("standard")
  val `reduced-redundancy`: AWSStorageClass  = AWSStorageClass("reduced-redundancy")
  val `standard-ia`: AWSStorageClass         = AWSStorageClass("standard-ia")
  val `onezone-ia`: AWSStorageClass          = AWSStorageClass("onezone-ia")
  val `intelligent-tiering`: AWSStorageClass = AWSStorageClass("intelligent-tiering")
  val `glacier`: AWSStorageClass             = AWSStorageClass("glacier")
  val `deep-archive`: AWSStorageClass        = AWSStorageClass("deep-archive")
  val `outposts`: AWSStorageClass            = AWSStorageClass("outposts")
  val `glacier-ir`: AWSStorageClass          = AWSStorageClass("glacier-ir")
}