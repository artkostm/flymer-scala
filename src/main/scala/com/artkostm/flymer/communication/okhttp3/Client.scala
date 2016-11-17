package com.artkostm.flymer.communication.okhttp3

import android.content.Context
import android.widget.Toast
import com.artkostm.flymer.communication.login.LoginInfo
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.{Cookie, OkHttpClient}
import com.artkostm.flymer.communication.{Flymer => flymer}

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

/**
  * Created by artsiom.chuiko on 13/10/2016.
  */
object Client {

  def RegisterClient(context: Context): OkHttpClient = {
    val cookieJar = new PersistentCookieJar(new SetCookieCache, new SharedPrefsCookiePersistor(context))
    new OkHttpClient.Builder().cookieJar(cookieJar).build()
  }

  def CheckReplies()(implicit okHttp: OkHttpClient, context: Context) = {
    import io.taig.communicator._
    //import scala.concurrent.ExecutionContext.Implicits.global
    import com.artkostm.flymer.app.Executor._
    //implicit val client = okHttp
    Request
      .prepare("http://www.scala-lang.org/")
      .start[String]()
      .onReceive {
        case _ => println
      }(Ui)
      .done { case Response(_, body) => Toast.makeText(context, body, Toast.LENGTH_LONG).show }
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

class Client {

}
