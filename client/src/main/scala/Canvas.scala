package evelyn

import scala.scalajs.js._
import scala.scalajs.js.Dynamic.{global=>g}

object Demo extends JSApp {

	def main: Unit = {
		g.document.getElementById("message").textContent = "This is a test"
	}

	def square (x: Int): Int = x * x
}
