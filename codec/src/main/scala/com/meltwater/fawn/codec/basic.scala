package com.meltwater.fawn.codec

/** Contains basic codecs to get you up and running, you should probably use some more specific ones
  */
object basic {
  implicit val basicStringEncoder: FawnEncoder[String] = s => s
  implicit val basicStringDecoder: FawnDecoder[String] = s => Right(s)
}
