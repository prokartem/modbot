package modbot.Models

import io.circe.{Decoder, HCursor}

case class User(id: Long, is_bot: Boolean, first_name: String) {
  def show: String =
    s"My name is $first_name. My id is $id."
}
object User {
  implicit val decodeUser: Decoder[User] = (c: HCursor) =>
    for {
      id         <- c.downField("id").as[Int]
      is_bot     <- c.downField("is_bot").as[Boolean]
      first_name <- c.downField("first_name").as[String]
    } yield User(id, is_bot, first_name)
}
