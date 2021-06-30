package modbot.Models

import io.circe.{Decoder, HCursor}

case class Chat(
    id: Long,
    title: String,
    typeChat: String,
) {
  def show: String = s"My title: $title. My id: $id and type: $typeChat"
}

object Chat {
  implicit val decodeChat: Decoder[Chat] = (c: HCursor) =>
    for {
      id <- c.downField("id").as[Long]
      title <-
        if (id < 0) c.downField("title").as[String]
        else c.downField("first_name").as[String]
      typeChat <- c.downField("type").as[String]
    } yield new Chat(id, title, typeChat)
}
