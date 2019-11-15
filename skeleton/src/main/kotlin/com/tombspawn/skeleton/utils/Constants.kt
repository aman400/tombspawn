package com.tombspawn.skeleton.utils

import com.tombspawn.base.common.CallSuccess
import com.tombspawn.base.common.Response
import kotlinx.coroutines.delay

class Constants {
    object Common {
        const val OUTPUT_SEPARATOR = "##***##"
        const val ARG_OUTPUT_SEPARATOR = "OUTPUT_SEPARATOR"
    }
    object Apis {
        const val TYPE_SELECT_BRANCH: String = "BRANCH"
        const val TYPE_SELECT_APP_PREFIX: String = "APP_PREFIX"
        const val TYPE_ADDITIONAL_PARAMS = "ADDITIONAL_PARAMS"
    }
}