package com.octanner.rabbit.utils

import akka.actor._
import akka.testkit._
import akka.pattern.ask
import org.scalatest._
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import com.spingo.op_rabbit.RabbitControl
import com.spingo.op_rabbit.properties._
import com.spingo.op_rabbit._
import scala.concurrent.ExecutionContext.Implicits.global

class RpcActorSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with FunSpecLike
    with Matchers
    with BeforeAndAfterAll
{
  implicit val timeout = Timeout(3 seconds)

  def this() = this(ActorSystem("TestSystem"))

  override def afterAll() = {
    subscriptionRef.close()
    TestKit.shutdownActorSystem(system)
  }

  val rabbitControl = system.actorOf(Props[RabbitControl])

  val subscriptionRef = Subscription.run(rabbitControl) {
    import Directives._
    channel(qos = 3) {
      consume(topic(queue("fooQ"), List("#"))) {
        (body(as[String])) { (msg) =>
          property(ReplyTo) { replyTo =>
            property(CorrelationId) { cId =>
              rabbitControl ! Message.topic("bar", replyTo, "", Seq(CorrelationId(cId)))
              ack
            }
          }
        }
      }
    }
  }

  describe("RPC Actor") {
    it("sends and receives messages from pantanal in rpc style") {
      val rpcActor = system.actorOf(RpcActor.props(rabbitControl, "amq.topic", "fooQ", "foo"))
      val msg = Await.result(rpcActor ? "foo", timeout.duration)
      msg should equal ("bar")
    }
  }


}
