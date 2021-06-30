package modbot.ActingEntities

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import com.typesafe.scalalogging._
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.Json
import modbot.Database.Storage
import modbot.Models.{BotCommand => BC, RegularMessage, _}
import modbot.TelegramAPI
import org.http4s.circe.CirceSensitiveDataEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.slf4j.LoggerFactory

class Processor[F[_]: ConcurrentEffect: ContextShift: Timer](
    client: Client[F],
    topic: Topic[F, Option[Update]],
    storage: Storage[F],
) {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private val telegram = new TelegramAPI[F]

  def onlyRegularMessages(wordfilter: List[String]): Stream[F, Unit] =
    topic
      .subscribe(10)
      .collect { case Some(Update(_, msg @ RegularMessage(_, _, _, _, _))) =>
        msg
      }
      .parEvalMap(10)(message =>
        for {
          _ <-
            if (
              wordfilter
                .exists(message.text.getOrElse("").map(_.toLower).contains(_))
            )
              for {
                _   <- storage.addUser(message.fromUser)
                res <- storage.startMonitoring(message)
                _ <- res match {
                  case Left(_) =>
                    client.status(telegram.banChatMember(message))
                  case Right(unit) => unit.pure[F]
                }
                _ <- client.status(telegram.deleteMessage(message))
                msgText = message.text.getOrElse("")
                _ <- client.status(
                  telegram.sendMessage(
                    message
                      .copy(text = Some(s"This message was deleted:\n$msgText")),
                  ),
                )
                _ = logger
                  .info(s"PROCESSOR DELETED MESSAGE ID: ${message.toString}")
              } yield ()
            else
              logger
                .info(s"PROCESSOR MESSAGE ID: ${message.toString}")
                .pure[F]
        } yield (),
      )

  def onlyBotCommands: Stream[F, Unit] =
    topic
      .subscribe(10)
      .collect {
        case Some(Update(_, msg @ BC.Start(_, _, _, _, _))) => msg
        case Some(Update(_, msg @ BC.Help(_, _, _, _, _)))  => msg
        case Some(Update(_, msg @ BC.Stop(_, _, _, _, _)))  => msg
      }
      .evalMap {
        case msg @ BC.Start(_, _, _, Some(BC.start), _) =>
          msg.chat.typeChat match {
            case "group" | "supergroup" =>
              logger.info("Start bot command without mention").pure[F]
            case _ =>
              for {
                json <- client.expect[Json](telegram.sendMessage(msg.answer))
                _ = logger
                  .info(s"PROCESSOR: \n$json")
                _ <- storage.addChat(msg.chat)
                _ = logger
                  .info(s"This chat was added to db: ${msg.chat.toString}")
              } yield ()
          }
        case msg @ BC.Start(_, _, _, Some(BC.startWithMention), _) =>
          for {
            json <- client.expect[Json](telegram.sendMessage(msg.answer))
            _ = logger
              .info(s"PROCESSOR: \n$json")
            _ <- storage.addChat(msg.chat)
            _ = logger
              .info(s"This chat was added to db: ${msg.chat.toString}")
          } yield ()

        case msg @ BC.Help(_, _, _, Some(BC.help), _) =>
          msg.chat.typeChat match {
            case "group" | "supergroup" =>
              logger.info("Help bot command without mention").pure[F]
            case _ =>
              for {
                json <- client.expect[Json](telegram.sendMessage(msg.answer))
                _ = logger
                  .info(s"PROCESSOR: \n$json")
              } yield ()
          }
        case msg @ BC.Help(_, _, _, Some(BC.helpWithMention), _) =>
          for {
            json <- client.expect[Json](telegram.sendMessage(msg.answer))
            _ = logger
              .info(s"PROCESSOR: \n$json")
          } yield ()

        case msg @ BC.Stop(_, _, _, Some(BC.stop), _) =>
          msg.chat.typeChat match {
            case "group" | "supergroup" =>
              logger.info("Stop bot command without mention").pure[F]
            case _ =>
              for {
                json <- client.expect[Json](telegram.sendMessage(msg.answer))
                _ = logger
                  .info(s"PROCESSOR: \n$json")
              } yield ()
          }
        case msg @ BC.Stop(_, _, _, Some(BC.stopWithMention), _) =>
          for {
            json <- client.expect[Json](telegram.sendMessage(msg.answer))
            _ = logger
              .info(s"PROCESSOR: \n$json")
          } yield ()
      }
}

object Processor {
  def apply[F[_]: ConcurrentEffect: ContextShift: Timer](
      client: Client[F],
      topic: Topic[F, Option[Update]],
      storage: Storage[F],
  ) = new Processor[F](client, topic, storage)
}
