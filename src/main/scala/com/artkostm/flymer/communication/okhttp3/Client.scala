package com.artkostm.flymer.communication.okhttp3

import android.app.Service
import android.content.Context
import android.util.Log
import com.artkostm.flymer.communication.login.LoginInfo
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
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
    import com.artkostm.flymer.app.Executor._
    import scala.concurrent.ExecutionContext.Implicits.global
    Request
      .prepare("http://www.scala-lang.org/")
      .start[String]()
      .onReceive {
        case x => Log.i("SCALA", x.toString())
      }(Ui)
      .done { case Response(_, body) => Log.i("SCALA", body) } (Ui)
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

trait HttpClientHolder { self: Service =>
  lazy val cookieJar = new PersistentCookieJar(new SetCookieCache, new SharedPrefsCookiePersistor(self))
  implicit lazy val okHttpClient = new OkHttpClient.Builder().cookieJar(cookieJar).build()
}
