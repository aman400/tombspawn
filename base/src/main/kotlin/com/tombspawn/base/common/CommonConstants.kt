package com.tombspawn.base.common

object CommonConstants {
    const val SUCCESS_CALLBACK_URI = "success_callback_uri"
    const val FAILURE_CALLBACK_URI = "failure_callback_uri"
    const val APP_PREFIX = "app_prefix"
    const val CALLBACK_URI = "callbackUri"
    const val TAG_LIMIT = "tagLimit"
    const val BRANCH_LIMIT = "branchLimit"
    const val COMMIT_ID = "commit_id"
    const val COMMIT_MESSAGE = "commit_message"
    const val COMMIT_AUTHOR = "commit_author"
}

object SlackConstants {
    const val TYPE_SELECT_BRANCH: String = "BRANCH"
    const val TYPE_SELECT_BUILD_TYPE: String = "BUILD_TYPE"
    const val TYPE_SELECT_FLAVOUR: String = "FLAVOUR"
    const val TYPE_SELECT_APP_PREFIX: String = "APP_PREFIX"
    const val TYPE_SELECT_URL: String = "APP_URL"
    const val TYPE_ADDITIONAL_PARAMS = "ADDITIONAL_PARAMS"
    const val TYPE_CREATE_MOCK_API = "mock-api"
}