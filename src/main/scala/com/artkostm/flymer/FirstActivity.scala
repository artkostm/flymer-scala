package com.artkostm.flymer

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget._
import com.artkostm.flymer.login.Login.AttemptLogin
import com.artkostm.flymer.view.Tweaks
import macroid.Contexts
import macroid.FullDsl._
import macroid.contrib.LpTweaks._
import com.artkostm.flymer.utils.ViewHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by artsiom.chuiko on 03/10/2016.
 */
class FirstActivity extends Activity with Contexts[Activity] {
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    var label = slot[Button]

    val mainView = l[LinearLayout] (
      w[EditText] <~ matchWidth,
      w[EditText] <~ matchWidth <~ Tweaks.password,
      w[Button] <~ matchWidth <~ wire(label) <~ text("Log in")
    ) <~ vertical

    setContentView(mainView.get)

    label.get.setOnClickListener { source: View =>
      Future {
        AttemptLogin("artkostm@gmail.com", "061994art")
      } map { value =>
        runOnUiThread { Toast.makeText(FirstActivity.this, value, Toast.LENGTH_LONG).show() }
      }
    }
  }
}
