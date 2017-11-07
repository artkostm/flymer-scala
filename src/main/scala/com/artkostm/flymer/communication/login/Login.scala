package com.artkostm.flymer.communication.login

import android.util.Log
import com.artkostm.flymer.communication.Flymer
import com.artkostm.flymer.communication.login.algebras.Account
import freestyle._

import scala.util.Try

/**
  * Created by artsiom.chuiko on 08/10/2016.
  */

@module trait FlymerLoginModule {
  val loader: PageLoader
  val dataExtractor: PageDataExtractor
  val keyResolver: KeyResolver
}

class Login [F[_]](implicit FLM: FlymerLoginModule[F]) {
  import FLM._

  type FS[A] = FreeS[F, A]

  def loginViaFlymer(account: Account): FS[Try[LoginInfo]] =
    for {
      connection <- loader.loadLoginPage()
      document <- dataExtractor.extractDocument(connection)
      cookies <- dataExtractor.extractCookies(connection)
      fkey <- keyResolver.getFkey(document)
      lkey <- keyResolver.getLkey(document)
      dkey <- keyResolver.getDkey(fkey)
      ac <- loader.loadAccount(account, Tuple3(fkey, lkey, dkey), cookies)
    } yield {
      Log.i("FLYMER_APP", s"$ac, $fkey")
      Try(LoginInfo(ac, fkey, cookies(Flymer.Sid)))
    }
}