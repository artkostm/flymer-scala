package com.artkostm.flymer.service

import com.google.android.gms.gcm.{GcmNetworkManager, GcmTaskService, TaskParams}

/**
  * Created by artsiom.chuiko on 10/10/2016.
  */
class PipelineService extends GcmTaskService {
  override def onRunTask(taskParams: TaskParams): Int = {
//    import com.artkostm.flymer.communication.okhttp3.Client._
//    implicit val context = getApplicationContext
//    implicit val client = RegisterClient(context)
//    CheckReplies()

    GcmNetworkManager.RESULT_SUCCESS
  }
}