import xerial.sbt.Sonatype._

name := "fawn"
publishArtifact := false


lazy val baseSettings = List(
  version := "0.1",
  scalaVersion           := "2.13.7",
  libraryDependencies ++= List(
    "org.typelevel"  %% "log4cats-slf4j"             % "1.3.1",
    "net.andimiller" %% "munit-cats-effect-2-styles" % "1.0.0" % Test
  ),
  organization           := "com.meltwater.fawn",
  testFrameworks += new TestFramework("munit.Framework"),
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full),
  scalacOptions += "-Ymacro-annotations",
  useGpg                 := true,
  publishTo              := sonatypePublishTo.value,
  licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  sonatypeProjectHosting := Some(GitHubHosting("meltwater", "fawn", "andi at andimiller dot net")),
  developers             := List(
    Developer(
      id = "andimiller",
      name = "Andi Miller",
      email = "andi@andimiller.net",
      url = url("http://andimiller.net")))
)

lazy val common = project
  .in(file("common"))
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= List(
    )
  )

lazy val commonDecline = project
  .in(file("common-decline"))
  .dependsOn(common)
  .settings(baseSettings: _*)
  .settings(
    name := "common-decline",
    libraryDependencies ++= List(
      "com.monovore" %% "decline" % "2.2.0"
    )
  )

lazy val auth = project
  .in(file("auth"))
  .dependsOn(common)
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= List(
      "org.http4s"    %% "http4s-client"    % "0.21.31",
      "org.http4s"    %% "http4s-scala-xml" % "0.21.31",
      "org.typelevel" %% "cats-parse"       % "0.3.4"
    )
  )

lazy val codec = project
  .in(file("codec"))
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= List(
      "org.typelevel"              %% "cats-effect"               % "2.5.1",
      "org.typelevel"              %% "cats-laws"                 % "2.0.0" % Test,
      "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.3" % Test
    )
  )

lazy val codecCirce = project
  .in(file("codec-circe"))
  .dependsOn(codec)
  .settings(baseSettings: _*)
  .settings(
    name := "codec-codec",
    libraryDependencies ++= List(
      "io.circe" %% "circe-core"   % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1"
    )
  )

lazy val sqs = project
  .in(file("sqs"))
  .dependsOn(auth, codec)
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= List(
      "com.lucidchart" %% "xtract"              % "2.3.0-alpha3",
      "org.typelevel"  %% "cats-tagless-macros" % "0.14.0",
      "org.http4s"     %% "http4s-dsl"          % "0.21.31" % Test
    ),
  )
  
lazy val s3 = project
  .in(file("s3"))
  .dependsOn(auth, codec)
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= List(
      "com.lucidchart" %% "xtract"              % "2.3.0-alpha3",
      "org.typelevel"  %% "cats-tagless-macros" % "0.14.0",
      "org.http4s"     %% "http4s-dsl"          % "0.21.31" % Test,
      "org.http4s"     %% "http4s-scala-xml"    % "0.21.31",
      "com.beachape"   %% "enumeratum"          % "1.7.0"
    ),
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(sqs, codecCirce, commonDecline, s3)
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies ++= List(
      "org.http4s"         %% "http4s-blaze-client" % "0.21.31",
      "ch.qos.logback"      % "logback-classic"     % "1.2.6",
      "io.circe"           %% "circe-generic"       % "0.14.1",
      "org.codehaus.janino" % "janino"              % "3.1.6" % "runtime"
    ),
    publishArtifact := false
  )

lazy val docs = project
  .in(file("docs-builder"))
  .dependsOn(s3, sqs, codecCirce, commonDecline)
  .enablePlugins(MdocPlugin, DocusaurusPlugin)
  .settings(baseSettings: _*)
  .settings(
    mdocVariables   := Map(
      "VERSION" -> version.value
    ),
    libraryDependencies ++= List(
      "org.http4s" %% "http4s-blaze-client" % "0.21.31",
      "io.circe"   %% "circe-literal"       % "0.14.1"
    ),
    publishArtifact := false
  )

val root = project
  .in(file("."))
  .aggregate(common, commonDecline, auth, codec, codecCirce, sqs, s3, examples, docs)
  .settings(
    publishArtifact := false
  )
