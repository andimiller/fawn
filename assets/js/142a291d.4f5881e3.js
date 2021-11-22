"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[223],{3905:function(e,n,t){t.d(n,{Zo:function(){return u},kt:function(){return m}});var r=t(7294);function a(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function i(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){a(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function s(e,n){if(null==e)return{};var t,r,a=function(e,n){if(null==e)return{};var t,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||(a[t]=e[t]);return a}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var c=r.createContext({}),l=function(e){var n=r.useContext(c),t=n;return e&&(t="function"==typeof e?e(n):i(i({},n),e)),t},u=function(e){var n=l(e.components);return r.createElement(c.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return r.createElement(r.Fragment,{},n)}},p=r.forwardRef((function(e,n){var t=e.components,a=e.mdxType,o=e.originalType,c=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),p=l(t),m=a,f=p["".concat(c,".").concat(m)]||p[m]||d[m]||o;return t?r.createElement(f,i(i({ref:n},u),{},{components:t})):r.createElement(f,i({ref:n},u))}));function m(e,n){var t=arguments,a=n&&n.mdxType;if("string"==typeof e||a){var o=t.length,i=new Array(o);i[0]=p;var s={};for(var c in n)hasOwnProperty.call(n,c)&&(s[c]=n[c]);s.originalType=e,s.mdxType="string"==typeof e?e:a,i[1]=s;for(var l=2;l<o;l++)i[l]=t[l];return r.createElement.apply(null,i)}return r.createElement.apply(null,t)}p.displayName="MDXCreateElement"},645:function(e,n,t){t.r(n),t.d(n,{frontMatter:function(){return s},contentTitle:function(){return c},metadata:function(){return l},toc:function(){return u},default:function(){return p}});var r=t(7462),a=t(3366),o=(t(7294),t(3905)),i=["components"],s={sidebar_position:2},c="Sending Data",l={unversionedId:"SQS/sending-data",id:"SQS/sending-data",isDocsHomePage:!1,title:"Sending Data",description:"To send data we can use the low level method to just send a string:",source:"@site/../docs-builder/target/mdoc/SQS/sending-data.md",sourceDirName:"SQS",slug:"/SQS/sending-data",permalink:"/fawn/docs/SQS/sending-data",editUrl:"https://github.com/meltwater/fawn/edit/main/website/../docs-builder/target/mdoc/SQS/sending-data.md",tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"tutorialSidebar",previous:{title:"Making a Client",permalink:"/fawn/docs/SQS/making-a-client"},next:{title:"Receiving Data",permalink:"/fawn/docs/SQS/receiving-data"}},u=[],d={toc:u};function p(e){var n=e.components,t=(0,a.Z)(e,i);return(0,o.kt)("wrapper",(0,r.Z)({},d,t,{components:n,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"sending-data"},"Sending Data"),(0,o.kt)("p",null,"To send data we can use the low level method to just send a string:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'queue.sendMessage("hello world")\n// res0: IO[SendMessageResponse] = IO$193684769\n')),(0,o.kt)("p",null,"We can also send some headers:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'queue.sendMessage("a message", Map("client" -> "fawn"))\n// res1: IO[SendMessageResponse] = IO$269971976\n')),(0,o.kt)("p",null,"To make things a bit easier we could use the method which understands encoding: "),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-scala"},'import com.meltwater.fawn.codec.circe.CirceCodec._\nimport io.circe.literal._\n\nqueue.sendAs(json"""{"some": "circe json"}""")\n// res2: IO[SendMessageResponse] = IO$25591829\nqueue.sendAs(json"""123""", Map("a header" -> "a header value"))\n// res3: IO[SendMessageResponse] = IO$1718350937\n')))}p.isMDXComponent=!0}}]);