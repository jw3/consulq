package wiii

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigRenderOptions
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

/**
 *
 */
class QueryConsul extends TestKit(ActorSystem()) with WordSpecLike with ImplicitSender with Matchers {
  implicit val mat = ActorMaterializer()

  "ConsulServices util" should {
    "list all services" in {
      val services = Await.result(ConsulServices.services(), 10.seconds)
      println(services)
    }
  }
}
