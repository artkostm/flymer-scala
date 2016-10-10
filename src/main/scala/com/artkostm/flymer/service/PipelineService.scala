package com.artkostm.flymer.service

import com.google.android.gms.gcm.{GcmNetworkManager, GcmTaskService, TaskParams}

/**
  * Created by arttsiom.chuiko on 10/10/2016.
  */
class PipelineService extends GcmTaskService {
  override def onRunTask(taskParams: TaskParams): Int = {

    GcmNetworkManager.RESULT_SUCCESS
  }
}
