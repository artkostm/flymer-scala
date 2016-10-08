package com.artkostm.flymer.login

/**
  * Created by arttsiom.chuiko on 08/10/2016.
  */
object Login {
  def Dkey(fkey: String): Int = {
    fkey.foldLeft(0)( (n, g) => {
      val m = (n << 5) - n + g
      m & m
    })
  }
}

class Login {

}