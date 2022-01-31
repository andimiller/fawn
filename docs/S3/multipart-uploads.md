---
sidebar-position: 4
---

```scala mdoc:invisible
import cats.effect._
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

import org.http4s.client.blaze.BlazeClientBuilder
import com.meltwater.fawn.common._
import com.meltwater.fawn.s3._
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

val credentials = AWSCredentials("KEYID", "SECRET")
val region      = AWSRegion.`eu-west-1`
implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]  
val s3Resource: Resource[IO, S3[IO]] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource.map { client =>
      S3[IO](client, credentials, region)
    }
  
  val (s3, dispose) = s3Resource.allocated.unsafeRunSync()
```

# Multipart Uploads

Multipart Uploads can be accomplished one of two ways, using either the raw request methods or using the higher-level method `startMultipartUpload`. Using the low-level request methods requires the handling of the `uploadId` of the upload and the `ETags` of the uploaded parts. The higher level method produces a `Resource[F, MultipartUpload]` which can be used.  

## Multipart Uploads Requests

Multipart uploads can be accomplished by using the raw requests to S3. These can be used in cases where a large file is required to be uploaded and should be seperated into smaller parts for upload. It is important to note that a file should be seperated into parts of at minumum of 5 MB in size, the final part is an exception to this however.  

All methods described here support additional optional headers that can be included in the request. Please refer to the [S3 Documentation](https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html) for more information on what headers can be applied to different methods.

A multipart upload begins by using the `createMultipartUpload` method. This method produces the `uploadId` which is needed by other requests.  

```scala mdoc:to-string
s3.createMultipartUpload("hello-world-bucket-example", "mp-file-example").flatMap { response =>
    IO { println(s"UploadID: ${response.uploadId}") }
}
```

If, for any reason, the multipart upload is needed to be cancelled, this can be done with the `abortMultipartUpload` method. 

```scala mdoc:to-string
s3.abortMultipartUpload(
        "hello-world-bucket-example", 
        "mp-file-example", 
        uploadId = "943465sdf54sdf654sd321fdf")
```

The `listMultipartUploads` method can be used to see ongoing uploads within a bucket. This also obtains the `uploadId` for each upload. 

```scala mdoc:to-string
def printMP(upload: Uploads): IO[Unit] = IO { println(s"UploadID: ${upload.uploadId}") }

s3.listMultipartUploads("hello-world-bucket-example").flatMap { response =>
    response.uploads.get.traverse(printMP _)
}
```

A list of parts uploaded so far in a multipart upload can be obtained using the `listParts` method. This returns each part's `ETag` which is necessary for completing the upload. 

```scala mdoc:to-string
def printPart(part: Parts): IO[Unit] = IO { println(s"Part Etag: ${part.eTag}") }

s3.listParts(
    "hello-world-bucket-example",
    "mp-file-example",
    uploadId = "943465sdf54sdf654sd321fdf")
    .flatMap { response => response.parts.traverse(printPart _) }
```

To upload a part, the `uploadPart` method is to be used. This requires an `EntityEncoder` in order to encode the contents of the chunk. It also requires a part number which is necessary for reassembling the parts into the completed file. In S3, part numbers are indexed from 1 to 10,000. This also returns the `ETag` of the part, which is also necessary for completing the upload. 

```scala mdoc:to-string
s3.uploadPart(
    "hello-world-bucket-example",
    "mp-file-example",
    1,
    "943465sdf54sdf654sd321fdf",
    "content-to -be-encoded")
    .flatMap { response => IO { println(s"Part ETag: ${response.eTag}") } }
```

Once all parts are uploaded, the upload can be completed. This is done using the method `completeMultipartUpload` method. This method requires all `ETags` for each part in a list, in order of assembly.

```scala mdoc:to-string
s3.completeMultipartUpload(
        "hello-world-bucket-example", 
        "mp-file-example", 
        uploadId = "943465sdf54sdf654sd321fdf",
        parts = List("9320f0j32f0j23f0j382jf", "9320f0j32f0mg59khf32jf"))
```

## Higher-Level Multipart Upload Method

To use the higher-level multipart upload, first invoke the `startMultipartUpload` method. This returns a `Resource[F, MultipartUpload]`.

This `Resource` includes the `sendPart` method which is used to send a file chunk. The Resource automatically keeps track of returned `ETags` and generates part numbers upon uploading another part. Upon the release of the `Resource`, the multipart upload is completed automatically. Parts sent to upload must be sent in the order that should be reassembled. If the `Resource` is cancelled or an error occurs, the multipart upload is aborted. 

```scala mdoc:to-string
import org.http4s.EntityEncoder

//For docs purposes, send a singular part t. 
def upload[T](t: List[T])(implicit enc: EntityEncoder[IO, T]) =
    s3.startMultipartUpload("bucket", "key").use { mp => mp.sendPart(t) }
```