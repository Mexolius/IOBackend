package com.gumi.moodle.rest_controllers

import com.gumi.moodle.IDField.ID
import com.gumi.moodle.UserSession
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.Role.*
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.*


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
            withRole(ADMIN, TEACHER, STUDENT) {
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
                route("/course/grade/{course_id}/{student_id}") {
                    post {
                        val grade = call.receive<Grade>()
                        val courseID = call.parameters["course_id"] ?: return@post call.respondText(
                            "Missing or malformed course id",
                            status = HttpStatusCode.BadRequest
                        )
                        val userID = call.parameters["student_id"] ?: return@post call.respondText(
                            "Missing or malformed user id",
                            status = HttpStatusCode.BadRequest
                        )
                        val updated = dao.updateOne(
                            courseID,
                            push(Course::students.keyProjection(userID), grade)
                        ) { Course::_id eq it }

                        if (updated) call.respond(HttpStatusCode.OK)
                        else call.respond(HttpStatusCode.NotModified)
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

                        val courses = dao.getAll(Course::students.keyProjection(id) exists (true))

                        courses.forEach { it.filterStudents(id) }

                        call.respond(courses)
                    }
                }
            }
            withRole(ADMIN, TEACHER, idField = ID()) {
                route("/courses/of-teacher/{id}") {
                    get {
                        val id = call.parameters["id"] ?: return@get call.respondText(
                            "Missing or malformed id",
                            status = HttpStatusCode.BadRequest
                        )

                        val courses = dao.getAll(Course::teachers contains id)

                        call.respond(courses)
                    }
                }
            }
            withRole(ADMIN, STUDENT, TEACHER, idField = ID("user_id")) {
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
                        val course = dao.getOne(courseID) { Course::_id eq it } ?: return@get call.respondText(
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


