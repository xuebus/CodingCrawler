package Actors

import Actors.ActorMsgs.StartCrawler
import akka.actor.{ActorRef, ActorSystem, Props}


object CrawlerSystem {
  var crawlerSystem: Option[ActorSystem] = None
  var crawlerRouterOpt: Option[ActorRef] = None
  var persistentActorOpt: Option[ActorRef] = None

  def initCrawlerSystem: Option[ActorSystem] = {
    val system = ActorSystem("CodingCrawlerSystem")
    initCrawlerRouter(system)
    persistentActorOpt = initPersistentActor(system)
    crawlerSystem = Some(system)
    crawlerSystem
  }

  private def initCrawlerRouter(system: ActorSystem) = {
    val crawlerRouter = system.actorOf(Props[CrawlerRouter], name = "CrawlerRouter")
    crawlerRouterOpt = Some(crawlerRouter)
  }

  private def initPersistentActor(system: ActorSystem) = {
    Some(system.actorOf(Props[PersistentActor], name = "PersistentActor"))
  }

  def scheduleOnce(system: ActorSystem, delay: Int) = {
    import system.dispatcher
    import scala.concurrent.duration._
    crawlerSystem.get.scheduler.scheduleOnce(delay.seconds) {
      crawlerRouterOpt.get ! StartCrawler()
    }
  }

  def stop() = {
    //todo stop the system
  }
}