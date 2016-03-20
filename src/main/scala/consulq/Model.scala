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
  implicit val details: RootJsonFormat[ConsulService] =
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
  lazy val value = {
    // unbase64 base64value
  }
}

object ConsulKvProtocol extends DefaultJsonProtocol {
  implicit val details: RootJsonFormat[ConsulKV] = jsonFormat(ConsulKV, "Key", "Value")
}
