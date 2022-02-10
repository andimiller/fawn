"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[641],{3905:function(e,n,t){t.d(n,{Zo:function(){return u},kt:function(){return m}});var r=t(7294);function a(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function i(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){a(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function s(e,n){if(null==e)return{};var t,r,a=function(e,n){if(null==e)return{};var t,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||(a[t]=e[t]);return a}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var c=r.createContext({}),l=function(e){var n=r.useContext(c),t=n;return e&&(t="function"==typeof e?e(n):i(i({},n),e)),t},u=function(e){var n=l(e.components);return r.createElement(c.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return r.createElement(r.Fragment,{},n)}},p=r.forwardRef((function(e,n){var t=e.components,a=e.mdxType,o=e.originalType,c=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),p=l(t),m=a,g=p["".concat(c,".").concat(m)]||p[m]||d[m]||o;return t?r.createElement(g,i(i({ref:n},u),{},{components:t})):r.createElement(g,i({ref:n},u))}));function m(e,n){var t=arguments,a=n&&n.mdxType;if("string"==typeof e||a){var o=t.length,i=new Array(o);i[0]=p;var s={};for(var c in n)hasOwnProperty.call(n,c)&&(s[c]=n[c]);s.originalType=e,s.mdxType="string"==typeof e?e:a,i[1]=s;for(var l=2;l<o;l++)i[l]=t[l];return r.createElement.apply(null,i)}return r.createElement.apply(null,t)}p.displayName="MDXCreateElement"},257:function(e,n,t){t.r(n),t.d(n,{frontMatter:function(){return s},contentTitle:function(){return c},metadata:function(){return l},toc:function(){return u},default:function(){return p}});var r=t(7462),a=t(3366),o=(t(7294),t(3905)),i=["components"],s={sidebar_position:3},c="Receiving Data",l={unversionedId:"SQS/receiving-data",id:"SQS/receiving-data",isDocsHomePage:!1,title:"Receiving Data",description:"Handling Decoding Yourself",source:"@site/../docs-builder/target/mdoc/SQS/receiving-data.md",sourceDirName:"SQS",slug:"/SQS/receiving-data",permalink:"/fawn/docs/SQS/receiving-data",editUrl:"https://github.com/meltwater/fawn/edit/main/docs/../docs-builder/target/mdoc/SQS/receiving-data.md",tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3},sidebar:"tutorialSidebar",previous:{title:"Sending Data",permalink:"/fawn/docs/SQS/sending-data"},next:{title:"Roadmap",permalink:"/fawn/docs/roadmap"}},u=[{value:"Handling Decoding Yourself",id:"handling-decoding-yourself",children:[],level:2},{value:"Delegating Decoding",id:"delegating-decoding",children:[{value:"Using a Consumer",id:"using-a-consumer",children:[],level:3}],level:2}],d={toc:u};function p(e){var n=e.components,t=(0,a.Z)(e,i);return(0,o.kt)("wrapper",(0,r.Z)({},d,t,{components:n,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"receiving-data"},"Receiving Data"),(0,o.kt)("h2",{id:"handling-decoding-yourself"},"Handling Decoding Yourself"),(0,o.kt)("p",null,"If we just wanted to receive some data we could call the ",(0,o.kt)("inlineCode",{parentName:"p"},"receiveMessage")," and ",(0,o.kt)("inlineCode",{parentName:"p"},"deleteMessage")," methods ourselves:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"import scala.concurrent.duration._\n\ndef printAction(s: String) = IO { println(s) }\n\nqueue.receiveMessage(max = 5, wait = Some(10.seconds)).flatMap { response =>\n  response.messages.traverse { message =>\n    printAction(message.body) *> queue.deleteMessage(message.receiptHandle)\n  }\n}\n// res0: IO[Vector[DeleteMessageResponse]] = IO$561259463\n")),(0,o.kt)("h2",{id:"delegating-decoding"},"Delegating Decoding"),(0,o.kt)("p",null,"Again there are some helpers that can decode for you:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"import com.meltwater.fawn.codec.circe.CirceCodec._\nimport io.circe.Json\n\ndef printAction(j: Json) = IO { println(j.noSpaces) }\n\nqueue.receiveAs[Json](max = 10, wait = Some(1.second)).flatMap { messages =>\n  messages.traverse { message =>\n    printAction(message.body) *> queue.ack(message)\n  } \n}\n// res1: IO[Vector[DeleteMessageResponse]] = IO$1752698413\n")),(0,o.kt)("h3",{id:"using-a-consumer"},"Using a Consumer"),(0,o.kt)("p",null,"This library provides an ",(0,o.kt)("inlineCode",{parentName:"p"},"SQSConsumer")," which handles most of the control flow for you, it can be used like this:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},"val consumer: SQSConsumer[IO] = SQSConsumer(queue)\n// consumer: SQSConsumer[IO] = com.meltwater.fawn.sqs.SQSConsumer$$anon$1@5b65e34\n\nconsumer.process[Json] { message =>\n  printAction(message.body)\n}\n// res2: fs2.Stream[IO, Unit] = Stream(..)\n")),(0,o.kt)("p",null,"This will give you one message at a time, with its headers, and handle acking for you if your action succeeded."))}p.isMDXComponent=!0}}]);