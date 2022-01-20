import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.{catsSyntaxOptionId, catsSyntaxTuple2Semigroupal}
import com.meltwater.fawn.common.decline.FawnDecline
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion}
import com.meltwater.fawn.s3.{AWSStorageClass, S3}
import com.monovore.decline.Command
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Header, Headers}

import scala.concurrent.ExecutionContext

object S3Example extends IOApp {

  val cli: Command[(AWSCredentials, AWSRegion)] =
    Command("s3-example", "an S3 processing example")(
      (
        FawnDecline.credentials,
        FawnDecline.strictRegion
      ).tupled)

  val bucket = "andi-meltwater-test-staging-fawn"

  def s3ListBucketsExample(s3: S3[IO]): IO[Unit] = s3.listBuckets().flatMap { r =>
    IO {
      println(
        s"Owner ID: ${r.ownerID}, Owner Name: ${r.ownerName}, Found ${r.buckets.size} buckets.")
    }
  }

  def s3ListObjectsExample(s3: S3[IO]): IO[Unit] =
    s3.listObjectsV2(bucket).flatMap { r =>
      IO {
        println(
          s"${r.name} bucket contains ${r.contents.size} file(s). First file key: ${r.contents.getOrElse(Vector())}")
      }
    }

  def s3PutExample(s3: S3[IO]): IO[Unit] = s3
    .putObject(
      bucket,
      "test4.txt",
      "testing",
      Headers(Header("x-amz-storage-class", AWSStorageClass.`STANDARD`.value)).some)
    .flatMap { r => IO { println(s"eTag: ${r.eTag}, Other Headers: ${r.headers}") } }

  def s3DeleteExample(s3: S3[IO]): IO[Unit] =
    s3.deleteObject(bucket, "test2.txt").flatMap { r => IO { println(s"requestId: ${r.requestId}") } }

  def s3GetBucketAclExample(s3: S3[IO]): IO[Unit] = s3.getBucketAcl(bucket).flatMap{ r => IO { println(s"Owner Name: ${r.ownerName}, Grants: ${r.grants}") }}

  def s3GetMultipartUploadsExample(s3: S3[IO]): IO[Unit] = s3.listMultipartUploads(bucket).flatMap{ r => IO { println(s"Uploads: ${r.uploads}") }}

  override def run(args: List[String]): IO[ExitCode] =
    cli.parse(args, sys.env) match {
      case Left(value)                                             => IO { println(value) }.as(ExitCode.Error)
      case Right((credentials: AWSCredentials, region: AWSRegion)) =>
        BlazeClientBuilder[IO](ExecutionContext.global).resource.use { client =>
          val s3 = S3[IO](client, credentials, region)
          s3GetMultipartUploadsExample(s3).as(ExitCode.Success)
        }
    }
}
