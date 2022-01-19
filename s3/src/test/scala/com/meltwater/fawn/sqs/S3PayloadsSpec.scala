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
              ListAllMyBucketsResult.xmlDecoder.read(elem)
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
              ListBucketResult.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

}
