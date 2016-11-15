package pragmatiCSP

import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

trait Source[T] {
  protected var promise: Promise[T] = Promise[T]()

  /** Channels' "Write" or "put" function, called by an assigned to the instance(). */
  def update(t: T): Unit = if (!promise.isCompleted) promise.success(t)
}

trait Sink[T] extends Source[T] {

  /** Channels' "Read", "take" or "get" function, called by referring to the instance(). */
  def apply(): Future[T] = {
    promise = Promise[T]()
    promise.future
  }

  /** Channels' "Alt" function for waiting on two events */
  def ||(other: Channel[T]): Future[T] = {
    val p = Promise[T]()
    for {
      f <- Seq(other(), this ())
      t <- f
    } p.trySuccess(t)
    p.future
  }
}

/**
 * Single item channel primitive
 *
 * A channel that publishes any incoming events to all listeners,
 * dropping any events that come when nobody is listening.
 *
 * @tparam T Type of the message
 */
class Channel[T]() extends Source[T] with Sink[T] {

  /** Auxiliary constructor
   * Binds a continuation  with the "write" in casu update()
   *
   * @param cont Continuation
   */
  def this(cont: (T => Unit) => Unit) {
    this
    cont(update)
  }

  def filter(p: (T) => Boolean): Future[T] = {
    apply().flatMap(value => if (p(value)) Future(value) else filter(p))
  }

}
