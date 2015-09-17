package ss000101

import org.scalajs.dom.ext.KeyCode.{Left, Right}
import org.scalajs.dom.{MouseEvent, document}
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.async.Async.{async, await}
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Date


@JSExport
object AsyncWorkflow {
  val (f1, f2, f3a, f3b, f4, f5, f6, f7, f8, f9, f10s, f10, f11, f12) =
    (Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](), Chan[Null](),
      Chan[Null](), Chan[Null](), Chan[Int](), Chan[Null](), Chan[Int](), Chan[Null](), Chan[Null]())

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
  jQuery("button#ex9-button-next").click(() => f9() = Right)
  jQuery("button#ex9-button-prev").click(() => f9() = Left)
  jQuery("button#ex10-button-start-stop").click(() => f10s() = null)
  jQuery("button#ex10-button-next").click(() => f10() = Right)
  jQuery("button#ex10-button-prev").click(() => f10() = Left)
  jQuery("button#ex11-button").click(() => f11() = null)

  @JSExport
  def initialization() = {}

  // Example 1
  def ex01() =
    async {
      val _show = show("div#ex1-messages", _: String, _: Boolean)

      _show("Waiting for a click …", false)
      await(f1())
      _show("Got a click!", true)
    }

  // Example 2
  def ex02() =
    async {
      val _show = show("div#ex2-messages", _: String, _: Boolean)

      _show("Waiting for a click …", false)
      await(f2())
      _show("Got a click!", true)
      _show("Waiting for another click …", true)
      await(f2())
      _show("Done!", true)
    }

  // Example 3
  def ex03() =
    async {
      val _show = show("div#ex3-messages", _: String, _: Boolean)

      _show("Waiting for a click from Button A …", false)
      await(f3a())
      _show("Got a click!", true)
      _show("Waiting for a click from Button B …", true)
      await(f3b())
      _show("Done!", true)
    }

  //
  //TODO Example 4
  def ex04() = async {
    async {
      val _show = show("div#ex4-messages", _: String, _: Boolean)

      _show("Waiting for a click …", false)
      await(f1())
      _show("Got a click!", true)
    }

    val _show = show("div#ex4-messages", _: String, _: Boolean)

    val dateTime = new Date()

    _show(dateTime.toISOString(), false)

  }

/* (defn ex4 []
  (let [clicks (events->chan (by-id "ex4-button-a") EventType.CLICK)
  c0     (c han)
  show!  (partial show! "ex4-messages")]
  (go
    (show! "Waiting for click.")
    (<! clicks)
    (show! "Putting a value on channel c0, cannot proceed until someone takes")
    (>! c0 (js/Date.))
    (show! "We'll never get this far!")
    (<! c0))))*/

  //TODO Example 5
  def ex05() = async {}

  // Example 6
  def ex06() =
    async {
      val _show = show("div#ex6-messages", _: String, _: Boolean)

      _show("Click button to start tracking the mouse!", false)
      await(f6())
      show("button#ex6-button", "Stop")

      val mousemove = new Chan[MouseEvent](document.onmousemove = _)
      var running = true
      async {
        while (running) {
          val event: MouseEvent = await(mousemove())
          _show(s"[${event.clientX}, ${event.clientY}]", true)
        }
        _show("Done!", true)
      }
      await(f6())
      running = false

      show("button#ex6-button", "Done!")
      disableKey("button#ex6-button")
    }

  // Example 7
  def ex07() = async {
    val _show = show("div#ex7-messages", _: String, _: Boolean)

    _show("Click button to start tracking the mouse!", false)
    await(f7())
    show("button#ex7-button", "Stop")

    val mousemove = new Chan[MouseEvent](document.onmousemove = _)
    var running = true
    async {
      while (running) {
        val event: MouseEvent = await(mousemove.filter(_.clientY % 5 == 0))
        _show(s"[${event.clientX}, ${event.clientY}]", true)
      }
      _show("Done!", true)
    }
    await(f7())
    running = false

    show("button#ex7-button", "Done!")
    disableKey("button#ex7-button")
  }

  // Example 8
  def ex08() = async {
    val _show = show("div#ex8-messages", _: String, _: Boolean)

    _show("Click the button ten times!", false)
    var i=0
    if (i < 9) {
      i +=1
      await(f8())
      _show(s"$i click${if (i > 1) "s!" else " !"}", true)
    }
    _show("Done!", true)
  }

  // Example 9
  def ex09() = async {
    val UpperBound = list.size - 1
    var idx = 0
    while (true) {
      grayOut(idx, "button#ex9-button-prev", "button#ex9-button-next", list, idx, "td#ex9-card")
      await(f9()) match {
        case Left => idx = Math.max(idx - 1, 0)
        case Right => idx = Math.min(idx + 1, UpperBound)
      }
    }
  }

  def list = Vector("aardvark", "beetle", "cat", "dog", "elk", "ferret", "goose", "hippo", "ibis", "jellyfish", "kangaroo")

  // Example 10
  def ex10() {
    var idx = 0
    grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next")

    async {
      await(f10s())

      jQuery(document).keydown {
        evt: JQueryEventObject => f10() = evt.which
      }
      show("button#ex10-button-start-stop", "Stop")
      grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next", list, idx, "td#ex10-card")

      async {
        while (true) {
          grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next", list, idx, "td#ex10-card")
          val event = await(f10())
          event match {
            case Left => idx = math.max(idx - 1, 0)
            case Right => idx = math.min(idx + 1, list.size - 1)
          }
        }
      }

      await(f10s())
      show("button#ex10-button-start-stop", "Done!")
      disableKey("button#ex10-button-start-stop")

      grayOut(idx, "button#ex10-button-prev", "button#ex10-button-next")

    }
  }

  /** Disable a key by its given id */
  def disableKey(buttonId: String): Unit = jQuery(buttonId).addClass("disabled")

  /**
   * Given a element id and a message string, append a child paragraph element with the given message string.
   * @param display
   * @param message
   * @param append
   */
  def show(display: String, message: String, append: Boolean = false): Unit = {
    val item = jQuery(display)
    if (append) item.append(s"<p>$message</p>")
    else item.html(s"<p>$message</p>")
  }

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
case class Chan[T]() {
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