package com.artkostm.flymer.communication

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
}
