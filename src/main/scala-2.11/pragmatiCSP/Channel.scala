package pragmatiCSP

import scala.async.Async._
import scala.concurrent.{Future, Promise}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

class InternChan[T]() {
  private var promise: Promise[T] = null

  /** Channels' "Write" or "put" function, called by an assigned to the instance(). */
  def update(t: T): Unit = if (promise != null && !promise.isCompleted) promise.success(t)

  /** Channels' "Read" or "get" function, called by referring to the instance(). */
  def apply(): Future[T] = {
    promise = Promise[T]()
    promise.future
  }
}

/**
 * Single item channel primitive
 *
 * @tparam T
 */
class Channel[T]() {
  private val block = new InternChan[Null]()
  private var promise: Promise[T] = Promise[T]()

  /** Auxiliary constructor
    * Binds a continuation  with the "write" in casu update()
    * @param cont
    */
  def this(cont: (T => Unit) => Unit) {
    this
    cont(update)
  }

  /** Channels' "Write" or "put" function, called by an assigned to the instance(). */
  def update(t: T): Unit = async {
      //await(block())
      if (!promise.isCompleted) promise.success(t)
  }

  def filter(p: (T) => Boolean): Future[T] = {
    apply().flatMap(value => if (p(value)) Future(value) else filter(p))
  }

  /** Channels' "Read" or "get" function, called by referring to the instance(). */
  def apply(): Future[T] = {
    promise = Promise[T]()
    block()=null
    promise.future
  }

  /** Channels' "Alt" function for waiting on two events */
  def |(other: Channel[T]): Future[T] = {
    val p = Promise[T]()
    for {
      f <- Seq(other(), this())
      t <- f
    } p.trySuccess(t)
    p.future
  }
}