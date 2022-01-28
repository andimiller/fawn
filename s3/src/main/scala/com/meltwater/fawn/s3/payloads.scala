package com.meltwater.fawn.s3

import cats.implicits._
import com.lucidchart.open.xtract.XmlReader._
import com.lucidchart.open.xtract.{__, XmlReader}
import org.http4s.Headers

/** Represents a generic response that contains the AWS Request Id and all remaining headers.
  * @param requestId
  *   Amazon AWS Request ID
  * @param headers
  *   All Response Headers
  */
case class GenericResponse(requestId: String, headers: Headers)
object GenericResponse {
  def apply(requestId: String, headers: Headers): GenericResponse =
    new GenericResponse(requestId, headers.filter(h => h.value != requestId))
}

case class CreateBucketResponse(location: String, genericResponse: GenericResponse)

case class ListBucketsResponse(buckets: Vector[Bucket], owner: Owner)

object ListBucketsResponse {
  implicit val xmlDecoder: XmlReader[ListBucketsResponse] = (
    (__ \ "Buckets" \ "Bucket").read(seq[Bucket]).map(_.toVector),
    (__ \ "Owner").read[Owner]
  ).mapN(ListBucketsResponse.apply)
}

case class ListObjectsResponse(
    name: String,
    prefix: Option[String],
    keyCount: Int,
    maxKeys: Int,
    isTruncated: Boolean,
    contents: Vector[S3Object])

object ListObjectsResponse {
  implicit val xmlDecoder: XmlReader[ListObjectsResponse] = (
    (__ \ "Name").read[String],
    (__ \ "Prefix").read[String].optional,
    (__ \ "KeyCount").read[Int],
    (__ \ "MaxKeys").read[Int],
    (__ \ "IsTruncated").read[Boolean],
    (__ \ "Contents").read(seq[S3Object]).map(_.toVector)
  ).mapN(ListObjectsResponse.apply)
}

case class UploadFileResponse(eTag: String, genericResponse: GenericResponse)

case class DownloadFileResponse[T](eTag: String, body: T, genericResponse: GenericResponse)

case class CopyObjectResponse(eTag: String, lastModified: String)

object CopyObjectResponse {
  implicit val xmlDecoder: XmlReader[CopyObjectResponse] = (
    (__ \ "ETag").read[String],
    (__ \ "LastModified").read[String]
  ).mapN(CopyObjectResponse.apply)
}

case class HeadObjectResponse(
    eTag: String,
    contentLength: Int,
    contentType: String,
    genericResponse: GenericResponse)

case class CreateMultipartUploadResponse(bucket: String, key: String, uploadId: String)

object CreateMultipartUploadResponse {
  implicit val xmlDecoder: XmlReader[CreateMultipartUploadResponse] = (
    (__ \ "Bucket").read[String],
    (__ \ "Key").read[String],
    (__ \ "UploadId").read[String]
  ).mapN(apply)
}

case class ListMultipartUploadsResponse(
    bucket: String,
    keyMarker: Option[String],
    uploadIdMarker: Option[String],
    nextKeyMarker: Option[String],
    prefix: Option[String],
    delimiter: Option[String],
    nextUploadIdMarker: String,
    maxUploads: Int,
    isTruncated: Boolean,
    uploads: Option[Vector[Uploads]],
    commonPrefixes: Option[Vector[CommonPrefixes]])

object ListMultipartUploadsResponse {
  implicit val xmlDecoder: XmlReader[ListMultipartUploadsResponse] = (
    (__ \ "Bucket").read[String],
    (__ \ "KeyMarker").read[String].optional,
    (__ \ "UploadIdMarker").read[String].optional,
    (__ \ "NextKeyMarker").read[String].optional,
    (__ \ "Prefix").read[String].optional,
    (__ \ "Delimiter").read[String].optional,
    (__ \ "NextUploadIdMarker").read[String],
    (__ \ "MaxUploads").read[Int],
    (__ \ "IsTruncated").read[Boolean],
    (__ \ "Upload").read(seq[Uploads]).map(_.toVector).optional,
    (__ \ "CommonPrefixes").read(seq[CommonPrefixes]).map(_.toVector).optional
  ).mapN(ListMultipartUploadsResponse.apply)
}

case class ListPartsResponse(
    bucket: String,
    key: String,
    uploadId: String,
    partNumberMarker: Option[Int],
    nextPartNumberMarker: Option[Int],
    maxParts: Option[Int],
    isTruncated: Boolean,
    parts: Vector[Parts],
    initiator: Initiator,
    owner: Owner,
    storageClass: String
)

object ListPartsResponse {
  implicit val xmlDecoder: XmlReader[ListPartsResponse] = (
    (__ \ "Bucket").read[String],
    (__ \ "Key").read[String],
    (__ \ "UploadId").read[String],
    (__ \ "PartNumberMarker").read[Int].optional,
    (__ \ "NextPartNumberMarker").read[Int].optional,
    (__ \ "MaxParts").read[Int].optional,
    (__ \ "IsTruncated").read[Boolean],
    (__ \ "Part").read(seq[Parts]).map(_.toVector),
    (__ \ "Initiator").read[Initiator],
    (__ \ "Owner").read[Owner],
    (__ \ "StorageClass").read[String]
  ).mapN(ListPartsResponse.apply)
}
case class UploadPartResponse(eTag: String, genericResponse: GenericResponse)

case class CompleteMultipartUploadResponse(
    location: String,
    bucket: String,
    key: String,
    eTag: String)

object CompleteMultipartUploadResponse {
  implicit val xmlDecoder: XmlReader[CompleteMultipartUploadResponse] = (
    (__ \ "Location").read[String],
    (__ \ "Bucket").read[String],
    (__ \ "Key").read[String],
    (__ \ "ETag").read[String]
  ).mapN(CompleteMultipartUploadResponse.apply)
}
