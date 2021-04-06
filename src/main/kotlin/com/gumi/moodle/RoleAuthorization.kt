package com.gumi.moodle

import com.gumi.moodle.model.Role
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

data class UserSession(val name: String, val roles: Set<Role> = emptySet()) : Principal
enum class RequireRoles { ANY, ALL }
class AuthorizationException(override val message: String) : Exception(message)

class RoleAuthorization(config: Configuration) {
    private val getRoles = config.getRoles

    class Configuration {
        var getRoles: (Principal) -> Set<Role> = { emptySet() }
    }

    fun interceptPipeline(
        pipeline: ApplicationCallPipeline,
        roles: Set<Role>,
        require: RequireRoles
    ) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.ChallengePhase)
        pipeline.insertPhaseAfter(Authentication.ChallengePhase, AuthorizationPhase)

        pipeline.intercept(AuthorizationPhase) {
            val principal =
                call.authentication.principal<Principal>() ?: throw AuthorizationException("Missing principal")
            val userRoles = getRoles(principal)
            val denyReasons = mutableListOf<String>()
            when (require) {
                RequireRoles.ALL -> {
                    val missing = roles - userRoles
                    if (missing.isNotEmpty()) {
                        denyReasons += "Principal $principal lacks required role(s): $missing"
                    }
                }
                RequireRoles.ANY -> {
                    if (roles.none { it in userRoles }) {
                        denyReasons += "Principal $principal has none of the sufficient role(s): $roles"
                    }
                }
            }

            if (denyReasons.isNotEmpty()) {
                val message = denyReasons.joinToString(". ")
                call.application.environment.log.warn("Authorization failed for ${call.request.path()}. $message")
                throw AuthorizationException(message)
            }
        }
    }


    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RoleAuthorization> {
        override val key = AttributeKey<RoleAuthorization>("RoleBasedAuthorization")

        val AuthorizationPhase = PipelinePhase("Authorization")

        override fun install(
            pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit
        ): RoleAuthorization {
            val configuration = Configuration().apply(configure)
            return RoleAuthorization(configuration)
        }


    }
}

class AuthorizedRouteSelector(private val description: String) :
    RouteSelector(RouteSelectorEvaluation.qualityConstant) {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant

    override fun toString(): String = "(authorize ${description})"
}

fun Route.withRole(role: Role, build: Route.() -> Unit) =
    authorizedRoute(setOf(role), RequireRoles.ALL, build)

fun Route.withAllRoles(vararg roles: Role, build: Route.() -> Unit) =
    authorizedRoute(roles.toSet(), RequireRoles.ALL, build)

fun Route.withAnyRole(vararg roles: Role, build: Route.() -> Unit) =
    authorizedRoute(roles.toSet(), RequireRoles.ANY, build)

private fun Route.authorizedRoute(
    roles: Set<Role>,
    require: RequireRoles,
    build: Route.() -> Unit
): Route {
    val description = "$require of $roles"
    val authorizedRoute = createChild(AuthorizedRouteSelector(description))
    application.feature(RoleAuthorization).interceptPipeline(authorizedRoute, roles, require)
    authorizedRoute.build()
    return authorizedRoute
}
