package modbot

import cats.effect.ConcurrentEffect
import com.typesafe.scalalogging.Logger
import io.circe.literal._
import modbot.Models.Msg
import modbot.AppConfig.config
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.slf4j.LoggerFactory

class TelegramAPI[F[_]: ConcurrentEffect] {
  assert(config.token.nonEmpty, "Bot token is empty")
  assert(config.botName.nonEmpty, "Bot name is empty")
  private val baseUri             = uri"https://api.telegram.org"
  private val baseUriWithBotToken = baseUri / s"bot${config.token}"
  private val logger              = Logger(LoggerFactory.getLogger(this.getClass))

  final def getUpdates(nextUpdateId: Long = 0): Request[F] = {
    val body =
      json"""
          {
          "offset": $nextUpdateId,
          "timeout": ${config.longPollingTimeout},
          "allowed_updates": ["message"]
          }"""
    Request[F](method = Method.GET, uri = baseUriWithBotToken / "getUpdates")
      .withEntity(body)
  }

  final def banChatMember(msg: Msg): Request[F] = {
    val body =
      json"""
          {
          "chat_id": ${msg.chat.id},
          "user_id": ${msg.fromUser.id} 
      }"""
    Request[F](
      method = Method.POST,
      uri = baseUriWithBotToken / "banChatMember",
    )
      .withEntity(body)
  }

  final def sendMessage(msg: Msg): Request[F] = {
    val text: String = msg.text.getOrElse("ты чё дурак....")
    val body         =
      //    ${msg.chat.id}471322687
      json"""
          {
          "chat_id": ${msg.chat.id},
          "text": $text
          }"""

    //    val body_next_lvl = msg.asJson
    Request[F](method = Method.POST, uri = baseUriWithBotToken / "sendMessage")
      .withEntity(body)
  }

  final def deleteMessage(msg: Msg): Request[F] = {
    val body =
      json"""
          {
          "chat_id": ${msg.chat.id},
          "message_id": ${msg.message_id}
          }"""
    Request[F](
      method = Method.POST,
      uri = baseUriWithBotToken / "deleteMessage",
    )
      .withEntity(body)
  }
}
