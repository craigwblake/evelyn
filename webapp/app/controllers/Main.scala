package controllers

import akka.actor._
import akka.util.Timeout
import evelyn._
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Action._
import play.api.libs.concurrent.Akka._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import play.api.libs.json._
import scala.collection.parallel.mutable.ParHashSet
import scala.concurrent.duration._

case class ClientCreated(in: Iteratee[Update, _], out: Enumerator[Update])

object Main extends Controller {

	implicit val timeout = Timeout(1 second)

	val master = system.actorOf(Props[Master])

	def demo = Action { Ok(views.html.demo()) }

	def bridge = WebSocket.using[JsValue] { request =>
		(master ? Create) map { case ClientCreated(in, out) => (in, out) }
	}
}

class Client extends Actor {

	val out = Enumerator.imperative[JsValue]()

	val in = Iteratee.foreach[JsValue] { context.parent ! _ } mapDone { _ => context.parent ! Quit }

	def receive = {
		case update: Update => out push update
	}
}

class Master extends Actor {

	val clients = ParHashSet[Client].empty
	val state = new AtomicLongArray(10240)
	val lookup: Map[String, Int] = Map.empty

	def receive = {

		case RegisterMapping(name, id) =>
			Logger.debug(s"registered variable mapping for slot $id: $name")
			lookup += (name, id)

		case u: Update =>
			Logger.info(s"received update to property ${u.name} with new value ${u.value}")
			lookup(u.name) map { id =>
				state.set(id, u.value)
				clients map (_ ! u)
			}

		case Create =>
			Logger.info("received websocket connection, creating client actor")
			val client = context.actorOf(Props[Client])

			clients.synchronized {
				clients += client
			}

			sender ! ClientCreated(client.in, client.out)

		case Quit =>
			Logger.info("received quit, destroying child")

			clients.synchronized {
				clients -= sender
			}
	}
}
