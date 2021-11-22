---
sidebar_position: 3
---

# FawnDecoder

Similar to `circe` `Decoder` again, this time we allow decoding to fail with a `Throwable`, and there is an extra `emap` method to help combining.

```scala mdoc
abstract class FawnDecoder[T] { decoder =>
  def decode(s: String): Either[Throwable, T]

  def emap[B](f: T => Either[Throwable, B]): FawnDecoder[B] =
    (s: String) => decoder.decode(s).flatMap(f)
}
```

Also provided is `ApplicativeError`, which comes along with `Applicative` and `Semigroupal`, letting you do things like:

### orElse combination of decoders

```scala mdoc:reset:invisible
import com.meltwater.fawn.codec._
import cats.implicits._
import scala.util.control.NoStackTrace
```
```scala mdoc:to-string
case class SimpleError(msg: String) extends Throwable(msg) with NoStackTrace

val strict: FawnDecoder[Boolean] = new FawnDecoder[Boolean] {
  def decode(s: String) = s match {
    case "true"  => true.asRight
    case "false" => false.asRight
    case other   => SimpleError(s"'$other' is not a valid boolean").asLeft
  }
}
val lessStrict: FawnDecoder[Boolean] = new FawnDecoder[Boolean] {
  def decode(s: String) = s match {
    case "y"     => true.asRight
    case "n"     => false.asRight
    case other   => SimpleError(s"'$other' is not a valid boolean").asLeft
  }
}

val combined = strict.orElse(lessStrict)

combined.decode("true")
combined.decode("y")
combined.decode("oops")
```
