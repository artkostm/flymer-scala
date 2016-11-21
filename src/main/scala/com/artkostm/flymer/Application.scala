package com.artkostm.flymer

import java.util.concurrent.Executor

import android.app.Service
import android.content.Context
import android.os.{AsyncTask, Handler, Looper}

import scala.concurrent.ExecutionContext

/**
  * Created by artsiom.chuiko on 20/11/2016.
  */
class Application extends android.app.Application {

  override def onCreate(): Unit = {
    super.onCreate
    Application.context = this.getApplicationContext
  }
}

object Application {
  private var context: Context = _

  implicit lazy val Pool = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

  lazy val Ui = ExecutionContext.fromExecutor(new Executor {
    private val handler = new Handler(Looper.getMainLooper)

    override def execute(command: Runnable) = handler.post(command)
  })

  def getContext(): Context = context
}

trait ApplicationProvider { self: Service =>
  implicit lazy val application = self.getApplication.asInstanceOf[Application]
}
