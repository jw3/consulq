package consulq

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import consulq.ConsulServiceProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
 * Query the Consul service capturing the service info and converting it to [[ConsulService]]
 *
 * There is no filtering available here due to the invconvenient filtering in Consul.
 *
 * Basically just capture all services, and then filtering can be performed on the bean list
 * afterwards.  This could be modified to prevent some unmarshalling later, but its not that
 * great of worry at this point.
 *
 */
object ConsulQuery {
  type Connection = Flow[HttpRequest, HttpResponse, Future[OutgoingConnection]]
  val apiver = "v1"

  def apply()(implicit system: ActorSystem, mat: ActorMaterializer): ConsulQuery = {
    new ConsulQuery("localhost")
  }
}

class ConsulQuery(consulhost: String)(implicit system: ActorSystem, mat: ActorMaterializer) {
  import ConsulQuery._

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
               // hit the server again and resolve additional service info
               // todo; validate this nested source is correct pattern
               .flatMapMerge(1, svc => source(s"/$apiver/catalog/service/$svc").via(conn))
               .mapAsync(1)(r => Unmarshal(r.entity).to[Seq[ConsulService]].map(_.head))

    source(s"/$apiver/catalog/services").via(conn).via(flow)
  }

  private def source(path: String) = Source.single(HttpRequest(uri = path))
  private def connection() = Http().outgoingConnection(consulhost, 8500)
}
