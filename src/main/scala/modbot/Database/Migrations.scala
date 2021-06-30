package modbot.Database

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux

object Migrations {
  private val migration: Update0 =
    sql"""
         CREATE TABLE IF NOT EXISTS chats(
            id BIGINT NOT NULL,
            title TEXT NOT NULL,
            type TEXT NOT NULL,
            limit_bad_words INT NOT NULL,
            PRIMARY KEY (id)
         );
         CREATE TABLE IF NOT EXISTS users(
            id BIGINT NOT NULL,
            name TEXT NOT NULL,
            PRIMARY KEY (id)
         );
         CREATE TABLE IF NOT EXISTS state(
            user_id BIGINT NOT NULL,
            chat_id BIGINT NOT NULL,
            current_free_bad_words INT NOT NULL,
            CHECK (current_free_bad_words > 0),
            PRIMARY KEY (user_id, chat_id),
            FOREIGN KEY (chat_id)
               REFERENCES chats (id)
               ON DELETE CASCADE,
            FOREIGN KEY (user_id)
               REFERENCES users (id)
               ON DELETE CASCADE
         );         
         """.update
  def migrate[F[_]: Sync](xa: Aux[F, Unit]): F[Unit] =
    migration.run.void.transact(xa)
}
