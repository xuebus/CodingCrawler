package Actors

import Actors.ActorMsgs.{CodingUsers, CrawleUser}
import akka.actor.Actor
import util.HttpHelper

import scala.concurrent._
import ExecutionContext.Implicits.global
import org.json4s._
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.{parse => jsParse}

class CodingCrawler extends Actor {
  implicit val formats = DefaultFormats   // Brings in default date formats etc.
  private lazy val persistentActor = context.actorSelection("/user/PersistentActor")

  private def downloadPage(url: String): Future[String] = {
    Future {
      val httpHelper = new HttpHelper()
      httpHelper.downloadPageByGet(url) match {
        case Some(page) =>
          page
        case None =>
          ""
      }
    }
  }

  def getUserInfosFromCoding(name: String): Future[JValue]= {
    val page = downloadPage(s"https://coding.net/api/user/key/$name")
    for {
      infos <- page
    } yield jsParse(infos)
  }

  def getUserFriends(name: String): Future[JValue] = {
    val page = downloadPage(s"https://coding.net/api/user/friends/$name?page=1&pageSize=2000")
    for {
      friends <- page
    } yield jsParse(friends)
  }

  def getUserFollowers(name: String): Future[JValue] = {
    val page = downloadPage(s"https://coding.net/api/user/followers/$name?page=1&pageSize=2000")
    for {
      followers <- page
    } yield jsParse(followers)
  }

  def rmDuplicate[T](list: List[T]) = list.foldLeft(List.empty[T]){
    (rmDuped, current) => if(rmDuped.contains(current)) rmDuped else rmDuped :+ current
  }

  def getAllUsers(userInfos: JValue, followersJs: JValue, friendsJs: JValue): List[JValue] = {
    val followersOpt = (followersJs \\ "data").extractOpt[JValue]
    val friendsOpt = (friendsJs \\ "data").extractOpt[JValue]
    val userdataOpt = (userInfos \\ "data").extractOpt[JValue]

    val allUserInfos = for {
      followers <- followersOpt
      friends <- friendsOpt
      userdata <- userdataOpt
    } yield (followers \\ "list").extract[JArray].arr ++ (friends \\ "list").extract[JArray].arr :+ userdata

    allUserInfos match {
      case Some(users) => rmDuplicate(users)
      case None => List[JValue]()
    }
  }

  def receive = {
    case CrawleUser(user) =>
      val userInfoFT = getUserInfosFromCoding(user)
      val userFriendsFT = getUserFriends(user)
      val userFollowersFT = getUserFollowers(user)

      val ret = for {
        userInfo <- userInfoFT
        userFriends <- userFriendsFT
        userFollowers <- userFollowersFT
      } yield {
        (userInfo, userFollowers, userFriends)
      }

      ret.map {
        case (infosJs, followersJs, friendsJs) =>
          val users = getAllUsers(infosJs, followersJs, friendsJs)
          println(s"get user!!$user, and crawler user's firends or follwoers amount is ${users.size - 1}")
          persistentActor ! new CodingUsers(users)
      }

    case _ => println("CodingCrawler get somethng ")
  }
}
