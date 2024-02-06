package logger

import scala.concurrent.duration.FiniteDuration

/** A value that can be written into a json-like construct, provided a visitor.
  */
trait Context {
  def capture[J](jsonLike: JsonLike.Aux[J]): J
}

object Context {

  trait Encoder[A] {
    def encode[J](json: JsonLike.Aux[J], a: A): J
  }
  object Encoder {
    def apply[A](implicit ev: Encoder[A]): ev.type = ev

    implicit val stringEncoder: Encoder[String] = new Encoder[String] {
      def encode[J](json: JsonLike.Aux[J], a: String) = json.string(a)
    }

    implicit val intEncoder: Encoder[Int] = new Encoder[Int] {
      def encode[J](json: JsonLike.Aux[J], a: Int) = json.int(a)
    }

    implicit val timestampEncoder: Encoder[FiniteDuration] =
      new Encoder[FiniteDuration] {
        def encode[J](json: JsonLike.Aux[J], a: FiniteDuration) =
          json.timestamp(a)
      }
  }

  implicit def toContext[A: Encoder](a: A): Context =
    DeferredRecord(a, Encoder[A])

  private case class DeferredRecord[A](a: A, encoder: Encoder[A])
      extends Context {
    def capture[J](jsonLike: JsonLike.Aux[J]): J = encoder.encode(jsonLike, a)
  }
}
