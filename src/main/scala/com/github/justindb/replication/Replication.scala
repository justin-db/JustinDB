package com.github.justindb.replication

import com.github.justindb.common.{ Node, Record }
import scalaz.Monad
import scala.language.higherKinds

trait Replication {
  def replicate[T, M[_]: Monad](r: Record[T], destinyNode: Node): M[Unit]
}

