package modbot.Database

import modbot.Models.{Chat, Msg, User}

trait Storage[F[_]] {
  def addChat(chat: Chat): F[Unit]
  def addUser(user: User): F[Unit]
  def startMonitoring(msg: Msg): F[Either[String, Unit]]
}
