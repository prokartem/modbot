package modbot.Models

import modbot.AppConfig.config

object BotCommand {
  assert(config.botName.nonEmpty, "Bot name is empty")
  val mention: String          = config.botName
  val start                    = "/start"
  val help                     = "/help"
  val stop                     = "/stop"
  val startWithMention: String = start ++ mention
  val helpWithMention: String  = help ++ mention
  val stopWithMention: String  = stop ++ mention

  case class Start(
      message_id: Int,
      fromUser: User,
      chat: Chat,
      text: Option[String],
      typeMessage: Option[String],
  ) extends Msg
      with Product {
    final val name: String        = start
    final val description: String = "Starts bot."

    def answer: RegularMessage = RegularMessage(
      message_id,
      fromUser,
      chat,
      Some(
        s"Hello, ${fromUser.first_name}!\n" ++ Help(
          message_id,
          fromUser,
          chat,
          text,
          typeMessage,
        ).helpText,
      ),
      typeMessage,
    )
  }

  case class Help(
      message_id: Int,
      fromUser: User,
      chat: Chat,
      text: Option[String],
      typeMessage: Option[String],
  ) extends Msg
      with Product {
    final val name: String        = help
    final val description: String = "Outputs information about bot."
    val helpText: String =
      s"""
        |What you can do with me:
        |$start  - ${Start(message_id, fromUser, chat, text, typeMessage).description}
        |$help   - $description
        |$stop   - ${Stop(message_id, fromUser, chat, text, typeMessage).description}
        |""".stripMargin
    def answer: RegularMessage = RegularMessage(
      message_id,
      fromUser,
      chat,
      Some(helpText),
      typeMessage,
    )
  }

  case class Stop(
      message_id: Int,
      fromUser: User,
      chat: Chat,
      text: Option[String],
      typeMessage: Option[String],
  ) extends Msg
      with Product {
    final val name: String        = stop
    final val description: String = "Stops moderation in current chat"

    def answer: Msg = RegularMessage(
      message_id,
      fromUser,
      chat,
      Some("Okay. You are free now.\nJust... be careful."),
      typeMessage,
    )
  }
}
