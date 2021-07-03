package modbot

import pureconfig.ConfigReader.Result
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object AppConfig {
  final case class AppConfig(
      token: String,
      botName: String,
      defaultBadWordsLimit: Int,
      badWordsLanguages: List[String],
  )

  val config: AppConfig = ConfigSource.default.load[AppConfig] match {
    case Right(conf) => conf
    case Left(error) => throw new Exception(error.toString())
  }
}
