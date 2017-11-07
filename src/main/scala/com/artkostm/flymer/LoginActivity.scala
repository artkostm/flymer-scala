package com.artkostm.flymer

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.artkostm.flymer.actions.LoginActionHandler
import com.artkostm.flymer.view.LoginView
import macroid.Contexts
import macroid._
import diode._

/**
 * Created by artsiom.chuiko on 03/10/2016.
 */
class LoginActivity extends AppCompatActivity with Contexts[Activity] with Circuit[LoginModel] {

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView {
      Ui.get(LoginView.render(this))
    }
  }

  override def onBackPressed(): Unit = moveTaskToBack(true)

  override protected def initialModel: LoginModel = LoginModel(User())

  override protected def actionHandler: HandlerFunction = new LoginActionHandler(zoomTo(_.user), this)
}

case class LoginModel(user: User)
case class User(email: String = "", password: String = "")