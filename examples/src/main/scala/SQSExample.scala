import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import com.meltwater.fawn.codec.circe.CirceCodec.{fromCirceDecoder, fromCirceEncoder}
import com.meltwater.fawn.common.decline.FawnDecline
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion}
import com.meltwater.fawn.sqs.{SQSConsumer, SQSQueue}
import com.monovore.decline.{Command, Opts}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext

object SQSExample extends IOApp {
  val cli: Command[(AWSCredentials, AWSRegion, Long, String)] =
    Command("sqs-example", "an SQS processing example")(
      (
        FawnDecline.credentials,
        FawnDecline.strictRegion,
        Opts.option[Long]("account-id", "Account ID for the account you're accessing"),
        Opts.option[String]("queue-name", "Queue name for the queue you're accessing")
      ).tupled)

  case class Payload(s: String, i: Int)
  implicit val encoder: Encoder[Payload] = deriveEncoder[Payload]
  implicit val decoder: Decoder[Payload] = deriveDecoder[Payload]

  override def run(args: List[String]): IO[ExitCode] =
    cli.parse(args, sys.env) match {
      case Left(value)                                        => IO { println(value) }.as(ExitCode.Error)
      case Right((credentials, region, accountId, queueName)) =>
        BlazeClientBuilder[IO](ExecutionContext.global).resource.use { client =>
          val queue =
            SQSQueue[IO](client, credentials, region, accountId, queueName)

          val consumer = SQSConsumer(queue)

          val inputs = (1 to 10).toList.map { i =>
            (Payload("a" * i, i), Map.empty[String, String])
          }
          queue
            .sendManyAs(inputs: _*)
            .flatMap { r => IO { println("result" + r.toString) } } *>
            consumer
              .process[Payload] { s =>
                IO { println((s.body, s.headers)) }
              }
              .compile
              .drain
              .as(ExitCode.Success)

        }

    }
}
