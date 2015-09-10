package com.octanner.rabbit.utils

import java.util.UUID
import akka.actor.{Actor, Props, ActorRef}
import scala.concurrent.Await
import akka.pattern.ask
import com.spingo.op_rabbit._
import com.spingo.op_rabbit.RabbitControl
import com.spingo.op_rabbit.properties._
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import com.thenewmotion.akka.rabbitmq._
import com.rabbitmq.client.AMQP.BasicProperties
import RabbitControl.GetConnectionActor
import scala.concurrent.duration._

object RpcActor {
  case class Delivery(body: String)
  case class SetReplyTo(replyTo: String)
  def props(
    rabbitControl: ActorRef,
    exchange: String,
    routingKey: String,
    replyTo: String
  ) = Props(new RpcActor(rabbitControl, exchange, routingKey, replyTo))
}

class RpcActor(
  rabbitControl: ActorRef,
  exchangeName: String,
  routingKey: String,
  replyTo: String) extends Actor {
  import RpcActor._

  implicit val timeout = Timeout(3 seconds)


  val connectionActor = Await.result((rabbitControl ? GetConnectionActor).mapTo[ActorRef], timeout.duration)
  val publisher = connectionActor.createChannel(ChannelActor.props(setupSubscriber))

  def receive = {
    case msg: String => sendMsg(msg)
    case _ => 
  }

  def sendMsg(msg: String) = {
    val name = UUID.randomUUID().toString
    publisher ! ChannelMessage(publish(msg.getBytes, name))
    var _ = context.actorOf(CallBackActor.props(sender()), name)
  }

  def publish(msg: Array[Byte], correlationId: String) = {
    (channel: Channel) =>
    val properties = new BasicProperties.Builder()
      .correlationId(correlationId)
      .replyTo(replyTo)
      .build()
    channel.basicPublish(exchangeName, routingKey, properties, msg)
  }

  def setupSubscriber(channel: Channel, self: ActorRef) = {
    val queue = channel.queueDeclare(replyTo, false, true, true, null).getQueue

    val consumer = new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String,
        envelope: Envelope,
        properties: BasicProperties,
        body: Array[Byte]) =
      {
        context.child(properties.getCorrelationId) match {
          case Some(callbackActor) =>
            callbackActor ! Delivery(new String(body, "UTF-8"))
          case None => //todo log the fact that the actor was lost/if this happens something went way wrong
        }
      }
    }
    var _ = channel.basicConsume(queue, true, consumer)
  }
}
