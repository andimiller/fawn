package com.meltwater.fawn.sqs

import cats.implicits._
import cats.effect.Sync
import com.meltwater.fawn.codec.FawnDecoder
import com.meltwater.fawn.sqs.SQSQueue.SQSMessage
import fs2.Stream

import scala.concurrent.duration.DurationInt

/** This provides a high level variant of the interface, outputting streams
  */
trait SQSConsumer[F[_]] {
  def process[T: FawnDecoder](f: SQSMessage[T] => F[Unit]): Stream[F, Unit]
  def processWithRollbackOnAckFailure[T: FawnDecoder](
      f: SQSMessage[T] => F[Unit],
      rollback: (Throwable, SQSMessage[T]) => F[Unit]): Stream[F, Unit]

}

object SQSConsumer {

  def apply[F[_]: Sync](queue: SQSQueue[F]): SQSConsumer[F] =
    new SQSConsumer[F] {
      override def process[T: FawnDecoder](f: SQSMessage[T] => F[Unit]): Stream[F, Unit] =
        Stream
          .repeatEval(
            queue.receiveAs[T](10, 10.seconds.some)
          )
          .flatMap(Stream.emits)
          .evalMap { m =>
            Sync[F].uncancelable(
              f(m) <* queue.ack(m)
            )
          }

      override def processWithRollbackOnAckFailure[T: FawnDecoder](
          f: SQSMessage[T] => F[Unit],
          rollback: (Throwable, SQSMessage[T]) => F[Unit]): Stream[F, Unit] =
        Stream
          .repeatEval(           // repeatedly call AWS to get payloads
            queue.receiveAs[T](10, 10.seconds.some))
          .flatMap(Stream.emits) // split the stream into individual payloads
          .evalMap { m =>
            Sync[F].uncancelable(     // this is our transactional block
              f(m) <*
                queue
                  .ack(m)
                  .void
                  .recoverWith(e =>
                    rollback(e, m) *> Sync[F]
                      .raiseError(e)) // if the ack fails, recover with rollback, then rethrow
            )
          }

    }

}
