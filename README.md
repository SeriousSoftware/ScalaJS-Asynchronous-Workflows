# Scala.js Asynchronous Workflow
<a href="http://www.w3.org/html/logo/">
<img src="https://www.w3.org/html/logo/badge/html5-badge-h-css3-graphics-semantics.png" width="99" height="32" alt="HTML5 Powered with CSS3 / Styling, Graphics, 3D &amp; Effects, and Semantics" title="HTML5 Powered with CSS3 / Styling, Graphics, 3D &amp; Effects, and Semantics"></a>[![Scala.js](https://img.shields.io/badge/scala.js-0.6.13%2B-blue.svg?style=flat)](https://www.scala-js.org)
## Introduction
>(Expirimental) Front End Application with Async Await for sequential workflow. It's cross compiled from Scala to JavaScript by use of Scala.js.
Even JS is single threaded, in an async block, blocking reads can pause its execution without disturbing other parts of the application. That give the illusion of blocking, even in single-threaded context (i.e. JS hosts).
Logic is written in a linear fashion.
Await expressions let you write asynchronous code almost as if it were synchronous.
I was inspired by [Designing Front End Applications with core.async](http://go.cognitect.com/core_async_webinar_recording) about Clojure go blocks with core.async library.<hr>

*This code is very much a work in progress.*
[Live demo](http://goo.gl/xvrPEl).

## Usage
1. Naturally, at least a Java SE Runtime Environment (JRE) is installed on your platform and has a path to it enables execution.
1. (Optional) Test this by submitting a `java -version` command in a [Command Line Interface (CLI, terminal)](https://en.wikipedia.org/wiki/Command-line_interface). The output should like this:
```
java version "1.8.0_102"
Java(TM) SE Runtime Environment (build 1.8.0_102-b14)
Java HotSpot(TM) 64-Bit Server VM (build 25.102-b14, mixed mode)
```
1. Make sure sbt is runnable from almost any work directory, use eventually one of the platform depended installers:
    1. [Installing sbt on Mac](http://www.scala-sbt.org/release/docs/Installing-sbt-on-Mac.html) or
    1. [Installing sbt on Windows](http://www.scala-sbt.org/release/docs/Installing-sbt-on-Windows.html) or
    1. [Installing sbt on Linux](http://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html) or
    1. [Manual installation](http://www.scala-sbt.org/release/docs/Manual-Installation.html) (not recommended)
1. (Optional ) To test if sbt is effective submit the `sbt sbtVersion` command. The response should like as this:
```
[info] Set current project to fransdev (in build file:/C:/Users/FransDev/)
[info] 0.13.12
```
Remenber shells (CLI's) are not reactive. To pick up the new [environment variables](https://en.wikipedia.org/wiki/Environment_variable) the CLI must restarted.
1. Run sbt in one of the next modes in a CLI, a compilation will be started and a local web server will be spinned up using:
    1. Inline mode on the command line: `sbt fastOptJS` or
    1. Interactive mode, start first the sbt by hitting in the CLI `sbt` followed by `fastOptJS` on the sbt prompt, or
    1. Triggered execution by a `~` before the command so `~fastOptJS`. This command will execute and wait after the
    target code is in time behind the source code (Auto build).
1.  sbt will give a notice that the server is listening by the message: `Bound to localhost/127.0.0.1:12345`
    (Ignore the dead letter notifications)
1. Open this application in a browser on [this given URL](http://localhost:12345/target/scala-2.11/classes/index-dev.html)

When running this way a tool ["workbench"](https://github.com/lihaoyi/workbench) also will be running in the browser noticeable by opening the console of the browser.

## In conclusion
Hereby I proved that [Scala.js](https://scala-js.org) has at least the same capabilities as ClosureScript.

## Async await
Based on CSP (communicating sequential processes) – naturally captures one-off async tasks AND async streams/queues (e.g. mouse movements, etc)
Async blocks are a source transform that give the illusion of blocking, even in single-threaded context (i.e. JS hosts)
### Terminology
* Channels – conduit between different processes – put values in, take values out
* Transducers – efficient way to control how values enter and exit channel, e.g. map/filter
* Async blocks – async blocks of execution with the illusion of blocking operations
** Core Operations
1. putting values onto a channel
1. taking values off a channel
1. selecting over multiple channels (puts & takes)

## 10 Short examples

1. Example1
create a channel of clicks, block, and add a note, blocking read inside of async blocks
1. Example 2 – same, but wait for 2 clicks
1. Example 3 – 2 channels, wait for one from each channel
1. Example 4 – shows how writing to a channel blocks that program until some other async block takes from the channel
1. Example 5 – separate async block to consume the channel async blocks are like forking a process. It looks like 2 separate processes running together and communicating over channels
1. Example 6 – stream processing
use transducer to convert events before they come into the channel
looks like an infinite loop – but getting a message off the click channel ends it
alts – select over multiple channels – non-deterministically read from whichever channel has data first
 pattern matching on the channel
transducers let you work at a higher level of abstraction and data like you like it
1. Example 7 – more transducers, discard events unless the y is divisible by 5. Loops can use the loop var bindings to record state
1. Example 8 – use loop vars to store state
1. Example 9 – more like real web, next/prev buttons to loop through a list
listen on prev/next channels to change vals, enable/disable prev/next
1. Example 10 – bigger example, broken out into helpers
more transducers – take all key presses, map the keyCode, filter by the set #{37 39}, and map them to :prev/:next
efficient because all those transformations happen in one function, inside the channel
click start, construct the keys channel
async/merge – take any number of channels, merge them into one channel (e.g. prev, next, keys) → a single channel that spits out :prev/:next
you can add more channels (e.g. left/right swipe on a tablet) and convert them to the same channels

## Licence
AsyncWorkflow.scala 2015-09-11 Asynchronous Workflows
© 2015 by F.W. van den Berg
Licensed under the EUPL V.1.1

This Software is provided to You under the terms of the European Union Public License (the "EUPL") version 1.1
as published by the European Union. Any use of this Software, other than as authorized under this License is
strictly prohibited (to the extent such use is covered by a right of the copyright holder of this Software).
 
This Software is provided under the License on an "AS IS" basis and without warranties of any kind concerning
the Software, including without limitation merchantability, fitness for a particular purpose, absence of defects
or errors, accuracy, and non-infringement of intellectual property rights other than copyright. This disclaimer
of warranty is an essential part of the License and a condition for the grant of any rights to this Software.
