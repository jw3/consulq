package consulq

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{Matchers, AsyncWordSpecLike}

import scala.concurrent.Future

/**
 *
 */
class KvSpec extends TestKit(ActorSystem()) with AsyncWordSpecLike with ImplicitSender with Matchers {
  implicit val mat = ActorMaterializer()

  "kv query" should {
    val key = "some.key"

    "be empty" in {
      ConsulQuery().getKV().map { res =>
        res.size shouldBe 0
      }
    }

    "add kv" in {
      ConsulQuery().putKV(key, "someval").map { res =>
        res shouldBe Done
      }
    }

    "get keys" in {
      ConsulQuery().getKV().map { res =>
        res.size shouldBe 1
        res.head.key shouldBe key
      }
    }

    "get key" in {
      ConsulQuery().getKV(key).collect {
        case Some(kv) => kv.key shouldBe key
      }
    }

    "get nonexistant key" in {
      ConsulQuery().getKV("fookey").map(_ shouldBe None)
    }

    "remove kv" in {
      ConsulQuery().deleteKV(key).map { res =>
        res shouldBe Done
      }
    }

    "be empty again" in {
      ConsulQuery().getKV().map { res =>
        res.size shouldBe 0
      }
    }
  }
}
