package com.meltwater.fawn.s3

import cats.effect.IO
import net.andimiller.munit.cats.effect.styles.FlatIOSpec
import org.http4s._
import org.http4s.scalaxml._

import scala.xml.Elem

class S3PayloadsSpec extends FlatIOSpec {

  val listBucketsExample: Elem =
    <ListAllMyBucketsResult>
       <Buckets>
        <Bucket>
          <CreationDate>2022-01-17</CreationDate>
          <Name>bucket-name</Name>
        </Bucket>
       </Buckets>
       <Owner>
       <DisplayName>accountname</DisplayName>
       <ID>accountid</ID>
       </Owner>
      </ListAllMyBucketsResult>

  test("decode a list all my buckets response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(listBucketsExample)
          .transformHeaders(_.put(Header("Content-Type", "text/xml"))),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            ListBucketsResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val listObjectsExample: Elem =
    <ListBucketResponse>
       <Name>bucket</Name>
       <KeyCount>205</KeyCount>
       <MaxKeys>1000</MaxKeys>
       <IsTruncated>false</IsTruncated>
       <Contents>
          <Key>my-image.jpg</Key>
          <LastModified>2009-10-12T17:50:30.000Z</LastModified>
          <ETag>"fba9dede5f27731c9771645a39863328"</ETag>
          <Size>434234</Size>
          <StorageClass>STANDARD</StorageClass>
       </Contents>
      </ListBucketResponse>

  test("decode a list all objects within a bucket response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(listObjectsExample)
          .transformHeaders(_.put(Header("Content-Type", "text/xml"))),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            ListObjectsResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val copyObjectExample: Elem =
    <CopyObjectResult>
      <ETag>"9b2cf535f27731c974343645a3985328"</ETag>
      <LastModified>2009-10-28T22:32:00</LastModified>
    </CopyObjectResult>

  test("decode a copy object response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(copyObjectExample)
          .transformHeaders(_.put(Header("Content-Type", "text/xml"))),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            CopyObjectResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val createMultipartUploadExample: Elem =
    <InitiateMultipartUploadResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
       <Bucket>example-bucket</Bucket>
       <Key>example-object</Key>
       <UploadId>VXBsb2FkIElEIGZvciA2aWWpbmcncyBteS1tb3ZpZS5tMnRzIHVwbG9hZA</UploadId>
    </InitiateMultipartUploadResult>

  test("decode a create multipart upload response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(createMultipartUploadExample)
          .transformHeaders(_.put(Header("Content-Type", "text/xml"))),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            CreateMultipartUploadResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val listMultipartUploadsResponseExample: Elem =
    <ListMultipartUploadsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
       <Bucket>bucket</Bucket>
       <KeyMarker></KeyMarker>
       <UploadIdMarker></UploadIdMarker>
       <NextKeyMarker>my-movie.m2ts</NextKeyMarker>
       <NextUploadIdMarker>YW55IGlkZWEgd2h5IGVsdmluZydzIHVwbG9hZCBmYWlsZWQ</NextUploadIdMarker>
       <MaxUploads>3</MaxUploads>
       <IsTruncated>true</IsTruncated>
       <Upload>
          <Key>my-divisor</Key>
          <UploadId>XMgbGlrZSBlbHZpbmcncyBub3QgaGF2aW5nIG11Y2ggbHVjaw</UploadId>
          <Initiator>
             <ID>arn:aws:iam::111122223333:user/user1-11111a31-17b5-4fb7-9df5-b111111f13de</ID>
             <DisplayName>user1-11111a31-17b5-4fb7-9df5-b111111f13de</DisplayName>
          </Initiator>
          <Owner>
             <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>
             <DisplayName>OwnerDisplayName</DisplayName>
          </Owner>
          <StorageClass>STANDARD</StorageClass>
          <Initiated>2010-11-10T20:48:33.000Z</Initiated>
       </Upload>
       <Upload>
          <Key>my-movie.m2ts</Key>
          <UploadId>VXBsb2FkIElEIGZvciBlbHZpbmcncyBteS1tb3ZpZS5tMnRzIHVwbG9hZA</UploadId>
          <Initiator>
             <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
             <DisplayName>InitiatorDisplayName</DisplayName>
          </Initiator>
          <Owner>
             <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
             <DisplayName>OwnerDisplayName</DisplayName>
          </Owner>
          <StorageClass>STANDARD</StorageClass>
          <Initiated>2010-11-10T20:48:33.000Z</Initiated>
       </Upload>
       <Upload>
          <Key>my-movie.m2ts</Key>
          <UploadId>YW55IGlkZWEgd2h5IGVsdmluZydzIHVwbG9hZCBmYWlsZWQ</UploadId>
          <Initiator>
             <ID>arn:aws:iam::444455556666:user/user1-22222a31-17b5-4fb7-9df5-b222222f13de</ID>
             <DisplayName>user1-22222a31-17b5-4fb7-9df5-b222222f13de</DisplayName>
          </Initiator>
          <Owner>
             <ID>b1d16700c70b0b05597d7acd6a3f92be</ID>
             <DisplayName>OwnerDisplayName</DisplayName>
          </Owner>
          <StorageClass>STANDARD</StorageClass>
          <Initiated>2010-11-10T20:49:33.000Z</Initiated>
       </Upload>
      </ListMultipartUploadsResult>

  test("decode a list multipart upload response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(listMultipartUploadsResponseExample)
          .transformHeaders(_.put(Header("Content-Type", "text/xml"))),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            ListMultipartUploadsResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val listPartsExample: Elem =
    <ListPartsResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
        <Bucket>example-bucket</Bucket>
        <Key>example-object</Key>
        <UploadId>XXBsb2FkIElEIGZvciBlbHZpbmcncyVcdS1tb3ZpZS5tMnRzEEEwbG9hZA</UploadId>
        <Initiator>
            <ID>arn:aws:iam::111122223333:user/some-user-11116a31-17b5-4fb7-9df5-b288870f11xx</ID>
            <DisplayName>umat-user-11116a31-17b5-4fb7-9df5-b288870f11xx</DisplayName>
        </Initiator>
        <Owner>
          <ID>75aa57f09aa0c8caeab4f8c24e99d10f8e7faeebf76c078efc7c6caea54ba06a</ID>
          <DisplayName>someName</DisplayName>
        </Owner>
        <StorageClass>STANDARD</StorageClass>
        <PartNumberMarker>1</PartNumberMarker>
        <NextPartNumberMarker>3</NextPartNumberMarker>
        <MaxParts>2</MaxParts>
        <IsTruncated>true</IsTruncated>
        <Part>
          <PartNumber>2</PartNumber>
          <LastModified>2010-11-10T20:48:34.000Z</LastModified>
          <ETag>"7778aef83f66abc1fa1e8477f296d394"</ETag>
          <Size>10485760</Size>
        </Part>
        <Part>
          <PartNumber>3</PartNumber>
          <LastModified>2010-11-10T20:48:33.000Z</LastModified>
          <ETag>"aaaa18db4cc2f85cedef654fccc4a4x8"</ETag>
          <Size>10485760</Size>
        </Part>
      </ListPartsResult>

  test("decode a list multipart upload parts response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(listMultipartUploadsResponseExample)
          .transformHeaders(_.put(Header("Content-Type", "text/xml"))),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            ListPartsResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val completeMultipartUploadExample: Elem =
    <CompleteMultipartUploadResult xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
       <Location>http://Example-Bucket.s3.amazonaws.com/Example-Object</Location>
       <Bucket>Example-Bucket</Bucket>
       <Key>Example-Object</Key>
       <ETag>"3858f62230ac3c915f300c664312c11f-9"</ETag>
    </CompleteMultipartUploadResult>

  test("decode a complete multipart upload response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(completeMultipartUploadExample)
          .transformHeaders(_.put(Header("Content-Type", "text/xml"))),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            CompleteMultipartUploadResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

}
