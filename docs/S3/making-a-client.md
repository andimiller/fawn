---
side-bar-position: 1
---

# Making a Client

To start with S3, we want to make an instance of `S3` to represent all the interactions we can make with S3.

```scala mdoc:invisible
import cats.effect._
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
```

We will want cats-effect for this example will use `cats.effect.IO`.

We'll also be using the `Blaze` client from http4s, so we'll need the builder for that.

Following that, we need all of the common classes in `Fawn`, and all of the `S3` classes. 

```scala mdoc
import cats.effect._
import org.http4s.client.blaze.BlazeClientBuilder
import com.meltwater.fawn.common._
import com.meltwater.fawn.s3._

import scala.concurrent.ExecutionContext
```

In order to create our client, we will need some extra settings, including AWS credentials. These would normally be brought in via a config file or command line parser, but for this example we will instantiate them here. 

```scala mdoc
val credentials = AWSCredentials("KEYID", "SECRET")
val region      = AWSRegion.`eu-west-1`
val accountId   = 123456L
```

We can then make a `Resource` for our `http4s` client and then map it into an `S3`. `S3` requires a hostname, this allows it to integrate with other API's such as DigitalOcean.

```scala mdoc:silent
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]  

val s3Resource: Resource[IO, S3[IO]] = 
  BlazeClientBuilder[IO](ExecutionContext.global).resource.map { client =>
    S3[IO](client, credentials, region, s"s3.$region.amazonaws.com")
  }
```

You would then `use` it or tie it in with your program's other `Resource`s:

```scala mdoc:silent
s3Resource.use { s3 =>
  s3.createBucket("hello-world-bucket")
}
```
