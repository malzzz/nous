package nous.network
package loss

import scala.reflect.ClassTag

import nous.kernels.loss._
import spire.algebra._

final case class MeanSquaredError[A: Field: ClassTag](implicit m: Module[Vector[A], A]) extends LossF[A] {
  def forward(y: NetworkOutput[A]): Vector[A] = {
    euclidean(y.vectorX, y.vectorY)
  }

  def backward(y: Vector[A], p: Vector[A]): Vector[A] = {
    euclideanD(y, p)
  }
}