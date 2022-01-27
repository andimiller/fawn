---
sidebar_position: 2
---

# Interacting with Buckets

```scala mdoc:invisible
import scala.concurrent.ExecutionContext

import cats.effect._
import org.http4s.client.blaze.BlazeClientBuilder
import com.meltwater.fawn.common._
import com.meltwater.fawn.s3._

val credentials = AWSCredentials("KEYID", "SECRET")
  val region      = AWSRegion.`eu-west-1`
  val s3Resource: Resource[IO, S3[IO]] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource.map { client =>
      S3[IO](client, credentials, region)
    }
  
  val (s3, dispose) = s3Resource.allocated.unsafeRunSync()
```

To create a bucket we can use the `createBucket` method. For this method and all others here, please refer to the [S3 Documentation](https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html) for additional optional headers than can be included in the method.

In this example, a new bucket is made called `"hello-world-bucket-example"` with an additional header added that sets the ACL to `public-read`, allowing all users other than the owner read access. 

```scala mdoc:to-string
s3.createBucket(
      "hello-world-bucket-example", 
      Headers(Header("x-amz-acl", "public-read")))
```

