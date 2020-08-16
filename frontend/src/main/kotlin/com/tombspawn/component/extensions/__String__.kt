package com.tombspawn.component.extensions

inline fun <C, R> C?.ifNullOrEmpty(defaultValue: () -> R): R where R : CharSequence, C : R =
    if (this != undefined && !isNullOrEmpty()) this else defaultValue()

fun CharSequence?.isJsNullOrEmpty(): Boolean = this == undefined || isNullOrEmpty()

fun CharSequence?.isJsNull(): Boolean = this == null || this == undefined