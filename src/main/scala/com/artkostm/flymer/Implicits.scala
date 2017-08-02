package com.artkostm.flymer

import com.artkostm.flymer.communication.login.LoginInfo
import com.artkostm.flymer.communication.{Flymer => flymer}
import okhttp3.Cookie
import okhttp3.internal.http.HttpDate
import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer

/**
  * Created by artsiom.chuiko on 02/08/2017.
  */
object Implicits {

  implicit def logInfoToCookies(loginInfo: LoginInfo): java.util.List[Cookie] = {
    val buffer = ListBuffer.empty[Cookie]
    buffer += buildCookie(flymer.Fkey, loginInfo.fkey)
    buffer += buildCookie(flymer.Sid, loginInfo.sid)
    buffer += buildCookie(flymer.Ac, loginInfo.ac)

    buffer.asJava
  }

  implicit def cookieStringToCookies(cookieStr: String) : java.util.List[Cookie] = {
    val buffer = ListBuffer.empty[Cookie]
    cookieStr.split(";").foreach(pair => {
      val cookie = pair.split("=")
      buffer += buildCookie(cookie(0).trim, cookie(1))
    })
    buffer.asJava
  }

  private[flymer] def buildCookie(name: String, value: String): Cookie = new Cookie.Builder().
    domain(flymer.Domain).
    path("/").
    name(name).
    value(value).
    httpOnly().
    expiresAt(HttpDate.MAX_DATE).
    build()
}
