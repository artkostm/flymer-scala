package com.artkostm.flymer.service

import android.R
import android.app.{NotificationManager, PendingIntent}
import android.content.{Context, Intent}
import android.support.v4.app.{NotificationCompat, TaskStackBuilder}
import android.util.Log
import com.artkostm.flymer.LoginActivity
import com.google.android.gms.gcm.{GcmNetworkManager, GcmTaskService, TaskParams}
import macroid.Contexts

/**
  * Created by artsiom.chuiko on 10/10/2016.
  */
class PipelineService extends GcmTaskService with Contexts[GcmTaskService]{
  override def onRunTask(taskParams: TaskParams): Int = {
//    import com.artkostm.flymer.communication.okhttp3.Client._
//    implicit val context = getApplicationContext
//    implicit val client = RegisterClient(context)
//    CheckReplies()
//    Toast.makeText(getApplicationContext, "Google services", Toast.LENGTH_SHORT).show
    Log.i("SCALA", s"Task is ${taskParams.getTag}")
    sendNotification()
    GcmNetworkManager.RESULT_SUCCESS
  }

  def sendNotification(): Unit = {
    val mBuilder =
      new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_dialog_map)
        .setContentTitle("Flymer")
        .setContentText("Hello World!")
    val resultIntent = new Intent(this, classOf[LoginActivity])
    val stackBuilder = TaskStackBuilder.create(this)
    stackBuilder.addParentStack(classOf[LoginActivity])
    stackBuilder.addNextIntent(resultIntent)
    val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    mBuilder.setContentIntent(resultPendingIntent)
    val mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    mNotificationManager.notify(1, mBuilder.build())
  }
}

object PipelineService {
  val Tag = "flymer_periodic_task"
}