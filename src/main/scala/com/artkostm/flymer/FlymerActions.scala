package com.artkostm.flymer

import diode.Action

/**
  * Created by artsiom.chuiko on 05/08/2017.
  */
case object VkLogin extends Action
case class FlymerLogin(email: Option[String], password: Option[String]) extends Action
