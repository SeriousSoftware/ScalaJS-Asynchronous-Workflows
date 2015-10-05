version := "0.0"
organization := "io.github.serioussoftware"
organizationName := "Serious Software"
organizationHomepage := Some(url("http://serioussoftware.github.io"))
homepage := Some(url("http://serioussoftware.github.io"))
startYear := Some(2015)
description := "Scala.JS CSP Application with Async Await in sequential workflow"
licenses += "EUPL v1.1" -> url("https://joinup.ec.europa.eu/community/eupl/og_page/european-union-public-licence-eupl-v11")

// Turn this project into a Scala.js project by importing these settings
lazy val root = (project in file(".")).enablePlugins(ScalaJSPlugin)
workbenchSettings

// Name is a prefix in the object code filename.
name := "Asynchronous Workflows"
scalaVersion := "2.11.7"

scalacOptions ++= Seq("-feature")

// Optional, necessary to sbt run, needs phantomJS to be installed.
jsDependencies += RuntimeDOM
scalaJSStage in Global := FastOptStage

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-async" % "0.9.5",
  "org.scala-js" %%% "scalajs-dom" % "0.8.2",
  "be.doeraene" %%% "scalajs-jquery" % "0.8.0",
  "com.lihaoyi" %%% "scalatags" % "0.5.2",
  "com.lihaoyi" %%% "utest" % "0.3.1" % "test"
)
skip in packageJSDependencies := false // All JavaScript dependencies to be concatenated to a single file

// Workbench has to know how to restart your application.
bootSnippet := "ss000101.AsyncWorkflow().initialization();"
// Update without refreshing the page every time fastOptJS completes
updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)

testFrameworks += new TestFramework("utest.runner.Framework")
