package com.gumi.moodle.rest_controllers

import com.gumi.moodle.DAO.UserDAO
import com.gumi.moodle.model.User
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

class UserController

fun Application.userRoutes() {
    val dao = UserDAO()

    routing {
        route("/users") {
            get {
                val users = dao.getUsers()

                call.respond(users)
            }
        }
        route("/user") {
            post {
                val user = call.receive<User>()
                dao.addUser(user)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}