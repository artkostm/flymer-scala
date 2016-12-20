package com.artkostm.flymer.utils

/**
  * Created by artsiom.chuiko on 29/11/2016.
  */
object Events {
  case class Event[T](source: T)

  //type EventHandler = PartialFunction[Event, Unit]
}
