package com.ramukaka.utils

class Constants {
    class Common {
        companion object {
            const val LOCALHOST: String = "127.0.0.1"
            const val DEFAULT_REDIS_PORT = 6739

            const val APP_CONSUMER = "consumer"

            const val OUTPUT_SEPARATOR = "##***##"
            const val ARG_OUTPUT_SEPARATOR = "OUTPUT_SEPARATOR"

            const val GET = "GET"
            const val PUT = "PUT"
            const val POST = "POST"
            const val DELETE = "DELETE"
            const val PATCH = "PATCH"
            const val HEAD = "HEAD"
            const val OPTIONS = "OPTIONS"
        }
    }

    class EnvironmentVariables {
        companion object {
            const val ENV_UPLOAD_DIR_PATH = "UPLOAD_DIR_PATH"
        }
    }

    class Slack {
        companion object {
            const val SESSION = "slack_session"
            const val CALLBACK_STANDUP_MESSAGE: String = "callback_standup_message"
            const val CALLBACK_STANDUP_DIALOG: String = "callback_standup_dialog"
            const val DEFAULT_BOT_ID: String = "USLACKBOT"

            const val CALLBACK_GENERATE_APK = "generate_apk_"
            const val CALLBACK_CONFIRM_GENERATE_APK = "confirm_generate_apk_"
            const val CALLBACK_CREATE_API = "create_api"

            const val TOKEN = "token"
            const val CHANNEL = "channel"
            const val TEXT = "text"
            const val TS = "ts"
            const val ATTACHMENTS = "attachments"
            const val DIALOG = "dialog"
            const val USER = "user"
            const val CURSOR = "cursor"
            const val LIMIT = "limit"
            const val CLIENT_ID = "client_id"
            const val CLIENT_SECRET = "client_secret"
            const val CODE = "code"

            const val CALLBACK_SUBSCRIBE_CONSUMER = "subscribe_consumer_"

            const val TYPE_SUBSCRIBE_CONSUMER = "subscribe-consumer"
            const val TYPE_SUBSCRIBE_FLEET = "subscribe-fleet"
            const val TRIGGER_ID: String = "trigger_id"
            const val CHANNEL_ID: String = "channel_id"
            const val USER_ID: String = "user_id"

            const val TYPE_SELECT_VERB = "VERB"
            const val TYPE_SELECT_RESPONSE = "RESPONSE"

            const val TYPE_SELECT_BRANCH: String = "BRANCH"
            const val TYPE_SELECT_BUILD_TYPE: String = "BUILD_TYPE"
            const val TYPE_SELECT_FLAVOUR: String = "FLAVOUR"
            const val TYPE_SELECT_APP_PREFIX: String = "APP_PREFIX"
            const val TYPE_SELECT_URL: String = "APP_URL"
            const val TYPE_ADDITIONAL_PARAMS = "ADDITIONAL_PARAMS"
            const val TYPE_CREATE_MOCK_API = "mock-api"
        }
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

    object RequestErr {
        const val ERR_CLIENT_ID = "ERR_MISSING_CLIENT_ID"
        const val ERR_NO_SESSION = "ERR_NO_SESSION_FOUND"
    }
}