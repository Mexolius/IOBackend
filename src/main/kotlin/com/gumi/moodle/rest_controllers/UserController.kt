package com.gumi.moodle.rest_controllers

import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.model.Role
import com.gumi.moodle.model.User
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


class UserController

fun Application.userRoutes() {
    val dao = UserDAO()

    routing {
        authenticate("basicAuth") {
            withRole(Role.ADMIN) {
                route("/users") {
                    get {
                        val users = dao.getAll()

                        call.respond(users)
                    }
                }
            }
            route("/user") {
                post {
                    val user = call.receive<User>()
                    dao.add(User.createUserWithPlaintextInput(user))
                    call.respond(HttpStatusCode.OK)
                }
            }
            route("/user/{email}") {
                get {
                    val email = call.parameters["email"] ?: return@get call.respondText(
                        "Missing or malformed email",
                        status = HttpStatusCode.BadRequest
                    )
                    val user = dao.getOne(email) ?: return@get call.respond(HttpStatusCode.NotFound)
                    call.respond(user)
                }
            }
        }
        route("/register") {
            post {
                val user = call.receive<User>()
                user.roles = setOf(Role.STUDENT)
                val result = dao.add(User.createUserWithPlaintextInput(user))
                if (!result) {
                    return@post call.respondText("User already exists", status = HttpStatusCode.Conflict)
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
