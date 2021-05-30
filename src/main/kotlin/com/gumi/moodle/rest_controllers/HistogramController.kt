package com.gumi.moodle.rest_controllers

import com.gumi.moodle.*
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.histogram.*
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Role.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.eq

fun Application.histogramRoutes() {
    val courseDAO: CourseDAO by inject()

    routing {
        authenticate("basicAuth") {
            withRole(ADMIN, TEACHER, STUDENT, idField = IDField.ID()) {
                route("/histogram/grades/{$course_id}/{$user_id}") {
                    get {
                        parameters(course_id, user_id) { (courseID, userID) ->
                            val grades = courseDAO.getOne(courseID) { Course::_id eq it }?.grades
                                ?: return@get notFoundResponse()

                            calculateParentGrades(grades)

                            val response = grades.associate {
                                it._id to HistogramResponse(it.studentPoints[userID], gradeList(it))
                            }

                            call.respond(response)
                        }
                    }
                }
                route("/histogram/buckets/{$buckets}/{$course_id}/{$user_id}") {
                    get {
                        parameters(course_id, user_id, buckets) { (courseID, userID, buckets) ->
                            val grades = courseDAO.getOne(courseID) { Course::_id eq it }?.grades
                                ?: return@get notFoundResponse()

                            calculateParentGrades(grades)

                            val result =
                                grades.associate { it._id to histogramResponse(it, bucketList(it, buckets), userID) }

                            call.respond(result)
                        }
                    }
                }
                route("/histogram/bucketsWithEmpty/{$buckets}/{$course_id}/{$user_id}") {
                    get {
                        parameters(course_id, user_id, buckets) { (courseID, userID, buckets) ->
                            val grades = courseDAO.getOne(courseID) { Course::_id eq it }?.grades
                                ?: return@get notFoundResponse()

                            calculateParentGrades(grades)

                            val result = grades.associate {
                                it._id to histogramResponse(it, bucketListWithEmpty(it, buckets), userID)
                            }

                            call.respond(result)
                        }
                    }
                }
            }
        }
    }
}


