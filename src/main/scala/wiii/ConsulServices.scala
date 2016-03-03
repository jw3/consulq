package wiii

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{Uri, HttpRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory._
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.concurrent.Future

/**
 * {
 * "Node": "foobar",
 * "Address": "10.1.10.12",
 * "ServiceID": "redis",
 * "ServiceName": "redis",
 * "ServiceTags": null,
 * "ServiceAddress": "",
 * "ServicePort": 8000
 * }
 */
object ConsulServices {
  val cfgflow = Flow.fromFunction((s: ByteString) => ConfigFactory.parseString(s.utf8String))

  def services(server: Uri = "http://localhost")(implicit sys: ActorSystem, mat: ActorMaterializer): Future[Config] = {
    import sys.dispatcher

    Http()
    .singleRequest(HttpRequest(uri = s"$server:8500/v1/catalog/services"))
    .flatMap(r => r.entity.dataBytes.map(b => parseString(b.utf8String)).runWith(Sink.head))
  }

  object Keys {
    val node = "Node"
    val address = "Address"
    val serviceID = "ServiceID"
    val serviceName = "ServiceName"
    val serviceTags = "ServiceTags"
    val serviceAddr = "ServiceAddress"
    val servicePort = "ServicePort"
  }
}
