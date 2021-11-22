package org.http4s

import org.http4s.internal.parboiled2.CharPredicate

object AwsEncodings {
  // re-export this so we can use it for the AWS encoding
  val unreserved: CharPredicate = Uri.Unreserved
}
