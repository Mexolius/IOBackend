package com.gumi.moodle.rest_controllers

import com.gumi.moodle.*
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.atKey
import com.gumi.moodle.dao.setTo
import com.gumi.moodle.dao.withGradeID
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.Role.ADMIN
import com.gumi.moodle.model.Role.TEACHER
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.*


class GradeController

fun Application.gradeRoutes() {
    val dao = CourseDAO()

    routing {
        authenticate("basicAuth") {
            withRole(ADMIN, TEACHER) {
                route("/grade/{$course_id}") {
                    post {
                        parameters(course_id) { (courseID) ->
                            val grade = call.receive<Grade>()
                            val updated = dao.updateOne(
                                courseID,
                                push(Course::gradeModel, grade)
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
                            val updated = dao.updateOne(
                                courseID,
                                Course::gradeModel setTo grades
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
                            val updated = dao.updateOne(
                                courseID,
                                Course::gradeModel.posOp setTo grade
                            ) { Course::_id eq it withGradeID gradeID }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)

                        }
                    }
                    delete {
                        parameters(course_id, grade_id) { (courseID, gradeID) ->
                            val updated = dao.updateOne(
                                courseID,
                                pullByFilter(Course::gradeModel, Grade::_id eq gradeID)
                            ) { Course::_id eq it }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
                route("/grade/{$course_id}/{$grade_id}/{$user_id}") {
                    post {
                        parameters(course_id, grade_id, user_id) { (courseID, gradeID, studentID) ->
                            val grade = call.receive<Int>()
                            val updated = dao.updateOne(
                                courseID,
                                Course::gradeModel.posOp / Grade::studentPoints atKey studentID setTo grade
                            ) { Course::_id eq it withGradeID gradeID }

                            if (updated) call.respond(HttpStatusCode.OK)
                            else call.respond(HttpStatusCode.NotModified)
                        }
                    }
                }
            }
        }
    }
}