---
sidebar_position: 2
---

# Interacting with Buckets

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

To create a bucket we can use the `createBucket` method. For this method and all others here, please refer to the [S3 Documentation](https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html) for additional optional headers than can be included in the method.

In this example, a new bucket is made called `"hello-world-bucket-example"` with an additional header added that sets the ACL to `public-read`, allowing all users other than the owner read access. 

```scala mdoc:to-string
import org.http4s.{Header, Headers}

s3.createBucket(
      "hello-world-bucket-example", 
      Some(Headers(Header("x-amz-acl", "public-read"))))
```

To delete a bucket, call the `deleteBucket` method.

```scala mdoc:to-string
s3.deleteBucket("hello-world-bucket-example")
```

To list all buckets available to the user, use the `listBuckets` method.

```scala mdoc:to-string
s3.listBuckets()
```