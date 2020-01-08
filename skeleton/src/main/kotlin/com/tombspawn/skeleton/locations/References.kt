package com.tombspawn.skeleton.locations

import io.ktor.locations.Location

@Location("/references")
data class References(val callbackUri: String, val branchLimit: Int = -1, val tagLimit: Int = -1)

@Location("/flavours")
data class Flavours(val callbackUri: String)

@Location("/build-variants")
data class BuildVariants(val callbackUri: String)