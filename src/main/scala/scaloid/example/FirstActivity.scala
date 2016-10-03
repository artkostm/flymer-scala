package scaloid.example

import android.app.Activity
import android.os.Bundle
import android.widget.{LinearLayout, TextView}
import macroid.{Ui, Contexts}
import macroid.FullDsl._

/**
 * Created by arttsiom.chuiko on 03/10/2016.
 */
class FirstActivity extends Activity with Contexts[Activity] {
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    val mainView = l[LinearLayout] (
      w[TextView] <~ text("This is Test layout for Flymer login activity")
    ) <~ vertical

    setContentView(mainView.get)
  }
}
