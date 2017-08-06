package com.artkostm.flymer

import com.artkostm.flymer.communication.login.LoginInfo
import diode.Action

import scala.util.Try

/**
  * Created by artsiom.chuiko on 05/08/2017.
  */
case object VkLogin extends Action
case class FlymerLogin(email: Option[String], password: Option[String]) extends Action
case class FlymerUser(user: Try[LoginInfo]) extends Action
case class SaveLoginInfo(info: LoginInfo) extends Action
case object RunService extends Action