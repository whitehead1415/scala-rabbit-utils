package com.octanner.rabbit.utils

import akka.actor._
import RpcActor.Delivery
import scala.concurrent.duration._

object CallBackActor {
  def props(origin: ActorRef, timeout: Duration = 10.seconds) =
    Props(new CallBackActor(origin, timeout))
}

class CallBackActor(origin: ActorRef, timeout: Duration) extends Actor {

  context.setReceiveTimeout(timeout)

  def receive = {
    case Delivery(msg) =>
      origin ! msg
      self ! PoisonPill
    case ReceiveTimeout =>
      origin ! "Request Timed Out!"
      self ! PoisonPill
  }

}
