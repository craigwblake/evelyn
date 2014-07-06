package evelyn

import Orientation._
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.jquery._
import scala.scalajs.js
import scala.scalajs.js._
import scala.concurrent.Future
import scala.concurrent.Future._
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.util._

object Canvas extends JSApp {

	case class State (arrow: Image, orientation: Orientation, arrowCoords: (Int, Int), targetCoords: (Int, Int))
	case class Context (button: JQuery, textarea: JQuery, sound: Audio, c: CanvasRenderingContext2D)

	def main: Unit = {
		val canvas = document.getElementById("board").asInstanceOf[HTMLCanvasElement]
		val button = jQuery("#submit")
		val textarea = jQuery("#script")
		val sound = document.getElementById("sound").asInstanceOf[Audio]

		implicit val c = Context(button, textarea, sound, canvas.getContext("2d"))

		val state = initializeBoard

		afterClick(c.button, state).flatMap(runScriptToCompletion(_))
	}

	def runScriptToCompletion (state: State)(implicit c: Context): Future[State] = {
		println(s"runScriptToCompletion")
		def complete (state: State): Future[State] = {
			if (state.arrowCoords == state.targetCoords) {
				c.sound.play
				dom.alert("You win!")
			} else dom.alert("Try again!")

			println(s"clearing board for new game")
			clearBoard
			delay(state).flatMap(_ => again)
		}

		delay(state).flatMap(runScript(c.textarea.value.toString.lines.toList, _)).flatMap(complete(_))
	}

	def again (implicit c: Context): Future[State] = {
		println(s"playing again")
		val state = initializeBoard
		c.textarea.value("")
		c.textarea.focus()
		c.button.off("click")

		afterClick(c.button, state).flatMap(runScriptToCompletion(_))
	}

	def runLine (line: String, state: State)(implicit c: Context): Future[State] = runScript(line :: Nil, state)

	def runScript (lines: List[String], state: State)(implicit c: Context): Future[State] = lines match {

		case Nil =>
			successful(state)

		case x :: xs =>
			println(s"executing: $x")
			lines.head.trim match {

				case "" =>
					runScript(xs, state)

				case r"(\d+)$number times {" =>
					val iterations = number.toInt
					val block = lines.drop(1).takeWhile(_ != "}")
					val rest = lines.drop(block.size + 2)

					val unrolled = (1 to iterations).foldLeft(List.empty[String]) { (x: List[String], y: Int) => x ++ block}
					val computation = unrolled.foldLeft(successful(state)) { (x: Future[State], y: String) => x.flatMap(runLine(y, _)) }
					computation.flatMap(runScript(rest, _))

				case "clockwise" =>
					val orientation = state.orientation match {
						case North => East
						case East => South
						case South => West
						case West => North
					}
					val next = State(state.arrow, orientation, state.arrowCoords, state.targetCoords)
					drawBoard(next)
					delay(next).flatMap(runScript(xs, _))

				case "counter-clockwise" =>
					val orientation = state.orientation match {
						case North => West
						case West => South
						case South => East
						case East => North
					}
					val next = State(state.arrow, orientation, state.arrowCoords, state.targetCoords)
					drawBoard(next)
					delay(next).flatMap(runScript(xs, _))

				case "forward" =>
					val arrowCoords = state.orientation match {
						case North => (state.arrowCoords._1, Math.max(state.arrowCoords._2 - 1, 0))
						case East => (Math.min(state.arrowCoords._1 + 1, 10), state.arrowCoords._2)
						case South => (state.arrowCoords._1, Math.min(state.arrowCoords._2 + 1, 10))
						case West => (Math.max(state.arrowCoords._1 - 1, 0), state.arrowCoords._2)
					}
					val next = State(state.arrow, state.orientation, arrowCoords, state.targetCoords)
					drawBoard(next)
					delay(next).flatMap(runScript(xs, _))
			}
	}

	def initializeBoard (implicit c: Context): State = {
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
		val state = State(arrow, orientation, startCoords, targetCoords)
		arrow.src = "assets/images/arrow.png"
		arrow.onload = () => { drawBoard(state) }
		state
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

	def clearBoard (implicit c: Context): Unit = {
		c.c.save()
		c.c.clearRect(0, 0, 800, 800)
		c.c.restore()
	}

	def drawBoard (state: State)(implicit c: Context): Unit = {
		clearBoard

		val p = 50
		c.c.save()
		c.c.beginPath()
		c.c.strokeStyle = "black"
		for (i <- 0 to 700 by 70) {
			c.c.moveTo(i + p, p)
			c.c.lineTo(i + p, p + 700)
		}
		for (i <- 0 to 700 by 70) {
			c.c.moveTo(p, i + p)
			c.c.lineTo(p + 700, i + p)
		}
		c.c.stroke()
		c.c.restore()
		drawTarget(state.targetCoords._1 * 70, state.targetCoords._2 * 70)
		drawArrow(state.arrowCoords._1 * 70, state.arrowCoords._2 * 70, state.arrow, state.orientation)
	}

	def drawTarget (x: Int, y: Int)(implicit c: Context) = {
		c.c.save()
		c.c.beginPath()
		c.c.rect(x - 20, y - 20, 70, 70)
		c.c.fill()
		c.c.restore()
	}

	def drawArrow (x: Int, y: Int, arrow: Image, orientation: Orientation)(implicit c: Context) = {
		c.c.save()
		c.c.beginPath()
		c.c.translate(x + 15, y + 15);
		val degrees = orientation match {
			case North => 0
			case East => 90
			case South => 180
			case West => 270
		}
		drawRotated(degrees, () => { c.c.drawImage(arrow, -30, -30, 60, 60) } )
		c.c.restore()
	}

	def drawRotated (degrees: Int, f: () => Unit)(implicit c: Context) = {
		//println(s"drawing at $degrees degrees")
		c.c.rotate(degrees * Math.PI / 180)
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

trait Audio extends js.Object {
	def play (): Unit
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
	def clearRect (x: Int, y: Int, h: Int, w: Int): Unit
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
