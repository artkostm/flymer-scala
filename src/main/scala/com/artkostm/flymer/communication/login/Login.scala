package com.artkostm.flymer.communication.login

import org.jsoup.nodes.Document
import org.jsoup.{Connection, Jsoup}
import com.artkostm.flymer.communication.{Flymer => flymer}

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by artsiom.chuiko on 08/10/2016.
  */
object Login {

  def Dkey(fkey: String): Int = {
    fkey.foldLeft(0)( (n, g) => {
      val m = (n << 5) - n + g
      m & m
    })
  }

  def AttemptLogin(email: String, pass: String): Future[Try[LoginInfo]] = Future {
    Try({
      val con = requestLoginPage()
      val doc = con.get
      val cookies = con.response.cookies
      val fkey = getAttr(doc, flymer.FkeyCssSelector, "value")
      val lkey = getAttr(doc, flymer.LkeyCssSelector, "value")
      val dkey = Dkey(fkey)
      cookies.put(flymer.Fkey, fkey)
      val ac = requestAccount(email, pass, fkey, lkey, dkey, cookies)
      new LoginInfo(ac, fkey, cookies.get(flymer.Sid))
    })
  }

  protected def requestLoginPage(): Connection = Jsoup.connect(flymer.BaseUrl).userAgent(flymer.UserAgent).method(Connection.Method.GET)

  protected def requestAccount(email: String, pass: String, fkey: String, lkey: String,
                               dkey: Int, cookies: java.util.Map[String, String]): String = {
    Jsoup.connect(flymer.LoginUrl(System.currentTimeMillis)).
      data(flymer.Pass, pass).data(flymer.Email, email).
      data(flymer.Fkey, fkey).data(flymer.Lkey, lkey).
      data(flymer.Dkey, String.valueOf(dkey)).
      header("Content-Type", "application/x-www-form-urlencoded").
      header("Connection", "keep-alive").
      cookies(cookies).
      method(Connection.Method.POST).
      execute().
      cookie(flymer.Ac)
  }

  protected def getAttr(doc: Document, cssSelector: String, attrName: String): String = doc.select(cssSelector).first.attr(attrName)

  protected def getAttr(cookies: java.util.Map[String, String], name: String): String = cookies.get(name)
}

case class LoginInfo(ac: String, fkey: String, sid: String) {
  override def toString: String = s"{ac:$ac\nfkey:$fkey\nsid:$sid}"
}