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
import com.artkostm.flymer.view.{LoginView, Tweaks}
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
    setContentView {
      Ui.get(LoginView.render(null))
    }
  }

  lazy val flymerLogin: Ui[Unit] = Ui {
    if(validate) {
      val dialog = openDialog()
      val sharedPrefsPersistor = getApplication.asInstanceOf[Application].sharedPrefsCookiePersistor
      import com.artkostm.flymer.Application._
      attemptLogin(emailSlot.get.getText, passwordSlot.get.getText) mapUi { loginInfoTry =>
        dialog.dismiss()
        import com.artkostm.flymer.Implicits._
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
      import com.artkostm.flymer.Implicits._
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

  import com.artkostm.flymer.Implicits._
  def validate: Boolean = {
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
    }).map(_.slot.getOrElse(None[TextInputEditText]))
    erroneous.foreach(_.requestFocus())
    erroneous.isEmpty
  }

  private def openDialog(): ProgressDialog = {
    val dialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog)
    dialog.setIndeterminate(true)
    dialog.setMessage("Authenticating...")
    dialog.show()
    dialog
  }
}

case class Slot(slot: Option[TextInputEditText])
case class EmailSlot(email: Option[TextInputEditText]) extends Slot(email)
case class PasswordSlot(password: Option[TextInputEditText]) extends Slot(password)