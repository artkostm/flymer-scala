package com.artkostm.flymer

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget._
import com.artkostm.flymer.communication.login.Login.attemptLogin
import com.artkostm.flymer.communication.login.{VkCookieInterceptor, VkLoginDialog}
import com.artkostm.flymer.view.LoginView
import macroid.Contexts
import macroid._
import com.google.android.gms.gcm.{GcmNetworkManager, OneoffTask}

import scala.util.{Failure, Success}
import com.artkostm.flymer.service.PipelineService
import com.artkostm.flymer.utils.SharedPrefs
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import diode._

/**
 * Created by artsiom.chuiko on 03/10/2016.
 */
class LoginActivity extends AppCompatActivity with Contexts[Activity] with Circuit[LoginModel]{

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

class LoginActionHandler[M](modelRW: ModelRW[M, User], activity: Activity with Circuit[LoginModel]) extends ActionHandler(modelRW) with VkCookieInterceptor{
  var vkDialogD: VkLoginDialog = _

  import com.artkostm.flymer.Application._
  override protected def handle = {
    case VkLogin => effectOnly(Effect.action {
      vkDialogD = new VkLoginDialog(activity, this)
      vkDialogD.show()
      NoAction
    })
    case FlymerLogin(Some(email), Some(password)) => updated(
      value.copy(email, password), Effect(
        attemptLogin(email, password).map(attempt => FlymerUser(attempt))
      )
    )
    case FlymerUser(Success(loginInfo)) => effectOnly(Effect.action(SaveLoginInfo(loginInfo)))
    case FlymerUser(Failure(e)) => effectOnly(Effect.action {
      Toast.makeText(activity, e.getMessage, Toast.LENGTH_SHORT).show
      NoAction
    })
    case SaveLoginInfo(info) => effectOnly(Effect.action {
      import com.artkostm.flymer.Implicits._
      val persistor = activity.getApplication.asInstanceOf[Application].sharedPrefsCookiePersistor
      persistor.saveAll(info)
      new SharedPrefs(activity.getApplicationContext).save(Map(UserActivity.Email -> value.email, UserActivity.Pass -> value.password))
      RunService
    })
    case RunService => effectOnly(Effect.action {
      val gcmManager = GcmNetworkManager.getInstance(activity)
      val task = new OneoffTask.Builder().setService(classOf[PipelineService]).
        setTag(PipelineService.TagOneOff).
        setExecutionWindow(5, 20).
        build()
      val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
      if (ConnectionResult.SUCCESS == resultCode) gcmManager.schedule(task)
      else Toast.makeText(activity, "Cannot run Google services", Toast.LENGTH_SHORT).show
      activity.finish()
      NoAction
    })
  }

  override def onCookieIntercepted(cookieString: String): Unit = {
    import com.artkostm.flymer.Implicits._
    activity.getApplicationContext.asInstanceOf[Application].sharedPrefsCookiePersistor.saveAll(cookieString)
    new SharedPrefs(activity.getApplicationContext).save(Map(UserActivity.Vk -> "ok"))
    activity.dispatch(RunService)
  }

  override def onPageStarted(): Unit = if (vkDialogD != null) vkDialogD.dismiss() //maybe just hide and then, in onCookieIntercepted, call dismiss()
}

case class LoginModel(user: User)
case class User(email: String = "", password: String = "")