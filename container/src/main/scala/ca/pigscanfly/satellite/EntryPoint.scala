package ca.pigscanfly.satellite


import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.typed.{ Cluster, Subscribe }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.javadsl.AkkaManagement
import akka.{ actor => classic }

object EntryPoint extends App {
  // Say hi to the world with butts
  println("Butts!")
  ActorSystem[Nothing](Behaviors.setup[Nothing] { context =>
    import akka.actor.typed.scaladsl.adapter._
    implicit val classicSystem: classic.ActorSystem = context.system.toClassic
    implicit val ec = context.system.executionContext

    val cluster = Cluster(context.system)
    context.log.info("Started [" + context.system + "], cluster.selfAddress = " + cluster.selfMember.address + ")")

    Http().newServerAt("0.0.0.0", 8080).bind(complete("Do you like butts?"))

    // Create an actor that handles cluster domain events
    val listener = context.spawn(Behaviors.receive[ClusterEvent.MemberEvent]((ctx, event) => {
      ctx.log.info("MemberEvent: {}", event)
      Behaviors.same
    }), "listener")

    Cluster(context.system).subscriptions ! Subscribe(listener, classOf[ClusterEvent.MemberEvent])

    AkkaManagement.get(classicSystem).start()
    ClusterBootstrap.get(classicSystem).start()
    Behaviors.empty
  }, "messaging-apka")

}
