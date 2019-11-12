package com.tombspawn.skeleton.locations

import io.ktor.locations.Location

@Location("/references")
data class References(val branchLimit: Int = -1, val tagLimit: Int = -1)