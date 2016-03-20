import akka.http.scaladsl.model.HttpRequest
import com.github.marklister.base64.Base64
import com.typesafe.config.{ConfigFactory, Config}
import scala.collection.JavaConversions._

package object consulq {
  type RequestBuilder = HttpRequest => HttpRequest

  def toConfig(kvs: Seq[ConsulKV]): Config = {
    val kvmap = kvs.flatMap(kv => kv.value.map(kv.key -> _)).toMap
    ConfigFactory.parseMap(kvmap)
  }

  def toBase64(s: String) = {
    Base64.Encoder(s.getBytes).toBase64
  }

  def fromBase64(s: String) = {
    Base64.Decoder(s).toByteArray.map(_.toChar).mkString
  }
}
