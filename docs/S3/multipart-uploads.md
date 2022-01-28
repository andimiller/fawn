---
sidebar-position: 4
---

# Multipart Uploads

Multipart Uploads can be accomplished one of two ways, using either the raw request methods or using the higher-level method `startMultipartUpload`. Using the low-level request methods requires the handling of the `uploadId` of the upload and the `ETags` of the uploaded parts. The higher level method produces a `Resource[F, MultipartUpload]` which can be used.  

## Multipart Uploads Requests

Multipart uploads can be accomplished by using the raw requests to S3. These can be used in cases where a large file is required to be uploaded and should be seperated into smaller parts for upload. It is important to note that a file should be seperated into parts of at minumum of 5 MB in size, the final part is an exception to this however.  

All methods described here support additional optional headers that can be included in the request. Please refer to the [S3 Documentation](https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html) for more information on what headers can be applied to different methods.

A multipart upload begins by using the `createMultipartUpload` method. This method produces the `uploadId` which is needed by other requests.  

```scala mdoc:to-string
val resp = s3.createMultipartUpload("hello-world-bucket-example", "mp-file-example")
val uploadId = resp.uploadId
```

If, for any reason, the multipart upload is needed to be cancelled, this can be done with the `abortMultipartUpload` method. 

```scala mdoc:to-string
s3.createMultipartUpload(
        "hello-world-bucket-example", 
        "mp-file-example", 
        uploadId = "943465sdf54sdf654sd321fdf")
```

The `listMultipartUploads` method can be used to see ongoing uploads within a bucket. This also obtains the `uploadId` for each upload. 

```scala mdoc:to-string
s3.listMultipartUploads("hello-world-bucket-example")
```

A list of parts uploaded so far in a multipart upload can be obtained using the `listParts` method. This returns each part's `ETag` which is necessary for completing the upload. 

```scala mdoc:to-string
s3.listParts(
        "hello-world-bucket-example", 
        "mp-file-example", 
        uploadId = "943465sdf54sdf654sd321fdf")
```

To upload a part, the `uploadPart` method is to be used. This requires an `EntityEncoder` in order to encode the contents of the chunk. It also requires a part number which is necessary for reassembling the parts into the completed file. In S3, part numbers are indexed from 1 to 10,000. This also returns the `ETag` of the part, which is also necessary for completing the upload. 

```scala mdoc:to-string
s3.uploadParts(
        "hello-world-bucket-example", 
        "mp-file-example",
        1 
        uploadId = "943465sdf54sdf654sd321fdf",
        contents)
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

