package co.example

import akka.actor.{ Actor, DiagnosticActorLogging, ActorRef, ActorSystem, PoisonPill, Props }
import akka.event.Logging.MDC
import language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

case object Ping
case class PingEnd(context: MDC)
case object Pong

class Pinger extends Actor with DiagnosticActorLogging {
  var countDown = 100

  def receive = {
    case Pong =>
      log.info(s"${self.path} received pong, count down $countDown")

      if (countDown > 0) {
        countDown -= 1
        sender() ! Ping
      } else {
        sender() ! PoisonPill
        self ! PoisonPill
      }
  }
}

class Ponger(pinger: ActorRef) extends Actor with DiagnosticActorLogging {
  var id = -1
  override def mdc(currentMessage : Any): MDC = {
    currentMessage match {
      case Ping => {
       id = Random.nextInt(420)
       Map("contextId" -> s"Ping $id")
      }
      case PingEnd(context) => {
        context
      }
    }
  }

  def receive = {
    case Ping =>
      log.info("BEFORE FUTURE")
      val cont = log.mdc
      pinger().foreach{ _ => {
                         Thread.sleep(1)
                         self ! PingEnd(cont)}}
    case PingEnd(_) =>
      log.info("AFTER FUTURE")
      pinger ! Pong
  }

  private def pinger(): Future[Unit] = Future{
      //log.info("INSIDE FUTURE")
  }
}

object PocAkka extends App {
    val system = ActorSystem("pingpong")

    val pinger = system.actorOf(Props[Pinger](), "pinger")

    val ponger = system.actorOf(Props(classOf[Ponger], pinger), "ponger")

    import system.dispatcher
    system.scheduler.scheduleOnce(500 millis) {
      ponger ! Ping
    }
}

