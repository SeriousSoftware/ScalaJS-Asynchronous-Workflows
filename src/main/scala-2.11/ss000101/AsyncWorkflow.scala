package ss000101

import org.scalajs.dom.ext.KeyCode.{Left, Right}
import org.scalajs.dom.{MouseEvent, document}
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.async.Async.{async, await}
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Date

/**
 * Even JS is single threaded, in an async block, blocking reads can
 * pause its execution without disturbing other parts of the application.
 *
 * Logic is written in a linear fashion.
 * Await expressions let you write asynchronous code almost as if it were synchronous.
 *
 */

@JSExport
object AsyncWorkflow {
  val (chan1, chan2, chan3a, chan3b, chan4, chan4D, chan5, chan5D, chan6, chan7, chan8, chan9, chan10s, chan10, chan11) =
    (Chan("button#ex1-button"),
      Chan("button#ex2-button"),
      Chan("button#ex3-button-a"),
      Chan("button#ex3-button-b"),
      Chan("button#ex4-button"),
      new Chan[Date](),
      Chan("button#ex5-button"),
      new Chan[Date](),
      Chan("button#ex6-button"),
      Chan("button#ex7-button"),
      Chan("button#ex8-button"),
      Chan(("button#ex9-button-next", Right), ("button#ex9-button-prev", Left)),
      Chan("button#ex10-button-start-stop"),
      Chan(("button#ex10-button-next", Right), ("button#ex10-button-next", Left)),
      Chan("button#ex11-button"))

  @JSExport
  def initialization() = {} // Called from the page, let all the object code run

  /**
   * Given a element id and a message string, alter a child paragraph element with the given message string.
   * @param display
   * @param message
   * @param append
   */
  def show(display: String, message: String, append: Boolean = false): Boolean = {
    val item = jQuery(display)
    if (append) item.append(s"<p>$message</p>")
    else item.html(s"<p>$message</p>")
    true
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  // Example 1
  def ex01() =
    async {
      var append = false
      val _show = show("div#ex1-messages", _: String, append) // Partially applied

      append = _show("Waiting for a click …")
      await(chan1()) // Do a blocking read for this async block of the channel.
      _show("Got a click!")
    }

  // Example 2
  def ex02() =
    async {
      var append = false
      val _show = show("div#ex2-messages", _: String, append)

      append = _show("Waiting for a click …")
      await(chan2())
      _show("Got a click!")
      _show("Waiting for another click …")
      await(chan2())
      _show("Done!")
    }

  // Example 3
  def ex03() =

    async {
      var append = false
      val _show = show("div#ex3-messages", _: String, append)

      append = _show("Waiting for a click from Button A …")
      await(chan3a())
      _show("Got a click!")
      _show("Waiting for a click from Button B …")
      await(chan3b())
      _show("Done!")
    }

  //
  //TODO Example 4
  def ex04() = async {
    var append = false
    val _show = show("div#ex4-messages", _: String, append)

    append = _show("Waiting for a click …")
    await(chan4())
    _show("Got a click!")
    chan4D() = new Date()
    _show("We'll going further!")
    await(chan4D())
    _show("But we'll never get this far!")
  }

  //TODO Example 5
  def ex05() = {
    var append = false
    val _show = show("div#ex5-messages", _: String, append)

    async {
      append = _show("Waiting for a click …")
      await(chan5())
      _show("Got a click!")
      chan5D() = new Date()
      _show("We'll going further!")
      val date = await(chan5D())
      _show(s"Someone put the value on the channel: ${date.toISOString()}")
    }
    async {
      while (true) {
        val date = await(chan5D())
        _show(s"We got a value from waiting channel: ${date.toISOString()}")
        chan5D() = new Date()
      }
    }
  }

  // Example 6
  def ex06() =
    async {
      var append = false
      val _show = show("div#ex6-messages", _: String, append)

      append = _show("Click button to start tracking the mouse!")
      await(chan6())
      show("button#ex6-button", "Stop")

      val mousemove = new Chan[MouseEvent](document.onmousemove = _)
      val keyPressed = new Chan[MouseEvent]
      var running = true
      async {
        while (running) {
          val event: MouseEvent = await(mousemove | keyPressed)
          if (event != null)
            _show(s"[${event.clientX}, ${event.clientY}]")
        }
        _show("Done!")
      }
      await(chan6())
      running = false
      keyPressed() = null

      show("button#ex6-button", "Done!")
      disableKey("button#ex6-button")
    }

  // Example 7
  def ex07() = async {
    var append = false
    val keyPressed = new Chan[MouseEvent]
    val _show = show("div#ex7-messages", _: String, append)

    append = _show("Click button to start tracking the mouse!")
    await(chan7())
    show("button#ex7-button", "Stop")

    val mousemove = new Chan[MouseEvent](document.onmousemove = _)
    var running = true
    async {
      while (running) {
        val event: MouseEvent = await(keyPressed | mousemove)
        if (event != null && event.clientY /*% 5*/ == 0) _show(s"[${event.clientX}, ${event.clientY}]")
      }
      _show("Done!")
    }
    await(chan7())
    running = false
    keyPressed() = null

    show("button#ex7-button", "Done!")
    disableKey("button#ex7-button")
  }

  // Example 8
  def ex08() = async {
    var append = false
    val (n, _show) = (10, show("div#ex8-messages", _: String, append))

    append = _show(s"Click the button $n times!")
    var i = 0
    while (i < n) {
      i += 1
      await(chan8())
      _show(f"<PRE>$i%3d click${if (i > 1) "s!" else " !"}%s</PRE>")
    }
    _show("Done!")
  }

  // Example 9
  def ex09() = async {
    val UpperBound = list.size - 1
    var idx = 0
    while (true) {
      grayOut(idx, "button#ex9-button-prev", "button#ex9-button-next", list, idx, "td#ex9-card")
      await(chan9()) match {
        case Left => idx = math.max(idx - 1, 0)
        case Right => idx = math.min(idx + 1, UpperBound)
      }
    }
  }

  def list = Vector("aardvark", "beetle", "cat", "dog", "elk", "ferret", "goose", "hippo", "ibis", "jellyfish", "kangaroo")

  // Example 10
  def ex10() {
    var idx = 0
    grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next")

    async {
      await(chan10s())

      jQuery(document).keydown {
        evt: JQueryEventObject => chan10() = evt.which
      }
      show("button#ex10-button-start-stop", "Stop")
      grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next", list, idx, "td#ex10-card")

      async {
        while (true) {
          grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next", list, idx, "td#ex10-card")
          await(chan10()) match {
            case Left => idx = math.max(idx - 1, 0)
            case Right => idx = math.min(idx + 1, list.size - 1)
          }
        }
      }

      await(chan10s())
      show("button#ex10-button-start-stop", "Done!")
      disableKey("button#ex10-button-start-stop")

      grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next")

    }
  }

  /** Disable a key by its given id */
  def disableKey(buttonId: String): Unit = jQuery(buttonId).addClass("disabled")

  /**
   * Given a current index and the collection disable or enable the given previous and next controls.
   * @param i
   * @param buttonPrev
   * @param buttonNext
   * @param list
   * @param idx
   * @param displayId
   * @return
   */
  def grayOut(i: Int,
              buttonPrev: String,
              buttonNext: String,
              list: Vector[String] = Vector(),
              idx: Int = 0,
              displayId: String = "") = {

    val running = displayId != ""
    require(running == (list != Vector()), "Func Grayout: List must be supplyed for the running mode.")

    lazy val UpperBound = list.length - 1
    if (running) {
      i match {
        case 0 =>
          disableKey(buttonPrev)
          jQuery(buttonNext).removeClass()
        case UpperBound =>
          disableKey(buttonNext)
          jQuery(buttonPrev).removeClass()
        case _ =>
          jQuery(buttonPrev).removeClass()
          jQuery(buttonNext).removeClass()
      }
      show(displayId, s"${list(idx)}")
    } else {
      disableKey(buttonPrev)
      disableKey(buttonNext)
    }
  }

  ex01()
  ex02()
  ex03()
  ex04()
  ex05()
  ex06()
  ex07()
  ex08()
  ex09()
  ex10()
  jQuery("div#ex11").remove()
}

/**
 * Single item channel primitive
 *
 * @tparam T
 */
class Chan[T]() {
  private var promise: Promise[T] = Promise[T]()

  /** Auxiliary constructor
   Binds a handler with the "write" in casu update() */
  def this(handler: (T => Unit) => Unit) {
    this
    handler(update)
  }

  /** Channels' "Write" or "put" function, called by an assigned to the instance(). */
  def update(t: T): Unit = if (!promise.isCompleted) promise.success(t)

  def filter(p: (T) => Boolean): Future[T] = {
    apply().flatMap(value => if (p(value)) Future(value) else filter(p))
  }

  /** Channels' "Read" or "get" function, called by referring to the instance(). */
  def apply(): Future[T] = {
    promise = Promise[T]()
    promise.future
  }

  /** Channels' "Alt" function for waiting on two events */
  def |(other: Chan[T]): Future[T] = {
    val p = Promise[T]()
    for {
      f <- Seq(other(), this())
      t <- f
    } p.trySuccess(t)
    p.future
  }
}

/**
 * Given a target DOM element and event type create and return a channel of observed events.
 */
object Chan {

  def apply(domElemId: String): Chan[Null] = apply((domElemId, null))

  def apply[T](domElemIdCombies: (String, T)*) = {
    val instance = new Chan[T]()
    // An assignment to an instance of a Scala class calls the update method.
    domElemIdCombies.foreach { combi => jQuery(combi._1).click({ () => instance() = combi._2 }) }
    instance
  }
}