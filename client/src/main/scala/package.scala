import org.scalajs.dom
import org.scalajs.jquery._
import scala.scalajs.js._
import scala.concurrent.{Promise, Future}
import scala.util.matching.Regex

package object evelyn {

	object Orientation extends Enumeration {
		type Orientation = Value
		val North, South, East, West = Value
	}
	
	implicit class RegexContext (sc: StringContext) {
		def r = new Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
	}

	def afterClick [T] (element: JQuery, t: T): Future[T] = {
		val promise = Promise[T]
		element.click(() => promise.success(t))
		promise.future
	}

	def delay [T] (t: T): Future[T] = {
		val promise = Promise[T]
		dom.setTimeout(() => promise.success(t), 1000)
		promise.future
	}
}
