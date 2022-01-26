import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.meltwater.fawn.common.decline.FawnDecline
import com.meltwater.fawn.common.{AWSCredentials, AWSRegion}
import com.meltwater.fawn.s3.{AWSStorageClass, S3}
import com.monovore.decline.Command
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityDecoder, Header, Headers}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

object S3Example extends IOApp {

  val cli: Command[(AWSCredentials, AWSRegion)] =
    Command("s3-example", "an S3 processing example")(
      (
        FawnDecline.credentials,
        FawnDecline.strictRegion
      ).tupled)

  val bucket   = "andi-meltwater-test-staging-fawn"
  val key      = "multi-test.txt"
  val uploadId = ""
  val body1    = "testing1"
  val body2    = "testing2"

  def s3CreateBucketsExample(s3: S3[IO]): IO[Unit] = s3.createBucket(bucket).flatMap { r =>
    IO {
      println(s"Location: ${r.location}.")
    }
  }

  //def s3DeleteBucketsExample(s3: S3[IO]): IO[Unit] = s3.deleteBucket(bucket).flatMap { r =>
  //  IO {
  //    println(s"RequestId: ${r.requestId}.")
  //  }
  // }

  def s3ListBucketsExample(s3: S3[IO]): IO[Unit] = s3.listBuckets().flatMap { r =>
    IO {
      println(
        s"Owner ID: ${r.owner.id}, Owner Name: ${r.owner.displayName}, Found ${r.buckets.size} buckets.")
    }
  }

  def s3GetBucketAclExample(s3: S3[IO]): IO[Unit] = s3.getBucketAcl(bucket).flatMap { r =>
    IO {
      println(s"Owner Name: ${r.owner.displayName}, Grants: ${r.grants}")
    }
  }

  def s3ListObjectsExample(s3: S3[IO]): IO[Unit] =
    s3.listObjectsV2(bucket).flatMap { r =>
      IO {
        println(s"${r.name} bucket contains ${r.contents.size} file(s). First file key: ${r.contents
          .getOrElse(Vector())}")
      }
    }

  def s3PutExample(s3: S3[IO]): IO[Unit] = s3
    .putObject(
      bucket,
      "test4.txt",
      "testing",
      Headers(Header("x-amz-storage-class", AWSStorageClass.`STANDARD`.toString)).some)
    .flatMap { r =>
      IO { println(s"eTag: ${r.eTag}, Other Headers: ${r.genericResponse.headers}") }
    }

  implicit val decoder: EntityDecoder[IO, String] = EntityDecoder.text
  def s3GetObjectExample(s3: S3[IO]): IO[Unit]    =
    s3.getObject(bucket, "copiedobject").flatMap { r => IO { println(s"body: ${r.body}") } }

  def s3DeleteExample(s3: S3[IO]): IO[Unit] =
    s3.deleteObject(bucket, "test2.txt").flatMap { r =>
      IO { println(s"requestId: ${r.requestId}") }
    }

  def s3CopyObjectExample(s3: S3[IO]): IO[Unit] =
    s3.copyObject(bucket, "copiedobject", s"$bucket/test3.txt").flatMap { r =>
      IO { println(s"New Etag: ${r.eTag}") }
    }

  def s3HeadObjectExample(s3: S3[IO]): IO[Unit] =
    s3.headObject(bucket, "test3.txt").flatMap { r =>
      IO {
        println(
          s"Etag: ${r.eTag}, Content-Type: ${r.contentType}, Content-Length: ${r.contentLength}, Headers: ${r.headers}")
      }
    }

  def s3CreateMultipartUploadExample(s3: S3[IO]): IO[Unit] =
    s3.createMultipartUpload(bucket, key).flatMap { r =>
      IO { println(s"UploadId: ${r.uploadId}") }
    }

  def s3GetMultipartUploadsExample(s3: S3[IO]): IO[Unit] =
    s3.listMultipartUploads(bucket).flatMap { r =>
      IO { println(s"Uploads: ${r.uploads}") }
    }

  def s3AbortMultipartUploadExample(s3: S3[IO]): IO[Unit] = s3
    .abortMultipartUpload(
      bucket,
      key,
      uploadId
    )
    .flatMap { r => IO { println(s"requestId: ${r.requestId}") } }

  def s3UploadPartExample(s3: S3[IO]): IO[Unit] =
    s3.uploadPart(
      bucket,
      key,
      2,
      uploadId,
      body1
    ).flatMap { r =>
      IO { println(s"eTag: ${r.eTag}") }
    }

  def s3ListPartsExample(s3: S3[IO]): IO[Unit] =
    s3.listParts(bucket, key, uploadId).flatMap { r =>
      IO { println(s"Parts ${r.parts}") }
    }

  def s3CompleteMultipartUploadExample(s3: S3[IO]): IO[Unit] =
    s3.completeMultipartUpload(
      bucket,
      key,
      uploadId,
      List()
    ).flatMap { r => IO { println(r.eTag) } }

  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  override def run(args: List[String]): IO[ExitCode] =
    cli.parse(args, sys.env) match {
      case Left(value)                                             => IO { println(value) }.as(ExitCode.Error)
      case Right((credentials: AWSCredentials, region: AWSRegion)) =>
        BlazeClientBuilder[IO](ExecutionContext.global).resource.use { client =>
          val s3 = S3[IO](client, credentials, region)
          //s3CreateMultipartUploadExample(s3).as(ExitCode.Success)
          //s3UploadPartExample(s3).as(ExitCode.Success)
          //s3CompleteMultipartUploadExample(s3).as(ExitCode.Success)
          s3ListPartsExample(s3).as(ExitCode.Success)
        // s3GetMultipartUploadsExample(s3).as(ExitCode.Success)
        //s3AbortMultipartUploadExample(s3).as(ExitCode.Success)
        //s3GetBucketAclExample(s3).as(ExitCode.Success)
        }
    }
}
