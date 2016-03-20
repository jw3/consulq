package consulq

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import com.github.marklister.base64.Base64
import org.scalatest.{AsyncWordSpecLike, Matchers}

/**
 *
 */
class KvSpec extends TestKit(ActorSystem()) with AsyncWordSpecLike with ImplicitSender with Matchers {
  implicit val mat = ActorMaterializer()

  "kv query" should {
    val key = "some.key"
    val value = UUID.randomUUID.toString

    "be empty" in {
      ConsulQuery().getKV().map { res =>
        res.size shouldBe 0
      }
    }

    "add kv" in {
      ConsulQuery().putKV(key, value).map { res =>
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
      ConsulQuery().getKV(UUID.randomUUID.toString.take(4)).map(_ shouldBe None)
    }

    "have proper encoding" in {
      ConsulQuery().putKV(key, value).flatMap(r => ConsulQuery().getKV(key)).collect {
        case Some(kv) =>
          kv.base64value shouldNot be(Some(value))
          kv.base64value shouldBe Some(Base64.Encoder(value.getBytes).toBase64)
      }
    }

    "get decoded value" in {
      ConsulQuery().putKV(key, value).flatMap(r => ConsulQuery().getKV(key)).collect {
        case Some(kv) =>
          kv.value shouldBe Some(value)
      }
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
