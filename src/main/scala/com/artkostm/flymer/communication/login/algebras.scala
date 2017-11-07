package com.artkostm.flymer.communication.login

import freestyle._
import org.jsoup.Connection
import org.jsoup.nodes.Document

/**
  * Created by artsiom.chuiko on 07/11/2017.
  */
object algebras {
  type Keys = (String, String, String)
  type Account = (String, String)

  def extractAttribute(doc: Document, cssSelector: String, attrName: String): String =
    doc.select(cssSelector).first.attr(attrName)
}

import algebras._
@free trait PageLoader {
  def loadLoginPage(): FS[Connection]
  def loadAccount(account: Account, keys: Keys, cookies: Map[String, String]): FS[String]
}

@free trait PageDataExtractor {
  def extractDocument(connection: Connection): FS[Document]
  def extractCookies(connection: Connection): FS[Map[String, String]]
}

@free trait KeyResolver {
  def getDkey(fkey: String): FS[String]
  def getFkey(doc: Document): FS[String]
  def getLkey(doc: Document): FS[String]
}

case class LoginInfo(ac: String, fkey: String, sid: String)