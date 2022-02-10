"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[190],{3905:function(e,n,t){t.d(n,{Zo:function(){return l},kt:function(){return f}});var r=t(7294);function o(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function c(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function a(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?c(Object(t),!0).forEach((function(n){o(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):c(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function i(e,n){if(null==e)return{};var t,r,o=function(e,n){if(null==e)return{};var t,r,o={},c=Object.keys(e);for(r=0;r<c.length;r++)t=c[r],n.indexOf(t)>=0||(o[t]=e[t]);return o}(e,n);if(Object.getOwnPropertySymbols){var c=Object.getOwnPropertySymbols(e);for(r=0;r<c.length;r++)t=c[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(o[t]=e[t])}return o}var d=r.createContext({}),s=function(e){var n=r.useContext(d),t=n;return e&&(t="function"==typeof e?e(n):a(a({},n),e)),t},l=function(e){var n=s(e.components);return r.createElement(d.Provider,{value:n},e.children)},u={inlineCode:"code",wrapper:function(e){var n=e.children;return r.createElement(r.Fragment,{},n)}},p=r.forwardRef((function(e,n){var t=e.components,o=e.mdxType,c=e.originalType,d=e.parentName,l=i(e,["components","mdxType","originalType","parentName"]),p=s(t),f=o,m=p["".concat(d,".").concat(f)]||p[f]||u[f]||c;return t?r.createElement(m,a(a({ref:n},l),{},{components:t})):r.createElement(m,a({ref:n},l))}));function f(e,n){var t=arguments,o=n&&n.mdxType;if("string"==typeof e||o){var c=t.length,a=new Array(c);a[0]=p;var i={};for(var d in n)hasOwnProperty.call(n,d)&&(i[d]=n[d]);i.originalType=e,i.mdxType="string"==typeof e?e:o,a[1]=i;for(var s=2;s<c;s++)a[s]=t[s];return r.createElement.apply(null,a)}return r.createElement.apply(null,t)}p.displayName="MDXCreateElement"},1687:function(e,n,t){t.r(n),t.d(n,{frontMatter:function(){return i},contentTitle:function(){return d},metadata:function(){return s},toc:function(){return l},default:function(){return p}});var r=t(7462),o=t(3366),c=(t(7294),t(3905)),a=["components"],i={sidebar_position:2},d="FawnEncoder",s={unversionedId:"codecs/encoder",id:"codecs/encoder",isDocsHomePage:!1,title:"FawnEncoder",description:"Much like the circe Encoder interface, this assumes encoding cannot fail, and since SQS usually deals in strings, we expect a string output.",source:"@site/../docs-builder/target/mdoc/codecs/encoder.md",sourceDirName:"codecs",slug:"/codecs/encoder",permalink:"/fawn/docs/codecs/encoder",editUrl:"https://github.com/meltwater/fawn/edit/main/docs/../docs-builder/target/mdoc/codecs/encoder.md",tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"tutorialSidebar",previous:{title:"Intro",permalink:"/fawn/docs/codecs/intro"},next:{title:"FawnDecoder",permalink:"/fawn/docs/codecs/decoder"}},l=[],u={toc:l};function p(e){var n=e.components,t=(0,o.Z)(e,a);return(0,c.kt)("wrapper",(0,r.Z)({},u,t,{components:n,mdxType:"MDXLayout"}),(0,c.kt)("h1",{id:"fawnencoder"},"FawnEncoder"),(0,c.kt)("p",null,"Much like the ",(0,c.kt)("inlineCode",{parentName:"p"},"circe")," ",(0,c.kt)("inlineCode",{parentName:"p"},"Encoder")," interface, this assumes encoding cannot fail, and since SQS usually deals in strings, we expect a string output."),(0,c.kt)("pre",null,(0,c.kt)("code",{parentName:"pre",className:"language-scala"},"trait FawnEncoder[T] {\n  def encode(t: T): String\n}\n")),(0,c.kt)("p",null,"This provides a cats ",(0,c.kt)("inlineCode",{parentName:"p"},"Contravariant")," so encoders have ",(0,c.kt)("inlineCode",{parentName:"p"},"contramap")," and other useful methods available:"),(0,c.kt)("pre",null,(0,c.kt)("code",{parentName:"pre",className:"language-scala"},"val longEncoder: FawnEncoder[Long] = new FawnEncoder[Long] {\n  def encode(l: Long) = l.toString\n}\n// longEncoder: FawnEncoder[Long] = repl.MdocSession$App0$$anon$1@6cfd7336\nval intEncoder: FawnEncoder[Int] = longEncoder.contramap(_.toLong)\n// intEncoder: FawnEncoder[Int] = com.meltwater.fawn.codec.FawnEncoder$$anon$1$$Lambda$7998/376033915@45608027\n")))}p.isMDXComponent=!0}}]);