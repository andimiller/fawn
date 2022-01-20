package com.meltwater.fawn.sqs

import cats.effect.IO
import com.meltwater.fawn.s3._
import net.andimiller.munit.cats.effect.styles.FlatIOSpec
import org.http4s.scalaxml.xml
import org.http4s.{EntityDecoder, Header, Response}

import scala.xml.Elem

class S3PayloadsSpec extends FlatIOSpec{

  val exampleReceive: String =
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

  test("decode a list all my buckets response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(exampleReceive)
          .withHeaders(Header("content-type", "text/xml")),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map {
            elem =>
              ListAllMyBucketsResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val exampleListObjectsV2: String =
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

  test("decode a list all objects within a bucket response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(exampleListObjectsV2)
          .withHeaders(Header("content-type", "text/xml")),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map {
            elem =>
              ListBucketResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val getBucketAclExample =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<AccessControlPolicy>
      | <Owner>
      |    <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>
      |    <DisplayName>CustomersName@amazon.com</DisplayName>
      |  </Owner>
      |  <AccessControlList>
      |    <Grant>
      |      <Grantee xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      |			xsi:type="CanonicalUser">
      |        <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>
      |        <DisplayName>CustomersName@amazon.com</DisplayName>
      |      </Grantee>
      |      <Permission>FULL_CONTROL</Permission>
      |    </Grant>
      |  </AccessControlList>
      |</AccessControlPolicy>
      |""".stripMargin

  test("decode a get bucket acl response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(getBucketAclExample)
          .withHeaders(Header("content-type", "text/xml")),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map {
            elem =>
              GetBucketAclResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
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

  test("decode a list multipart upload response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(getBucketAclExample)
          .withHeaders(Header("content-type", "text/xml")),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map {
            elem =>
              ListMultipartUploadsResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

}
