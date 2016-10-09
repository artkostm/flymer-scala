package com.artkostm.flymer.login

import android.os.{Handler, Message}
import android.util.Log
import org.jsoup.{Connection, Jsoup}

/**
  * Created by arttsiom.chuiko on 08/10/2016.
  */
object Login {
  def Dkey(fkey: String): Int = {
    fkey.foldLeft(0)( (n, g) => {
      val m = (n << 5) - n + g
      m & m
    })
  }

  def AttemptLogin(email: String, pass: String): String = {
    try{
      val connection = Jsoup.connect("http://flymer.ru")
      connection.userAgent("Mozilla").method(Connection.Method.GET)
      val doc = connection.get()
      val cookies = connection.response().cookies()
      val fkey = doc.select("input[name='fkey']").first().attr("value")
      val lkey = doc.select("input[name='lkey']").first().attr("value")
      Log.i("scala", "FKEY:" + fkey + ", LKEY:" + lkey)
      val dkey = Dkey(fkey)
      cookies.put("fkey", fkey)

      val ac = Jsoup.connect("http://flymer.ru/req/login?ts="+System.currentTimeMillis())
        .data("pass", pass)
        .data("email", email)
        .data("fkey",fkey)
        .data("lkey",lkey)
        .data("dkey",String.valueOf(dkey))
        .header("Content-Type","application/x-www-form-urlencoded")
        .header("Connection","keep-alive")
        .cookies(cookies)
        .method(Connection.Method.POST)
        .execute().cookie("ac");

      cookies.put("lkey", lkey);
      cookies.put("dkey", String.valueOf(dkey))

      "ac:"+ac+"\nsid:"+cookies.get("sid")
    } catch {
      case e: Exception => {
        Log.i("scala", e.getMessage, e)
        e.printStackTrace()
        e.getMessage }
    }

  }
}

class Login {

}