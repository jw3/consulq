package wiii

import spray.json._


case class ConsulService(name: String, address: String, port: Int, tags: List[String])

object Protocols extends DefaultJsonProtocol {
  implicit val details = jsonFormat(ConsulService, "ServiceName", "ServiceAddress", "ServicePort", "ServiceTags")
}
