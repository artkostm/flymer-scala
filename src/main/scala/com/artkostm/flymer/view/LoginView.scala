package com.artkostm.flymer.view

import android.support.design.widget.{TextInputEditText, TextInputLayout}
import android.support.v7.widget.AppCompatButton
import android.text.Html
import android.util.Patterns
import android.view.{Gravity, View}
import android.view.ViewGroup.LayoutParams
import android.widget.{ImageView, LinearLayout, ScrollView, TextView}
import com.artkostm.flymer._
import diode._
import macroid.FullDsl._
import macroid.Ui
import macroid._
import macroid.contrib.{ImageTweaks, TextTweaks}

/**
  * Created by artsiom.chuiko on 05/08/2017.
  */
object Id extends IdGenerator(start = 1000)
sealed trait Slot { def slot: Option[TextInputEditText] }
case class EmailSlot(email: Option[TextInputEditText]) extends Slot {
  override def slot: Option[TextInputEditText] = email
}
case class PasswordSlot(password: Option[TextInputEditText]) extends Slot {
  override def slot: Option[TextInputEditText] = password
}

object LoginView {

  private var emailSlot = slot[TextInputEditText]
  private var passwordSlot = slot[TextInputEditText]
  private lazy val textExtractor: TextInputEditText => String = _.getText.toString

  def render(dispatch: Dispatcher)(implicit ctx: ContextWrapper) : Ui[View] = l[ScrollView] (
    l[LinearLayout] (
      w[ImageView]
        <~ ImageTweaks.res(R.drawable.logo)
        <~ Tweaks.mp(LayoutParams.WRAP_CONTENT, 76 dp, bottom = 24 dp, gravity = Gravity.CENTER_HORIZONTAL),
      l[TextInputLayout](
        w[TextInputEditText]
          <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 8 dp, bottom = 8 dp)
          <~ id(Id.email)
          <~ wire(emailSlot)
          <~ hint("Email")
          <~ Tweaks.emailInputType
      ) <~ vertical
        <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
      l[TextInputLayout](
        w[TextInputEditText]
          <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 8 dp, bottom = 8 dp)
          <~ id(Id.password)
          <~ wire(passwordSlot)
          <~ hint("Password")
          <~ Tweaks.password
      ) <~ vertical
        <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
      w[AppCompatButton]
        <~ id(Id.loginBtn)
        <~ text("Login")
        <~ padding(all = 12 dp)
        <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 24 dp, bottom = 24 dp)
        <~ On.click(Ui { if (validate) dispatch(FlymerLogin(emailSlot.map(textExtractor), passwordSlot.map(textExtractor))) }),
      w[TextView]
        <~ text("Login via Vk")
        <~ TextTweaks.size(16)
        <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, bottom = 24 dp)
        <~ Tweaks.textGravity(Gravity.CENTER)
        <~ Tweaks.clickable[TextView]
        <~ Tweaks.textFocusableInTouchMode
        <~ On.click(Ui { dispatch(VkLogin) })
    ) <~ vertical
      <~ padding(top = 56 dp, left = 24 dp, right = 24 dp)
  ) <~ Tweaks.fitsSystemWindow(true) <~ Tweaks.fillViewport

  import com.artkostm.flymer.Implicits._
  private[view] def validate: Boolean = {
    val erroneous = List(EmailSlot(emailSlot), PasswordSlot(passwordSlot)).filter(slot => slot match {
      case EmailSlot(Some(email)) if (email.isEmpty || !Patterns.EMAIL_ADDRESS.matcher(email).matches) => {
        email.setError(Html.fromHtml("<font color='red'>enter a valid email address</font>"))
        true
      }
      case PasswordSlot(Some(password)) if (password.isEmpty || password.length < 4 || password.length > 20) => {
        password.setError(Html.fromHtml("<font color='red'>between 4 and 20 alphanumeric characters</font>"))
        true
      }
      case _ => false
    }).map(_.slot)
    erroneous.foreach(slot => slot match {
      case Some(editText) => editText.requestFocus()
    })
    erroneous.isEmpty
  }
}