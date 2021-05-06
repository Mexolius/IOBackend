package com.gumi.moodle

import com.gumi.moodle.dao.UserDAO
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

class MalformedRouteException(val msg: String) : Exception()

fun ApplicationCall.getParameters(vararg names: String): List<String> =
    names.map { this.parameters[it] ?: throw MalformedRouteException("Missing or malformed $it") }

@ContextDsl
suspend fun PipelineContext<Unit, ApplicationCall>.parameters(
    vararg names: String,
    body: suspend (List<String>) -> Unit
) = body(names.map {
    call.parameters[it] ?: return call.respondText(
        "Missing or malformed $it",
        status = HttpStatusCode.BadRequest
    )
})


suspend fun validateUser(credentials: UserPasswordCredential): UserSession? {
    val user = UserDAO().getOne(credentials.name)
    return if (user != null && user.checkPassword(credentials.password)) UserSession(
        credentials.name,
        user._id!!,
        user.roles
    ) else null
}

const val MONGO_URI = "mongodb://localhost:27017"
const val MONGO_DB_NAME = "IOtest"
const val USER_COLLECTION = "User"
const val COURSE_COLLECTION = "Course"

const val user_id = "user_id"
const val course_id = "course_id"
const val grade_id = "grade_id"
const val email = "email"
