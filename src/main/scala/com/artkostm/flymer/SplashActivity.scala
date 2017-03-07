package com.artkostm.flymer

import android.app.Activity
import android.content.Intent
import android.os.{Bundle, Handler}

/**
 * Created by artsiom.chuiko on 21/11/2016.
 */
class SplashActivity extends Activity {
  import scala.collection.JavaConversions._
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    import com.artkostm.flymer.utils.FlymerImplicits._
    new Handler().postDelayed({
      Thread.sleep(1500)
      val cookies = getApplication.asInstanceOf[Application].sharedPrefsCookiePersistor.loadAll()
      if (cookies.exists(cookie => cookie.name() == "ac")) startActivity(new Intent(SplashActivity.this, classOf[UserActivity]))
      else startActivity(new Intent(SplashActivity.this, classOf[LoginActivity]))
      finish
    }, 2000)
  }
}
