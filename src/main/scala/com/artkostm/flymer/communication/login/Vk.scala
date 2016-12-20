package com.artkostm.flymer.communication.login

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.{CookieManager, WebView, WebViewClient}

/**
  * Created by artsiom.chuiko on 15/10/2016.
  */
object Vk {

}

class VkLoginDialog(context: Context, interceptor: VkCookieInterceptor) extends Dialog(context) {
  val webView = new WebView(context)
  webView.setWebViewClient(new VkWebViewClient(interceptor))
  webView.getSettings.setJavaScriptEnabled(true)
  webView.setHorizontalScrollBarEnabled(false)

  override def onCreate(savedInstanceState: Bundle): Unit = {
    setContentView(webView)
    setTitle("Vk.com")
    webView.loadUrl("https://oauth.vk.com/authorize?client_id=3206293&scope=&redirect_uri=http%3A%2F%2Fflymer.ru%2Foauth%2Fvk&response_type=code&v=5.4")
  }
}

class VkWebViewClient(interceptor: VkCookieInterceptor) extends WebViewClient {
  override def onPageFinished(view: WebView, url: String): Unit = {
    Log.i("VkWebViewClient.onPageFinished", url)
    if (url.contains("http://flymer.ru")) {
      val cookie = CookieManager.getInstance().getCookie("http://flymer.ru/")
      interceptor.onCookieIntercepted(cookie)
    }
  }
}

trait VkCookieInterceptor {
  def onCookieIntercepted(cookieString: String): Unit
}
