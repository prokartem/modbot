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
//  val loaded: Result[AppConfig] = ConfigSource.default.load[AppConfig]
//  assert(loaded.isRight, s"Can not load config $loaded")
//
//  val config: AppConfig = loaded.getOrElse(AppConfig("", "", ""))

//  final val token: String = {
//    val loaded = ConfigSource.default.load[String]
//    assert(loaded.isRight, s"Can not load token $loaded")
//    loaded.getOrElse("")
//  }
//  final val botName: String = {
//    val loaded = ConfigSource.default.load[String]
//    assert(loaded.isRight, s"Can not load botName $loaded")
//    loaded.getOrElse("")
//  }
//  final val defaultBadWordsLimit = {
//    val loaded = ConfigSource.default.load[String]
//    assert(loaded.isRight, s"Can not load default bad words limit $loaded")
//    loaded.getOrElse("")
//  }
}
