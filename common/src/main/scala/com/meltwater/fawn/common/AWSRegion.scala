package com.meltwater.fawn.common

/** Represents an AWS region, most of them are provided in the companion object, make your own if
  * you need one not provided
  */
case class AWSRegion(value: String)
object AWSRegion {
  val `us-east-1`: AWSRegion      = AWSRegion("us-east-1")
  val `us-east-2`: AWSRegion      = AWSRegion("us-east-2")
  val `us-west-1`: AWSRegion      = AWSRegion("us-west-1")
  val `us-west-2`: AWSRegion      = AWSRegion("us-west-2")
  val `af-south-1`: AWSRegion     = AWSRegion("af-south-1")
  val `ap-east-1`: AWSRegion      = AWSRegion("ap-east-1")
  val `ap-south-1`: AWSRegion     = AWSRegion("ap-south-1")
  val `ap-northeast-1`: AWSRegion = AWSRegion("ap-northeast-1")
  val `ap-northeast-2`: AWSRegion = AWSRegion("ap-northeast-2")
  val `ap-northeast-3`: AWSRegion = AWSRegion("ap-northeast-3")
  val `ap-southeast-1`: AWSRegion = AWSRegion("ap-southeast-1")
  val `ap-southeast-2`: AWSRegion = AWSRegion("ap-southeast-2")
  val `ca-central-1`: AWSRegion   = AWSRegion("ca-central-1")
  val `eu-central-1`: AWSRegion   = AWSRegion("eu-central-1")
  val `eu-west-1`: AWSRegion      = AWSRegion("eu-west-1")
  val `eu-west-2`: AWSRegion      = AWSRegion("eu-west-2")
  val `eu-west-3`: AWSRegion      = AWSRegion("eu-west-3")
  val `eu-south-1`: AWSRegion     = AWSRegion("eu-south-1")
  val `eu-north-1`: AWSRegion     = AWSRegion("eu-north-1")
  val `me-south-1`: AWSRegion     = AWSRegion("me-south-1")
  val `sa-east-1`: AWSRegion      = AWSRegion("sa-east-1")

  /** Alternate way to make your own Region, so it's obvious it's a non-standard one
    */
  def other(s: String): AWSRegion = AWSRegion(s)

  /** List of all known regions, add any new ones here
    */
  val regions = List(
    `us-east-1`,
    `us-east-2`,
    `us-west-1`,
    `us-west-2`,
    `af-south-1`,
    `ap-east-1`,
    `ap-south-1`,
    `ap-northeast-1`,
    `ap-northeast-2`,
    `ap-northeast-3`,
    `ap-southeast-1`,
    `ap-southeast-2`,
    `ca-central-1`,
    `eu-central-1`,
    `eu-west-1`,
    `eu-west-2`,
    `eu-west-3`,
    `eu-south-1`,
    `eu-north-1`,
    `me-south-1`,
    `sa-east-1`
  )
}
