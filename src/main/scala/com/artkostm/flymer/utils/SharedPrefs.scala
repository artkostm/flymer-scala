package com.artkostm.flymer.utils

import android.content.Context

/**
  * Created by artsiom.chuiko on 06/03/2017.
  */
class SharedPrefs(context: Context) {
  val appPrefs = context.getSharedPreferences("FlymerApp", Context.MODE_PRIVATE)

  import scala.collection.JavaConversions._
  def load() = appPrefs.getAll.toMap

  def save(prefMap: Map[String, String]): Unit = {
    val editor = appPrefs.edit()
    prefMap.foreach { entry => editor.putString(entry._1, entry._2) }
    editor.apply()
  }

  def remove(prefMap: Map[String, String]): Unit = {
    val editor = appPrefs.edit()
    prefMap.keys.foreach(editor.remove(_))
    editor.apply()
  }

  def clear(): Unit = appPrefs.edit().clear().apply()
}
