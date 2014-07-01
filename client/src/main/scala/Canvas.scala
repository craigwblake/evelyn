package evelyn

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.jquery._
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
		implicit var c = canvas.getContext("2d")

		val (arrow, orientation, startCoords, targetCoords) = initializeBoard

		val button = jQuery("#submit")
		val textarea = jQuery("#script")

		def handler = () => { runScript(textarea, arrow, orientation, startCoords, targetCoords) }

		button.click { handler _ }
	}

	def runScript (textarea: JQuery, arrow: Image, orientation: Orientation, startCoords: (Int, Int), targetCoords: (Int, Int))(implicit c: CanvasRenderingContext2D) = {
		val script = textarea.text
		println(s"script $script")
	}


	def initializeBoard (implicit c: CanvasRenderingContext2D): (Image, Orientation, (Int, Int), (Int, Int)) = {

		val orientation = Orientation(Math.round(Math.random * 3).toInt)
		println(s"orientation: $orientation")

		val target = Math.round(Math.random * 90)
		val targetx = (Math.round(target / 10) * 70).toInt + 70
		val targety = (Math.round(target % 10) * 70).toInt + 70
		val targetCoords = (targetx / 70, targety / 70)
		println(s"target x=${targetCoords._1} y=${targetCoords._2}")

		val startCoords = chooseArrow(targetCoords)
		println(s"start x=${startCoords._1} y=${startCoords._2}")

		val arrow = new Image
		arrow.src = "assets/images/arrow.png"
		arrow.onload = () => { drawBoard(arrow, orientation, startCoords, targetCoords) }
		(arrow, orientation, startCoords, targetCoords)
	}

	def chooseArrow (targetCoords: (Int, Int)): (Int, Int) = {
		val start = Math.round(Math.random * 90)
		val startx = (Math.round(start / 10) * 70).toInt + 70
		val starty = (Math.round(start % 10) * 70).toInt + 70
		val startCoords = (startx / 70, starty / 70)
		if (startCoords == targetCoords) {
			println(s"oops, target and arrow overlap, rechoosing")
			return chooseArrow(targetCoords)
		} else return startCoords
	}

	def drawBoard (arrow: Image, orientation: Orientation, arrowCoords: (Int, Int), targetCoords: (Int, Int))(implicit c: CanvasRenderingContext2D): Unit = {
		val p = 50

		c.save()
		c.beginPath()
		c.strokeStyle = "black"
		for (i <- 0 to 700 by 70) {
			c.moveTo(i + p, p)
			c.lineTo(i + p, p + 700)
		}
		for (i <- 0 to 700 by 70) {
			c.moveTo(p, i + p)
			c.lineTo(p + 700, i + p)
		}
		c.stroke()
		c.restore()
		drawTarget(targetCoords._1 * 70, targetCoords._2 * 70)
		drawArrow(arrowCoords._1 * 70, arrowCoords._2 * 70, arrow, orientation)
	}

	def drawTarget (x: Int, y: Int)(implicit c: CanvasRenderingContext2D) = {
		c.save()
		c.beginPath()
		c.rect(x - 20, y - 20, 70, 70)
		c.fill()
		c.restore()
	}

	def drawArrow (x: Int, y: Int, arrow: Image, orientation: Orientation)(implicit c: CanvasRenderingContext2D) = {
		c.save()
		c.beginPath()
		c.translate(x + 15, y + 15);
		val degrees = orientation match {
			case North => 0
			case East => 90
			case South => 180
			case West => 270
		}
		drawRotated(degrees, () => { c.drawImage(arrow, -30, -30, 60, 60) } )
		c.restore()
	}

	def drawRotated (degrees: Int, f: () => Unit)(implicit c: CanvasRenderingContext2D) = {
		println(s"drawing at $degrees degrees")
		c.rotate(degrees * Math.PI / 180)
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
