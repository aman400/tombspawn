package com.tombspawn.utils

class Constants {
    object Common {
        const val DEFAULT_HOST = "0.0.0.0"
        const val DEFAULT_PORT = 8662
        const val SKELETON_DEBUG_PORT = 5006
        const val DEFAULT_SCHEME = "http"
        // Redis default config
        const val DEFAULT_REDIS_PORT = 6379
        const val DEFAULT_REDIS_HOST = "redis://127.0.0.1"

        const val APP_CONSUMER = "consumer"
    }

    object Slack {
        const val CALLBACK_STANDUP_MESSAGE: String = "callback_standup_message"
        const val CALLBACK_STANDUP_DIALOG: String = "callback_standup_dialog"
        const val DEFAULT_BOT_ID: String = "USLACKBOT"

        const val CALLBACK_GENERATE_APK = "generate_apk_"
        const val CALLBACK_CONFIRM_GENERATE_APK = "confirm_generate_apk_"
        const val CALLBACK_SUBSCRIBE_BRANCH = "subscribe_branch_"

        const val TOKEN = "token"
        const val CHANNEL = "channel"
        const val TEXT = "text"
        const val TS = "ts"
        const val ATTACHMENTS = "attachments"
        const val DIALOG = "dialog"
        const val USER = "user"
        const val CURSOR = "cursor"
        const val LIMIT = "limit"

        const val CALLBACK_SUBSCRIBE_CONSUMER = "subscribe_consumer_"
        const val CALLBACK_UNSUBSCRIBE_CONSUMER = "unsubscribe_consumer_"
        const val NAME_SEPARATOR = "$#---#$"

        const val TYPE_SUBSCRIBE_CONSUMER = "subscribe-consumer"
        const val TYPE_SUBSCRIBE_FLEET = "subscribe-fleet"
        const val TRIGGER_ID: String = "trigger_id"
        const val CHANNEL_ID: String = "channel_id"
        const val TEAM_ID = "team_id"
        const val TEAM_DOMAIN = "team_domain"
        const val CHANNEL_NAME = "channel_name"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val COMMAND = "command"
        const val RESPONSE_URL = "response_url"
    }

    class Github {
        companion object {
            const val HEADER_KEY_EVENT = "X-GitHub-Event"
            const val HEADER_VALUE_EVENT_PUSH = "push"
            const val HEADER_VALUE_EVENT_DELETE: String = "delete"
            const val HEADER_VALUE_EVENT_CREATE: String = "create"
            const val HEADER_VALUE_EVENT_PING: String = "ping"
        }
    }

    class Database {
        companion object {
            const val USER_TYPE_USER = "user"
            const val USER_TYPE_BOT = "bot"
        }
    }

}