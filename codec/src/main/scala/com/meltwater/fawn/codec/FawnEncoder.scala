package com.meltwater.fawn.codec

import cats._

trait FawnEncoder[T] {
  def encode(t: T): String
}

object FawnEncoder {
  def apply[T: FawnEncoder]: FawnEncoder[T] = implicitly

  implicit val contravariant: Contravariant[FawnEncoder] = new Contravariant[FawnEncoder] {
    override def contramap[A, B](fa: FawnEncoder[A])(f: B => A): FawnEncoder[B] =
      (t: B) => fa.encode(f(t))
  }

}
