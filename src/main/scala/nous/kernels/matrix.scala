package nous.kernels

import spire.algebra._
import spire.math._
import spire.implicits._

object matrix {

  def gemm_ref[A](
      transA  : String,
      transB  : String,
      m       : Int,
      n       : Int,
      k       : Int,
      alpha   : A,
      a       : Array[A],
      b       : Array[A],
      beta    : A,
      c       : Array[A])(
      implicit
      field: Field[A]): Unit = {

    val lda = if (transA == "N") k else m
    val ldb = if (transB == "N") n else k
    val ldc = m

    cfor(0)(_ < m, _ + 1) { i =>
      cfor(0)(_ < n, _ + 1) { j =>
        c.update(i * ldc + j, c(i * ldc + j) * beta)
      }
    }

    (transA == "T", transB == "T") match {
      case (false, false) =>
        cfor(0)(_ < m, _ + 1) { i =>
          cfor(0)(_ < k, _ + 1) { kk =>
            val ap = alpha * a(i * lda + kk)
            cfor(0)(_ < n, _ + 1) { j =>
              c.update(i * ldc + j, c(i * ldc + j) + ap * b(kk * ldb + j))
            }
          }
        }

      case (true, false) =>
        cfor(0)(_ < m, _ + 1) { i =>
          cfor(0)(_ < k, _ + 1) { kk =>
            val ap = alpha * a(kk * lda + i)
            cfor(0)(_ < n, _ + 1) { j =>
              c.update(i * ldc + j, c(i * ldc + j) + ap * b(kk * ldc + j))
            }
          }
        }

      case (false, true) =>
        cfor(0)(_ < m, _ + 1) { i =>
          cfor(0)(_ < n, _ + 1) { j =>
            var s = field.zero
            cfor(0)(_ < k, _ + 1) { kk =>
              s = s + alpha * a(i * lda + kk) * b(j * ldb + kk)
            }
            c.update(i * ldc + j, c(i * ldc + j) + s)
          }
        }

      case _ =>
        cfor(0)(_ < m, _ + 1) { i =>
          cfor(0)(_ < n, _ + 1) { j =>
            var s = field.zero
            cfor(0)(_ < k, _ + 1) { kk =>
              s = s + alpha * a(i + kk * lda) * b(kk + j * ldb)
            }
            c.update(i * ldc + j, c(i * ldc + j) + s)
          }
        }
    }
  }

  private[nous] def blockTranspose[A](inR: Int, inC: Int, in: Array[A], out: Array[A]) = {
    val XOVER = 60

    var r = 0
    val rsz = inR
    val csz = inC
    while (r < rsz) {
      val blockHeight = if (XOVER < rsz - r) XOVER else rsz - r
      var inRow  = r * csz  // first element of current row
      var outCol = r        // first element of current col
      var c = 0
      while (c < csz) {
        val blockWidth = if (XOVER < csz - c) XOVER else csz - c
        val rowEnd = inRow + blockWidth
        while (inRow < rowEnd) {
          var rowSrc = inRow
          var colDst = outCol
          val colEnd = colDst + blockHeight
          while (colDst < colEnd) {
            out(colDst) = in(rowSrc)
            colDst += 1
            rowSrc += csz
          }
          outCol += rsz
          inRow += 1
        }
        c += XOVER
      }
      r += XOVER
    }
  }

  private[nous] def squareTranspose[A](sz: Int, out: Array[A]) = {
    val csz = sz
    val rsz = sz

    var i = 0
    var idx1 = 1
    var cols = csz
    while (i < rsz) {
      var idx2 = (i + 1) * csz + i
      while (idx1 < cols) {
        val v = out(idx1)
        out(idx1) = out(idx2)
        out(idx2) = v
        idx1 += 1
        idx2 += csz
      }
      i += 1
      idx1 += (i + 1)
      cols += csz
    }
  }

}
