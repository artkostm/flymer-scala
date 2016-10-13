package com.artkostm.flymer.communication.login

import org.jsoup.nodes.Document
import org.jsoup.{Connection, Jsoup}

import scala.util.{Failure, Success, Try}

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

  def AttemptLogin(email: String, pass: String): Try[LoginInfo] = requestLoginPage() match {
    case Success(con) => {
      val doc = con.get
      val cookies = con.response.cookies
      val fkey = getAttr(doc, "input[name='fkey']", "value")
      val lkey = getAttr(doc, "input[name='lkey']", "value")
      val dkey = Dkey(fkey)
      cookies.put("fkey", fkey)
      val ac = requestAccount(email, pass, fkey, lkey, dkey, cookies)
      Success(new LoginInfo(ac.get, fkey, cookies.get("sid")))
    }

    case Failure(e) => Failure(e)
  }

  protected def requestLoginPage(): Try[Connection] = Try(Jsoup.connect("http://flymer.ru").userAgent("Mozilla").method(Connection.Method.GET))

  protected def requestAccount(email: String, pass: String, fkey: String, lkey: String, dkey: Int, cookies: java.util.Map[String, String]): Try[String] = Try(
    Jsoup.connect(s"http://flymer.ru/req/login?ts=${System.currentTimeMillis}").
      data("pass", pass).data("email", email).data("fkey",fkey).data("lkey",lkey).data("dkey",String.valueOf(dkey)).
      header("Content-Type", "application/x-www-form-urlencoded").header("Connection", "keep-alive").
      cookies(cookies).
      method(Connection.Method.POST).
      execute().
      cookie("ac"))

  protected def getAttr(doc: Document, cssSelector: String, attrName: String): String = doc.select(cssSelector).first.attr(attrName)

  protected def getAttr(cookies: java.util.Map[String, String], name: String): String = cookies.get(name)
}

class Login {

}

case class LoginInfo(ac: String, fkey: String, sid: String) {
  override def toString: String = s"{ac:$ac\nfkey:$fkey\nsid:$sid}"
}