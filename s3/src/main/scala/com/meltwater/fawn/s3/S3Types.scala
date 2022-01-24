package com.meltwater.fawn.s3

import cats.implicits._
import com.lucidchart.open.xtract.XmlReader.{`enum`, attribute}
import com.lucidchart.open.xtract.{__, XmlReader}

case class Owner(displayName: String, id: String)

object Owner {
  implicit val xmlDecoder: XmlReader[Owner] = (
    (__ \ "DisplayName").read[String],
    (__ \ "ID").read[String]
  ).mapN(apply)
}

case class Initiator(displayName: String, id: String)

object Initiator {
  implicit val xmlDecoder: XmlReader[Initiator] = (
    (__ \ "DisplayName").read[String],
    (__ \ "ID").read[String]
  ).mapN(apply)
}

case class Bucket(creationDate: String, name: String)

object Bucket {
  implicit val xmlDecoder: XmlReader[Bucket] = (
    (__ \ "CreationDate").read[String],
    (__ \ "Name").read[String]
  ).mapN(apply)
}

case class Object(
    key: String,
    lastModified: String,
    eTag: String,
    size: Int,
    storageClass: AWSStorageClass)

object Object {
  implicit val xmlDecoder: XmlReader[Object] = (
    (__ \ "Key").read[String],
    (__ \ "LastModified").read[String],
    (__ \ "ETag").read[String],
    (__ \ "Size").read[Int],
    (__ \ "StorageClass").read[AWSStorageClass]
  ).mapN(apply)
}

object GranteeType extends Enumeration {
  val CanonicalUser: GranteeType.Value         = Value("CanonicalUser")
  val AmazonCustomerByEmail: GranteeType.Value = Value("AmazonCustomerByEmail")
  val Group: GranteeType.Value                 = Value("Group")
  val Unknown: GranteeType.Value               = Value("Unknown")
}

case class Grant(
    displayName: String,
    email: String,
    id: String,
    uri: String,
    gtype: GranteeType.Value,
    permission: AWSACL)

object Grant {
  implicit val xmlDecoder: XmlReader[Grant] = (
    (__ \ "Grantee" \ "DisplayName").read[String],
    (__ \ "Grantee" \ "EmailAddress").read[String],
    (__ \ "Grantee" \ "ID").read[String],
    attribute[String]("xmlns:xsi"),
    attribute("xsi:type")(enum(GranteeType)).default(GranteeType.Unknown),
    (__ \ "Permission").read[AWSACL]
  ).mapN(apply)
}

case class Uploads(
    initiated: String,
    initiator: Initiator,
    key: String,
    owner: Owner,
    storageClass: AWSStorageClass,
    uploadId: String)

object Uploads {
  implicit val xmlDecoder: XmlReader[Uploads] = (
    (__ \ "Initiated").read[String],
    (__ \ "Initiator").read[Initiator],
    (__ \ "Key").read[String],
    (__ \ "Owner").read[Owner],
    (__ \ "StorageClass").read[AWSStorageClass],
    (__ \ "UploadId").read[String]
  ).mapN(apply)
}

case class CommonPrefixes(prefix: String)

object CommonPrefixes {
  implicit val xmlDecoder: XmlReader[CommonPrefixes] =
    (__ \ "Prefix").read[String].map(CommonPrefixes.apply)
}

case class Parts(eTag: String, lastModified: String, partNumber: Int, size: Int)

object Parts {
  implicit val xmlDecoder: XmlReader[Parts] = (
    (__ \ "ETag").read[String],
    (__ \ "LastModified").read[String],
    (__ \ "PartNumber").read[Int],
    (__ \ "Size").read[Int]
  ).mapN(apply)
}
