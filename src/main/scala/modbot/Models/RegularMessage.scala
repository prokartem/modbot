package modbot.Models

import cats.implicits.{catsSyntaxOptionId, none}
import com.typesafe.scalalogging.Logger
import io.circe._
import modbot.Models.BotCommand._
import org.slf4j.LoggerFactory

trait Msg {
  val message_id: Int
  val fromUser: User
  val text: Option[String]
  val chat: Chat
}

case class Entity(typeE: String, length: Int)
object Entity {
  implicit val decodeEntity: Decoder[Entity] = (c: HCursor) =>
    for {
      typeE  <- c.downField("type").as[String]
      length <- c.downField("length").as[Int]
    } yield new Entity(typeE, length)
}

case class RegularMessage(
    message_id: Int,
    fromUser: User,
    chat: Chat,
    text: Option[String],
    typeMessage: Option[String],
) extends Msg
    with Product {
  final def show: String =
    s"\nMessage id: $message_id\nText: ${text.getOrElse("")} Type:${typeMessage.getOrElse("")} \n\n"
}
object RegularMessage {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  implicit val decodeRegularMessage: Decoder[Msg] =
    (c: HCursor) =>
      for {
        message_id <- c.downField("message_id").as[Int]
        fromUser   <- c.downField("from").as[User]
        chat       <- c.downField("chat").as[Chat]
        text       <- c.downField("text").as[Option[String]]
        entity = c.downField("entities").as[List[Entity]]
        typeMessage = entity match {
          case Left(error) => none[String]
          case Right(list) => list.head.typeE.some
        }
        lengthOfBotCommand = entity match {
          case Left(error) => none[Int]
          case Right(list) => list.head.length.some
        }
        res <- typeMessage match {
          case Some("bot_command") =>
            text match {
              case Some(BotCommand.start) | Some(
                    BotCommand.startWithMention,
                  ) =>
                Right(Start(message_id, fromUser, chat, text, typeMessage))
              case Some(BotCommand.help) | Some(BotCommand.helpWithMention) =>
                Right(Help(message_id, fromUser, chat, text, typeMessage))
              case Some(BotCommand.stop) | Some(
                    BotCommand.stopWithMention,
                  ) =>
                Right(
                  Stop(message_id, fromUser, chat, text, typeMessage),
                )
              case Some(str) =>
                Right(
                  RegularMessage(
                    message_id,
                    fromUser,
                    chat,
                    Some(s"Unknown bot command:\n${str.take(
                      lengthOfBotCommand.getOrElse(0),
                    )} ${str.drop(lengthOfBotCommand.getOrElse(0))}"),
                    typeMessage,
                  ),
                )
              case _ =>
                Left(
                  DecodingFailure(
                    s"Unknown error $message_id, $fromUser, ${chat.show}, ${text
                      .getOrElse("")}, $typeMessage",
                    List(CursorOp.Field("text")),
                  ),
                )
            }
          case _ =>
            Right(
              RegularMessage(message_id, fromUser, chat, text, typeMessage),
            )
        }
      } yield res
}
