---
sidebar_position: 3
---

```scala mdoc:invisible
import cats.effect._
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

import org.http4s.client.blaze.BlazeClientBuilder
import com.meltwater.fawn.common._
import com.meltwater.fawn.s3._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.implicits._

val credentials = AWSCredentials("KEYID", "SECRET")
val region      = AWSRegion.`eu-west-1`
implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
val s3Resource: Resource[IO, S3[IO]] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource.map { client =>
      S3[IO](client, credentials, region)
    }

  val (s3, dispose) = s3Resource.allocated.unsafeRunSync()
```

# Interacting With Objects

All methods described here support additional optional headers that can be included in the request. Please refer to the [S3 Documentation](https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html) for more information on what headers can be applied to different methods.

To list all objects in a bucket, we can use the `listObjectsV2` method. This makes use of the V2 version of the action in S3.

```scala mdoc:to-string
//Print an objects name (key) and ETag.
def printObject(s3Object: S3Object): IO[Unit] = IO {
    println(s"S3Object Name: ${s3Object.key},  S3Object ETag: ${s3Object.eTag}")
}

s3.listObjectsV2("hello-world-bucket-example").flatMap { response =>
    response.contents.traverse(printObject _)
}
```

To upload a file into a bucket as an object, use the `putObject` method. This method requires an `EntityEncoder[F, T]` for what you intend to upload.

```scala mdoc:to-string
import org.http4s.EntityEncoder

implicit val encoder: EntityEncoder[IO, String] = EntityEncoder.stringEncoder

val body = "testing-file-body"

s3.putObject("hello-world-bucket-example", "example-file.txt", body)
```

To download an object from a bucket, use the `getObject` method. In order to decode the contents of the file, an `EntityDecoder[F, T]` must be provided.

```scala mdoc:to-string
import org.http4s.EntityDecoder

implicit val decoder: EntityDecoder[IO, String] = EntityDecoder.text

//Download the object then print it's decoded contents.
s3.getObject("hello-world-bucket-example", "example-file.txt").flatMap { response =>
    IO { println(response.body) }
}
```

An object can be deleted using the `deleteObject` method.

```scala mdoc:to-string
s3.deleteObject("hello-world-bucket-example", "example-file.txt")
```

An object can be copied using `copyObject`.

```scala mdoc:to-string
s3.copyObject("hello-world-bucket-example", "new-example-file.txt","example-file.txt")
```

To obtain the metadata of an object in the form of headers, `headObject` can be used.

```scala mdoc:to-string
//Print the object's ETag
s3.headObject("hello-world-bucket-example", "example-file.txt").flatMap { response =>
    IO { println(response.eTag) }
}
```
