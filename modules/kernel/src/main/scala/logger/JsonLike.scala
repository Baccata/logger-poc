package logger

import scala.concurrent.duration.FiniteDuration

/** A visitor-like construct that allows for capturing contextual values of
  * several types, without enforcing an in-memory representation or a third-party dependency.
  */
trait JsonLike {

  type J

  def nul: J
  def bool(value: Boolean): J
  def int(value: Int): J
  def short(value: Int): J
  def long(value: Int): J
  def double(value: Double): J
  def timestamp(ts: FiniteDuration): J
  def string(value: String): J
  def obj(bindings: (String, J)*): J
  def arr(elems: J*): J
}

object JsonLike {
  type Aux[Json] = JsonLike {
    type J = Json
  }
}
