package wiii

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import wiii.ConsulServiceProtocols._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object ConsulServices {
  type Connection = Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]]
  val apiver = "v1"

  def apply()(implicit system: ActorSystem, mat: ActorMaterializer): ConsulServices = {
    new ConsulServices("localhost")
  }
}

class ConsulServices(consulhost: String)(implicit system: ActorSystem, mat: ActorMaterializer) {
  import ConsulServices._

  /**
   * Execute service query against Consul, returning all registered [[ConsulService]]
   *
   * @return Sequence of ConsulServices
   */
  def services(): Future[Seq[ConsulService]] = {
    query(connection()).runWith(Sink.seq)
  }


  /**
   * Query Consul for all services using /v1/catalog/services
   *
   * The keys are the service names, and the array values provide all known tags for a given service.
   *
   * Example Response:
   * {
   * "consul": [],
   * "redis": [],
   * "postgresql": [
   * "master",
   * "slave"
   * ]
   * }
   *
   * @param conn http connection
   * @return
   */
  private def query(conn: Connection) = {
    val flow = Flow[HttpResponse]
               .mapAsync(1)(r => Unmarshal(r.entity).to[Map[String, Seq[String]]])
               .map(_.keys)
               .flatMapConcat(x => Source.fromIterator(() => x.iterator))
               .flatMapMerge(1, svc => source(s"/$apiver/catalog/service/$svc").via(conn))
               .mapAsync(1)(r => Unmarshal(r.entity).to[Seq[ConsulService]].map(_.head))

    source(s"/$apiver/catalog/services").via(conn).via(flow)
  }

  private def source(path: String) = Source.single(HttpRequest(uri = path))
  private def connection() = Http().outgoingConnection(consulhost, 8500)
}
