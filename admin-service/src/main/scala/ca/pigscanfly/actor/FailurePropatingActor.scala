package ca.pigscanfly.actor

import akka.actor.{Actor, Status}

trait FailurePropatingActor extends Actor {
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    sender() ! Status.Failure(reason)
  }
}
