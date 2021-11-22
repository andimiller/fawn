package com.meltwater.fawn.codec.circe

import com.meltwater.fawn.codec.{FawnDecoder, FawnEncoder}
import io.circe.{Decoder => CirceDecoder, Encoder => CirceEncoder, Printer}

object CirceCodec extends LowPriorityCirceCodecs {
  case class Configuration(printer: Printer)

  implicit def fromCirceEncoder[T: CirceEncoder](implicit c: Configuration): FawnEncoder[T] = t =>
    c.printer.print(CirceEncoder[T].apply(t))

  implicit def fromCirceDecoder[T: CirceDecoder]: FawnDecoder[T] = t =>
    io.circe.parser.parse(t).flatMap(CirceDecoder[T].decodeJson(_))
}

trait LowPriorityCirceCodecs {
  implicit val default: CirceCodec.Configuration = CirceCodec.Configuration(Printer.noSpaces)
}
