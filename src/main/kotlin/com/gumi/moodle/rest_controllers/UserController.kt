package com.gumi.moodle.rest_controllers

import com.gumi.moodle.IDField.EMAIL
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.email
import com.gumi.moodle.model.Role.ADMIN
import com.gumi.moodle.model.User
import com.gumi.moodle.parameters
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
            withRole(ADMIN) {
                route("/users") {
                    get {
                        val users: List<User> = dao.getAll()

                        call.respond(users)
                    }
                }
                route("/user") {
                    post {
                        val user = call.receive<User>()
                        dao.add(User.createUserWithPlaintextInput(user))
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
            withRole(ADMIN, idField = EMAIL()) {
                route("/user/{$email}") {
                    get {
                        parameters(email) { (email) ->
                            val user = dao.getOne(email, includeCrypto = false)
                                ?: return@parameters call.respond(HttpStatusCode.NotFound)
                            call.respond(user)
                        }
                    }
                }
            }
        }
        route("/register") {
            post {
                val user = call.receive<User>()
                val result = dao.add(User.createUserWithPlaintextInput(user))
                if (!result) {
                    return@post call.respondText("User already exists", status = HttpStatusCode.Conflict)
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}