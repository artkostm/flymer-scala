package com.artkostm.flymer

import android.app.{Activity, ProgressDialog}
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.util.Patterns
import android.view.{Gravity, View, ViewGroup}
import android.view.ViewGroup.LayoutParams
import android.widget._
import com.artkostm.flymer.communication.login.Login.AttemptLogin
import com.artkostm.flymer.view.Tweaks
import macroid.{ContextWrapper, Contexts}
import macroid.FullDsl._
import macroid.contrib.LpTweaks._
import com.artkostm.flymer.utils.FlymerHelper._
import com.google.android.gms.gcm.{GcmNetworkManager, PeriodicTask}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import com.artkostm.flymer.service.PipelineService
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import macroid.contrib.ImageTweaks

/**
 * Created by artsiom.chuiko on 03/10/2016.
 */
class LoginActivity extends AppCompatActivity with Contexts[Activity] {

  var loginBtn = slot[AppCompatButton]
  var emailSlot = slot[EditText]
  var passwordSlot = slot[EditText]

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView((l[ScrollView] (
      l[LinearLayout] (
        w[ImageView]
          <~ ImageTweaks.res(R.drawable.logo)
          <~ Tweaks.mp(LayoutParams.WRAP_CONTENT, 76, bottom = 24, gravity = Gravity.CENTER_HORIZONTAL),
        l[TextInputLayout](
          w[EditText]
            <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 8, bottom = 8)
            <~ wire(emailSlot)
            <~ hint("Email")
            <~ Tweaks.emailInputType
        ) <~ vertical
          <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
        l[TextInputLayout](
          w[EditText]
            <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 8, bottom = 8)
            <~ wire(passwordSlot)
            <~ hint("Password")
            <~ Tweaks.password
        ) <~ vertical
          <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
        w[AppCompatButton]
          <~ matchWidth
          <~ wire(loginBtn)
          <~ text("Login")
          <~ padding(all = 12)
          <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, left = 24, right = 24)
      ) <~ vertical
        <~ padding(top = 56, left = 24, right = 24)
    ) <~ Tweaks.fitsSystemWindow(true)).get)
    //runService

    import com.artkostm.flymer.Application._
    loginBtn.get.setOnClickListener { source: View =>
      val dialog = openDialog()
      Future {
        AttemptLogin(emailSlot.get.getText, passwordSlot.get.getText)
      }.map { value => value match {
          case Success(loginInfo) =>  { dialog.dismiss(); Toast.makeText(LoginActivity.this, loginInfo.toString, Toast.LENGTH_LONG).show }
//            getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE).edit().
          case Failure(e) => { dialog.dismiss(); Toast.makeText(LoginActivity.this, e.getMessage, Toast.LENGTH_LONG).show }
        }
      } (Ui)
    }
  }

  override def onBackPressed(): Unit = moveTaskToBack(true)

  def onLoginFailed(): Unit = loginBtn.get.setEnabled(true)

  private def runService(): Unit = {
    val gcmManager = GcmNetworkManager.getInstance(LoginActivity.this)
    val task = new PeriodicTask.Builder().setService(classOf[PipelineService]).
      setPeriod(30). setFlex(10). setTag(PipelineService.Tag).
      //setPersisted(true).
      build()
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this)
    if (ConnectionResult.SUCCESS == resultCode) gcmManager.schedule(task)
    else Toast.makeText(LoginActivity.this, "Cannot run Google services", Toast.LENGTH_SHORT).show
  }

  private def validate(): Boolean = {
    val email = emailSlot.get.getText.toString
    val password = passwordSlot.get.getText.toString

    def validateEmail(): Boolean = if (email.isEmpty || !Patterns.EMAIL_ADDRESS.matcher(email).matches) {
      emailSlot.get.setError("enter a valid email address")
      false
    } else {
      emailSlot.get.setError(null)
      true
    }

    def validatePassword(): Boolean = if (password.isEmpty || password.length < 4 || password.length > 20) {
      passwordSlot.get.setError("between 4 and 20 alphanumeric characters")
      false
    } else {
      passwordSlot.get.setError(null)
      true
    }

    validateEmail() && validatePassword()
  }

  private def openDialog(): ProgressDialog = {
    val dialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog)
    dialog.setIndeterminate(true)
    dialog.setMessage("Authenticating...")
    dialog.show()
    dialog
  }
}
