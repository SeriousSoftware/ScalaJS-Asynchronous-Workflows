package pragmatiCSP

case class BufferSuccess[T](v: T,
                            noLongerEmpty: Boolean = false,
                            noLongerFull: Boolean = false,
                            nowEmpty: Boolean = false,
                            nowFull: Boolean = false)

// The only thing exciting about a ChanBuffer is that you pass its put/take methods
// a promise to fulfill should that operation render the buffer no longer empty/full.
abstract class ChanBuffer[T]() {
  def put(v: T): Option[BufferSuccess[T]]

  def get: Option[BufferSuccess[T]]
}

class NormalBuffer[T](bufSize: Int, dropping: Boolean, shifting: Boolean) extends ChanBuffer[T] {
  val b = scala.collection.mutable.Buffer.empty[T]

  def put(v: T): Option[BufferSuccess[T]] = this.synchronized {
    val s = b.size
    val noLongerEmpty = s == 0
    val nowFull = s == bufSize - 1
    if (s >= bufSize) {
      // Buffer full
      if (dropping || shifting)
        if (shifting) {
          b.remove(0)
          b += v
        } else b.update(bufSize - 1, v)
      else return None
    } else b += v
    Some(BufferSuccess(v, noLongerEmpty = noLongerEmpty, nowFull = nowFull))
  }

  def get: Option[BufferSuccess[T]] = this.synchronized {
    val s = b.size
    var noLongerFull = false
    var nowEmpty = false
    if (s > 0) {
      if (s == bufSize) {
        noLongerFull = true
        if (s == 1) {
          nowEmpty = true
        }
      }
      Some(BufferSuccess(b.remove(0), nowEmpty = nowEmpty, noLongerFull = noLongerFull))
    } else {
      None
    }
  }
}
