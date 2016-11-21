package Actors

import org.json4s.JValue

object ActorMsgs {
  case class StartCrawler()
  case class GetUsers()
  case class CrawleUser(name: String)
  case class CodingUsers(users: List[JValue])
}
