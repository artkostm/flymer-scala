package com.artkostm.flymer.utils

import android.view.View

/**
  * Created by arttsiom.chuiko on 08/10/2016.
  */
object ViewHelper {
  implicit def onClick(handler: View => Unit) = new View.OnClickListener() {
    override def onClick(source: View) = handler(source)
  }
}
