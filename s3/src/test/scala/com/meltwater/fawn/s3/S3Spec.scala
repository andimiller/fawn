package com.meltwater.fawn.s3

import cats.effect._
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion}
import net.andimiller.munit.cats.effect.styles.FlatIOSpec
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, Header, Headers, HttpRoutes}
import org.typelevel.log4cats.slf4j.Slf4jLogger

class S3Spec extends FlatIOSpec {

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  val credentials = AWSCredentials("accesskey", "secretkey")
  val region      = AWSRegion.`af-south-1`

  val bucket = "example-testing-bucket"
  val key    = "key-example.txt"
  val body   = "testing-body-example"

  val uploadId = "upload-id-example"

  val requestIdExample                 = "request-id-example"
  val genericResponse: GenericResponse = GenericResponse(
    requestIdExample,
    Headers(
      Header("Content-Type", "text/plain; charset=UTF-8"),
      Header("Content-Length", 0.toString)))

  val eTagExample = "dcalknjgfrewlknocadvsnlkjsdfalnk"

  test("Test create bucket") {
    val client = HttpRoutes
      .of[IO] { case PUT -> Root =>
        Ok("").map(_.transformHeaders(
          _.put(Header("Location", s"/$bucket"), Header("x-amz-request-id", requestIdExample))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.createBucket(bucket).map(_.location).assertEquals(s"/$bucket")
  }

  test("Test delete bucket") {
    val client = HttpRoutes
      .of[IO] { case DELETE -> Root =>
        Ok("").map(_.transformHeaders(_.put(Header("x-amz-request-id", requestIdExample))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.deleteBucket(bucket).assertEquals(genericResponse)
  }

  val listBucketsResponseExample =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<ListAllMyBucketsResult>
      | <Buckets>
      |  <Bucket>
      |    <CreationDate>2022-01-17</CreationDate>
      |    <Name>bucket-name</Name>
      |  </Bucket>
      | </Buckets>
      | <Owner>
      | <DisplayName>accountname</DisplayName>
      | <ID>accountid</ID>
      | </Owner>
      |</ListAllMyBucketsResult>""".stripMargin

  test("Test listing buckets") {
    val client = HttpRoutes
      .of[IO] { case GET -> Root =>
        Ok(listBucketsResponseExample)
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.listBuckets().map(_.buckets.size).assertEquals(1)
  }

  val listObjectsExample: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<ListBucketResponse>
      | <Name>bucket</Name>
      | <KeyCount>205</KeyCount>
      | <MaxKeys>1000</MaxKeys>
      | <IsTruncated>false</IsTruncated>
      | <Contents>
      |  <Key>my-image.jpg</Key>
      |  <LastModified>2009-10-12T17:50:30.000Z</LastModified>
      |  <ETag>"fba9dede5f27731c9771645a39863328"</ETag>
      |  <Size>434234</Size>
      |  <StorageClass>STANDARD</StorageClass>
      | </Contents>
      |</ListBucketResponse>""".stripMargin

  test("Test listing objects") {
    val client = HttpRoutes
      .of[IO] { case GET -> Root =>
        Ok(listObjectsExample)
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.listObjectsV2(bucket).map(_.contents.size).assertEquals(1)
  }

  test("Test putting objects") {
    val client = HttpRoutes
      .of[IO] { case PUT -> Root / `key` =>
        Ok("").map(_.transformHeaders(
          _.put(Header("x-amz-request-id", requestIdExample), Header("ETag", eTagExample))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.putObject(bucket, key, body)
      .map(_.eTag)
      .assertEquals(eTagExample)
  }

  implicit val decoder: EntityDecoder[IO, String] = EntityDecoder.text
  test("Test get object") {
    val client = HttpRoutes
      .of[IO] { case GET -> Root / `key` =>
        Ok(body).map(_.transformHeaders(
          _.put(Header("x-amz-request-id", requestIdExample), Header("ETag", eTagExample))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.getObject(bucket, key).map(_.body).assertEquals(body)
  }

  test("Test delete object") {
    val client = HttpRoutes
      .of[IO] { case DELETE -> Root / `key` =>
        Ok().map(_.transformHeaders(_.put(Header("x-amz-request-id", requestIdExample))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.deleteObject(bucket, key)
      .map(_.requestId)
      .assertEquals(requestIdExample)
  }

  val copyObjectResponse =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<CopyObjectResult>
      |  <ETag>9b2cf535f27731c974343645a3985328</ETag>
      |  <LastModified>2009-10-28T22:32:00</LastModified>
      |</CopyObjectResult>""".stripMargin

  test("Test copying objects") {
    val client = HttpRoutes
      .of[IO] { case PUT -> Root / `key` =>
        Ok(copyObjectResponse)
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.copyObject(bucket, key, key)
      .map(_.eTag)
      .assertEquals("9b2cf535f27731c974343645a3985328")
  }

  test("Test head object") {
    val client = HttpRoutes
      .of[IO] { case HEAD -> Root / `key` =>
        Ok("").map(
          _.transformHeaders(
            _.put(
              Header("x-amz-request-id", requestIdExample),
              Header("ETag", eTagExample),
              Header("Content-Length", 1.toString),
              Header("Content-Type", "text/plain"))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.headObject(bucket, key).map(_.eTag).assertEquals(eTagExample)
  }

  val createMultipartUploadExample: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<InitiateMultipartUploadResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      |<Bucket>example-bucket</Bucket>
      |<Key>example-object</Key>
      |<UploadId>VXBsb2FkIElEIGZvciA2aWWpbmcncyBteS1tb3ZpZS5tMnRzIHVwbG9hZA</UploadId>
      |</InitiateMultipartUploadResult>""".stripMargin

  test("Test create multipart upload") {
    val client = HttpRoutes
      .of[IO] { case POST -> Root / `key` =>
        Ok(createMultipartUploadExample)
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.createMultipartUpload(bucket, key)
      .map(_.uploadId)
      .assertEquals("VXBsb2FkIElEIGZvciA2aWWpbmcncyBteS1tb3ZpZS5tMnRzIHVwbG9hZA")
  }

  test("Test abort multipart upload") {
    val client = HttpRoutes
      .of[IO] { case DELETE -> Root / `key` =>
        Ok().map(_.transformHeaders(_.put(Header("x-amz-request-id", requestIdExample))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.abortMultipartUpload(bucket, key, uploadId)
      .map(_.requestId)
      .assertEquals(requestIdExample)
  }

  val listMultipartUploadsResponseExample: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<ListMultipartUploadsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      |  <Bucket>bucket</Bucket>
      |  <KeyMarker></KeyMarker>
      |  <UploadIdMarker></UploadIdMarker>
      |  <NextKeyMarker>my-movie.m2ts</NextKeyMarker>
      |  <NextUploadIdMarker>YW55IGlkZWEgd2h5IGVsdmluZydzIHVwbG9hZCBmYWlsZWQ</NextUploadIdMarker>
      |  <MaxUploads>3</MaxUploads>
      |  <IsTruncated>true</IsTruncated>
      |  <Upload>
      |    <Key>my-divisor</Key>
      |    <UploadId>XMgbGlrZSBlbHZpbmcncyBub3QgaGF2aW5nIG11Y2ggbHVjaw</UploadId>
      |    <Initiator>
      |      <ID>arn:aws:iam::111122223333:user/user1-11111a31-17b5-4fb7-9df5-b111111f13de</ID>
      |      <DisplayName>user1-11111a31-17b5-4fb7-9df5-b111111f13de</DisplayName>
      |    </Initiator>
      |    <Owner>
      |      <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>
      |      <DisplayName>OwnerDisplayName</DisplayName>
      |    </Owner>
      |    <StorageClass>STANDARD</StorageClass>
      |    <Initiated>2010-11-10T20:48:33.000Z</Initiated>
      |  </Upload>
      |  <Upload>
      |    <Key>my-movie.m2ts</Key>
      |    <UploadId>VXBsb2FkIElEIGZvciBlbHZpbmcncyBteS1tb3ZpZS5tMnRzIHVwbG9hZA</UploadId>
      |    <Initiator>
      |      <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
      |      <DisplayName>InitiatorDisplayName</DisplayName>
      |    </Initiator>
      |    <Owner>
      |      <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
      |      <DisplayName>OwnerDisplayName</DisplayName>
      |    </Owner>
      |    <StorageClass>STANDARD</StorageClass>
      |    <Initiated>2010-11-10T20:48:33.000Z</Initiated>
      |  </Upload>
      |  <Upload>
      |    <Key>my-movie.m2ts</Key>
      |    <UploadId>YW55IGlkZWEgd2h5IGVsdmluZydzIHVwbG9hZCBmYWlsZWQ</UploadId>
      |    <Initiator>
      |      <ID>arn:aws:iam::444455556666:user/user1-22222a31-17b5-4fb7-9df5-b222222f13de</ID>
      |      <DisplayName>user1-22222a31-17b5-4fb7-9df5-b222222f13de</DisplayName>
      |    </Initiator>
      |    <Owner>
      |      <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
      |      <DisplayName>OwnerDisplayName</DisplayName>
      |    </Owner>
      |    <StorageClass>STANDARD</StorageClass>
      |    <Initiated>2010-11-10T20:49:33.000Z</Initiated>
      |  </Upload>
      |</ListMultipartUploadsResult>""".stripMargin

  test("Test list multipart uploads") {
    val client = HttpRoutes
      .of[IO] { case GET -> Root =>
        Ok(listMultipartUploadsResponseExample)
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.listMultipartUploads(bucket)
      .map(_.uploads.get.size)
      .assertEquals(3)
  }

  val listPartsExample: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<ListPartsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      |  <Bucket>example-bucket</Bucket>
      |  <Key>example-object</Key>
      |  <UploadId>XXBsb2FkIElEIGZvciBlbHZpbmcncyVcdS1tb3ZpZS5tMnRzEEEwbG9hZA</UploadId>
      |  <Initiator>
      |      <ID>arn:aws:iam::111122223333:user/some-user-11116a31-17b5-4fb7-9df5-b288870f11xx</ID>
      |      <DisplayName>umat-user-11116a31-17b5-4fb7-9df5-b288870f11xx</DisplayName>
      |  </Initiator>
      |  <Owner>
      |    <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>
      |    <DisplayName>someName</DisplayName>
      |  </Owner>
      |  <StorageClass>STANDARD</StorageClass>
      |  <PartNumberMarker>1</PartNumberMarker>
      |  <NextPartNumberMarker>3</NextPartNumberMarker>
      |  <MaxParts>2</MaxParts>
      |  <IsTruncated>true</IsTruncated>
      |  <Part>
      |    <PartNumber>2</PartNumber>
      |    <LastModified>2010-11-10T20:48:34.000Z</LastModified>
      |    <ETag>"7778aef83f66abc1fa1e8477f296d394"</ETag>
      |    <Size>10485760</Size>
      |  </Part>
      |  <Part>
      |    <PartNumber>3</PartNumber>
      |    <LastModified>2010-11-10T20:48:33.000Z</LastModified>
      |    <ETag>"aaaa18db4cc2f85cedef654fccc4a4x8"</ETag>
      |    <Size>10485760</Size>
      |  </Part>
      |</ListPartsResult>""".stripMargin

  test("Test list parts") {
    val client = HttpRoutes
      .of[IO] { case GET -> Root / `key` =>
        Ok(listPartsExample)
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.listParts(bucket, key, uploadId)
      .map(_.parts.size)
      .assertEquals(2)
  }

  test("Test uploading part") {
    val client = HttpRoutes
      .of[IO] { case PUT -> Root / `key` =>
        Ok().map(_.transformHeaders(
          _.put(Header("x-amz-request-id", requestIdExample), Header("ETag", eTagExample))))
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.uploadPart(bucket, key, 1, uploadId, body)
      .map(_.eTag)
      .assertEquals(eTagExample)
  }

  val completeMultipartUploadExample: String =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<CompleteMultipartUploadResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
      |   <Location>http://Example-Bucket.s3.amazonaws.com/Example-Object</Location>
      |   <Bucket>Example-Bucket</Bucket>
      |   <Key>Example-Object</Key>
      |   <ETag>3858f62230ac3c915f300c664312c11f-9</ETag>
      |</CompleteMultipartUploadResult>""".stripMargin

  test("Test completing multipart upload") {
    val client = HttpRoutes
      .of[IO] { case POST -> Root / `key` =>
        Ok(completeMultipartUploadExample)
      }
      .orNotFound
    val s3     = S3[IO](Client.fromHttpApp(client), credentials, region)
    s3.completeMultipartUpload(bucket, key, uploadId, List(eTagExample))
      .map(_.eTag)
      .assertEquals("3858f62230ac3c915f300c664312c11f-9")
  }

}
