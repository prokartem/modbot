# modbot
Purely functional Telegram bot for cleaning groups from dirty, naughty, obscene and otherwise bad language.

Criteria: default wordlist that you can find 
in [`src/main/resources/<language>.conf`](https://github.com/prokartem/modbot/tree/public/src/main/resources)

Ban chat member after user reached default limit of bad words.

## Launching the bot
WORK IN PROGRESS, BUGS ARE POSSIBLE!!

1. Create a new bot for yourself with [@BotFather](https://core.telegram.org/bots#6-botfather)
2. Set configuration values [`src/main/resources/application.conf`](https://github.com/prokartem/modbot/blob/public/src/main/resources/application.conf)
3. `sbt run`
4. Add to your group and promote to admin

## Implemented with:
- [Cats](https://github.com/typelevel/cats)
- [FS2](https://github.com/typelevel/fs2)
- [Http4s](https://github.com/http4s/http4s)
- [Doobie](https://github.com/tpolecat/doobie)
- [PostgreSQL](https://www.postgresql.org/)
- [Pureconfig](https://github.com/pureconfig/pureconfig)


