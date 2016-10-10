package com.artkostm.flymer.login

import android.os.{Handler, Message}
import android.util.Log
import org.jsoup.{Connection, Jsoup}

import scala.util.Try

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

  def AttemptLogin(email: String, pass: String): LoginInfo = {
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

      new LoginInfo(ac, fkey, cookies.get("sid"))
  }
}

class Login {

}

case class LoginInfo(ac: String, fkey: String, sid: String) {
  override def toString: String = "{ac:" + ac + "\nfkey:" + fkey + "\nsid:" + sid + "}"
}