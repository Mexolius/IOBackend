package com.gumi.moodle.rest_controllers

import com.gumi.moodle.*
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.model.Bucket
import com.gumi.moodle.model.Role.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import kotlin.math.min


class HistogramController

fun Application.histogramRoutes() {
    val dao: CourseDAO by inject()

    routing {
        authenticate("basicAuth") {
            withRole(ADMIN, TEACHER, STUDENT, idField = IDField.ID()) {
                route("/histogram/grades/{$course_id}/{$grade_id}") {
                    get {
                        parameters(course_id, grade_id) { (courseID, gradeID) ->
                            val grade = dao.getGrade(courseID, gradeID) ?: return@parameters notFoundResponse()

                            val studentPoints = grade.studentPoints.values.sorted()

                            call.respond(studentPoints)
                        }
                    }
                }
                route("/histogram/buckets/{$buckets}/{$course_id}/{$grade_id}") {
                    get {
                        parameters(course_id, grade_id, buckets) { (courseID, gradeID, buckets) ->
                            val grade = dao.getGrade(courseID, gradeID) ?: return@parameters notFoundResponse()

                            val bucketRange = grade.maxPoints / buckets.toInt()

                            val result = grade.studentPoints.values.sorted()
                                .groupBy { (it / bucketRange) * bucketRange }
                                .mapValues { it.value.size }
                                .entries
                                .map { Bucket(it.key, min(it.key + bucketRange - 1, grade.maxPoints), it.value) }

                            call.respond(result)
                        }
                    }
                }
                route("/histogram/bucketsWithEmpty/{$buckets}/{$course_id}/{$grade_id}") {
                    get {
                        parameters(course_id, grade_id, buckets) { (courseID, gradeID, buckets) ->
                            val grade = dao.getGrade(courseID, gradeID) ?: return@parameters notFoundResponse()

                            val bucketRange = grade.maxPoints / buckets.toInt()

                            val studentPoints = grade.studentPoints.values.sorted()
                                .groupBy { (it / bucketRange) * bucketRange }
                                .mapValues { it.value.size }

                            val result = (0..grade.maxPoints step bucketRange).associateWith { 0 }
                                .plus(studentPoints)
                                .entries
                                .map { Bucket(it.key, min(it.key + bucketRange - 1, grade.maxPoints), it.value) }

                            call.respond(result)
                        }
                    }
                }
            }
        }
    }
}


