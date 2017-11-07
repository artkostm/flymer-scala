package com.artkostm.flymer.communication.login

import com.artkostm.flymer.communication.Flymer._
import com.artkostm.flymer.communication.login.algebras.{Account, Keys}
import org.jsoup.nodes.Document
import org.jsoup.{Connection, Jsoup}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by artsiom.chuiko on 07/11/2017.
  */
class handlers {

}

class FuturePageLoaderHandler(implicit ec: ExecutionContext) extends PageLoader.Handler[Future] {
  override protected[this] def loadLoginPage =
    Future(Jsoup.connect(BaseUrl).userAgent(UserAgent).method(Connection.Method.GET))

  override protected[this] def loadAccount(account: Account, keys: Keys, cookies: Map[String, String]) =
    Future(Jsoup.connect(LoginUrl(System.currentTimeMillis)).
      data(Pass, account._2).data(Email, account._1).
      data(Fkey, keys._1).data(Lkey, keys._2).
      data(Dkey, keys._3).
      header("Content-Type", "application/x-www-form-urlencoded").
      header("Connection", "keep-alive").
      cookies(cookies.asJava).
      method(Connection.Method.POST).
      execute().
      cookie(Ac))
}

class FuturePageDataExtractorHandler extends PageDataExtractor.Handler[Future] {
  override protected[this] def extractDocument(connection: Connection) = Future.successful(connection.get)

  override protected[this] def extractCookies(connection: Connection) =
    Future.successful(connection.response.cookies.asScala.toMap)
}

class FutureKeyResolverHandler extends KeyResolver.Handler[Future] {
  override protected[this] def getDkey(fkey: String) = Future.successful(fkey.foldLeft(0) { (n, g) =>
    val m = (n << 5) - n + g
    m & m
  }.toString)

  override protected[this] def getFkey(doc: Document) =
    Future.successful(algebras.extractAttribute(doc, FkeyCssSelector, "value"))

  override protected[this] def getLkey(doc: Document) =
    Future.successful(algebras.extractAttribute(doc, LkeyCssSelector, "value"))
}