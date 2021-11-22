---
sidebar_position: 1
---

# Making a Client 

To start off with SQS, we want to make an instance of `SQSQueue` to represent all the things we can do to a queue.

```scala mdoc:invisible
import cats.effect._
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
```

We'll start off by importing cats-effect, since this example will use `cats.effect.IO`.

We'll also be using the `Blaze` client from http4s, so we'll want the builder for that.

Then we want all of the `common` classes in Fawn, and all of the `sqs` classes.

```scala mdoc
import cats.effect._
import org.http4s.client.blaze.BlazeClientBuilder
import com.meltwater.fawn.common._
import com.meltwater.fawn.sqs._

import scala.concurrent.ExecutionContext
```

Next we'll want to create all of our settings, these would usually come from a config file or command line parser, but we'll instantiate them directly here.

```scala mdoc
val credentials = AWSCredentials("KEYID", "SECRET")
val region      = AWSRegion.`eu-west-1`
val accountId   = 123456L
val queueName   = "my-queue"
```

We can then create a `Resource` for our `http4s` client and map it into an `SQSQueue`

```scala mdoc:silent
val queueResource: Resource[IO, SQSQueue[IO]] = 
  BlazeClientBuilder[IO](ExecutionContext.global).resource.map { client =>
    SQSQueue[IO](client, credentials, region, accountId, queueName)
  }
```

You would then `use` it or tie it in with your program's other `Resource`s:

```scala mdoc:silent
queueResource.use { queue =>
  queue.sendMessage("hello world")
}
```

