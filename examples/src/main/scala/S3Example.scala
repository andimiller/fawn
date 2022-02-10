import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.meltwater.fawn.common.decline.FawnDecline
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion}
import com.meltwater.fawn.s3.S3
import com.monovore.decline.Command
import org.http4s.client.blaze.BlazeClientBuilder
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

object S3Example extends IOApp {

  val cli: Command[(AWSCredentials, AWSRegion)] =
    Command("s3-example", "an S3 processing example")(
      (
        FawnDecline.credentials,
        FawnDecline.strictRegion
      ).tupled)

  implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  override def run(args: List[String]): IO[ExitCode] =
    cli.parse(args, sys.env) match {
      case Left(value)                                             => IO { println(value) }.as(ExitCode.Error)
      case Right((credentials: AWSCredentials, region: AWSRegion)) =>
        BlazeClientBuilder[IO](ExecutionContext.global).resource.use { client =>
          val s3 = S3[IO](client, credentials, region)
          s3.listBuckets()
            .flatMap { r =>
              IO {
                println(
                  s"Owner ID: ${r.owner.id}, Owner Name: ${r.owner.displayName}, Found ${r.buckets.size} buckets.")
              }
            }
            .as(ExitCode.Success)
        }
    }
}
