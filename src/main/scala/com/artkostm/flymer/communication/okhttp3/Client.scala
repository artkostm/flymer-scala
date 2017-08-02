package com.artkostm.flymer.communication.okhttp3

import okhttp3.OkHttpClient
import macroid.ContextWrapper

/**
  * Created by artsiom.chuiko on 13/10/2016.
  */
object Client {

  def checkReplies()(implicit okHttp: OkHttpClient, ctx: ContextWrapper) = {
    import io.taig.communicator._
    import com.artkostm.flymer.Application._
    Request
      .prepare(s"https://flymer.ru/req/repcount?c=1&ts=${System.currentTimeMillis}")
      .addHeader("Accept", "*/*")
      .addHeader("Host", "flymer.ru")
      .addHeader("Connection", "keep-alive")
      .start[String]()
  }
}