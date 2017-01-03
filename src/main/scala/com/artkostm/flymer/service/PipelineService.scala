package com.artkostm.flymer.service

import android.app.{NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.support.v4.app.{NotificationCompat, TaskStackBuilder}
import com.artkostm.flymer.communication.FlymerResponse
import com.artkostm.flymer.{LoginActivity, R}
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import com.google.android.gms.gcm._
import io.taig.communicator.Response
import macroid.Contexts
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by artsiom.chuiko on 10/10/2016.
  */
class PipelineService extends GcmTaskService with Contexts[GcmTaskService] {

  override def onRunTask(taskParams: TaskParams): Int = {
    import com.artkostm.flymer.communication.okhttp3.Client._
    import com.artkostm.flymer.communication.okhttp3.ClientHolder._
    val request = CheckReplies()
    import com.artkostm.flymer.Application._
    import com.artkostm.flymer.communication.FlymerJsonProtocol._
    request.done {
      case Response(code, body) => {
        val num = body.parseJson.convertTo[FlymerResponse].replies.num.toInt
        sendNotification(s"You have received $num replies!", num > 0)
        createPeriodicTask(num > 0)//TODO: check if the server response has greater then 0 replies
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

  def sendNotification(body: String, wasNew: Boolean): Unit = {
    val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    wasNew match {
      case false => mNotificationManager.cancel(1)
      case true => {
        val mBuilder =
          new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Flymer")
            .setContentText(body)
        val resultIntent = new Intent(this, classOf[LoginActivity])
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(classOf[LoginActivity])
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        mNotificationManager.notify(1, mBuilder.build())
      }
    }
  }
}

object PipelineService {
  val TagPeriodic = "flymer_periodic_task"
  val TagOneOff = "flymer_oneOff_task"
  val DefaultPeriod: Int = 20
  val Flex: Int = 10
  var Counter: Int = 0

  def calculateTime(wasNew: Boolean): Int = wasNew match {
    case true => DefaultPeriod
    case false => Counter += Counter; DefaultPeriod + (DefaultPeriod * Counter * 0.1).toInt
  }
}