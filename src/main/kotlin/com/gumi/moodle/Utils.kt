package com.gumi.moodle

import com.gumi.moodle.dao.UserDAO
import io.ktor.application.*
import io.ktor.auth.*


class MalformedRouteException(val msg: String) : Exception()

fun ApplicationCall.getParameters(vararg names: String): List<String> =
    names.map { this.parameters[it] ?: throw MalformedRouteException("Missing or malformed $it") }

suspend fun validateUser(credentials: UserPasswordCredential): UserSession? {
    val user = UserDAO().getOne(credentials.name)
    return if (user != null && user.checkPassword(credentials.password)) UserSession(
        credentials.name,
        user._id!!,
        user.roles
    ) else null
}