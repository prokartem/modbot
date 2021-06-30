name := "modbot-pr"

version := "0.1"

scalaVersion := "2.13.5"

enablePlugins(JavaAppPackaging)

lazy val catsCore         = "2.4.2"
lazy val http4sVersion    = "0.21.24"
lazy val jawnfs2v         = "1.1.0"
lazy val circeVersion     = "0.14.0"
lazy val circefs2V        = "0.13.0"
lazy val fs2v             = "2.5.3"
lazy val doobieVersion    = "0.12.1"
lazy val logBack          = "1.2.3"
lazy val pureConfig       = "0.15.0"
lazy val scalaLogging     = "3.9.3"
lazy val scalatestVersion = "3.2.9"

libraryDependencies ++= Seq(
  "org.typelevel"              %% "cats-core"           % catsCore,
  "org.typelevel"              %% "cats-kernel"         % catsCore,
  "org.http4s"                 %% "http4s-dsl"          % http4sVersion,
  "org.http4s"                 %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"                 %% "http4s-blaze-client" % http4sVersion,
  "org.http4s"                 %% "http4s-circe"        % http4sVersion,
  "org.http4s"                 %% "jawn-fs2"            % jawnfs2v,
  "io.circe"                   %% "circe-generic"       % circeVersion,
  "io.circe"                   %% "circe-literal"       % circeVersion,
  "io.circe"                   %% "circe-core"          % circeVersion,
  "io.circe"                   %% "circe-parser"        % circeVersion,
  "io.circe"                   %% "circe-fs2"           % circefs2V,
  "co.fs2"                     %% "fs2-core"            % fs2v,
  "co.fs2"                     %% "fs2-io"              % fs2v,
  "org.tpolecat"               %% "doobie-core"         % doobieVersion,
  "org.tpolecat"               %% "doobie-postgres"     % doobieVersion,
  "org.tpolecat"               %% "doobie-specs2"       % doobieVersion,
  "ch.qos.logback"              % "logback-classic"     % logBack,
  "com.github.pureconfig"      %% "pureconfig"          % pureConfig,
  "com.typesafe.scala-logging" %% "scala-logging"       % scalaLogging,
  "org.scalatest"              %% "scalatest"           % scalatestVersion % "test",
//  "org.scalacheck"             %% "scalacheck"          % scalacheckVersion % "test",
)
