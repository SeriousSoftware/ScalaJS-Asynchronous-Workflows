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
  "be.doeraene"           %%% "scalajs-jquery" % "0.9.0",
  "com.lihaoyi"           %%% "scalatags"      % "0.6.0",
  "org.scala-js"          %%% "scalajs-dom"    % "0.9.1",
  "org.scalatest"         %%% "scalatest"      % "3.0.0" % "test",
  "org.scala-lang.modules" %% "scala-async"    % "0.9.5"
)

scalacOptions in (Compile,doc) ++= Seq("-doc-root-content", baseDirectory.value+"/src/main/scala-2.11/root-doc.md",
  "-groups", "-implicits")

// ** Scala.js configuration **
// lazy val root = (project in file(".")).
enablePlugins(ScalaJSPlugin)

// Necessary for testing
jsDependencies += RuntimeDOM
scalaJSUseRhino in Global := false
jsEnv := PhantomJSEnv(autoExit = false).value

// If true, a launcher script src="../[normalizedName]-launcher.js will be generated
// that always calls the main def indicated by the used JSApp trait.
persistLauncher := true
persistLauncher in Test := false

// Will create [normalizedName]-jsdeps.js containing all JavaScript libraries
jsDependencies += "org.webjars" % "jquery" % "3.1.0" / "3.1.0/jquery.js"
// jsDependencies += "org.webjars" % "bootstrap" % "3.3.6" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "2.2.4/jquery.js"
skip in packageJSDependencies := false // All JavaScript dependencies to be concatenated to a single file

// ScalaTest settings //
// testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oF")

// Workbench settings **
if (sys.env.isDefinedAt("CI")) {
  println("Workbench disabled ", sys.env.getOrElse("CI", "?"))
  Seq.empty
} else {
  println("Workbench enabled")
  workbenchSettings
}

if (sys.env.isDefinedAt("CI")) normalizedName := normalizedName.value // Dummy
else // Update without refreshing the page every time fastOptJS completes
  updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)

if (sys.env.isDefinedAt("CI")) normalizedName := normalizedName.value
else // Workbench has to know how to restart your application.
  bootSnippet := "ss000101.AsyncWorkflow().main();"
