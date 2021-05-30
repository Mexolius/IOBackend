package com.gumi.moodle.rest_controllers

import com.gumi.moodle.IDField.ID
import com.gumi.moodle.UserSession
import com.gumi.moodle.course_id
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.CourseSerializer
import com.gumi.moodle.model.CourseUserNamesSerializer
import com.gumi.moodle.model.Role.*
import com.gumi.moodle.user_id
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.builtins.ListSerializer
import org.koin.ktor.ext.inject
import org.litote.kmongo.addToSet
import org.litote.kmongo.contains
import org.litote.kmongo.eq

fun Application.courseRoutes() {
    val courseDAO: CourseDAO by inject()
    val userDAO: UserDAO by inject()

    routing {
        authenticate("basicAuth") {
            route("/courses") {
                get {
                    val courses = courseDAO.getAll()

                    call.respond(courses)
                }
            }
            withRole(ADMIN, TEACHER) {
                route("/course") {
                    post {
                        val course = call.receive<Course>()
                        if (courseDAO.exists(course)) return@post duplicateCourseNameResponse()
                        courseDAO.add(course)

                        call.respond(HttpStatusCode.OK)
                    }
                }
                route("/course/enroll-by-email/{$course_id}") {
                    post {
                        parameters(course_id) { (courseID) ->
                            val userEmail = call.receive<String>().removeSurrounding("\"")
                            val user = userDAO.getOne(userEmail)
                                ?: return@post notFoundResponse()
                            val list =
                                if (user.roles.contains(STUDENT)) Course::students
                                else Course::teachers
                            val updated = courseDAO.updateOne(
                                courseID,
                                addToSet(list, user._id)
                            ) { Course::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
            }
            withRole(ADMIN, TEACHER, STUDENT) {
                route("/course/enroll-by-id/{$course_id}") {
                    post {
                        parameters(course_id) { (courseID) ->
                            val studentID = call.receive<String>()
                            val updated = courseDAO.updateOne(
                                courseID,
                                addToSet(Course::students, studentID)
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
                        parameters(user_id) { (studentID) ->
                            val courses = courseDAO.getAll(Course::students contains studentID, studentID)
                            call.respond(ListSerializer(CourseSerializer(studentID)), courses)
                        }
                    }
                }
            }
            withRole(ADMIN, TEACHER, idField = ID()) {
                route("/courses/of-teacher/{$user_id}") {
                    get {
                        parameters(user_id) { (teacherID) ->
                            val courses = courseDAO.getAll(Course::teachers contains teacherID)
                            call.respond(courses)
                        }
                    }
                }
            }
            withRole(ADMIN, TEACHER, idField = ID()) {
                route("/courses/{$user_id}/{$course_id}") {
                    get {
                        parameters(user_id, course_id) { (userID, courseID) ->
                            val isStudent = STUDENT in (call.principal<Principal>() as UserSession).roles
                            var course =
                                if (isStudent)
                                    courseDAO.getOne(courseID, studentID = userID) { Course::_id eq it }
                                else
                                    courseDAO.getOne(courseID) { Course::_id eq it }

                            course = course ?: return@get notFoundResponse()

                            if (isStudent) call.respond(CourseSerializer(userID), course)
                            else call.respond(CourseUserNamesSerializer, course)
                        }
                    }
                }
            }
        }
    }
}
