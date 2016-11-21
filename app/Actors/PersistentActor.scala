package Actors

import Actors.ActorMsgs.CodingUsers
import akka.actor.Actor
import crawlerglobal.Global
import org.json4s._
import org.json4s.jackson.JsonMethods.{compact => jsCompact, render => jsRender}
import services.{RedisConfig, RedisService}

/**
  * Created by spoofer on 16-11-4.
  */
class PersistentActor extends Actor {
  implicit val formats = DefaultFormats
  private var redisServiceOpt: Option[RedisService] = None

  private def redisHost: String = {
    Global.configOpt match {
      case Some(config) =>
        config.getString("RedisHost") match {
          case Some(host) => host
          case None => "localhost"
        }
      case None => "localhost"
    }
  }

  private def redisPort = {
    Global.configOpt match {
      case Some(config) =>
        config.getInt("RedisPort") match {
          case Some(port) => port
          case None => 6379
        }
      case None => 6379
    }
  }

  override def preStart() = {
    redisServiceOpt = Some(new RedisService(RedisConfig(redisHost, redisPort)))
  }

  private def getUserQueueKey(default: String = "userqueuekey^") = {
    Global.configOpt match {
      case Some(config) =>
        config.getString("UserQueue.Key") match {
          case Some(key) => key
          case None => default
        }
      case None => default
    }
  }

  private def getKafkaTopic(default: String = "CodingCrawler") = {
    Global.configOpt match {
      case Some(config) =>
        config.getString("Kafka.Toptic") match {
          case Some(toptic) => toptic
          case None => default
        }
      case None => default
    }
  }

  private def store2Kafka(user: JValue) = {
    Global.KafkaClusterOpt match {
      case Some(kafka) =>
        kafka.send(getKafkaTopic(), jsCompact(jsRender(user)))
      case None =>
    }
  }

  private def storeUser(key: String, user: JValue) = {
    redisServiceOpt match {
      case Some(redis) =>
        if (!redis.isExist(key)) {
          redis.set(key, "1")
          redis.sadd(getUserQueueKey(), key)
          store2Kafka(user)
        }
      case None =>
    }
  }

  def receive = {
    case CodingUsers(users) =>
      users foreach { user =>
        try {
          val globalKeyOpt = (user \\ "global_key").extractOpt[String]
          globalKeyOpt match {
            case Some(key) => storeUser(key, user)
            case None =>
          }
        } catch {
          case ex: Exception =>
            println(ex.getMessage)
            println(user)
        }
      }
    case _ => println("get some thing")
  }
}
