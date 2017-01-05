package com.artkostm.flymer.communication

import spray.json._, DefaultJsonProtocol._

/**
  * Created by artsiom.chuiko on 13/10/2016.
  */
object Flymer {
  val Domain = "flymer.ru"
  val BaseUrl = s"http://$Domain"
  def LoginUrl(currentTime: Long) = s"$BaseUrl/req/login?ts=$currentTime"

  val Fkey = "fkey"
  val FkeyCssSelector = s"input[name='$Fkey']"
  val Lkey = "lkey"
  val Dkey = "dkey"
  val Sid = "sid"
  val LkeyCssSelector = s"input[name='$Lkey']"
  val Pass = "pass"
  val Email = "email"
  val Ac = "ac"

  val UserAgent = "Mozilla"

  private val responseType = "code"
  private val oauthVersion = "5.4"
  private val clientId = 3206293
  val OAuthUrl = s"https://oauth.vk.com/authorize?client_id=$clientId&scope=&redirect_uri=http%3A%2F%2F$Domain%2Foauth%2Fvk&response_type=$responseType&v=$oauthVersion"

  val NotificationId = 1
}

//case class FlymerError(errorType: String)
case class FlymerReplies(num: String, url: String)
object FlymerReplies {
  def apply(num: String, url: String): FlymerReplies = {
    if (url == null) FlymerReplies(num, url)
    else FlymerReplies(num, url.replaceAll("\\", ""))
  }
}
case class FlymerResponse(replies: FlymerReplies)

object FlymerJsonProtocol extends DefaultJsonProtocol {
  //implicit val FlymerErrorFormat = jsonFormat1(FlymerError)
  implicit val FlymerRepliesFormat = jsonFormat(FlymerReplies.apply _ _, "num", "url")
  implicit val FlymerResponseFormat = jsonFormat(FlymerResponse.apply _, "replies")
}
