package com.artkostm.flymer

import android.app.{Activity, ProgressDialog}
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.text.Html
import android.util.Patterns
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget._
import com.artkostm.flymer.communication.login.Login.AttemptLogin
import com.artkostm.flymer.communication.okhttp3.ClientHolder
import com.artkostm.flymer.view.Tweaks
import macroid.Contexts
import macroid.FullDsl._
import macroid._
import com.artkostm.flymer.utils.FlymerHelper._
import com.google.android.gms.gcm.{GcmNetworkManager, PeriodicTask}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import com.artkostm.flymer.service.PipelineService
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import macroid.contrib.{ImageTweaks, TextTweaks}

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
          <~ Tweaks.mp(LayoutParams.WRAP_CONTENT, 76 dp, bottom = 24 dp, gravity = Gravity.CENTER_HORIZONTAL),
        l[TextInputLayout](
          w[EditText]
            <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 8 dp, bottom = 8 dp)
            <~ wire(emailSlot)
            <~ hint("Email")
            <~ Tweaks.emailInputType
        ) <~ vertical
          <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
        l[TextInputLayout](
          w[EditText]
            <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 8 dp, bottom = 8 dp)
            <~ wire(passwordSlot)
            <~ hint("Password")
            <~ Tweaks.password
        ) <~ vertical
          <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
        w[AppCompatButton]
          <~ wire(loginBtn)
          <~ text("Login")
          <~ padding(all = 12 dp)
          <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 24 dp, bottom = 24 dp)
          <~ On.click(flymerLogin),
        w[TextView]
          <~ text("Login via Vk")
          <~ TextTweaks.size(16)
          <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, bottom = 24 dp)
          <~ Tweaks.textGravity(Gravity.CENTER)
          <~ Tweaks.clickable[TextView]
          <~ Tweaks.textFocusableInTouchMode
          <~ On.click(vkLogin)
      ) <~ vertical
        <~ padding(top = 56 dp, left = 24 dp, right = 24 dp)
    ) <~ Tweaks.fitsSystemWindow(true) <~ Tweaks.fillViewport).get)
  }

  lazy val flymerLogin: Ui[Unit] = Ui {
    if(validate()) {
      val dialog = openDialog()
      import com.artkostm.flymer.Application._
      Future {
        AttemptLogin(emailSlot.get.getText, passwordSlot.get.getText)
      } mapUi { loginInfoTry =>
        dialog.dismiss()
        import com.artkostm.flymer.communication.okhttp3.Client._
        loginInfoTry match {
          case Success(loginInfo) => {
            ClientHolder.sharedPrefsCookiePersistor.saveAll(loginInfo)
            runService()
            toast(loginInfo.toString) <~ long <~ fry
          }
          case Failure(e) => toast(e.getMessage) <~ long <~ fry
        }
      }
    }
  }

  lazy val vkLogin: Ui[Unit] = Ui {
    toast("Sorry, not supported yet") <~ long <~ fry
  }

  override def onBackPressed(): Unit = moveTaskToBack(true)

  def onLoginFailed(): Unit = loginBtn.get.setEnabled(true)

  private def runService(): Unit = {
    val gcmManager = GcmNetworkManager.getInstance(LoginActivity.this)
    val task = new PeriodicTask.Builder().setService(classOf[PipelineService]).
      setPeriod(60). setFlex(10). setTag(PipelineService.Tag).
      //setPersisted(true).
      build()
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this)
    if (ConnectionResult.SUCCESS == resultCode) gcmManager.schedule(task)
    else Toast.makeText(LoginActivity.this, "Cannot run Google services", Toast.LENGTH_SHORT).show
  }

  private def validate(): Boolean = {
    val email = emailSlot.get.getText.toString
    val password = passwordSlot.get.getText.toString
    var valid = true
    def validateEmail(): Boolean = if (email.isEmpty || !Patterns.EMAIL_ADDRESS.matcher(email).matches) {
      emailSlot.get.setError(Html.fromHtml("<font color='red'>enter a valid email address</font>"))
      false
    } else {
      emailSlot.get.setError(null)
      true
    }

    def validatePassword(): Boolean = if (password.isEmpty || password.length < 4 || password.length > 20) {
      passwordSlot.get.setError(Html.fromHtml("<font color='red'>between 4 and 20 alphanumeric characters</font>"))
      false
    } else {
      passwordSlot.get.setError(null)
      true
    }

    valid &&= validateEmail()
    valid && validatePassword()
  }

  private def openDialog(): ProgressDialog = {
    val dialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog)
    dialog.setIndeterminate(true)
    dialog.setMessage("Authenticating...")
    dialog.show()
    dialog
  }
}
