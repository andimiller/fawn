package com.meltwater.fawn.common

/** Represents an AWS service
  */
case class AWSService(value: String)
object AWSService {
  val sqs: AWSService = AWSService("sqs")
  val s3: AWSService  = AWSService("s3")
  val iam: AWSService = AWSService("iam")

  /** Provides an explicit way to make a custom service that's not in the predefined list
    */
  def other(s: String): AWSService = AWSService(s)
}
