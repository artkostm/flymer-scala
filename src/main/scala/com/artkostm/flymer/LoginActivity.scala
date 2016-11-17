package com.artkostm.flymer

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget._
import com.artkostm.flymer.communication.login.Login.AttemptLogin
import com.artkostm.flymer.view.Tweaks
import macroid.Contexts
import macroid.FullDsl._
import macroid.contrib.LpTweaks._
import com.artkostm.flymer.utils.FlymerHelper._
import com.google.android.gms.gcm.{GcmNetworkManager, PeriodicTask}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import com.artkostm.flymer.service.PipelineService
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability, GooglePlayServicesUtil}

/**
 * Created by artsiom.chuiko on 03/10/2016.
 */
class LoginActivity extends Activity with Contexts[Activity] {
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    var label = slot[Button]
    var email = slot[TextView]
    var password = slot[TextView]

    val mainView = l[LinearLayout] (
      w[EditText] <~ matchWidth <~ wire(email),
      w[EditText] <~ matchWidth <~ wire(password) <~ Tweaks.password,
      w[Button] <~ matchWidth <~ wire(label) <~ text("Log in")
    ) <~ vertical

    setContentView(mainView.get)
    runService

    label.get.setOnClickListener { source: View =>
      Future {
        AttemptLogin(email.get.getText, password.get.getText)
      } map { value => value match {
          case Success(loginInfo) => runOnUiThread {
            Toast.makeText(LoginActivity.this, loginInfo.toString, Toast.LENGTH_LONG).show
//            getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE).edit().
          }
          case Failure(e) => runOnUiThread { Toast.makeText(LoginActivity.this, e.getMessage, Toast.LENGTH_LONG).show }
        }
      }
    }
  }

  private def runService(): Unit = {
    val gcmManager = GcmNetworkManager.getInstance(LoginActivity.this)
//    Log.i("SCALA", s"Class: ${classOf[PipelineService]}")
//    val task = new PeriodicTask.Builder().
//      setService(classOf[TestService]).
//      setPeriod(60).
//      setFlex(10).
//      setTag("flymer_periodic_task").
//      setPersisted(true).
//      build()
    val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this)
    if (ConnectionResult.SUCCESS == resultCode) Toast.makeText(LoginActivity.this, "Founded Google services", Toast.LENGTH_SHORT).show //gcmManager.schedule(task)
    else Toast.makeText(LoginActivity.this, "Cannot run Google services", Toast.LENGTH_SHORT).show
  }
}
