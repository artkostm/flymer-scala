package com.artkostm.flymer

import java.util.concurrent.Executor

import android.os.{AsyncTask, Handler, Looper}
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient

import scala.concurrent.ExecutionContext

/**
  * Created by artsiom.chuiko on 20/11/2016.
  */
class Application extends android.app.Application with HttpClientProvider {
  override def onCreate(): Unit = super.onCreate
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
