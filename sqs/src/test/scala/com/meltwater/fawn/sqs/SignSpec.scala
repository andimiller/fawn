package com.meltwater.fawn.sqs

import munit.CatsEffectSuite

class SignSpec extends CatsEffectSuite {

  test("stringToBeSigned") {
    val mine   = """AWS4-HMAC-SHA256
      |20211112T103654Z
      |20211112/eu-west-1/sqs/aws4_request
      |0dc1ad690d3f95349775cfa675a487e4032d718bee3c0e298a7ec791cb103df1""".stripMargin
    val theirs =
      """AWS4-HMAC-SHA256
          |20211112T103654Z
          |20211112/eu-west-1/sqs/aws4_request
          |0dc1ad690d3f95349775cfa675a487e4032d718bee3c0e298a7ec791cb103df1""".stripMargin
    assertEquals(mine, theirs)
  }

  test("canonicalReq") {
    val mine   =
      """GET
        |/fakenumber/andi-test-queue-3
        |Action=ReceiveMessage&AttributeName=All&MaxNumberOfMessages=10&Version=2012-11-05&WaitTimeSeconds=10
        |accept:text/xml, text/html, application/xml
        |host:sqs.eu-west-1.amazonaws.com
        |x-amz-content-sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
        |x-amz-date:20211112T103654Z
        |
        |accept;host;x-amz-content-sha256;x-amz-date
        |e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855""".stripMargin
    val theirs =
      """GET
        |/fakenumber/andi-test-queue-3
        |Action=ReceiveMessage&AttributeName=All&MaxNumberOfMessages=10&Version=2012-11-05&WaitTimeSeconds=10
        |accept:text/xml, text/html, application/xml
        |host:sqs.eu-west-1.amazonaws.com
        |x-amz-content-sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
        |x-amz-date:20211112T103654Z
        |
        |accept;host;x-amz-content-sha256;x-amz-date
        |e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855""".stripMargin
    assertEquals(mine, theirs)
  }

}
