package com.tombspawn.component.skeleton

import com.tombspawn.externals.uuid.v4

data class GitConfig(
    var uuid: String,
    var id: String? = null,
    var tasks: List<String>? = null,
    var outputDir: String? = null,
    var useCache: Boolean? = false,
)

fun gitConfig(uuid: String = v4(), block: GitConfig.() -> Unit) = GitConfig(uuid).apply(block)
