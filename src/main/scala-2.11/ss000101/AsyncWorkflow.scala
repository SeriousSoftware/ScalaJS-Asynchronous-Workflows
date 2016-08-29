package ss000101

import org.scalajs.dom.ext.KeyCode.{Left, Right}
import org.scalajs.dom.{MouseEvent, document}
import org.scalajs.jquery.{JQueryEventObject, jQuery}
import pragmatiCSP._

import scala.async.Async.{async, await}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.{Date, JSApp}
import scalatags.Text.all.{p, pre, stringFrag}

object AsyncWorkflow extends JSApp {

  val chan7 = Chan("button#ex7-button")
  val chan8 = Chan("button#ex8-button")
  val chan9 = Chan(("button#ex9-button-next", Right), ("button#ex9-button-prev", Left))
  val chan11 = Chan("button#ex11-button")
  private val (mouseChannel: Channel[MouseEvent], mouseChan0, mouseChan1) =
    (new Channel[MouseEvent](document.onmousemove = _), Chan[MouseEvent](), Chan[MouseEvent]())

  def main() = {} // Called from the page, let all the object code run

  // Example 1
  def ex01() = {
    val chan1 = Chan("button#ex1-button")
    async {
      var append = false
      val _show = show("div#ex1-messages", _: String, append) // Partially applied

      append = _show("Waiting for a click …")
      await(chan1()) // Do a blocking read for this async block of the channel.
      _show("Got a click!")
    }
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  /**
   * Given a element id and a message string, alter a child paragraph element with the given message string.
   *
   * @param display
   * @param message
   * @param append
   */
  def show(display: String, message: String, append: Boolean = false): Boolean = {
    val item = jQuery(display)
    if (append) item.append(pre(message).toString()) else item.html(p(message).toString())
    true
  }

  // Example 2
  def ex02() = {
    val chan2 = Chan("button#ex2-button")
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
  }

  // Example 3
  def ex03() = {
    val (chan3a, chan3b) = (Chan("button#ex3-button-a"), Chan("button#ex3-button-b"))
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
  }

  // Example 4
  def ex04() = {
    val (chan4, chan4D) = (Chan("button#ex4-button"), Chan[Date]())
    async {
      var append = false
      val _show = show("div#ex4-messages", _: String, append)

      append = _show("Waiting for a click …")
      //    await(chan4())
      _show("Got a click!")
      _show("Putting a value on another channel, stalled because nobody takes")
      chan4D() = new Date()
      _show("But we'll never get this far!")
    }
  }

  /*;; Example 4
  (defn ex4 []
  (let [clicks (events->chan (by-id "ex4-button-a") EventType.CLICK)
  c0     (chan)
  show!  (partial show! "ex4-messages")]
  (go
    (show! "Waiting for click.")
    (<! clicks)
    (show! "Putting a value on channel c0, cannot proceed until someone takes")
    (>! c0 (js/Date.))
    (show! "We'll never get this far!")
    (<! c0))))*/

  //Example 5
  def ex05() = {
    val (chan5, chan5D) = (Chan("button#ex5-button"), Chan[Date]())
    var append = false
    val _show = show("div#ex5-messages", _: String, append)

    async {
      append = _show("Waiting for a click …")
      await(chan5())
      _show("Got a click!")
      chan5D() = new Date()
      _show("1. Putting a value on another channel, stalled until someone takes")
      val date = await(chan5D())
      _show(s"2. Someone put the value on the channel: ${date.toISOString()}")
    }
    async {
      while (true) {
        val date = await(chan5D())
        _show(s"3. We got a value from waiting channel: ${date.toISOString()}")
        chan5D() = new Date()
      }
    }
  }

  // Example 6
  def ex06() = {
    val chan6 = Chan("button#ex6-button")
    async {
      var append = false
      val _show = show("div#ex6-messages", _: String, append)

      append = _show("Click button to start tracking the mouse!")
      await(chan6())
      show("button#ex6-button", "Stop")

      val keyPressed = Chan[MouseEvent]()
      var running = true
      async {
        while (running) {
          val event: MouseEvent = await(mouseChan0 || keyPressed)
          if (event != null)
            _show(s"[${event.clientX}, ${event.clientY}]")
        }
        _show("Done!")
      }
      await(chan6())
      running = false
      keyPressed() =
        null

      show("button#ex6-button", "Done!")
      disableKey("button#ex6-button")
    }
  }

  // Example 7
  def ex07() = {
    val chan7 = Chan("button#ex7-button")
    async {
      var append = false
      val keyPressed = new Channel[MouseEvent]
      val _show = show("div#ex7-messages", _: String, append)

      append = _show("Click button to start tracking the mouse!")
      await(chan7())
      show("button#ex7-button", "Stop")

      var running = true
      async {
        while (running) {
          val event: MouseEvent = await(keyPressed || mouseChan1)
          if (event != null && event.clientY % 5 == 0) _show(s"[${event.clientX}, ${event.clientY}]")
        }
        _show("Done!")
      }
      await(chan7())
      running = false
      keyPressed() = null

      show("button#ex7-button", "Done!")
      disableKey("button#ex7-button")
    }
  }

  /** Disable a key by its given id */
  def disableKey(buttonId: String): Unit = jQuery(buttonId).addClass("disabled")

  // Example 8
  def ex08() = {
    val chan8 = Chan("button#ex8-button")
    async {
      var append = false
      val (n, _show) = (10, show("div#ex8-messages", _: String, append))

      append = _show(s"Click the button $n times!")
      var i = 0
      while (i < n) {
        i += 1
        await(chan8())
        _show(f"$i%2d click${if (i > 1) "s!" else " !"}")
      }
      _show("Done!")
    }
  }

  // Example 9
  def ex09() = {
    val chan9 = Chan(("button#ex9-button-next", Right), ("button#ex9-button-prev", Left))
    async {
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
  }

  def list = Vector("aardvark", "beetle", "cat", "dog", "elk", "ferret", "goose", "hippo", "ibis", "jellyfish", "kangaroo")

  // Example 10
  def ex10() {
    val (chan10s, chan10) = (Chan("button#ex10-button-start-stop"),
      Chan( ("button#ex10-button-prev", Left), ("button#ex10-button-next", Right)))
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
      jQuery(document).unbind("keydown")
    }
  }

  /**
   * Given a current index and the collection disable or enable the given previous and next controls.
   *
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
    require(running == (list != Vector()), "Func grayOut: List must be supplied for the running mode.")

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

  /* Distribution helper for mouse events */
  async {
    while (true) {
      val event: MouseEvent = await(mouseChannel())
      mouseChan0.update(event)
      mouseChan1.update(event)
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
 * Given a target DOM element and event type create and return a channel of observed events.
 */
object Chan {

  /**
   * Bare wait channel on a known event, message unnecessary.
   *
   * @param domElemId
   * @return
   */
  def apply(domElemId: String): Channel[Null] = apply((domElemId, null))

  /**
   * A channel where an event is labeled with a message (fuction).
   *
   * @param domElemIdCombies
   * @tparam T
   * @return
   */
  def apply[T](domElemIdCombies: (String, T)*): Channel[T] = {
    val instance = new Channel[T]
    // An assignment to an instance of a Scala class calls the update method.
    domElemIdCombies.foreach { case (domElemId, keyCode) => jQuery(domElemId).click({ () => instance() = keyCode }) }
    instance
  }
}

