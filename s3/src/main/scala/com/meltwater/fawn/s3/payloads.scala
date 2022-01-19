package com.meltwater.fawn.s3

import cats.implicits._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{XmlReader, __}

case class Bucket(creationDate: String, name: String)

object Bucket {
  implicit val xmlDecoder: XmlReader[Bucket] = (
    (__ \ "CreationDate").read[String],
    (__ \ "Name").read[String]
  ).mapN(Bucket.apply)
}

case class ListAllMyBucketsResult(buckets: Vector[Bucket], ownerName: String, ownerID: String)

object ListAllMyBucketsResult {
  implicit val xmlDecoder: XmlReader[ListAllMyBucketsResult] = (
    (__ \ "Buckets" \ "Bucket").read(seq[Bucket]).map(_.toVector),
    (__ \ "Owner" \ "DisplayName").read[String],
    (__ \ "Owner" \ "ID").read[String]
  ).mapN(ListAllMyBucketsResult.apply)
}

case class Contents(key: String, lastModified: String, eTag: String, size: Int, storageClass: String)

object Contents {
  implicit val xmlDecoder: XmlReader[Contents] = (
    (__ \ "Key").read[String],
    (__ \ "LastModified").read[String],
    (__ \ "ETag").read[String],
    (__ \ "Size").read[Int],
    (__ \ "StorageClass").read[String]
  ).mapN(Contents.apply)
}

case class ListBucketResult(name: String, prefix: String, keyCount: Int, maxKeys: Int, isTruncated: Boolean, contents: Vector[Contents])

object ListBucketResult {
  implicit val xmlDecoder: XmlReader[ListBucketResult] = (
    (__ \ "Name").read[String],
    (__ \ "Prefix").read[String],
    (__ \ "KeyCount").read[Int],
    (__ \ "MaxKeys").read[Int],
    (__ \ "IsTruncated").read[Boolean],
    (__ \ "Contents").read(seq[Contents]).map(_.toVector)
  ).mapN(ListBucketResult.apply)
}

case class UploadFileResponse(eTag: String, expiration: String)

case class DownloadFileResponse[T](eTag: String, body: T)




