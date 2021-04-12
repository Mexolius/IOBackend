package com.gumi.moodle

import com.gumi.moodle.model.Role
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

data class UserSession(val email: String, val id: String, val roles: Set<Role> = emptySet()) : Principal
enum class IDField(val getter: (UserSession) -> String) {
    NONE({ "none" }),
    EMAIL(UserSession::email),
    ID(UserSession::id)
}

class AuthorizationException(override val message: String) : Exception(message)

class RoleAuthorization(config: Configuration) {
    private val getRoles = config.getRoles

    class Configuration {
        var getRoles: (Principal) -> Set<Role> = { emptySet() }
    }

    fun interceptPipeline(
        pipeline: ApplicationCallPipeline,
        idField: IDField,
        roles: Set<Role>
    ) {
        pipeline.insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.ChallengePhase)
        pipeline.insertPhaseAfter(Authentication.ChallengePhase, AuthorizationPhase)

        pipeline.intercept(AuthorizationPhase) {
            val principal =
                call.authentication.principal<Principal>() ?: throw AuthorizationException("Missing principal")
            val callIDValue = call.parameters[idField.name] ?: ""
            val sessionIDValue = idField.getter(principal as UserSession)
            val userRoles = getRoles(principal)

            if (callIDValue != sessionIDValue && roles.none { it in userRoles }) {
                val message =
                    "Principal $principal has none of the sufficient role(s): $roles and doesn't match value at ${idField.name}"
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


fun Route.withRole(vararg roles: Role, idField: IDField = IDField.NONE, build: Route.() -> Unit) =
    authorizedRoute(idField, roles.toSet(), build)

private fun Route.authorizedRoute(
    idField: IDField,
    roles: Set<Role>,
    build: Route.() -> Unit
): Route {
    val description =
        "require any of roles: $roles" + if (idField != IDField.NONE) " and matching ${idField.name}" else ""
    val authorizedRoute = createChild(AuthorizedRouteSelector(description))
    application.feature(RoleAuthorization).interceptPipeline(authorizedRoute, idField, roles)
    authorizedRoute.build()
    return authorizedRoute
}
