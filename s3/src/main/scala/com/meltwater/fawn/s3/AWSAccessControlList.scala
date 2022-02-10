package com.meltwater.fawn.s3

import com.lucidchart.open.xtract.{__, ParseError, TypeError, XmlReader}
import enumeratum._

sealed trait AWSACL extends EnumEntry

object AWSACL extends Enum[AWSACL] {

  val values = findValues

  def AWSACLError: ParseError = TypeError(classOf[AWSACL])

  case object READ         extends AWSACL
  case object WRITE        extends AWSACL
  case object READ_ACP     extends AWSACL
  case object WRITE_ACP    extends AWSACL
  case object FULL_CONTROL extends AWSACL

  implicit val xmlDecoder: XmlReader[AWSACL] = (
    (__).read[String]
  ).tryMap(_ => AWSACLError)(AWSACL.withName)
}

sealed trait AWSCannedACL extends EnumEntry

object AWSCannedACL extends Enum[AWSCannedACL] {

  val values = findValues

  def AWSCannedACLError: ParseError = TypeError(classOf[AWSCannedACL])

  case object `private`                   extends AWSCannedACL
  case object `public-read`               extends AWSCannedACL
  case object `public-read-write`         extends AWSCannedACL
  case object `aws-exec-read`             extends AWSCannedACL
  case object `authenticated-read`        extends AWSCannedACL
  case object `bucket-owner-read`         extends AWSCannedACL
  case object `bucket-owner-full-control` extends AWSCannedACL
  case object `log-delivery-write`        extends AWSCannedACL

  implicit val xmlDecoder: XmlReader[AWSCannedACL] = (
    (__).read[String]
  ).tryMap(_ => AWSCannedACLError)(AWSCannedACL.withName)
}
