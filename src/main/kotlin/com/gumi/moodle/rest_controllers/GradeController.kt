package com.gumi.moodle.rest_controllers

import com.gumi.moodle.course_id
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.dao.atKey
import com.gumi.moodle.dao.setTo
import com.gumi.moodle.dao.withGradeID
import com.gumi.moodle.grade_id
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.Role.ADMIN
import com.gumi.moodle.model.Role.TEACHER
import com.gumi.moodle.user_id
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.*

fun Application.gradeRoutes() {
    val courseDAO: CourseDAO by inject()
    val userDAO: UserDAO by inject()

    routing {
        authenticate("basicAuth") {
            withRole(ADMIN, TEACHER) {
                route("/grade/{$course_id}") {
                    post {
                        parameters(course_id) { (courseID) ->
                            val grade = call.receive<List<Grade>>()
                            val updated = courseDAO.updateOne(
                                courseID,
                                pushEach(Course::grades, grade)
                            ) { Course::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
                route("/grades/{$course_id}") {
                    post {
                        parameters(course_id) { (courseID) ->
                            val grades = call.receive<MutableSet<Grade>>()
                            val updated = courseDAO.updateOne(
                                courseID,
                                Course::grades setTo grades
                            ) { Course::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
                route("/grade/{$course_id}/{$grade_id}") {
                    post {
                        parameters(course_id, grade_id) { (courseID, gradeID) ->
                            val grade = call.receive<Grade>()
                            val updated = courseDAO.updateOne(
                                courseID,
                                Course::grades.posOp setTo grade
                            ) { Course::_id eq it withGradeID gradeID }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)

                        }
                    }
                    delete {
                        parameters(course_id, grade_id) { (courseID, gradeID) ->
                            val updated = courseDAO.updateOne(
                                courseID,
                                pullByFilter(Course::grades, Grade::_id eq gradeID)
                            ) { Course::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
                route("/grade/many/{$course_id}/{$grade_id}") {
                    post {
                        parameters(course_id, grade_id) { (courseID, gradeID) ->
                            val grades = call.receive<Map<String, Int>>()
                            val updated = courseDAO.updateOne(
                                courseID,
                                combine(grades.map { (k, v) ->
                                    Course::grades.posOp / Grade::studentPoints atKey k setTo v
                                })
                            ) { Course::_id eq it withGradeID gradeID }
                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
                route("/grade/{$course_id}/{$grade_id}/{$user_id}") {
                    post {
                        parameters(course_id, grade_id, user_id) { (courseID, gradeID, studentID) ->
                            val points = call.receive<Int>()
                            val updated = courseDAO.updateOne(
                                courseID,
                                Course::grades.posOp / Grade::studentPoints atKey studentID setTo points
                            ) { Course::_id eq it withGradeID gradeID }

                            val course = courseDAO.getOne(courseID) { Course::_id eq it }
                            val grade = course?.grades?.find { it._id == gradeID }

                            if (updated && course != null && grade != null) {
                                userDAO.createNotification(course, grade, studentID)
                                call.respond(HttpStatusCode.OK)
                            } else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
            }
        }
    }
}


