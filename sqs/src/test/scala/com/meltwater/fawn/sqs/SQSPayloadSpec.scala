package com.meltwater.fawn.sqs

import cats.effect.IO
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion}
import net.andimiller.munit.cats.effect.styles.FlatIOSpec
import org.http4s.client.Client
import org.http4s.scalaxml.xml
import org.http4s.{EntityDecoder, Header, HttpRoutes, Response}

import scala.xml.Elem

class SQSPayloadSpec extends FlatIOSpec {

  val exampleReceive: String =
    """<?xml version="1.0"?><ReceiveMessageResponse xmlns="http://queue.amazonaws.com/doc/2012-11-05/"><ReceiveMessageResult><Message><MessageId>3eb81b51-c649-4511-9d5f-a4030fdc7e95</MessageId><ReceiptHandle>AQEBuSy+NSIIiM49mCOz0SPGs0PTQcVcRNfZ71FiI7yOIbulY3qQrEwy32gZjnek6gYU6s+FyXJIOIRz2rvJYEmgadfCt3vpmoVm3FuMLYoeMFTG+x7+0Zjz3WMyuSyS9O64SwMnrYdMRwpZT+eMMUI4NKU4tKLK0i3b9TyK8pQbMveATz6YMbrpJ2/V+e89jpt/6A8zaPTrHzHaS81G1PwSnOIZ9ue0zBDZ8vPg9zS0ySiaH4HQpsisSGWu0Tyivf5jGB2dx4eLbRUOQLtJQGRXTZwidSiOC8YCwJzCFw9+SLgL98PkaK1mW6ujdms5sOuDIxHGsHzqZ9v/cjmcbAKMO8Gj5jnPXEUKCrfDzAStUxdPntzIkfK/TAwJzsI2h7//6cd54DLbEE1PhrFl5XpCqg==</ReceiptHandle><MD5OfBody>5d41402abc4b2a76b9719d911017c592</MD5OfBody><Body>hello</Body><Attribute><Name>SenderId</Name><Value>AROA4XTNVF7FR4HFGORQ2:Andi.Miller@meltwater.com</Value></Attribute><Attribute><Name>ApproximateFirstReceiveTimestamp</Name><Value>1636650551584</Value></Attribute><Attribute><Name>ApproximateReceiveCount</Name><Value>1</Value></Attribute><Attribute><Name>SentTimestamp</Name><Value>1636650540794</Value></Attribute></Message></ReceiveMessageResult><ResponseMetadata><RequestId>67bda148-0d6e-534b-afa1-fb4f5d195fb4</RequestId></ResponseMetadata></ReceiveMessageResponse>"""

  test("decode a recievemessage response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(exampleReceive)
          .withHeaders(Header("content-type", "text/html")),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.map { elem =>
            ReceiveMessageResponse.xmlDecoder.read(elem)
          }.isRight
        }.assertEquals(true)
      }
  }

  val exampleSendMessageBatchResponse: String =
    """<?xml version="1.0"?><SendMessageBatchResponse xmlns="http://queue.amazonaws.com/doc/2012-11-05/"><SendMessageBatchResult><SendMessageBatchResultEntry><Id>9</Id><MessageId>a1cc97c7-7dbf-4be4-80e8-0d6c6d9bd537</MessageId><MD5OfMessageBody>60297a010eedea3dd63f4b423729be7c</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>10</Id><MessageId>d24c7b89-8f87-4436-9fb5-3f5bdb49d118</MessageId><MD5OfMessageBody>00242dab69819aa52a6131f95a805d46</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>7</Id><MessageId>0a8a3691-4921-4930-ba29-d44c914e8e10</MessageId><MD5OfMessageBody>c2a86c0a92fd0b05b63db1877e303aa2</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>8</Id><MessageId>bae42cd9-af3e-4218-b681-190c153e4ea0</MessageId><MD5OfMessageBody>5d8ee999739794db9dbf153d01c8d29e</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>5</Id><MessageId>54d8eba9-638a-4dee-8e36-92f9b82e8582</MessageId><MD5OfMessageBody>b73f4b696468d76ba85822e136dc2f56</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>6</Id><MessageId>1d54cf9d-2d81-4945-964d-52ab3b85a732</MessageId><MD5OfMessageBody>4aa889706e7769063940b8892b1c1c47</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>3</Id><MessageId>e62e5639-e012-4456-b705-5bb0d588215b</MessageId><MD5OfMessageBody>20df819e1db02652fa9112eccaf02622</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>4</Id><MessageId>f2004b8d-741f-4757-8097-faef8ac4c2b3</MessageId><MD5OfMessageBody>05ade96bdcc202c366bac7633417dcf7</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>1</Id><MessageId>e62e619f-2419-466e-8956-1d6bf29b9ed7</MessageId><MD5OfMessageBody>9404268ffd0a161fb71e5ab70b5a971c</MD5OfMessageBody></SendMessageBatchResultEntry><SendMessageBatchResultEntry><Id>2</Id><MessageId>db0f9d6c-1e7e-4bdd-bb86-d588de572535</MessageId><MD5OfMessageBody>c1951dbe40d009bd962f765db2c35467</MD5OfMessageBody></SendMessageBatchResultEntry></SendMessageBatchResult><ResponseMetadata><RequestId>45482723-09ec-5006-b193-7d32c6158daf</RequestId></ResponseMetadata></SendMessageBatchResponse>"""

  test("decode the multi send response") {
    EntityDecoder[IO, Elem]
      .decode(
        Response[IO]()
          .withEntity(exampleSendMessageBatchResponse)
          .withHeaders(Header("content-type", "text/html")),
        false
      )
      .value
      .flatMap { r =>
        IO {
          r.toOption.flatMap { elem =>
            SendMessageBatchResponse.xmlDecoder.read(elem).toOption
          }.isDefined
        }.assertEquals(true)
      }
  }

  test("decode multi send response in a real setup") {
    import org.http4s.dsl.io._
    import org.http4s.implicits._
    val routes = HttpRoutes.of[IO] { case POST -> Root / "1234" / "queue-name" =>
      Ok(
        exampleSendMessageBatchResponse
      ).map(_.withHeaders(Header("content-type", "text/xml")))
    }
    val client = Client.fromHttpApp(routes.orNotFound)
    val sqs    = SQSQueue[IO](
      client,
      AWSCredentials("fake", "creds"),
      AWSRegion.`eu-west-1`,
      1234,
      "queue-name")
    sqs
      .sendMessages(("hello world", Map.empty))
      .map(_.results.map(_.id))
      .assertEquals(
        Vector("9", "10", "7", "8", "5", "6", "3", "4", "1", "2")
      )
  }

  test("decode receive response in a real setup") {
    import org.http4s.dsl.io._
    import org.http4s.implicits._
    val routes = HttpRoutes.of[IO] { case GET -> Root / "1234" / "queue-name" =>
      Ok(
        exampleReceive
      ).map(_.withHeaders(Header("content-type", "text/xml")))
    }
    val client = Client.fromHttpApp(routes.orNotFound)
    val sqs    = SQSQueue[IO](
      client,
      AWSCredentials("fake", "creds"),
      AWSRegion.`eu-west-1`,
      1234,
      "queue-name")
    sqs
      .receiveMessage(10)
      .map(_.messages.map(_.body))
      .assertEquals(Vector("hello"))
  }

}
