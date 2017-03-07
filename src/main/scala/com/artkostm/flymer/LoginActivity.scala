package com.artkostm.flymer

import android.app.{Activity, ProgressDialog}
import android.os.Bundle
import android.support.design.widget.{TextInputEditText, TextInputLayout}
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.text.Html
import android.util.Patterns
import android.view.{Gravity, View}
import android.view.ViewGroup.LayoutParams
import android.widget._
import com.artkostm.flymer.communication.login.Login.attemptLogin
import com.artkostm.flymer.communication.login.{VkCookieInterceptor, VkLoginDialog}
import com.artkostm.flymer.view.Tweaks
import macroid.Contexts
import macroid.FullDsl._
import macroid.{Ui => uinterface}
import macroid._
import com.artkostm.flymer.utils.FlymerImplicits._
import com.google.android.gms.gcm.{GcmNetworkManager, OneoffTask}

import scala.util.{Failure, Success}
import com.artkostm.flymer.service.PipelineService
import com.artkostm.flymer.utils.SharedPrefs
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import macroid.contrib.{ImageTweaks, TextTweaks}

/**
 * Created by artsiom.chuiko on 03/10/2016.
 */
class LoginActivity extends AppCompatActivity with Contexts[Activity] {

  var loginBtn = slot[AppCompatButton]
  var emailSlot = slot[TextInputEditText]
  var passwordSlot = slot[TextInputEditText]
  var vkDialogD: VkLoginDialog = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView((l[ScrollView] (
      l[LinearLayout] (
        w[ImageView]
          <~ ImageTweaks.res(R.drawable.logo)
          <~ Tweaks.mp(LayoutParams.WRAP_CONTENT, 76 dp, bottom = 24 dp, gravity = Gravity.CENTER_HORIZONTAL),
        l[TextInputLayout](
          w[TextInputEditText]
            <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 8 dp, bottom = 8 dp)
            <~ wire(emailSlot)
            <~ hint("Email")
            <~ Tweaks.emailInputType
        ) <~ vertical
          <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT),
        l[TextInputLayout](
          w[TextInputEditText]
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
      val sharedPrefsPersistor = getApplication.asInstanceOf[Application].sharedPrefsCookiePersistor
      import com.artkostm.flymer.Application._
      attemptLogin(emailSlot.get.getText, passwordSlot.get.getText) mapUi { loginInfoTry =>
        dialog.dismiss()
        import com.artkostm.flymer.communication.okhttp3.Client._
        loginInfoTry match {
          case Success(loginInfo) => {
            sharedPrefsPersistor.saveAll(loginInfo)
            new SharedPrefs(getApplicationContext).save(Map(UserActivity.Email -> emailSlot.get.getText, UserActivity.Pass -> passwordSlot.get.getText))
            runService()
            LoginActivity.this.finish()
            uinterface.nop
          }
          case Failure(e) => toast(e.getMessage) <~ long <~ fry
        }
      }
    }
  }

  val interceptor = new VkCookieInterceptor {
    override def onCookieIntercepted(cookieString: String): Unit = {
      import com.artkostm.flymer.communication.okhttp3.Client._
      getApplication.asInstanceOf[Application].sharedPrefsCookiePersistor.saveAll(cookieString)
      runService()
      new SharedPrefs(getApplicationContext).save(Map(UserActivity.Vk -> "ok"))
      LoginActivity.this.finish()
    }

    override def onPageStarted(): Unit = if (vkDialogD != null) vkDialogD.dismiss() //maybe just hide and then, in onCookieIntercepted, call dismiss()
  }

  lazy val vkLogin: Ui[Unit] = Ui {
    vkDialogD = new VkLoginDialog(LoginActivity.this, interceptor)
    vkDialogD.show()
    uinterface.nop
  }

  override def onBackPressed(): Unit = moveTaskToBack(true)

  def onLoginFailed(): Unit = loginBtn.get.setEnabled(true)

  private def runService(): Unit = {
    val gcmManager = GcmNetworkManager.getInstance(LoginActivity.this)
    val task = new OneoffTask.Builder().setService(classOf[PipelineService]).
      setTag(PipelineService.TagOneOff).
      setExecutionWindow(5, 20).
      build()
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this)
    if (ConnectionResult.SUCCESS == resultCode) gcmManager.schedule(task)
    else Toast.makeText(LoginActivity.this, "Cannot run Google services", Toast.LENGTH_SHORT).show
  }

  private def validate(): Boolean = {
    val email = emailSlot.get.getText.toString
    val password = passwordSlot.get.getText.toString
    var valid = true
    var errorSlot: Option[View] = Option.empty
    def validateEmail(): Boolean = if (email.isEmpty || !Patterns.EMAIL_ADDRESS.matcher(email).matches) {
      emailSlot.get.setError(Html.fromHtml("<font color='red'>enter a valid email address</font>"))
      errorSlot = emailSlot
      false
    } else {
      emailSlot.get.setError(null)
      true
    }

    def validatePassword(): Boolean = if (password.isEmpty || password.length < 4 || password.length > 20) {
      passwordSlot.get.setError(Html.fromHtml("<font color='red'>between 4 and 20 alphanumeric characters</font>"))
      errorSlot = passwordSlot
      false
    } else {
      passwordSlot.get.setError(null)
      true
    }

    valid &&= validateEmail()
    valid &&= validatePassword()
    errorSlot.foreach(_.requestFocus())
    valid
  }

  private def openDialog(): ProgressDialog = {
    val dialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog)
    dialog.setIndeterminate(true)
    dialog.setMessage("Authenticating...")
    dialog.show()
    dialog
  }
}
