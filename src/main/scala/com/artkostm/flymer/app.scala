package com.artkostm.flymer

import java.util.concurrent.Executor

import android.os.{AsyncTask, Handler, Looper}

import scala.concurrent.ExecutionContext

/**
  * Created by artsiom.chuiko on 17/11/2016.
  */
package object app {
  val Executor = new {

    implicit lazy val Pool = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    lazy val Ui = ExecutionContext.fromExecutor(new Executor {
      private val handler = new Handler(Looper.getMainLooper)

      override def execute(command: Runnable) = handler.post(command)
    })
  }
}
