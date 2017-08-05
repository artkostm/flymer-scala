package com.artkostm.flymer

import java.util.concurrent.Executor

import android.os.{AsyncTask, Handler, Looper}
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import diode._
import okhttp3.OkHttpClient

import scala.concurrent.ExecutionContext

/**
  * Created by artsiom.chuiko on 20/11/2016.
  */
class Application extends android.app.Application with HttpClientProvider with Circuit[FlymerModel] {
  override def onCreate(): Unit = super.onCreate

  override protected def initialModel: FlymerModel = FlymerModel(User())

  override protected def actionHandler: HandlerFunction = new LoginActionHandler(zoomTo(_.user))
}

object Application {

  implicit lazy val Pool = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

  lazy val Ui = ExecutionContext.fromExecutor(new Executor {
    private val handler = new Handler(Looper.getMainLooper)
    override def execute(command: Runnable) = handler.post(command)
  })
}

trait HttpClientProvider { app: android.app.Application =>
  lazy val sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(app.getApplicationContext)
  lazy val cookieJar = new PersistentCookieJar(new SetCookieCache, sharedPrefsCookiePersistor)
  lazy val okHttpClient = new OkHttpClient.Builder().cookieJar(cookieJar).build()
}

case class FlymerModel(user: User)
case class User(email: String = "", password: String = "")

class LoginActionHandler[M](modelRW: ModelRW[M, User]) extends ActionHandler(modelRW) {
  override protected def handle = {
    case action: Action => ???
  }
}