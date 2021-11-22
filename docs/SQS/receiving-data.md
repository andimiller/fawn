---
sidebar_position: 3
---

# Receiving Data 

```scala mdoc:invisible
import cats.effect._
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

import cats.implicits._
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

## Handling Decoding Yourself

If we just wanted to receive some data we could call the `receiveMessage` and `deleteMessage` methods ourselves:

```scala mdoc:to-string
import scala.concurrent.duration._

def printAction(s: String) = IO { println(s) }

queue.receiveMessage(max = 5, wait = Some(10.seconds)).flatMap { response =>
  response.messages.traverse { message =>
    printAction(message.body) *> queue.deleteMessage(message.receiptHandle)
  }
}
```

## Delegating Decoding

Again there are some helpers that can decode for you:

```scala mdoc:to-string
import com.meltwater.fawn.codec.circe.CirceCodec._
import io.circe.Json

def printAction(j: Json) = IO { println(j.noSpaces) }

queue.receiveAs[Json](max = 10, wait = Some(1.second)).flatMap { messages =>
  messages.traverse { message =>
    printAction(message.body) *> queue.ack(message)
  } 
}
```

### Using a Consumer 

This library provides an `SQSConsumer` which handles most of the control flow for you, it can be used like this:

```scala mdoc:to-string

val consumer: SQSConsumer[IO] = SQSConsumer(queue)

consumer.process[Json] { message =>
  printAction(message.body)
}
```

This will give you one message at a time, with its headers, and handle acking for you if your action succeeded.

