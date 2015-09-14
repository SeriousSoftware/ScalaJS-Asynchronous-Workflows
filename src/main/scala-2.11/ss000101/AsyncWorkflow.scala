package ss000101

import org.scalajs.dom.ext.KeyCode.{Left, Right}
import org.scalajs.dom.{MouseEvent, document}
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.annotation.tailrec
import scala.async.Async.{async, await}
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport

@JSExport
object AsyncWorkflow {
  val (f1, f2, f3a, f3b, f4, f5, f6, f7, f8, f9, f10s, f10, f11, f12) =
    (Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](),
      Chan[Null](), Chan[Null](), Chan[Char](), Chan[Null](), Chan[Char](), Chan[Null](), Chan[Null]())

  // An assignment to an instance of a Scala class calls the update method.
  jQuery("button#ex1-button").click({ () => f1() = null })
  jQuery("button#ex2-button").click({ () => f2() = null })
  jQuery("button#ex3-button-a").click({ () => f3a() = null })
  jQuery("button#ex3-button-b").click({ () => f3b() = null })
  jQuery("button#ex4-button").click({ () => f4() = null })
  jQuery("button#ex5-button").click({ () => f5() = null })
  jQuery("button#ex6-button").click({ () => f6() = null })
  jQuery("button#ex7-button").click({ () => f7() = null })
  jQuery("button#ex8-button").click({ () => f8() = null })
  jQuery("button#ex9-button-next").click(() => f9() = '+')
  jQuery("button#ex9-button-prev").click(() => f9() = '-')
  jQuery("button#ex10-button-start-stop").click(() => f10s() = null)
  jQuery("button#ex10-button-next").click(() => f10() = '+')
  jQuery("button#ex10-button-prev").click(() => f10() = '-')
  jQuery("button#ex11-button").click(() => f11() = null)

  @JSExport
  def initialization() = {}

  //

  // Example 1
  def ex01 =
    async {
      val showHooked = show("div#ex1-messages", _: String, _: Boolean)

      showHooked("Waiting for a click …", false)
      await(f1())
      showHooked("Got a click!", true)
    }

  // Example 2
  def ex02 =
    async {
      val showHooked = show("div#ex2-messages", _: String, _: Boolean)

      showHooked("Waiting for a click …", false)
      await(f2())
      showHooked("Got a click!", true)
      showHooked("Waiting for another click …", true)
      await(f2())
      showHooked("Done!", true)
    }

  // Example 3
  def ex03 =
    async {
      val showHooked = show("div#ex3-messages", _: String, _: Boolean)

      showHooked("Waiting for a click from Button A …", false)
      await(f3a())
      showHooked("Got a click!", true)
      showHooked("Waiting for a click from Button B …", true)
      await(f3b())
      showHooked("Done!", true)
    }

  ex01

  //
  //TODO Example 4
  def ex04 = async {}

  ex02

  //TODO Example 5
  def ex05 = async {}

  ex03

  // Example 6
  def ex06 =
    async {
      val showHooked = show("div#ex6-messages", _: String, _: Boolean)

      showHooked("Click button to start tracking the mouse!", false)
      await(f6())
      jQuery("button#ex6-button").text("Stop")

      val mousemove = new Chan[MouseEvent](document.onmousemove = _)
      var running = true
      async {
        while (running) {
          val event: MouseEvent = await(mousemove())
          showHooked(s"[${event.clientX}, ${event.clientY}]", true)
        }
        showHooked("Done!", true)
      }
      await(f6())
      running = false

      jQuery("button#ex6-button").text("Done!")
      jQuery("button#ex6-button").addClass("disabled")

    }

  ex04

  // Example 7
  def ex07 = async {
    val showHooked = show("div#ex7-messages", _: String, _: Boolean)

    showHooked("Click button to start tracking the mouse!", false)
    await(f7())
    jQuery("button#ex7-button").text("Stop")

    val mousemove = new Chan[MouseEvent](document.onmousemove = _)
    var running = true
    async {
      while (running) {
        //mousemove().map { case move => if (move.clientY % 5 == 0) Success(move) else }

        val event: MouseEvent = await(mousemove.filter(_.clientY % 5 == 0))

        showHooked(s"[${event.clientX}, ${event.clientY}]", true)
      }
      showHooked("Done!", true)
    }
    await(f7())
    running = false

    jQuery("button#ex7-button").text("Done!")
    jQuery("button#ex7-button").addClass("disabled")
  }

  ex05

  /**
   * Given a element id and a message string append a child paragraph element with the given message string.
   * @param display
   * @param message
   * @param append
   */
  def show(display: String, message: String, append: Boolean = true): Unit = {
    val item = jQuery(display)
    if (append) item.append(s"<p>$message</p>")
    else item.html(s"<p>$message</p>")
  }

  ex06

  // Example 8
  def ex08 = async {
    val showHooked = show("div#ex8-messages", _: String, _: Boolean)

    showHooked("Click the button ten times!", false)
    var i = 0
    while (i < 10) {
      await(f8())
      i += 1
      showHooked(s"$i clicks!", true)
    }
    showHooked("Done!", true)
  }

  ex07

  // Example 9
  def ex09 = async {
    val UpperBound = list.size - 1
    var idx = 0
    while (true) {
      grayOut(idx, "button#ex9-button-prev", "button#ex9-button-next", list, idx, "td#ex9-card")
      await(f9()) match {
        case '-' => idx = Math.max(idx - 1, 0)
        case '+' => idx = Math.min(idx + 1, UpperBound)
      }
    }
  }

  ex08

  /**
   * Given a current index and the collection disable or enable the given previous and next controls.
   * @param i
   * @param buttonPrev
   * @param buttonNext
   * @param list
   * @param idx
   * @param display
   * @return
   */
  def grayOut(i: Int,
              buttonPrev: String,
              buttonNext: String,
              list: Vector[String] = Vector(),
              idx: Int = 0,
              display: String = "") = {

    val running = display != ""
    require(running == (list != Vector()))
    lazy val UpperBound = list.size - 1
    if (running) {
      i match {
        case 0 =>
          jQuery(buttonPrev).addClass("disabled")
          jQuery(buttonNext).removeClass()
        case UpperBound =>
          jQuery(buttonNext).addClass("disabled")
          jQuery(buttonPrev).removeClass()
        case _ =>
          jQuery(buttonPrev).removeClass()
          jQuery(buttonNext).removeClass()
      }
      show(display, s"${list(idx)}", append = false)
    } else {
      jQuery(buttonPrev).addClass("disabled")
      jQuery(buttonNext).addClass("disabled")
    }
  }

  ex09

  def list = Vector("aardvark", "beetle", "cat", "dog", "elk", "ferret", "goose", "hippo", "ibis", "jellyfish", "kangaroo")

  // Example 10
  def ex10 {
    var idx = 0
    grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next")

    async {
      await(f10s())

      jQuery(document).keydown {
        (evt: JQueryEventObject) =>
          evt.which match {
            case Left => f10() = '-'
            case Right => f10() = '+'
            case _ =>
          }
      }

      grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next", list, idx, "td#ex10-card")
      show("button#ex10-button-start-stop", "Stop !", append = false)

      async {
        while (true) {
          grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next", list, idx, "td#ex10-card")
          val event = await(f10())
          event match {
            case '-' => idx = math.max(idx - 1, 0)
            case '+' => idx = math.min(idx + 1, list.size - 1)
          }
        }
      }

      await(f10s())
      jQuery("button#ex10-button-start-stop").text("Done!")
      jQuery("button#ex10-button-start-stop").addClass("disabled")

      grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next")

    }
  }

  ex10
}

/**
 * Single item channel primitive
 *
 * @tparam T
 */
case class Chan[T]() {
  private[this] var promise: Promise[T] = _

  /** Auxiliary constructor
   Binds a handler with the "write" in casu update() */
  def this(handler: (T => Unit) => Unit) {
    this
    handler(update)
  }

  /** Channels' "Write" or "put" function, called by an assigned to the instance(). */
  def update(t: T): Unit = if (!(promise == null || promise.isCompleted)) promise.success(t)

  def filter(p: (T) => Boolean): Future[T] = {
    //var value1 : Option[T] = None
    apply().flatMap(value => if (p(value)) Future(value) else filter(p))
  }

  /* Channels' "Read" or "get" function, called by referring to the instance().*/
  def apply(): Future[T] = {
    promise = Promise[T]()
    promise.future
  }

  def |(other: Chan[T]): Future[T] = {
    val p = Promise[T]()
    for {
      f <- Seq(other(), this())
      t <- f
    } p.trySuccess(t)
    p.future
  }
}
