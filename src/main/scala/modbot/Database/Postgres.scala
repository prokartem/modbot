package modbot.Database

import modbot.AppConfig.config
import modbot.Models.{Chat, Msg, User}
import doobie.{Update => U, _}
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats.effect._
import cats.implicits._
import doobie.postgres.sqlstate

class Postgres[F[_]: Async: ContextShift] extends Storage[F] {
  assert(
    config.defaultBadWordsLimit > 1,
    "Default bad words limit is empty",
  )
  private val xa = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    "jdbc:postgresql:postgres",
    "postgres",
    "password",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous),
  )

  def migrate: F[Unit] = Migrations.migrate(xa)
  def demoSelect: F[List[String]] =
    sql"select (id, title, type, limit_bad_words) from chats"
      .query[String]
      .to[List]
      .transact(xa)

  override def addChat(chat: Chat): F[Unit] =
    sql"""
         INSERT INTO chats
         VALUES (${chat.id}, ${chat.title}, ${chat.typeChat}, ${config.defaultBadWordsLimit})
         ON CONFLICT DO NOTHING;
          """.update.run.void
      .transact(xa)

  override def addUser(user: User): F[Unit] =
    sql"""
         INSERT INTO users (id, name)
         VALUES (${user.id}, ${user.first_name})
         ON CONFLICT (id) DO NOTHING;
          """.update.run.void
      .transact(xa)

  def startMonitoring(msg: Msg): F[Either[String, Unit]] =
    sql"""
         INSERT INTO state (user_id, chat_id, current_free_bad_words)
         SELECT users.id, chats.id, limit_bad_words
         FROM users, chats
         WHERE users.id = ${msg.fromUser.id} 
         AND chats.id = ${msg.chat.id}
         AND (chats.type = 'group' OR chats.type = 'supergroup')
         ON CONFLICT (user_id, chat_id)
         DO UPDATE SET current_free_bad_words = state.current_free_bad_words - 1;
          """.update.run.void
      .transact(xa)
      .attemptSomeSqlState { case sqlstate.class23.CHECK_VIOLATION =>
        "Limit is reached."
      }
}
