package com.artkostm.flymer

import android.app.Activity
import android.content.Intent
import android.os.{Bundle, Handler}

/**
 * Created by artsiom.chuiko on 21/11/2016.
 */
class SplashActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    import com.artkostm.flymer.utils.FlymerImplicits._
    new Handler().postDelayed({
      Thread.sleep(2000)
      startActivity(new Intent(SplashActivity.this, classOf[LoginActivity]))
      finish
    }, 2000)
  }
}
