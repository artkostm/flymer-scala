package com.artkostm.flymer.view

import android.text.{Html, InputType}
import android.text.method.PasswordTransformationMethod
import android.view.ViewGroup.MarginLayoutParams
import android.view.{View, ViewGroup}
import android.widget._
import macroid.Tweak

/**
  * Created by artsiom.chuiko on 08/10/2016.
  */
object Tweaks {
  def password() = Tweak[EditText](_.setTransformationMethod(new PasswordTransformationMethod))
  def hint(hint: String) = Tweak[EditText](_.setHint(hint))

  def emailInputType() = Tweak[EditText](_.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))

  def layoutHeight[V <: View](height: Int) = Tweak[View]{ view =>
    view.getLayoutParams.height = height
    view.requestLayout()
  }

  def mp[V <: View](width: Int, height: Int,
                    top: Int = -1, bottom: Int = -1, left: Int = -1, right: Int = -1,
                    gravity: Int = -1) = Tweak[V]{ view =>
    val mp = new MarginLayoutParams(width, height)
    if (top != -1) mp.topMargin = top
    if (bottom != -1) mp.bottomMargin = bottom
    if (left != -1) mp.leftMargin = left
    if (right != -1) mp.rightMargin = right
    val ll = new LinearLayout.LayoutParams(mp)
    if (gravity != -1) ll.gravity = gravity
    view.setLayoutParams(ll)
  }

  def fitsSystemWindow(fits: Boolean) = Tweak[ScrollView](_.setFitsSystemWindows(fits))

  def textGravity(gravity: Int) = Tweak[TextView](_.setGravity(gravity))

  def textFocusableInTouchMode() = Tweak[TextView](_.setFocusableInTouchMode(true))

  def clickable[V <: View]() = Tweak[V](_.setClickable(true))

  def fillViewport() = Tweak[ScrollView](_.setFillViewport(true))

  def htmlError(msg: String) = Tweak[TextView](_.setError(Html.fromHtml(msg)))
}
