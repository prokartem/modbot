package modbot.ActingEntities

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import com.typesafe.scalalogging._
import fs2.{Pure, Stream}
import io.circe.Json
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import modbot.Models._
import modbot.TelegramAPI
import org.http4s._
import org.http4s.circe.jsonDecoder
import org.http4s.client.Client
import org.slf4j.LoggerFactory

import scala.concurrent.duration.{DurationInt, FiniteDuration}

class Requester[F[_]: ConcurrentEffect: ContextShift: Timer](
    val client: Client[F],
    val refUpdateId: Ref[F, Long],
    val requestF: F[Request[F]],
    val duration: FiniteDuration,
) {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private def copy(
      client: Client[F] = client,
      refUpdateId: Ref[F, Long] = refUpdateId,
      requestF: F[Request[F]] = requestF,
      duration: FiniteDuration = duration,
  ) =
    new Requester[F](
      client = client,
      refUpdateId = refUpdateId,
      requestF = requestF,
      duration = duration,
    ) {}

  def withRequestF(req: F[Request[F]]): Requester[F]  = copy(requestF = req)
  def withDuration(dur: FiniteDuration): Requester[F] = copy(duration = dur)

  private def requestLogic =
    for {
      req <- requestF
      _ = logger.info("Sending request...")
      json         <- client.expect[Json](req)
      updatesMaybe <- json.as[Stream[Pure, Update]].pure[F]
      res <- updatesMaybe match {
        case Left(error) =>
          for {
            _ <- logger
              .info(s"Something goes wrong with decoding json: $error")
              .pure[F]
            _ <- refUpdateId.update(_ + 1)
          } yield Stream[F, Option[Update]](None)
        case Right(stream) =>
          for {
            list <- stream.compile.toList.pure[F]
            _ = list.map(upd => logger.info(upd.show))
            updateId =
              if (list.isEmpty) 0
              else list.last.update_id
            _ <- refUpdateId.set(updateId + 1L)
            res = stream.covary[F].map(_.some)
          } yield res
      }
    } yield res

  def run: Stream[F, Option[Update]] =
    Stream
      .awakeEvery[F](duration)
      .zipRight(Stream.repeatEval(requestLogic))
      .flatten
}

object Requester {
  def apply[F[_]: ConcurrentEffect: ContextShift: Timer](
      client: Client[F],
      refUpdateId: Ref[F, Long],
  ) =
    new Requester[F](
      client = client,
      refUpdateId = refUpdateId,
      requestF = new TelegramAPI[F].getUpdates().pure[F],
      duration = 5.seconds,
    )
}
