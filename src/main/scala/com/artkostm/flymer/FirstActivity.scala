package com.artkostm.flymer

import android.app.Activity
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget._
import com.artkostm.flymer.login.Login.AttemptLogin
import com.artkostm.flymer.view.Tweaks
import macroid.Contexts
import macroid.FullDsl._
import macroid.contrib.LpTweaks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by arttsiom.chuiko on 03/10/2016.
 */
class FirstActivity extends Activity with Contexts[Activity] {
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    var label = slot[Button]

    val mainView = l[LinearLayout] (
      w[EditText] <~ matchWidth,
      w[EditText] <~ matchWidth <~ Tweaks.password(),
      w[Button] <~ wire(label) <~ text("This is Test layout for Flymer login activity")
    ) <~ vertical

    setContentView(mainView.get)

    label.get.setOnClickListener {
      new OnClickListener {
        override def onClick(v: View): Unit = {
          
          Future {
            AttemptLogin()
          } map { value =>
            runOnUiThread(new Runnable {
              override def run(): Unit = Toast.makeText(FirstActivity.this, value, Toast.LENGTH_LONG).show()
            })
          }
        }
      }
    }
  }
}
