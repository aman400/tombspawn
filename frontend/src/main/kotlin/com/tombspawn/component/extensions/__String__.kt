package com.tombspawn.component.extensions

inline fun <C, R> C?.ifNullOrEmpty(defaultValue: () -> R): R where R : CharSequence, C : R =
    if (this != undefined && !isNullOrEmpty()) this else defaultValue()