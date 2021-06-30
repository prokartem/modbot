package modbot

import cats.effect.concurrent.Ref
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.concurrent.Topic
import modbot.ActingEntities._
import modbot.Database.Postgres
import modbot.Models.{Update, Wordfilter}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._

object Main extends IOApp {
  private val telegram   = new TelegramAPI[IO]
  private val db         = new Postgres[IO]
  private val wordfilter = new Wordfilter[IO]

  override def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global)
      .withIdleTimeout(650.seconds)
      .withRequestTimeout(600.seconds)
      .resource
      .use(client =>
        for {
          _            <- db.migrate
          updateId     <- Ref.of[IO, Long](0)
          wf           <- wordfilter.getWords
          updatesTopic <- Topic[IO, Option[Update]](None)

          requester = Requester(client, updateId)
            .withRequestF(telegram.getUpdatesWithVariable(updateId))
            .withDuration(500.milliseconds)
            .run
            .through(updatesTopic.publish)

          processRegularMessage =
            Processor(client, updatesTopic, db).onlyRegularMessages(wf)
          processBotCommands =
            Processor(client, updatesTopic, db).onlyBotCommands

          _ <- Stream(requester, processRegularMessage, processBotCommands)
            .parJoin(3)
            .compile
            .drain
        } yield ExitCode.Success,
      )
}
