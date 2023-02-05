# CallingHome Bot

This is a simple Telegram bot written in Java, that monitors a website for arbitrary conditions and sends a message to 
a Telegram chat when new content is available. While it is quite extensible, it is currently only features a way to 
monitor the classifieds section of the  [www.ebay-kleinanzeigen.de](https://www.ebay-kleinanzeigen.de) website, since 
that is what I needed it for and wasn't able to find any other solution that would exactly match my needs 
(i.e. Dockerized container, simple, file based database and a blocklist solution).
                                                                       
## Features
                            
* Will send full title, content and the thumbnail of the new content to the Telegram chat
* Uses a simple, file based H2 database to keep track of the last seen content, so it can be restarted without losing any data
* Has an automated database cleanup, so the database doesn't grow indefinitely
* Can be run locally, or as a Docker container (batteries included)
* Uses [Dotenv](https://github.com/cdimascio/dotenv-java) to load environment variables from a `.env` file, so it can 
be run locally without having to set environment variables
                                                   
Be advised that the bot is single user, single url only. If you need to monitor multiple urls, you will have to run multiple instances of the bot.

## Usage

Follow these instructions on how to set up your own a Telegram bot: https://core.telegram.org/bots/features#botfather
then, copy the token and chat id from the botfather response and set them as environment variables.

The bot is intended to be run as a docker container. The following environment variables are required for it to work:

* `BOT_TOKEN`: The token of the telegram bot
* `CHAT_ID`: The id of the chat to send the messages to
* `INTERVAL`: The interval in minutes to check the url for new content
* `EBAY_KLEINANZEIGEN_URL`: The ebay kleinanzeigen url to monitor, e.g. `https://www.ebay-kleinanzeigen.de/s-berlin/arcade-automat/k0l3331`
* `DENY_LIST`: A comma separated list of words that will be used to filter out unwanted content



## Building
                     
### For local usage
The bot can be built using maven. The following command will build an `app.jar` file in the `target` directory:

    mvn clean package

Feel free to place the following blank `.env` file in the root directory of the project and adjust to your needs:

```
CHAT_ID=MyTelegramChatId
TELEGRAM_BOT_NAME=MyTelegramBotName
TELEGRAM_BOT_TOKEN=MyTelegramBotToken
DENY_LIST=a,b,c
EBAY_KLEINANZEIGEN_URL=https://www.ebay-kleinanzeigen.de/s-berlin/arcade-automat/k0l3331
INTERVAL=10
```

Or simply set the environment variables in the OS of your choice and run the jar file:

    java -jar target/app.jar
                                                                                                               
### For Docker
The bot can also be built as a docker container. The following command will build a docker image called `callinghome-bot`:

    docker build -t callinghome-bot .

Place these environment variables in the Dockerfile and adjust to your needs:                                

```
ENV CHAT_ID=MyTelegramChatId
ENV TELEGRAM_BOT_NAME=MyTelegramBotName
ENV TELEGRAM_BOT_TOKEN=MyTelegramBotToken
ENV DENY_LIST=a,b,c
ENV EBAY_KLEINANZEIGEN_URL=https://www.ebay-kleinanzeigen.de/s-berlin/arcade-automat/k0l3331
ENV INTERVAL=10
```

## Disclaimer

This bot is not affiliated with eBay Kleinanzeigen in any way. It is not intended to be used in a way that violates their ToS
