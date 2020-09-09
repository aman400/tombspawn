package com.tombspawn.auth

import arrow.core.Either
import arrow.core.right
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import java.util.logging.Logger

class RoleAuthorization internal constructor(config: Configuration) {

    private val log = Logger.getLogger("RoleAuthorization")

    constructor(provider: RoleBasedAuthorizer) : this(Configuration(provider))

    private var config = config.copy()

    class Configuration internal constructor(var provider: RoleBasedAuthorizer) {
        internal fun copy(): Configuration = Configuration(provider)
    }


    class RoleBasedAuthorizer {
        internal var authorizationFunction: suspend ApplicationCall.(Set<Role>) -> Either<Principal, Unit> = {
            Unit.right()
        }

        fun validate(body: suspend ApplicationCall.(Set<Role>) -> Either<Principal, Unit>) {
            authorizationFunction = body
        }
    }

    fun interceptPipeline(pipeline: ApplicationCallPipeline, roles: Set<Role>) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Features, authorizationPhase)
        pipeline.intercept(authorizationPhase) {
            val call = call
            config.provider.authorizationFunction(call, roles).fold(
                {
                    log.fine("Responding unauthorized because of error\n$it")
                    call.respond(HttpStatusCode.Forbidden, "Permission is denied")
                    finish()
                },
                {
                    return@intercept
                }
            )
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, RoleBasedAuthorizer, RoleAuthorization> {
        private val authorizationPhase = PipelinePhase("authorization")

        override val key: AttributeKey<RoleAuthorization> = AttributeKey("Roles")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: RoleBasedAuthorizer.() -> Unit
        ): RoleAuthorization {
            val configuration = RoleBasedAuthorizer().apply(configure)

            return RoleAuthorization(configuration)
        }
    }
}

class AuthorisedRouteSelector(val roles: List<Role>): RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
        RouteSelectorEvaluation.Constant

    override fun toString(): String = "(roles ${roles.joinToString { it.name }})"
}

fun Route.rolesAllowed(vararg roles: Role, build: Route.() -> Unit): Route {
    require(roles.isNotEmpty()) { "At least one role need to specified" }
    val configurationNames = roles.distinct()
    val authorisedRoute = createChild(AuthorisedRouteSelector(roles.toList()))

    application.featureOrNull(RoleAuthorization)?.interceptPipeline(authorisedRoute, configurationNames.toSet())

    authorisedRoute.build()
    return authorisedRoute
}