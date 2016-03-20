package consulq

import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConversions._

/**
 *
 */
class KvConfigSpec extends WordSpec with Matchers {
  import KvConfigSpec._

  val (k1, k2, k3, k4, k5) = ("k1", "k2", "k3", "k4", "k5")
  val (v1, v2, v3, v4, v5) = ("v1", "v2", "v3", "v4", "v5")

  "config conversion" should {
    "handle empty seq" in {
      toConfig(Seq()) shouldBe ConfigFactory.empty
    }

    "handle one value" in {
      toConfig(Seq(kv(k1, v1))) shouldBe ConfigFactory.parseMap(Map(k1 -> v1))
    }

    "handle two values" in {
      toConfig(Seq(kv(k1, v1), kv(k2, v2))) shouldBe ConfigFactory.parseMap(Map(k1 -> v1, k2 -> v2))
    }

    "handle many values" in {
      val seq = Seq(kv(k1, v1), kv(k2, v2), kv(k3, v3), kv(k4, v4), kv(k5, v5))
      val map = Map(k1 -> v1, k2 -> v2, k3 -> v3, k4 -> v4, k5 -> v5)
      toConfig(seq) shouldBe ConfigFactory.parseMap(map)
    }

    "exclude None values" in {
      val seq = Seq(k(k1), kv(k2, v2), k(k3), kv(k4, v4), k(k5))
      val map = Map(k2 -> v2, k4 -> v4)
      toConfig(seq) shouldBe ConfigFactory.parseMap(map)
    }
  }

}

object KvConfigSpec {
  def kv(k: String, v: String) = ConsulKV(k, Option(toBase64(v)))
  def k(k: String) = ConsulKV(k, None)
}
