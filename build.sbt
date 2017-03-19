import play.Project._

organization := "io.sphere.shop"

name := "sphere-fedora"

version := "1.0-SNAPSHOT"

playJavaSettings

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  "com.google.inject" % "guice" % "3.0",
  "io.sphere" %% "sphere-play-sdk" % "0.72.1" withSources(),
  "javax.mail" % "mail" % "1.4.7",
  "org.jsoup" % "jsoup" % "1.7.1",
  "de.paymill" % "paymill-java" % "2.6",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

lessEntryPoints := baseDirectory.value / "app" / "assets" / "stylesheets" * "*.less"

templatesImport ++= Seq(
  "utils.ViewHelper._",
  "utils.PrintUtils._",
  "utils.PriceUtils._",
  "forms._",
  "io.sphere.client.model._",
  "io.sphere.client.filters._",
  "io.sphere.client.shop.model._"
)

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
