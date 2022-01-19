package com.meltwater.fawn.s3

case class AWSAccessControlList(value: String)
object AWSAccessControlList {
  val `private`: AWSAccessControlList                   = AWSAccessControlList("private")
  val `public-read`: AWSAccessControlList               = AWSAccessControlList("public-read")
  val `public-read-write`: AWSAccessControlList         = AWSAccessControlList("public-read-write")
  val `authenticated-read`: AWSAccessControlList        = AWSAccessControlList("authenticated-read")
  val `aws-exec-read`: AWSAccessControlList             = AWSAccessControlList("aws-exec-read")
  val `bucket-owner-read`: AWSAccessControlList         = AWSAccessControlList("bucket-owner-read")
  val `bucket-owner-full-control`: AWSAccessControlList = AWSAccessControlList("bucket-owner-full-control")
}