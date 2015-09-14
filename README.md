# Scala.JS Asynchronous Workflow

Front End Application with Async Await for sequential workflow.

*This code is very much a work in progress.*

See at [Designing Front End Applications with core.async](http://go.cognitect.com/core_async_webinar_recording) about Clojure go blocks with core.async library.

## Usage
1. Naturally, at least a Java SE Development Kit is installed on your platform and environment variable JAVA_HOME has a
path to it. E.g. `C:\Program Files\Java\jdk1.8.0_51\` which holds the `jre` directory.
1. Make sure sbt is runnable from almost any work directory, use eventually one of the platform dependend installers:
    1. [Installing sbt on Mac](http://www.scala-sbt.org/release/tutorial/Installing-sbt-on-Mac.html) or
    1. [Installing sbt on Windows](http://www.scala-sbt.org/release/tutorial/Installing-sbt-on-Windows.html) or
    1. [Installing sbt on Linux](http://www.scala-sbt.org/release/tutorial/Installing-sbt-on-Linux.html) or
    1. [Manual installation](http://www.scala-sbt.org/release/tutorial/Manual-Installation.html) (not recommended)
1. Run sbt in one of the next modes in a Command Line Interface (CLI, terminal), a compilation and a webserver will be
    started using:
    1. Inline mode on the command line: `sbt fastOptJS` or
    1. Interactive mode, start first the sbt by hitting in the CLI `sbt` followed by `fullOptJS` on the sbt prompt, or
    1. Triggered execution by a `~` before the command so `~fullOptJS`. This command will execute and wait after the
    target code is in time behind the source code.
1.  sbt will give a notice that the server is listening by the message: "Bound to localhost/127.0.0.1:12345"
    (Ignore the dead letter notifications)
1. Open this application in a browser on [this given URL](http://localhost:12345/target/scala-2.11/classes/index-dev.html)

When running this way a tool ["workbench"](https://github.com/lihaoyi/workbench) also will be running in the browser noticeable
opening the console of the browser.

## Copyrights
AsyncWorkflow.scala 2015-09-11 Asynchronous Workflows
©2015 by F.W. van den Berg
Licensed under the EUPL V.1.1

This Software is provided to You under the terms of the European Union Public License (the "EUPL") version 1.1
as published by the European Union. Any use of this Software, other than as authorized under this License is
strictly prohibited (to the extent such use is covered by a right of the copyright holder of this Software).
 
This Software is provided under the License on an "AS IS" basis and without warranties of any kind concerning
the Software, including without limitation merchantability, fitness for a particular purpose, absence of defects
or errors, accuracy, and non-infringement of intellectual property rights other than copyright. This disclaimer
of warranty is an essential part of the License and a condition for the grant of any rights to this Software.