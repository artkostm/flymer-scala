package com.artkostm.flymer

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.{LinearLayout, TextView}
import macroid.{Contexts, Ui}
import macroid.FullDsl._
import com.artkostm.flymer.utils.SharedPrefs
import com.artkostm.flymer.view.Tweaks
/**
 * Created by artsiom.chuiko on 06/03/2017.
 */
class UserActivity extends AppCompatActivity with Contexts[Activity]{
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    val prefs = new SharedPrefs(getApplicationContext)
    val pairs = prefs.load()
    pairs.foreach(c => Log.i("TEEEST", c._1))
    setContentView((l[LinearLayout](
      w[TextView]
        <~ {
          if (pairs.keys.exists(_ == UserActivity.Vk)) text("logged via Vk")
          else text(pairs.get(UserActivity.Email).map(_.toString).getOrElse("None :("))
        }
        <~ Tweaks.textGravity(Gravity.CENTER),
      w[AppCompatButton]
        <~ On.click(Ui {
          prefs.clear()
          finish()
        })
        <~padding(all = 12 dp)
        <~ text("Exit")
        <~ Tweaks.mp(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, top = 24 dp, bottom = 24 dp)
    ) <~ vertical
      <~ lp[LinearLayout](LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)).get)
  }

}

object UserActivity {
  val Email = "email"
  val Pass = "pass"
  val Vk = "vk"
}