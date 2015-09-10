package ss000101

import org.scalajs.dom.{MouseEvent, document}
import org.scalajs.dom.ext.KeyCode.{Left, Right}
import org.scalajs.jquery.{JQueryEventObject, jQuery}

import scala.async.Async.{async, await}
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport

@JSExport
object AsyncWorkflow {
  val (f1, f2, f3a, f3b, f4, f5, f6, f7, f8, f9, f10s, f10, f11, f12) =
    (LiChan[Null](), LiChan[Null](), LiChan[Null](), LiChan[Null](), LiChan[Null](), LiChan[Null](), LiChan[Null](),
      LiChan[Null](), LiChan[Null](), LiChan[Char](), LiChan[Null](), LiChan[Char](), LiChan[Null](), LiChan[Null]())

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
      show(display,s"${list(idx)}", append = false)
    } else {
      jQuery(buttonPrev).addClass("disabled")
      jQuery(buttonNext).addClass("disabled")
    }
  }

  def show(display: String, message: String, append: Boolean = true): Unit = {
    val item = jQuery(display)
    if (append) item.append(s"<p>$message</p>")
    else item.html(s"<p>$message</p>")
  }

  // Example 1
  async {
    val showHooked= show("div#ex1-messages", _: String, _: Boolean )
    showHooked("Waiting for a click …",  false)
    await(f1())
    showHooked( "Got a click!", true)
  }

  // Example 2
  async {
    def showHooked = show("div#ex2-messages", _:String, _:Boolean)
    showHooked( "Waiting for a click …",  false)
    await(f2())
    showHooked( "Got a click!", true)
    showHooked( "Waiting for another click …", true)
    await(f2())
    showHooked( "Done!", true)
  }

  // Example 3
  async {
    def showHooked = show("div#ex3-messages", _:String, _:Boolean)
    showHooked("Waiting for a click from Button A …",  false)
    await(f3a())
    showHooked( "Got a click!", true)
    showHooked( "Waiting for a click from Button B …",true)
    await(f3b())
    showHooked( "Done!", true)
  }

  // Example 4
  // Example 5
  // Example 6

  async {
    def showHooked = show("div#ex6-messages", _:String, _:Boolean)
    showHooked("Click button to start tracking the mouse!",  false)
    await(f6())
    jQuery("button#ex6-button").text("Stop")

    val mousemove = ComplexChan[MouseEvent](document.onmousemove = _)
    var running = true
    async {
      while (running) {
        val start = await(mousemove())
        showHooked( s"[${start.clientX}, ${start.clientY}]", true)
      }
      showHooked( "Done!", true)
    }
    await(f6())
    running = false

    jQuery("button#ex6-button").text("Done!")
    jQuery("button#ex6-button").addClass("disabled")

  }

  // Example 7
  async {
    def showHooked = show("div#ex7-messages", _:String, _:Boolean)
    showHooked("Click button to start tracking the mouse!", false)
    await(f7())
    jQuery("button#ex7-button").text("Stop")

    val mousemove = ComplexChan[MouseEvent](document.onmousemove = _)
    var running = true
    async {
      while (running) {
        //       mousemove().withFilter { case move => (move.clientY % 5 == 0) }

        val start = await(mousemove().withFilter(_ => true /* _.clientY % 5 == 0*/))
        showHooked( s"[${start.clientX}, ${start.clientY}]",true)
      }
      showHooked("Done!",true)
    }
    await(f7())
    running = false

    jQuery("button#ex7-button").text("Done!")
    jQuery("button#ex7-button").addClass("disabled")
  }

  // Example 8
  async {
    def showHooked = show("div#ex8-messages", _:String, _:Boolean)
    showHooked("Click the button ten times!", false)
    var i = 0
    while (i < 10) {
      await(f8())
      i += 1
      showHooked( s"$i clicks!", true)
    }
    showHooked( "Done!", true)
  }

  // Example 9
  async {
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

  def list = Vector("aardvark", "beetle", "cat", "dog", "elk", "ferret", "goose", "hippo", "ibis", "jellyfish", "kangaroo")

  // Example 10
  {
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
      show("button#ex10-button-start-stop","Stop !",false)

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
}

case class LiChan[T]() {
  private[this] var value: Promise[T] = _

  def update(t: T) {
    if (!(value == null || value.isCompleted)) value.success(t)
  }

  def apply(): Future[T] = {
    value = Promise[T]()
    value.future
  }

  def |(other: LiChan[T]): Future[T] = {
    val p = Promise[T]()
    for {
      f <- Seq(other(), this())
      t <- f
    } p.trySuccess(t)
    p.future
  }

}

case class ComplexChan[T](init: (T => Unit) => Unit) {
  init(update)
  private[this] var value: Promise[T] = null

  def apply(): Future[T] = {
    value = Promise[T]()
    value.future
  }

  def update(t: T): Unit = {
    if (value != null && !value.isCompleted) value.success(t)
  }

}