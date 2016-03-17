package wiii

import spray.json._


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
case class ConsulService(name: String, address: String, port: Int, tags: List[String])

object ConsulServiceProtocol extends DefaultJsonProtocol {
  implicit val details: RootJsonFormat[ConsulService] =
    jsonFormat(ConsulService, "ServiceName", "ServiceAddress", "ServicePort", "ServiceTags")
}
