package nous.kernels

import scala.{specialized => sp}
import scala.reflect.ClassTag

import cats.Eval
import spire.algebra._
import spire.math._
import spire.implicits._

object activations {

  implicit val dim = JetDim(3)

  def linear[A](x: A) = x

  def relu[A: Order](x: A)(implicit f: Field[A]): A = spire.math.max(f.zero, x)

  def sigmoid[A: Field](x: A)(implicit t: Trig[A]) =
    1 / (1 + t.exp(implicitly[Field[A]].negate(x)))

  def softmax[A: Field](x: Vector[A])(implicit t: Trig[A], o: Order[A]): Vector[A] = {
    val eval =
      for {
        xmax <- Eval.later(x.view.reduce(o.max))
        xp <- Eval.later(x.view.map(a => t.exp(a - xmax)))
        xpsum <- Eval.later(xp.view.reduce(_ + _))
    } yield xp.view.map(_ * (1 / xpsum))
    eval.value.toVector
  }

  def softplus[A](x: A)(implicit t: Trig[A]): A = t.log1p(t.exp(x))

  def tanh[A: Trig](x: A) = spire.math.tanh(x)

}
