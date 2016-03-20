package consulq

import akka.{NotUsed, Done}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.OutgoingConnection
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpMethods, StatusCodes, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
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
    ConsulQuery("localhost")
  }

  def apply(host: String)(implicit system: ActorSystem, mat: ActorMaterializer): ConsulQuery = {
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

  def getKV(): Future[Seq[ConsulKV]] = {
    import ConsulKvProtocol._
    val flow = Flow[HttpResponse]
               .mapAsync(1) {
                 case HttpResponse(StatusCodes.NotFound, _, _, _) => Future.successful(Seq())
                 case r => Unmarshal(r.entity).to[Seq[ConsulKV]]
               }

    source(s"/$apiver/kv/?recurse")(identity).via(connection()).via(flow).runWith(Sink.head)
  }

  def getKV(k: String): Future[Option[ConsulKV]] = {
    import ConsulKvProtocol._
    val flow = Flow[HttpResponse]
               .mapAsync(1) {
                 case HttpResponse(StatusCodes.NotFound, _, _, _) => Future.successful(None)
                 case r => Unmarshal(r.entity).to[Seq[ConsulKV]].map {
                   case s if s.nonEmpty => Option(s.head)
                   case _ => None
                 }
               }

    source(s"/$apiver/kv/$k")(identity).via(connection()).via(flow).runWith(Sink.head)
  }

  def putKV(k: String, v: String): Future[Done] = {
    source(s"/$apiver/kv/$k") { r =>
      r.withEntity(v).withMethod(HttpMethods.PUT)
    }.via(connection())
    .runWith(Sink.ignore)
  }

  def deleteKV(k: String): Future[Done] = {
    source(s"/$apiver/kv/$k") { r =>
      r.withMethod(HttpMethods.DELETE)
    }.via(connection())
    .runWith(Sink.ignore)
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
               .flatMapMerge(1, svc => source(s"/$apiver/catalog/service/$svc")(identity).via(conn))
               .mapAsync(1)(r => Unmarshal(r.entity).to[Seq[ConsulService]].map(_.head))

    source(s"/$apiver/catalog/services")(identity).via(conn).via(flow)
  }

  private def source(path: String)(builder: RequestBuilder): Source[HttpRequest, _] = {
    Source.single(builder(HttpRequest(uri = path)))
  }
  private def connection() = Http().outgoingConnection(consulhost, 8500)
}
