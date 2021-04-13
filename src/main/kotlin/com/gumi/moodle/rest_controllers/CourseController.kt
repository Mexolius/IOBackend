package com.gumi.moodle.rest_controllers

import com.gumi.moodle.IDField.ID
import com.gumi.moodle.UserSession
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Role.ADMIN
import com.gumi.moodle.model.Role.TEACHER
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
            withRole(ADMIN, TEACHER) {
                route("/course") {
                    post {
                        val course = call.receive<Course>()
                        if (dao.getOne(course.name) != null) return@post call.respondText(
                            "Duplicate course name",
                            status = HttpStatusCode.Conflict
                        )
                        dao.add(course)

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
            withRole(ADMIN, idField = ID()) {
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
            withRole(ADMIN, idField = ID()) {
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
            withRole(ADMIN, STUDENT, idField = ID("user_id")) {
                route("/courses/{user_id}/{course_id}") {
                    get {
                        val userID = call.parameters["user_id"] ?: return@get call.respondText(
                            "Missing or malformed user id",
                            status = HttpStatusCode.BadRequest
                        )
                        val courseID = call.parameters["course_id"] ?: return@get call.respondText(
                            "Missing or malformed course id",
                            status = HttpStatusCode.BadRequest
                        )
                        val course = dao.getOne(courseID) { it._id ?: "" } ?: return@get call.respondText(
                            "No course matches requested course id",
                            status = HttpStatusCode.BadRequest
                        )

                        if (STUDENT in (call.principal<Principal>() as UserSession).roles) course.filterStudents(userID)
                        call.respond(course)
                    }
                }
            }
        }
    }
}


