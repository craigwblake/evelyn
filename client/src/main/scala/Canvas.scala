package evelyn

import org.scalajs.dom
import org.scalajs.dom.document
import scala.scalajs.js
import scala.scalajs.js._

object Canvas extends JSApp {

	object Orientation extends Enumeration {
		type Orientation = Value
		val North, South, East, West = Value
	}

	def main: Unit = {
		var canvas = document.getElementById("board").asInstanceOf[HTMLCanvasElement]
		var context = canvas.getContext("2d")

		val p = 50

		context.strokeStyle = "#000000"
		for (i <- 0 to 700 by 70) {
			context.moveTo(i + p, p)
			context.lineTo(i + p, p + 700)
		}
		for (i <- 0 to 700 by 70) {
			context.moveTo(p, i + p)
			context.lineTo(p + 700, i + p)
		}

		val start = Math.round(Math.random * 90)
		val orientation = Orientation(Math.round(Math.random * 3).toInt)
		println(s"start: $start")
		println(s"orientation: $orientation")
		val x = Math.round(start / 10) * 70
		val y = Math.round(start % 10) * 70

		val image = new Image
		image.onload = () => { context.drawImage(image, x + 55, y + 55, 60, 60) }
		image.src = "assets/images/arrow.png"

		context.stroke()
	}
}

class Image extends js.Object {
	var src: String = ???
	var onload: js.Function0[Unit] = ???
}

trait HTMLCanvasElement extends js.Object {
	def getContext (kind: String): CanvasRenderingContext2D
}

trait CanvasRenderingContext2D extends js.Object {
	val canvas: HTMLCanvasElement

	var fillStyle: String
	var lineWidth: Double
	var strokeStyle: String

	def fillRect (x: Double, y: Double, w: Double, h: Double): Unit
	def strokeRect (x: Double, y: Double, w: Double, h: Double): Unit
	def beginPath (): Unit
	def fill (): Unit
	def stroke (): Unit
	def closePath (): Unit
	def moveTo (x: Double, y: Double): Unit
	def lineTo (x: Double, y: Double): Unit
	def drawImage (image: Image, x: Double, y: Double, w: Double, h: Double)

	def arc (x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean): Unit
}
