package Actors

import Actors.ActorMsgs.{CrawleUser, GetUsers, StartCrawler}
import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorRef, OneForOneStrategy, Props}
import akka.routing.RoundRobinPool
import crawlerConfig.CrawlerConfig
import services.{RedisConfig, RedisService}

import scala.concurrent.duration._


class CrawlerRouter extends Actor {
  private var codingRouter: Option[ActorRef] = None
  private var redisServiceOpt: Option[RedisService] = None

  @inline
  private def redisHost: String = {
    CrawlerConfig.getValue("RedisHost", "localhost")
  }

  @inline
  private def redisPort: Int = {
    CrawlerConfig.getValue("RedisPort", 6379.toString).toInt
  }

  override def preStart() = {
    redisServiceOpt = Some(new RedisService(RedisConfig(redisHost, redisPort)))

    val codingEscalator = OneForOneStrategy() {
      case ex: Exception =>
        Restart
    }

    val router = context.actorOf(RoundRobinPool(getCrwalerAmount(),
      supervisorStrategy = codingEscalator).props(Props[CodingCrawler]), "CrawlerRoute")
    codingRouter = Some(context.watch(router))

    import context.dispatcher
    context.system.scheduler.schedule(5.seconds, getCrwaleInterval().seconds, self, GetUsers())
  }

  private def getCrwalerAmount(default: Int = 8): Int = {
    CrawlerConfig.getValue("CrwalerAmount", default.toString).toInt
  }

  private def getCrwaleInterval(default: Int = 10): Int = {
    CrawlerConfig.getValue("CrwaleInterval", default.toString).toInt
  }

  private def getDefaultCrwalerUser(default: String = "dgl") = {
    CrawlerConfig.getValue("CrwaledUser", default)
  }

  private def getUserQueueKey(default: String = "userqueuekey^") = {
    CrawlerConfig.getValue("UserQueue.Key", default)
  }

  private def getUserFromRedis = {
    redisServiceOpt match {
      case Some(redis) =>
        try {
          redis.spop(getUserQueueKey())
        } catch {
          case ex: Exception =>
            println("get user in redis err " + ex.getMessage)
            None
        }
      case None => None
    }
  }

  def receive = {
    case StartCrawler() =>
      codingRouter match {
        case Some(router) =>
          val user = getDefaultCrwalerUser(default = "dgl")
          router ! CrawleUser(user)
        case None => println("can't find codingRouter")
      }

    case GetUsers() =>
      val amount = getCrwalerAmount() - 1
      for (i <- 0 to amount) {
        val userAndRouter = for {
          user <- getUserFromRedis
          router <- codingRouter
        } yield (user, router)

        userAndRouter match {
          case Some((user: String, router: ActorRef)) =>
            router ! CrawleUser(user)
          case None =>
            println("not user in queue!")
        }
      }

    case _ => println("CrawlerRouter get somethng ")
  }
}