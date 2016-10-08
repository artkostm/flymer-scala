package com.artkostm.flymer.view

import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import macroid.Tweak

/**
  * Created by arttsiom.chuiko on 08/10/2016.
  */
object Tweaks {
  def password() = {
    Tweak[EditText](_.setTransformationMethod(new PasswordTransformationMethod))
  }
}
