package com.artkostm.flymer.utils

import android.view.View

/**
  * Created by artsiom.chuiko on 08/10/2016.
  */
object ViewHelper {

  implicit def toRun(handler: => Unit) = new Runnable {
    override def run(): Unit = handler
  }

  implicit def toOnClickListener[V <: View](handler: V => Any) = new View.OnClickListener {
    override def onClick(source: View): Unit = handler(source.asInstanceOf[V])
  }

  implicit def charSequenceToString(str: CharSequence) = str.toString

}
