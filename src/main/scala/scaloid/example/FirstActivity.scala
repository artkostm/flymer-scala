package scaloid.example

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{EditText, LinearLayout, TextView, Toast}
import com.artkostm.flymer.view.Tweaks
import macroid.{Contexts, Ui}
import macroid.FullDsl._
import macroid.contrib.LpTweaks._

/**
 * Created by arttsiom.chuiko on 03/10/2016.
 */
class FirstActivity extends Activity with Contexts[Activity] {
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    var label = slot[TextView]

    val mainView = l[LinearLayout] (
      w[EditText] <~ matchWidth,
      w[EditText] <~ matchWidth <~ Tweaks.password(),
      w[TextView] <~ wire(label) <~ text("This is Test layout for Flymer login activity")
    ) <~ vertical

    setContentView(mainView.get)

    label.get.setOnClickListener {
      new OnClickListener {
        override def onClick(v: View): Unit = Toast.makeText(FirstActivity.this, "Hello, Lol!", Toast.LENGTH_LONG).show();
      }
    }
  }
}
