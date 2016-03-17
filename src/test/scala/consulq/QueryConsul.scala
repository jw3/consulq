package consulq

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{AsyncWordSpecLike, Matchers}

/**
 *
 */
class QueryConsul extends TestKit(ActorSystem()) with AsyncWordSpecLike with ImplicitSender with Matchers {
  implicit val mat = ActorMaterializer()

  "queries" should {
    "list all services" in {
      ConsulQuery().services().map { res =>
        res.size shouldBe 7
      }
    }

    "get service names" in {
      ConsulQuery().services().map { res =>
        res.collect { case ConsulService(name, _, _, _) => name } should contain allOf("mm", "elastic")
      }
    }
  }
}
