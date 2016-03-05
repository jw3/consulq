package wiii

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory._
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling._
import Protocols._

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
  def source(path: String) = Source.single(HttpRequest(uri = path))


  /**
   * List all services defined by the specified Consul host
   * @param consulHostname
   * @param system
   * @param mat
   * @return
   */
  def services(consulHostname: String = "localhost")(implicit system: ActorSystem, mat: ActorMaterializer): Future[Seq[ConsulService]] = {
    val conn = Http().outgoingConnection(consulHostname, 8500)

    // the flows are a work in progress, not entirely sure each is best way to do it.. but working
    // also would be ideal to have the flows exist outside the services function, so they are not recreated each call
    val map = Flow[HttpResponse].mapAsync(1)(r => Unmarshal(r.entity).to[Map[String, Seq[String]]]).map(_.keys)
    val map2 = Flow[Iterable[String]].flatMapConcat(x => Source.fromIterator(() => x.iterator))
    val map3 = Flow[String].flatMapMerge(1, svc => source(s"/v1/catalog/service/$svc").via(conn))
    val map4 = Flow[HttpResponse].mapAsync(1)(r => Unmarshal(r.entity).to[Seq[ConsulService]].map(_.head))

    source("/v1/catalog/services")
    .via(conn)
    .via(map)
    .via(map2)
    .via(map3)
    .via(map4)
    .runWith(Sink.seq)
  }
}
