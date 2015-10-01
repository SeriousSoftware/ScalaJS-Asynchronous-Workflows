package pragmatiCSP

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions

/*
object Timeout {
  val timer = new Timer()
  def timeout(d: Long): Future[Unit] = {
    val p = Promise[Unit]()
    val f = p.future
    val tt = new TimerTask() {
      def run {
        p.success(())
      }
    }
    timer.schedule(tt, d)
    f
  }
}
*/

object TentativeOfferResult extends Enumeration {
  type TentativeOfferResult = Value
  val Retracted, // The promise had already been completed by someone else
  Accepted, // Our offer succeeded; we completed the promise.
  Refused = // Our offer failed; promise is still uncompleted.
    Value
}

import pragmatiCSP.TentativeOfferResult._

/** Promise that might not be fulfilled.
  */
class TentativePromise[T] {
  val p = Promise[T]

  def future: scala.concurrent.Future[T] = p.future

  /** A normal Promise.tryComplete might fail if the promise is already completed; this
    * one can also fail because the lazy offer value returns None.
    * The result is a TentativeOfferResult
    */
  def tryComplete(o: => Option[T]): TentativeOfferResult = this.synchronized {
    if (!p.isCompleted) o match {
      case Some(t) =>
        p.success(t)
        Accepted
      case None =>
        Refused
    }
    else Retracted
  }
}

object TentativePromise {
  def apply[T] = new TentativePromise[T]
}


/** Promise that fulfills tentative promises.
  */
class ReadyPromise[T, U] extends Promise[U] {
  type TP = TentativePromise[T]
  val p = Promise[U]
  val h: scala.collection.mutable.HashMap[TP, TP => Unit] = new scala.collection.mutable.HashMap()

  def future = p.future

  def isCompleted: Boolean = p.isCompleted

  def tryComplete(result: scala.util.Try[U]): Boolean = this.synchronized {
    val ret = if (p.tryComplete(result)) {
      // fires any standard listeners
      // Notify all clients.  Some of the deliveries might fail.
      h.foreach { case (pDeliver, deliverTo) => deliverTo(pDeliver) }
      true
    } else false
    h.clear()
    ret
  }

  /**
   * When this ReadyPromise is complete, attempt to complete
   * the TentativePromise pDeliver by passing it to f.
   */
  def tryDeliver(recipient: TentativePromise[T])(deliverTo: TP => Unit): Unit = this.synchronized {
    if (p.isCompleted) {
      deliverTo(recipient)
    } else {
      h += ((recipient, deliverTo))
      recipient.future.map { _ => this.synchronized {
        h -= recipient
      }
      }
    }
  }
}

object ReadyPromise {
  def apply[T, U] = new ReadyPromise[T, U]()

  def successful[T, U](u: U): ReadyPromise[T, U] = {
    val p = new ReadyPromise[T, U]()
    p.trySuccess(u)
    p
  }
}

sealed trait ChanHolder[T] {
  def chan: Channel[T]
}

class Channel[T](val buffer: ChanBuffer[T]) extends ChanHolder[T] {
  import pragmatiCSP.Channel.CV
  private[this] var pReadyForWrite = ReadyPromise.successful[CV[T], Unit](Unit)
  private[this] var pReadyForRead = ReadyPromise[CV[T], Unit]

  /** Auxiliary constructor
   Binds a continuation  with the "write" in casu update() */
  def this(cont: (T => Unit) => Unit) = {
    this(new NormalBuffer[T](1, dropping = false, shifting = true))
    cont(update)
  }

  /** Channels' "Alt" function for waiting on two events
    * of the same type Y
    */
  def |(other: Channel[T]): Future[T] = {
    val p = Promise[T]()
    for {
      f <- Seq(other(), this())
      t <- f
    } p.trySuccess(t)
    p.future
  }

  def chan = this

  override def toString = s"Chan($buffer) rfw=${pReadyForWrite.isCompleted} rfr=${pReadyForRead.isCompleted}"

  // Extract the value in a chan-value pair, properly cast.  Note we explicitly match
  // CV[Chan.Pretender], because Chan is invariant.
  def unapply(cv: CV[Channel.Pretender]): Option[T] =
    if (cv.c eq this) {
      Some(cv.v.asInstanceOf[T])
    } else None

  /** Return a future that completes on successful write to the channel.
    */
  def update(v: T): Future[Unit] = this.synchronized {
    val p = TentativePromise[CV[T]]
    pReadyForWrite.tryDeliver(p)(tryWrite(v, _))
    p.future.map(_ => Unit)
  }

  def apply[T]() = read

  private[this] def read: Future[T] = this.synchronized {
    val p = TentativePromise[CV[T]]
    pReadyForRead.tryDeliver(p)(tryRead)
    p.future.map(_.v)
  }

  def read(pNotify: TentativePromise[CV[T]]): Unit = this.synchronized {
    pReadyForRead.tryDeliver(pNotify)(tryRead)
  }


  def write(v: T, pNotify: TentativePromise[CV[T]]): Unit = this.synchronized {
    //logger.debug(s"$this write $pNotify")
    pReadyForWrite.tryDeliver(pNotify)(tryWrite(v, _))
  }

  // Only reschedule if we failed to write to the buffer, not if the promise was already completed.
  private[this] def tryWrite(v: T, pClient: TentativePromise[CV[T]]): Unit = this.synchronized {
    //logger.debug(s"tryWrite $this $v $pClient")
    def processPutResult(br: BufferSuccess[T]): CV[T] = {
      if (br.nowFull) pReadyForWrite = ReadyPromise[CV[T], Unit]
      if (br.noLongerEmpty) pReadyForRead.trySuccess(Unit) // must come second
      //logger.debug(s"tryWrite ${this} $br")
      CV(this, v)
    }
    pClient.tryComplete(buffer.put(v).map(processPutResult)) match {
      case Refused =>
        // i.e. someone wrote before we could, and now the buffer is full; reschedule.
        //logger.debug(s"tryWrite ${this} refused $v}")
        pReadyForWrite.tryDeliver(pClient)(tryWrite(v, _))
      case Accepted =>
      // it worked; no need to reschedule
      //logger.debug(s"tryWrite ${this.name} accepted $v")
      case Retracted =>
      // the client no longer wishes to put; no need to reschedule
      //logger.debug(s"tryWrite ${this.name} retracted $v")
    }
  }

  private[this] def tryRead(pClient: TentativePromise[CV[T]]): Unit = this.synchronized {
    //logger.debug(s"tryRead $this $pClient")
    def processTakeResult(br: BufferSuccess[T]): CV[T] = {
      if (br.nowEmpty) pReadyForRead = ReadyPromise[CV[T], Unit]
      if (br.noLongerFull) pReadyForWrite.trySuccess(Unit) // must come second
      //logger.debug(s"tryRead $this $br")
      CV(this, br.v)
    }
    pClient.tryComplete(buffer.get.map(processTakeResult)) match {
      case Refused =>
        //logger.debug(s"tryRead ${this.name} refused")
        pReadyForRead.tryDeliver(pClient)(tryRead)
      //pReadyForRead.future map {_ => tryRead(pClient)}
      case Accepted =>
      //logger.debug(s"tryRead ${this.name} accepted")
      case Retracted =>
      //logger.debug(s"tryRead ${this.name} retracted")
    }
  }

}

object Channel {
  type Pretender

  //implicit def toFuture[T](c: Channel[T]): Future[T] = c.apply[T]()

  case class CV[T](val c: Channel[T], val v: T) extends ChanHolder[T] {
    def chan = c
  }


  def apply[T](name: String) = new Channel[T](new NormalBuffer[T](1, dropping = false, shifting = true))

  def apply[T] = new Channel[T](new NormalBuffer[T](1, dropping = false, shifting = true))

  /*  def timeout[T](d: Long, v: T, name: String): Chan[T] = {
      val c = Chan[T](name)
      //logger.debug(s"Creating timeout channel ${d}")
      Timeout.timeout(d) flatMap {logger.debug(s"Timeout $name fired"); _ => c.write(v) }
      c
    }
    def timeout(m: Long, name: String) = timeout[String](m, name, name)
    def timeout(m: Long) : Chan[Unit] = timeout[Unit](m,Unit,"timeout" + UUID.randomUUID.toString())
  */

  //  def apply[T](n: Int) = new Channel[T](new NormalBuffer(n, false, false))

  def alts(cs: ChanHolder[Pretender]*): Future[CV[Pretender]] = {
    val p = TentativePromise[CV[Pretender]]
    cs.foreach {
      _ match {
        case c: Channel[Pretender] => c.chan.read(p)
        case CV(c, v) => c.chan.write(v, p)
      }
    }
    p.future
  }

//  implicit def ghastly[T](c: Channel[T]): Channel[Pretender] = c.asInstanceOf[Channel[Pretender]]
//  implicit def ghastly2[T](p: Promise[CV[T]]) = p.asInstanceOf[Promise[Unit]]

}
