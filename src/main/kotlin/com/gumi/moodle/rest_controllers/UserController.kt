package com.gumi.moodle.rest_controllers

import com.gumi.moodle.IDField.EMAIL
import com.gumi.moodle.course_id
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.dao.setTo
import com.gumi.moodle.email
import com.gumi.moodle.model.Notification
import com.gumi.moodle.model.Role.ADMIN
import com.gumi.moodle.model.Role.STUDENT
import com.gumi.moodle.model.User
import com.gumi.moodle.model.UserSerializer
import com.gumi.moodle.user_id
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.eq
import org.litote.kmongo.pullByFilter

fun Application.userRoutes() {
    val userDAO: UserDAO by inject()

    routing {
        authenticate("basicAuth") {
            withRole(ADMIN) {
                route("/users") {
                    get {
                        val users: List<User> = userDAO.getAll()

                        call.respond(users)
                    }
                }
                route("/user") {
                    post {
                        val user = call.receive<User>()
                        userDAO.add(User.createUserWithPlaintextInput(user))
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
            withRole(ADMIN, idField = EMAIL()) {
                route("/user/{$email}") {
                    get {
                        parameters(email) { (userEmail) ->
                            val user = userDAO.getOne(userEmail)
                                ?: return@get notFoundResponse()
                            call.respond(UserSerializer, user)
                        }
                    }
                }
            }
            withRole(ADMIN, STUDENT) {
                route("/notifications/user/{$user_id}") {
                    get {
                        parameters(user_id) { (userID) ->
                            val user = userDAO.getOne(userID, includeNotifications = true) { User::_id eq it }
                                ?: return@get notFoundResponse()
                            call.respond(user.notifications)
                        }
                    }
                    delete {
                        parameters(user_id) { (userID) ->
                            val updated =
                                userDAO.updateOne(userID, User::notifications setTo mutableSetOf()) { User::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
                route("/notifications/user/{$user_id}/{$course_id}") {
                    delete {
                        parameters(user_id, course_id) { (userID, courseID) ->
                            val updated = userDAO.updateOne(
                                userID,
                                pullByFilter(User::notifications, (Notification::courseID eq courseID))
                            ) { User::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
            }
        }
        route("/register") {
            post {
                val user = call.receive<User>()
                val result = userDAO.add(User.createUserWithPlaintextInput(user))
                if (!result) {
                    return@post call.respondText("User already exists", status = HttpStatusCode.Conflict)
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
