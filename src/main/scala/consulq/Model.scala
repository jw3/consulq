package consulq

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
  implicit val details: JsonFormat[ConsulService] =
    jsonFormat(ConsulService, "ServiceName", "ServiceAddress", "ServicePort", "ServiceTags")
}

/**
 * {
 * "LockIndex":0,
 * "Key":"test",
 * "Flags":0,
 * "Value":"dmFsdWU=",
 * "CreateIndex":669,
 * "ModifyIndex":671
 * }
 */
case class ConsulKV(key: String, base64value: Option[String]) {
  lazy val value: Option[String] = {
    base64value.map(fromBase64)
  }
}

object ConsulKvProtocol extends DefaultJsonProtocol {
  implicit val details: JsonFormat[ConsulKV] = jsonFormat(ConsulKV, "Key", "Value")
}
