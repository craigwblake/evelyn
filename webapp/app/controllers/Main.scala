package controllers

import akka.actor._
import evelyn._
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Action._
import play.api.mvc.WebSocket.FrameFormatter.jsonFrame
import play.api.libs.concurrent.Akka._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.Logger
import scala.collection.mutable.HashSet


case class ClientCreated(in: Iteratee[String, _], out: Enumerator[JsValue])

object Main extends Controller {

	def index = Action { Ok(views.html.index()) }

	def bridge = WebSocket.acceptWithActor { request => out => Props(classOf[Client], out) }
}

class Client (out: ActorRef) extends Actor {

	def receive = {
		case x =>
			Logger.info(s"received $x")
			// compile and run program
	}
}
