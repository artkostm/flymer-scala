package com.artkostm.flymer.service

import android.app.{NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.support.v4.app.{NotificationCompat, TaskStackBuilder}
import android.util.Log
import com.artkostm.flymer.{Application, LoginActivity, R}
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.android.gms.gcm.{GcmNetworkManager, GcmTaskService, TaskParams}
import io.taig.communicator.Response
import macroid.Contexts
import okhttp3.OkHttpClient

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by artsiom.chuiko on 10/10/2016.
  */
class PipelineService extends GcmTaskService with Contexts[GcmTaskService] {

  override def onRunTask(taskParams: TaskParams): Int = {
    import com.artkostm.flymer.communication.okhttp3.Client._
    import com.artkostm.flymer.communication.okhttp3.ClientHolder._
    print()
    val request = CheckReplies()
    import com.artkostm.flymer.Application._
    request.done {
      case Response(code, body) => sendNotification(body)
    } (Ui)
    Await.result(request, Duration.Inf)
    GcmNetworkManager.RESULT_SUCCESS
  }

  def sendNotification(body: String): Unit = {
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
    val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    mNotificationManager.notify(1, mBuilder.build())
  }

  def print()(implicit okHttpClient: OkHttpClient): Unit = {
    Log.i("SCALA", s"Http Client is ${okHttpClient}")
  }
}

object PipelineService {
  val Tag = "flymer_periodic_task"
}