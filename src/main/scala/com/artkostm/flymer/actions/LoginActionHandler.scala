package com.artkostm.flymer.actions

import android.app.Activity
import android.widget.Toast
import freestyle._
import freestyle.implicits._
import cats.instances.future._
import com.artkostm.flymer.communication.login._
import com.artkostm.flymer.service.PipelineService
import com.artkostm.flymer.utils.SharedPrefs
import com.artkostm.flymer._
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import com.google.android.gms.gcm.{GcmNetworkManager, OneoffTask}
import diode._
import diode.Action._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by artsiom.chuiko on 07/11/2017.
  */
class LoginActionHandler[M](modelRW: ModelRW[M, User], activity: Activity with Circuit[LoginModel]) extends ActionHandler(modelRW) with VkCookieInterceptor{
  implicit val loader = new FuturePageLoaderHandler()(Application.Pool)
  implicit val extractor = new FuturePageDataExtractorHandler
  implicit val resolver = new FutureKeyResolverHandler
  val program = new Login[FlymerLoginModule.Op]()
  var vkDialogD: VkLoginDialog = _

  import com.artkostm.flymer.Application._
  override protected def handle = {
    case VkLogin => effectOnly(Effect.action {
      vkDialogD = new VkLoginDialog(activity, this)
      vkDialogD.show()
      NoAction
    })
    case FlymerLogin(Some(email), Some(password)) =>
      updated(value.copy(email, password), Effect(
        program.loginViaFlymer((email, password)).interpret[Future]
          .map(li => FlymerUser(Success(li)))
          .recover { case ex => FlymerUser(Failure(ex)) }
      ))
    case FlymerUser(Success(loginInfo)) => effectOnly(Effect.action(SaveLoginInfo(loginInfo)))
    case FlymerUser(Failure(e)) =>
      effectOnly(Effect.action {
        Toast.makeText(activity, e.getMessage, Toast.LENGTH_SHORT).show
        NoAction
      })
    case SaveLoginInfo(info) =>
      effectOnly(Effect.action {
        import com.artkostm.flymer.Implicits._
        val persistor = activity.getApplication.asInstanceOf[Application].sharedPrefsCookiePersistor
        persistor.saveAll(info)
        new SharedPrefs(activity.getApplicationContext).save(Map(UserActivity.Email -> value.email, UserActivity.Pass -> value.password))
        RunService
      })
    case RunService =>
      effectOnly(Effect.action {
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
