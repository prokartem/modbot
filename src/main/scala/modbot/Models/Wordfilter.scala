package modbot.Models

import cats._
import cats.effect._
import cats.implicits._
import modbot.AppConfig.config

import scala.io.Source

class Wordfilter[F[_]: Applicative: Sync] {
  private def readWords: Map[String, F[List[String]]] =
    config.badWordsLanguages
      .map(language =>
        language -> Resource
          .fromAutoCloseable(
            Source.fromFile(s"./src/main/resources/$language.conf").pure[F],
          )
          .use(file => file.getLines().toList.pure[F]),
      )
      .toMap

  val getWords: F[List[String]] =
    this.readWords.values.toList.sequence.flatMap(_.flatten.pure[F])
}
