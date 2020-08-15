package com.tombspawn.skeleton

import com.tombspawn.ApplicationService
import com.tombspawn.models.config.App


suspend fun ApplicationService.addNewApp(app: App) {
    val callbackUri = baseUri.get().path("apps", app.id, "init").build().toString()
//    dockerService.createContainer(app, common.basePort + index, SKELETON_DEBUG_PORT + index, callbackUri)
}
