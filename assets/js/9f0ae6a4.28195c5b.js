"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[210],{3905:function(e,t,n){n.d(t,{Zo:function(){return u},kt:function(){return m}});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function c(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=r.createContext({}),s=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},u=function(e){var t=s(e.components);return r.createElement(l.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},d=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,l=e.parentName,u=c(e,["components","mdxType","originalType","parentName"]),d=s(n),m=a,h=d["".concat(l,".").concat(m)]||d[m]||p[m]||o;return n?r.createElement(h,i(i({ref:t},u),{},{components:n})):r.createElement(h,i({ref:t},u))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=d;var c={};for(var l in t)hasOwnProperty.call(t,l)&&(c[l]=t[l]);c.originalType=e,c.mdxType="string"==typeof e?e:a,i[1]=c;for(var s=2;s<o;s++)i[s]=n[s];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}d.displayName="MDXCreateElement"},5538:function(e,t,n){n.r(t),n.d(t,{frontMatter:function(){return c},contentTitle:function(){return l},metadata:function(){return s},toc:function(){return u},default:function(){return d}});var r=n(7462),a=n(3366),o=(n(7294),n(3905)),i=["components"],c={sidebar_position:2},l="Interacting with Buckets",s={unversionedId:"S3/interacting-with-buckets",id:"S3/interacting-with-buckets",isDocsHomePage:!1,title:"Interacting with Buckets",description:"To create a bucket we can use the createBucket method. For this method and all others here, please refer to the S3 Documentation for additional optional headers than can be included in the method.",source:"@site/../docs-builder/target/mdoc/S3/interacting-with-buckets.md",sourceDirName:"S3",slug:"/S3/interacting-with-buckets",permalink:"/fawn/docs/S3/interacting-with-buckets",editUrl:"https://github.com/meltwater/fawn/edit/main/docs/../docs-builder/target/mdoc/S3/interacting-with-buckets.md",tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"tutorialSidebar",previous:{title:"Roadmap",permalink:"/fawn/docs/roadmap"},next:{title:"Interacting With Objects",permalink:"/fawn/docs/S3/interacting-with-objects"}},u=[],p={toc:u};function d(e){var t=e.components,n=(0,a.Z)(e,i);return(0,o.kt)("wrapper",(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"interacting-with-buckets"},"Interacting with Buckets"),(0,o.kt)("p",null,"To create a bucket we can use the ",(0,o.kt)("inlineCode",{parentName:"p"},"createBucket")," method. For this method and all others here, please refer to the ",(0,o.kt)("a",{parentName:"p",href:"https://docs.aws.amazon.com/AmazonS3/latest/API/API_Operations_Amazon_Simple_Storage_Service.html"},"S3 Documentation")," for additional optional headers than can be included in the method."),(0,o.kt)("p",null,"In this example, a new bucket is made called ",(0,o.kt)("inlineCode",{parentName:"p"},'"hello-world-bucket-example"')," with an additional header added that sets the ACL to ",(0,o.kt)("inlineCode",{parentName:"p"},"public-read"),", allowing all users other than the owner read access. The owner is given full control permissions over the bucket.  "),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'import org.http4s.{Header, Headers}\n\ns3.createBucket(\n      "hello-world-bucket-example", \n      Headers(Header("x-amz-acl", "public-read")))\n// res0: IO[CreateBucketResponse] = IO$240324739\n')),(0,o.kt)("p",null,"To delete a bucket, call the ",(0,o.kt)("inlineCode",{parentName:"p"},"deleteBucket")," method. The user must have the required permissions to complete this action."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'s3.deleteBucket("hello-world-bucket-example")\n// res1: IO[Headers] = IO$1423846553\n')),(0,o.kt)("p",null,"To list all buckets available to the user, use the ",(0,o.kt)("inlineCode",{parentName:"p"},"listBuckets")," method."),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'def printBucket(bucket: Bucket): IO[Unit] = IO {\n    println(s"Bucket Name: ${bucket.name}, Creation Date: ${bucket.creationDate}")\n  }\n\ns3.listBuckets().flatMap{ response => response.buckets.traverse(printBucket _) }\n// res2: IO[Vector[Unit]] = IO$1186542797\n')))}d.isMDXComponent=!0}}]);