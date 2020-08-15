package com.tombspawn.component.utils

import com.tombspawn.externals.semantic.ui.form.FormError
import kotlin.js.json

fun String.toUIError(pointing: FormError.Pointing = FormError.Pointing.above) = json("content" to this, "pointing" to pointing.name)