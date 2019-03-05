/*
 * Copyright (C) 2016-2018 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.stream.alpakka.stdinout.impl

import akka.annotation.InternalApi
import akka.stream._
import akka.stream.alpakka.stdinout.StdinSourceReader
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler, StageLogging}

import scala.util.{Failure, Success, Try}

/**
 * INTERNAL API
 */
@InternalApi
private[stdinout] final class StdinSource(reader: StdinSourceReader) extends GraphStage[SourceShape[String]] {

  val out: Outlet[String] = Outlet("StdinSource")
  override val shape: SourceShape[String] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new StdinSourceStageLogic(shape, reader)
}

/**
 * INTERNAL API
 */
@InternalApi
private[stdinout] final class StdinSourceStageLogic(val shape: SourceShape[String], reader: StdinSourceReader)
    extends GraphStageLogic(shape)
    with StageLogging {

  private def out: Outlet[String] = shape.out

  setHandler(
    out,
    new OutHandler {
      override def onPull(): Unit = {
        log.debug("StdinSource has been pulled")
        val stringFromReader: Try[Option[String]] = reader.read()
        log.debug(s"StdinSource has read message from reader of: $stringFromReader")

        stringFromReader match {
          case Success(Some(msg)) => push(out, msg)
          case Success(None) => completeStage()
          case Failure(exc) => failStage(exc)
        }
      }
    }
  )
}
