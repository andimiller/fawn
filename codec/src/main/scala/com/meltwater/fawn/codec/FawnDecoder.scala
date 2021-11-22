package com.meltwater.fawn.codec

import cats._
import cats.data.Kleisli
import cats.implicits._

abstract class FawnDecoder[T] { decoder =>
  def decode(s: String): Either[Throwable, T]

  def emap[B](f: T => Either[Throwable, B]): FawnDecoder[B] =
    (s: String) => decoder.decode(s).flatMap(f)
}

object FawnDecoder {
  def apply[T: FawnDecoder]: FawnDecoder[T] = implicitly

  implicit val applicativeError: ApplicativeError[FawnDecoder, Throwable] =
    new ApplicativeError[FawnDecoder, Throwable] {
      override def map[A, B](fa: FawnDecoder[A])(f: A => B): FawnDecoder[B] =
        (s: String) => fa.decode(s).map(f)

      override def pure[A](x: A): FawnDecoder[A]               = _ => x.asRight
      override def raiseError[A](e: Throwable): FawnDecoder[A] = _ => e.asLeft

      override def handleErrorWith[A](fa: FawnDecoder[A])(
          f: Throwable => FawnDecoder[A]): FawnDecoder[A] =
        s =>
          fa.decode(s)
            .fold(
              t => f(t).decode(s),
              _.asRight[Throwable]
            )

      override def ap[A, B](ff: FawnDecoder[A => B])(fa: FawnDecoder[A]): FawnDecoder[B]      =
        map(product(ff, fa)) { case (f, a) => f(a) }

      override def product[A, B](fa: FawnDecoder[A], fb: FawnDecoder[B]): FawnDecoder[(A, B)] =
        (Kleisli(fa.decode), Kleisli(fb.decode)).tupled.run(_)
    }

}
