package com.meltwater.fawn.common
package decline

import cats.implicits._
import com.monovore.decline.{Argument, Opts}

/** Provides Decline command line parsers for the core Fawn types
  *
  * These will take either command line flag or environment variable
  *
  * The variable names are the same as those for the AWS command line tool
  */
object FawnDecline {

  private def flagOrEnv[T: Argument](flag: String, env: String, help: String): Opts[T] =
    Opts.option[T](flag, help).orElse(Opts.env[T](env, help))

  val credentials: Opts[AWSCredentials] = (
    flagOrEnv[String]("aws-access-key-id", "AWS_ACCESS_KEY_ID", "AWS Access Key ID"),
    flagOrEnv[String]("aws-secret-access-key", "AWS_SECRET_ACCESS_KEY", "AWS Secret Access Key")
  ).mapN(AWSCredentials)

  val strictRegion: Opts[AWSRegion] = {
    import instances.strictRegionArgument
    flagOrEnv[AWSRegion]("aws-default-region", "AWS_DEFAULT_REGION", "AWS Region to query")
  }

  val openRegion: Opts[AWSRegion] =
    flagOrEnv[String]("aws-default-region", "AWS_DEFAULT_REGION", "AWS Region to query").map(
      AWSRegion.other)

}
