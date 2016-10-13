package com.artkostm.flymer.communication.okhttp3

import android.content.Context
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient

/**
  * Created by artsiom.chuiko on 13/10/2016.
  */
object Client {

  def RegisterClient(context: Context): OkHttpClient = {
    val cookieJar = new PersistentCookieJar(new SetCookieCache, new SharedPrefsCookiePersistor(context))
    new OkHttpClient()
  }
  
}

class Client {

}
