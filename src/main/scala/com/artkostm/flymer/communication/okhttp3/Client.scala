package com.artkostm.flymer.communication.okhttp3

import com.artkostm.flymer.Application
import com.artkostm.flymer.communication.login.LoginInfo
import okhttp3.{Cookie, OkHttpClient}
import com.artkostm.flymer.communication.{Flymer => flymer}
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import macroid.ContextWrapper
import okhttp3.internal.http.HttpDate

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

/**
  * Created by artsiom.chuiko on 13/10/2016.
  */
object Client {

  def CheckReplies()(implicit okHttp: OkHttpClient, ctx: ContextWrapper) = {
    import io.taig.communicator._
    import com.artkostm.flymer.Application._
    Request
      .prepare(s"http://flymer.ru/req/repcount?c=1&ts=${System.currentTimeMillis}")
      .addHeader("Accept", "*/*")
      .addHeader("Host", "flymer.ru")
      .addHeader("Connection", "keep-alive")
      .start[String]()
  }

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

  protected def buildCookie(name: String, value: String): Cookie = new Cookie.Builder().
    domain(flymer.Domain).
    path("/").
    name(name).
    value(value).
    httpOnly().
    expiresAt(HttpDate.MAX_DATE).
    build()
}

object ClientHolder {
  lazy val sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(Application.getContext)
  private lazy val cookieJar = new PersistentCookieJar(new SetCookieCache, sharedPrefsCookiePersistor)
  implicit lazy val okHttpClient = new OkHttpClient.Builder().cookieJar(cookieJar).build()
}