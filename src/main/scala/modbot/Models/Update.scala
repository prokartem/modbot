package modbot.Models

import fs2.{Pure, Stream}
import io.circe._
import modbot.Models.RegularMessage.decodeRegularMessage

case class Update(update_id: Long, message: Msg) {
  def show: String =
    s"My update_id: $update_id and my message: ${message.toString}"
}
object Update {
  implicit val decodeUpdate: Decoder[Update] = (c: HCursor) =>
    for {
      update_id <- c.downField("update_id").as[Long]
      message   <- c.downField("message").as[Msg]
    } yield new Update(update_id, message)
  implicit val decodeStreamOfUpdate: Decoder[Stream[Pure, Update]] =
    (c: HCursor) =>
      for {
        updatesList <- c.downField("result").as[List[Update]]
        updates = Stream.emits(updatesList)
      } yield updates
}
