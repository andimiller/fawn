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
s3.listObjectsV2("hello-world-bucket-example")
```

To upload a file into a bucket as an object, use the `putObject` method. This method requres an `EntityEncoder[F, T]` for what you intend to upload. 

```scala mdoc:to-string
s3.putObject("hello-world-bucket-example", "example-file.txt", "testing-file")
```

To download an object from a bucket, use the `getObject` method. In order to decode the contents of the file, an `EntityDecoder[F, T]` must be provided.

```scala mdoc:to-string
import org.http4s.EntityDecoder

implicit val decoder: EntityDecoder[IO, String] = EntityDecoder.text

s3.getObject("hello-world-bucket-example", "example-file.txt")
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
s3.headObject("hello-world-bucket-example", "example-file.txt")
```
