package com.artkostm.flymer.communication.okhttp3

import com.artkostm.flymer.communication.login.LoginInfo
import okhttp3.{Cookie, OkHttpClient}
import com.artkostm.flymer.communication.{Flymer => flymer}
import macroid.ContextWrapper

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

  protected def buildCookie(name: String, value: String): Cookie = new Cookie.Builder().
    domain(flymer.Domain).
    path("/").
    name(name).
    value(value).
    httpOnly().
    secure().
    build()
}