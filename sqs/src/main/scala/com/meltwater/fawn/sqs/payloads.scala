package com.meltwater.fawn.sqs

import cats.implicits._
import com.lucidchart.open.xtract.{__, XmlReader}
import com.lucidchart.open.xtract.XmlReader._

case class Message(
    id: String,
    receiptHandle: String,
    md5OfBody: String,
    body: String,
    attributes: Map[String, String])

object Message {
  implicit val attributeDecoder: XmlReader[(String, String)] = (
    (__ \ "Name").read[String],
    (__ \ "Value").read[String]
  ).tupled

  implicit val xmlDecoder: XmlReader[Message] = (
    (__ \ "MessageId").read[String],
    (__ \ "ReceiptHandle").read[String],
    (__ \ "MD5OfBody").read[String],
    (__ \ "Body").read[String],
    (__ \ "Attribute").read(seq[(String, String)].map(_.toMap))
  ).mapN(Message.apply)
}

case class ReceiveMessageResponse(messages: Vector[Message], requestId: String)

object ReceiveMessageResponse {
  implicit val xmlDecoder: XmlReader[ReceiveMessageResponse] =
    (
      (__ \ "ReceiveMessageResult" \ "Message").read(seq[Message]).map(_.toVector),
      (__ \ "ResponseMetadata" \ "RequestId").read[String]
    ).mapN(ReceiveMessageResponse.apply)
}

case class DeleteMessageResponse(requestId: String)

object DeleteMessageResponse {
  implicit val xmlDecoder: XmlReader[DeleteMessageResponse] =
    (__ \ "ResponseMetadata" \ "RequestId").read[String].map(DeleteMessageResponse.apply)
}

case class SendMessageResult(bodyMd5: String, attributeMd5: Option[String], messageId: String)

object SendMessageResult {
  implicit val xmlDecoder: XmlReader[SendMessageResult] = (
    (__ \ "MD5OfMessageBody").read[String],
    (__ \ "MD5OfMessageAttributes").read[String].optional,
    (__ \ "MessageId").read[String]
  ).mapN(SendMessageResult.apply)
}

case class SendMessageResponse(result: SendMessageResult, requestId: String)

object SendMessageResponse {

  implicit val xmlDecoder: XmlReader[SendMessageResponse] = (
    (__ \ "SendMessageResult").read[SendMessageResult],
    (__ \ "ResponseMetadata" \ "RequestId").read[String]
  ).mapN(SendMessageResponse.apply)
}

case class SendMessageBatchResultEntry(
    id: String,
    bodyMd5: String,
    attributeMd5: Option[String],
    messageId: String)

object SendMessageBatchResultEntry {
  implicit val xmlDecoder: XmlReader[SendMessageBatchResultEntry] = (
    (__ \ "Id").read[String],
    (__ \ "MD5OfMessageBody").read[String],
    (__ \ "MD5OfMessageAttributes").read[String].optional,
    (__ \ "MessageId").read[String]
  ).mapN(SendMessageBatchResultEntry.apply)
}

case class SendMessageBatchResponse(results: Vector[SendMessageBatchResultEntry], requestId: String)

object SendMessageBatchResponse {
  implicit val xmlDecoder = (
    (__ \ "SendMessageBatchResult" \ "SendMessageBatchResultEntry")
      .read(seq[SendMessageBatchResultEntry])
      .map(_.toVector),
    (__ \ "ResponseMetadata" \ "RequestId").read[String]
  ).mapN(SendMessageBatchResponse.apply)
}
