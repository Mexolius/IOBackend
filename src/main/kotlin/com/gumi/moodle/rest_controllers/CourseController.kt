package com.gumi.moodle.rest_controllers

import com.gumi.moodle.*
import com.gumi.moodle.IDField.ID
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Role.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.contains
import org.litote.kmongo.eq
import org.litote.kmongo.push


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
                        if (dao.exists(course)) return@post duplicateCourseNameResponse()
                        dao.add(course)

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
            withRole(ADMIN, TEACHER, STUDENT) {
                route("/course/enroll/{$course_id}") {
                    post {
                        parameters(course_id) { (courseID) ->
                            val student = call.receive<String>()
                            val updated = dao.updateOne(
                                courseID,
                                push(Course::students, student)
                            ) { Course::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
            }
            withRole(ADMIN, idField = ID()) {
                route("/courses/of-student/{$user_id}") {
                    get {
                        parameters(user_id) { (id) ->
                            val courses = dao.getAll(Course::students contains id, studentID = id)
                            call.respond(courses)
                        }
                    }
                }
            }
            withRole(ADMIN, TEACHER, idField = ID()) {
                route("/courses/of-teacher/{$user_id}") {
                    get {
                        parameters(user_id) { (id) ->
                            val courses = dao.getAll(Course::teachers contains id)
                            call.respond(courses)
                        }
                    }
                }
            }
            withRole(ADMIN, TEACHER, idField = ID()) {
                route("/courses/{$user_id}/{$course_id}") {
                    get {
                        parameters(user_id, course_id) { (userID, courseID) ->
                            var course =
                                if (STUDENT in (call.principal<Principal>() as UserSession).roles)
                                    dao.getOne(courseID, studentID = userID) { Course::_id eq it }
                                else
                                    dao.getOne(courseID) { Course::_id eq it }

                            course = course ?: return@parameters notFoundResponse()

                            call.respond(course)
                        }
                    }
                }
            }
        }
    }
}