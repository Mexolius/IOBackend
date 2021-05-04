package com.gumi.moodle.rest_controllers

import com.gumi.moodle.*
import com.gumi.moodle.IDField.ID
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Role
import com.gumi.moodle.model.Role.ADMIN
import com.gumi.moodle.model.Role.TEACHER
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.contains
import org.litote.kmongo.eq


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
                        if (dao.exists(course)) return@post call.respondText(
                            "Duplicate course name",
                            status = HttpStatusCode.Conflict
                        )
                        dao.add(course)

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
            withRole(ADMIN, idField = ID()) {
                route("/courses/of-student/{$user_id}") {
                    get {
                        val id = call.parameters[user_id] ?: return@get call.respondText(
                            "Missing or malformed id",
                            status = HttpStatusCode.BadRequest
                        )

                        val courses = dao.getAll(Course::students contains id, studentID = id)

                        call.respond(courses)
                    }
                }
            }
            withRole(ADMIN, TEACHER, idField = ID()) {
                route("/courses/of-teacher/{$user_id}") {
                    get {
                        val id = call.parameters[user_id] ?: return@get call.respondText(
                            "Missing or malformed id",
                            status = HttpStatusCode.BadRequest
                        )

                        val courses = dao.getAll(Course::teachers contains id)

                        call.respond(courses)
                    }
                }
            }
            withRole(ADMIN, TEACHER, idField = ID(user_id)) {
                route("/courses/{$user_id}/{$course_id}") {
                    get {
                        try {
                            val (userID, courseID) = call.getParameters(user_id, course_id)
                            var course =
                                if (Role.STUDENT in (call.principal<Principal>() as UserSession).roles)
                                    dao.getOne(courseID, studentID = userID) { Course::_id eq it }
                                else
                                    dao.getOne(courseID) { Course::_id eq it }

                            course = course ?: return@get call.respondText(
                                "No course matches requested course id",
                                status = HttpStatusCode.BadRequest
                            )

                            call.respond(course)
                        } catch (e: MalformedRouteException) {
                            return@get call.respondText(e.msg, status = HttpStatusCode.BadRequest)
                        }
                    }
                }
            }
        }
    }
}


