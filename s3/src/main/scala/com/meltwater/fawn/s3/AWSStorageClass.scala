package com.meltwater.fawn.s3

import enumeratum._
import com.lucidchart.open.xtract.{__, ParseError, TypeError, XmlReader}

sealed trait AWSStorageClass extends EnumEntry

object AWSStorageClass extends Enum[AWSStorageClass] {

  val values = findValues

  def AWSStorageClassError: ParseError = TypeError(classOf[AWSStorageClass])

  case object STANDARD            extends AWSStorageClass
  case object REDUCED_REDUNDANCY  extends AWSStorageClass
  case object STANDARD_IA         extends AWSStorageClass
  case object ONEZONE_IA          extends AWSStorageClass
  case object INTELLIGENT_TIERING extends AWSStorageClass
  case object GLACIER             extends AWSStorageClass
  case object DEEP_ARCHIVE        extends AWSStorageClass
  case object OUTPOSTS            extends AWSStorageClass
  case object GLACIER_IR          extends AWSStorageClass

  implicit val xmlDecoder: XmlReader[AWSStorageClass] = (
    (__).read[String]
  ).tryMap(_ => AWSStorageClassError)(AWSStorageClass.withName)
}
