package com.artkostm.flymer.service

import android.app.{NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.net.Uri
import android.provider.Browser
import android.support.v4.app.{NotificationCompat, TaskStackBuilder}
import com.artkostm.flymer.communication._
import com.artkostm.flymer.communication.login._
import com.artkostm.flymer.utils.SharedPrefs
import com.artkostm.flymer.{R => _, _}
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import com.google.android.gms.gcm._
import io.taig.communicator.Response
import macroid.Contexts
import spray.json._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

/**
  * Created by artsiom.chuiko on 10/10/2016.
  */
class PipelineService extends GcmTaskService with Contexts[GcmTaskService] {

  override def onRunTask(taskParams: TaskParams): Int = {
    import com.artkostm.flymer.communication.okhttp3.Client._
    implicit val fl = FlymerJsonProtocol.FlymerResponseFormat
    implicit val client = getApplication.asInstanceOf[Application].okHttpClient
    val request = checkReplies()
    import com.artkostm.flymer.Application._
    import com.artkostm.flymer.communication.FlymerJsonProtocol._
    request.done {
      case Response(code, body) => body.parseJson.convertTo[FlymerResponse] match {
        case FlymerResponse(Some(FlymerReplies(num, url)), _) =>
          val numberOfReplies = Try(num.toInt).getOrElse(0)
          sendNotification(url, numberOfReplies)
          createPeriodicTask(numberOfReplies > 0)
        case FlymerResponse(_, Some(FlymerError(errorType))) =>
          implicit val loader = new FuturePageLoaderHandler()(Application.Pool)
          implicit val extractor = new FuturePageDataExtractorHandler
          implicit val resolver = new FutureKeyResolverHandler
          val program = new Login[FlymerLoginModule.Op]()
          val prefs = new SharedPrefs(this).load()
          program.loginViaFlymer((prefs(UserActivity.Email).toString,
            prefs(UserActivity.Pass).toString)).interpret[Future]
            .map(li => FlymerUser(Success(li)))
            .recover { case ex => FlymerUser(Failure(ex)) }
      }
    } (Ui)
    Await.result(request, Duration.Inf)
    GcmNetworkManager.RESULT_SUCCESS
  }

  def createPeriodicTask(wasNew: Boolean): Unit = {
    val gcmManager = GcmNetworkManager.getInstance(this)
        val task = new PeriodicTask.Builder().setService(classOf[PipelineService]).
          setPeriod(PipelineService.calculateTime(wasNew)).
          setFlex(PipelineService.Flex).setTag(PipelineService.TagPeriodic).
          setRequiredNetwork(Task.NETWORK_STATE_CONNECTED).
          setPersisted(true).
          setUpdateCurrent(true).
          build()
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
    if (ConnectionResult.SUCCESS == resultCode) gcmManager.schedule(task)
  }

  def sendNotification(url: Option[String], num: Int): Unit = {
    val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    num > 0 match {
      case false => mNotificationManager.cancel(Flymer.NotificationId)
      case true => {
        val mBuilder =
          new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Flymer")
            .setContentText(s"You have received $num replies!")
            .setNumber(num)
            .setAutoCancel(true)
        val resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.get.replace("\\", "")))
        resultIntent.putExtra(Browser.EXTRA_APPLICATION_ID, Flymer.getClass.getCanonicalName)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(classOf[LoginActivity])
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        mNotificationManager.notify(Flymer.NotificationId, mBuilder.build())
      }
    }
  }
}

object PipelineService {
  val TagPeriodic = "flymer_periodic_task"
  val TagOneOff = "flymer_oneOff_task"
  val DefaultPeriod: Int = 15
  val Flex: Int = 10
  var Counter: Int = 0

  def calculateTime(wasNew: Boolean): Int = wasNew match {
    case true => DefaultPeriod
    case false => Counter += Counter; DefaultPeriod + (DefaultPeriod * Counter * 0.1).toInt
  }
}