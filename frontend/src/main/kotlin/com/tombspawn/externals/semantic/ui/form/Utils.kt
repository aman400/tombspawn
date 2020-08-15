package com.tombspawn.externals.semantic.ui.form

import kotlinx.serialization.Serializable


@Serializable
data class FormError(var content: String? = null, var pointing: Pointing? = null) {
    enum class Pointing {
        below,
        above,
        left,
        right
    }
}

fun formError(block: FormError.() -> Unit) = FormError().apply(block)