package com.gumi.moodle.rest_controllers

import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Role.*
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


class CourseController

fun Application.courseRoutes() {
    val dao = CourseDAO()

    routing {
        authenticate("basicAuth") {
            route("/courses") {
                get {
                    val courses = dao.getAll()

                    call.respond(courses)
                }
            }
            route("/course") {
                post {
                    val courses = call.receive<Course>()
                    dao.add(courses)

                    call.respond(HttpStatusCode.OK)
                }
            }
            withRole(ADMIN, TEACHER, ID) {
                route("/courses/of-student/{id}") {
                    get {
                        val id = call.parameters["id"] ?: return@get call.respondText(
                            "Missing or malformed id",
                            status = HttpStatusCode.BadRequest
                        )
                        val courses = dao.getAll().filter { it.students.containsKey(id) }

                        call.respond(courses)
                    }
                }
            }
            withRole(ADMIN, ID) {
                route("/courses/of-teacher/{id}") {
                    get {
                        val id = call.parameters["id"] ?: return@get call.respondText(
                            "Missing or malformed id",
                            status = HttpStatusCode.BadRequest
                        )

                        val courses = dao.getAll().filter { id in it.teachers }

                        call.respond(courses)
                    }
                }
            }
        }
    }
}


