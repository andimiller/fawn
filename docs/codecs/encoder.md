---
sidebar_position: 2
---

# FawnEncoder

Much like the `circe` `Encoder` interface, this assumes encoding cannot fail, and since SQS usually deals in strings, we expect a string output.

```scala mdoc
trait FawnEncoder[T] {
  def encode(t: T): String
}
```

This provides a cats `Contravariant` so encoders have `contramap` and other useful methods available:

```scala mdoc:reset:invisible
import com.meltwater.fawn.codec._
import cats.implicits._
```
```scala mdoc:to-string
val longEncoder: FawnEncoder[Long] = new FawnEncoder[Long] {
  def encode(l: Long) = l.toString
}
val intEncoder: FawnEncoder[Int] = longEncoder.contramap(_.toLong)
```
