import akka.http.scaladsl.model.HttpRequest


package object consulq {
  type RequestBuilder = HttpRequest => HttpRequest
  val nopbuilder: RequestBuilder = r => r
}
