package com.meltwater.fawn.sqs

import cats.Functor
import cats.implicits._
import cats.effect.{Clock, Sync}
import cats.tagless.{autoFunctorK, autoInstrument, autoProductNK, autoSemigroupalK, finalAlg}
import com.lucidchart.open.xtract.{ParseFailure, ParseSuccess, PartialParseSuccess, XmlReader}
import com.meltwater.fawn.codec.{FawnDecoder, FawnEncoder}
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion, AWSService}
import org.http4s.{
  EntityDecoder,
  Headers,
  MalformedMessageBodyFailure,
  Method,
  Request,
  Response,
  Status,
  Uri
}
import org.http4s.client.Client
import org.http4s.scalaxml.xml

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NoStackTrace
import SQSQueue.SQSMessage
import com.meltwater.fawn.auth.V4Middleware

/** Represents an SQS queue on AWS
  *
  * Provides low-level methods that correspond to AWS endpoints and use payloads close to AWS's
  *
  * Also provides higher level methods that use Encoders/Decoders and smaller payload classes
  */
@finalAlg
@autoFunctorK
@autoSemigroupalK
@autoProductNK
@autoInstrument
trait SQSQueue[F[_]] {
  // low-level methods
  def sendMessage(body: String, attributes: Map[String, String] = Map.empty): F[SendMessageResponse]
  def sendMessages(messages: (String, Map[String, String])*): F[SendMessageBatchResponse]
  def receiveMessage(max: Int = 1, wait: Option[FiniteDuration] = None): F[ReceiveMessageResponse]
  def deleteMessage(receiptHandle: String): F[DeleteMessageResponse]

  // mid-level methods
  def sendAs[T: FawnEncoder](
      t: T,
      attributes: Map[String, String] = Map.empty): F[SendMessageResponse]
  def sendManyAs[T: FawnEncoder](messages: (T, Map[String, String])*): F[SendMessageBatchResponse]
  def receiveAs[T: FawnDecoder](
      max: Int = 1,
      wait: Option[FiniteDuration] = None): F[Vector[SQSMessage[T]]]
  def ack[T](msg: SQSMessage[T]): F[DeleteMessageResponse]
}

object SQSQueue {
  implicit def xtractDecoder[F[_]: Sync, T: XmlReader]: EntityDecoder[F, T] = xml[F].transform {
    r =>
      r.flatMap { elem =>
        XmlReader.of[T].read(elem) match {
          case ParseSuccess(get)              => get.asRight
          case PartialParseSuccess(_, errors) =>
            MalformedMessageBodyFailure(s"Unable to parse body: $errors").asLeft
          case ParseFailure(errors)           =>
            MalformedMessageBodyFailure(s"Unable to parse body: $errors").asLeft
        }
      }
  }

  // this is the version of the Amazon SQS API we use, bump this if a new one comes out
  final val version = "2012-11-05"

  case class SQSMessage[T](body: T, headers: Map[String, String], receipt: String)
  object SQSMessage {
    implicit val functor: Functor[SQSMessage] = new Functor[SQSMessage] {
      override def map[A, B](fa: SQSMessage[A])(f: A => B): SQSMessage[B] =
        fa.copy(body = f(fa.body))
    }
  }

  case class SQSError(code: Status, headers: Headers, body: String)
      extends Throwable
      with NoStackTrace {
    override def getMessage: String =
      s"SQSError(code = $code, headers = $headers, body = \"$body\")"
  }

  def apply[F[_]: Sync: Clock](
      baseClient: Client[F],
      credentials: AWSCredentials,
      awsRegion: AWSRegion,
      accountId: Long,
      queueName: String): SQSQueue[F] =
    new SQSQueue[F] {
      private val client: Client[F] =
        V4Middleware[F](credentials, awsRegion, AWSService.sqs).apply(baseClient)

      private val region: String = awsRegion.value

      private def handleError(r: Response[F]): F[Throwable] =
        r.bodyText.compile.foldMonoid.map { b =>
          SQSError(r.status, r.headers, b)
        }

      override def receiveMessage(
          max: Int,
          wait: Option[FiniteDuration]): F[ReceiveMessageResponse] =
        client
          .expectOr(
            Request[F](
              Method.GET,
              (Uri
                .fromString(s"https://sqs.$region.amazonaws.com")
                .toOption
                .get / accountId.toString / queueName)
                .withQueryParams(Map(
                  "Action"              -> "ReceiveMessage".some,
                  "MaxNumberOfMessages" -> max.toString.some,
                  "WaitTimeSeconds"     -> wait.map(_.toSeconds.toString),
                  "AttributeName"       -> "All".some,
                  "Version"             -> version.some
                ).flattenOption)
            )
          )(handleError)

      override def deleteMessage(receiptHandle: String): F[DeleteMessageResponse] =
        client.expectOr(
          Request[F](
            Method.DELETE,
            (Uri
              .fromString(s"https://sqs.$region.amazonaws.com")
              .toOption
              .get / accountId.toString / queueName)
              .withQueryParams(
                Map(
                  "Action"        -> "DeleteMessage".some,
                  "ReceiptHandle" -> receiptHandle.some,
                  "Version"       -> version.some
                ).flattenOption)
          )
        )(handleError)

      override def sendMessage(
          body: String,
          attributes: Map[String, String]): F[SendMessageResponse] = client.expectOr(
        Request[F](
          Method.POST,
          (Uri
            .fromString(s"https://sqs.$region.amazonaws.com")
            .toOption
            .get / accountId.toString / queueName)
            .withQueryParams(
              (
                List(
                  "Action"      -> "SendMessage",
                  "MessageBody" -> body,
                  "Version"     -> version
                ) ++ attributes.toList.zipWithIndex.flatMap { case ((k, v), idx) =>
                  val i = idx + 1 // we index from 1
                  List(
                    s"MessageAttribute.$i.Name"              -> k,
                    s"MessageAttribute.$i.Value.StringValue" -> v,
                    s"MessageAttribute.$i.Value.DataType"    -> "String"
                  )
                }
              ).toMap
            )
        )
      )(handleError)

      override def sendMessages(
          messages: (String, Map[String, String])*): F[SendMessageBatchResponse] =
        client.expectOr(
          Request[F](
            Method.POST,
            (Uri
              .fromString(s"https://sqs.$region.amazonaws.com")
              .toOption
              .get / accountId.toString / queueName)
              .withQueryParams(
                (List(
                  "Action"  -> "SendMessageBatch",
                  "Version" -> version
                ) ++
                  messages.toList.zipWithIndex.flatMap { case ((body, attributes), bidx) =>
                    val bi = bidx + 1 // we index from 1
                    List(
                      s"SendMessageBatchRequestEntry.$bi.Id"          -> bi.toString,
                      s"SendMessageBatchRequestEntry.$bi.MessageBody" -> body
                    ) ++ attributes.toList.zipWithIndex.flatMap { case ((k, v), idx) =>
                      val i = idx + 1 // we index from 1
                      List(
                        s"SendMessageBatchRequestEntry.$bi.MessageAttribute.$i.Name"              -> k,
                        s"SendMessageBatchRequestEntry.$bi.MessageAttribute.$i.Value.StringValue" -> v,
                        s"SendMessageBatchRequestEntry.$bi.MessageAttribute.$i.Value.DataType"    -> "String"
                      )
                    }
                  }).toMap
              )
          )
        )(handleError)

      override def sendAs[T: FawnEncoder](
          t: T,
          attributes: Map[String, String]): F[SendMessageResponse] =
        sendMessage(FawnEncoder[T].encode(t), attributes)

      override def sendManyAs[T: FawnEncoder](
          messages: (T, Map[String, String])*): F[SendMessageBatchResponse] =
        sendMessages(messages.map { case (m, a) => (FawnEncoder[T].encode(m), a) }: _*)

      override def receiveAs[T: FawnDecoder](
          max: Int,
          wait: Option[FiniteDuration]): F[Vector[SQSMessage[T]]]           =
        receiveMessage(max, wait).flatMap { resp =>
          resp.messages.traverse { m =>
            Sync[F].fromEither(
              FawnDecoder[T].decode(m.body).map { t =>
                SQSMessage(t, m.attributes, m.receiptHandle)
              }
            )
          }
        }

      override def ack[T](msg: SQSMessage[T]): F[DeleteMessageResponse] = deleteMessage(msg.receipt)

    }

}
