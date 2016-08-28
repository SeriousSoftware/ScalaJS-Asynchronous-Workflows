                name := "Asynchronous Workflows"
             version := "0.0"
         description := "Scala.js CSP Application with Async Await in sequential workflow"
        organization := "io.github.serioussoftware"
    organizationName := "Serious Software"
organizationHomepage := Some(url("http://serioussoftware.github.io"))
            homepage := Some(url("http://serioussoftware.github.io"))
           startYear := Some(2015)
            licenses += "EUPL v.1.1" -> url("http://joinup.ec.europa.eu/community/eupl/og_page/european-union-public-licence-eupl-v11")

// KEEP THIS normalizedName CONSTANTLY THE SAME, otherwise the outputted JS filename will be changed.
      normalizedName := "main"

// ** Scala dependencies **
scalaVersion in ThisBuild := "2.11.8"

libraryDependencies ++= Seq(
  "be.doeraene"           %%% "scalajs-jquery" % "0.8.1",
  "com.lihaoyi"           %%% "scalatags"      % "0.6.0",
  "org.scala-js"          %%% "scalajs-dom"    % "0.9.1",
  "org.scalatest"         %%% "scalatest"      % "3.0.0" % "test",
  "org.scala-lang.modules" %% "scala-async"    % "0.9.5"
)
skip in packageJSDependencies := false // All JavaScript dependencies to be concatenated to a single file

scalacOptions in (Compile,doc) ++= Seq("-doc-root-content", baseDirectory.value+"/src/main/scala-2.11/root-doc.md",
  "-groups", "-implicits")

// ** Scala.js configuration **
// lazy val root = (project in file(".")).
enablePlugins(ScalaJSPlugin)

// Necessary for testing
jsDependencies += RuntimeDOM
// Turn this project into a Scala.js project by importing these setting
workbenchSettings



// Workbench has to know how to restart your application.
bootSnippet := "ss000101.AsyncWorkflow().initialization();"
// Update without refreshing the page every time fastOptJS completes
updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)
