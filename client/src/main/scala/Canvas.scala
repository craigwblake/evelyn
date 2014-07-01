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
	import Orientation._

	def main: Unit = {
		var canvas = document.getElementById("board").asInstanceOf[HTMLCanvasElement]
		implicit var context = canvas.getContext("2d")

		val p = 50

		context.save()
		context.beginPath()
		context.strokeStyle = "black"
		for (i <- 0 to 700 by 70) {
			context.moveTo(i + p, p)
			context.lineTo(i + p, p + 700)
		}
		for (i <- 0 to 700 by 70) {
			context.moveTo(p, i + p)
			context.lineTo(p + 700, i + p)
		}
		context.stroke()
		context.restore()

		val start = Math.round(Math.random * 90)
		val target = Math.round(Math.random * 90)
		val orientation = Orientation(Math.round(Math.random * 3).toInt)
		println(s"orientation: $orientation")

		val startx = (Math.round(start / 10) * 70).toInt + 70
		val starty = (Math.round(start % 10) * 70).toInt + 70
		val startCoords = (startx / 70, starty / 70)
		println(s"start x=${startCoords._1} y=${startCoords._2}")

		val targetx = (Math.round(target / 10) * 70).toInt + 70
		val targety = (Math.round(target % 10) * 70).toInt + 70
		val targetCoords = (targetx / 70, targety / 70)
		println(s"target x=${targetCoords._1} y=${targetCoords._2}")

		drawTarget(targetx, targety)

		val arrow = new Image
		arrow.onload = () => { drawArrow(startx, starty, arrow, orientation) }
		arrow.src = "assets/images/arrow.png"
	}

	def drawTarget (x: Int, y: Int)(implicit context: CanvasRenderingContext2D) = {
		context.save()
		context.beginPath()
		context.rect(x - 20, y - 20, 70, 70)
		context.fill()
		context.restore()
	}

	def drawArrow (x: Int, y: Int, arrow: Image, orientation: Orientation)(implicit context: CanvasRenderingContext2D) = {
		context.save()
		context.beginPath()
		context.translate(x + 15, y + 15);
		val degrees = orientation match {
			case North => 0
			case East => 90
			case South => 180
			case West => 270
		}
		drawRotated(degrees, () => { context.drawImage(arrow, -30, -30, 60, 60) } )
		context.restore()
	}

	def drawRotated (degrees: Int, f: () => Unit)(implicit context: CanvasRenderingContext2D) = {
		println(s"drawing at $degrees degrees")
		context.rotate(degrees * Math.PI / 180)
		f()
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
	def restore (): Unit
	def save (): Unit
	def fill (): Unit
	def stroke (): Unit
	def rect (x: Int, y: Int, h: Int, w: Int): Unit
	def closePath (): Unit
	def translate (x: Int, y: Int): Unit
	def rotate (degrees: Double): Unit
	def moveTo (x: Double, y: Double): Unit
	def lineTo (x: Double, y: Double): Unit
	def drawImage (image: Image, x: Double, y: Double, w: Double, h: Double)

	def arc (x: Double, y: Double, radius: Double, startAngle: Double, endAngle: Double, anticlockwise: Boolean): Unit
}
