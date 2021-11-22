---
sidebar_position: 2
---

# Sending Data

```scala mdoc:invisible
import cats.effect._
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

import cats.effect._
import org.http4s.client.blaze.BlazeClientBuilder
import com.meltwater.fawn.common._
import com.meltwater.fawn.sqs._

import scala.concurrent.ExecutionContext

val credentials = AWSCredentials("KEYID", "SECRET")
val region      = AWSRegion.`eu-west-1`
val accountId   = 123456L
val queueName   = "my-queue"
val queueResource: Resource[IO, SQSQueue[IO]] = 
  BlazeClientBuilder[IO](ExecutionContext.global).resource.map { client =>
    SQSQueue[IO](client, credentials, region, accountId, queueName)
  }
  
// just for documentation purposes
val (queue, dispose) = queueResource.allocated.unsafeRunSync()
```

To send data we can use the low level method to just send a string:

```scala mdoc:to-string
queue.sendMessage("hello world")
```

We can also send some headers:

```scala mdoc:to-string
queue.sendMessage("a message", Map("client" -> "fawn"))
```

To make things a bit easier we could use the method which understands encoding: 

```scala mdoc:to-string
import com.meltwater.fawn.codec.circe.CirceCodec._
import io.circe.literal._

queue.sendAs(json"""{"some": "circe json"}""")
queue.sendAs(json"""123""", Map("a header" -> "a header value"))
```

