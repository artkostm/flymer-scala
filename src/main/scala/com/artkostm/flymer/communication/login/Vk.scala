package com.artkostm.flymer.communication.login

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.{CookieManager, WebView, WebViewClient}
import com.artkostm.flymer.communication.Flymer

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
    webView.loadUrl(Flymer.OAuthUrl)
  }
}

class VkWebViewClient(interceptor: VkCookieInterceptor) extends WebViewClient {
  override def onPageFinished(view: WebView, url: String): Unit = {
    if (url.contains(Flymer.BaseUrl)) {
      val cookie = CookieManager.getInstance().getCookie(Flymer.BaseUrl)
      interceptor.onCookieIntercepted(cookie)
    }
  }

  override def onPageStarted(view: WebView, url: String, favicon: Bitmap): Unit = interceptor.onPageStarted()
}

trait VkCookieInterceptor {
  def onCookieIntercepted(cookieString: String): Unit
  def onPageStarted(): Unit
}
