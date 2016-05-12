package com.dwolla.testutils

import org.specs2.mock.Mockito

import scala.language.implicitConversions

trait WithBehaviorMocking { this: Mockito ⇒
  implicit def addWithBehavior[T](t: T): WithBehavior[T] = new WithBehavior[T](t)
}

class WithBehavior[T](t: T) {
  def withBehavior(body: T ⇒ Unit): T = {
    body(t)
    t
  }
}
