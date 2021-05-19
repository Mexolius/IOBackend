package com.gumi.moodle.rest_controllers

import com.gumi.moodle.course_id
import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.export.Exporter
import com.gumi.moodle.format
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Role
import com.gumi.moodle.model.User
import com.gumi.moodle.withRole
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.`in`
import org.litote.kmongo.eq


class ExportController

fun Application.exportRoutes() {
    val dao: UserDAO by inject()
    val courseDao: CourseDAO by inject()

    routing {
        authenticate("basicAuth") {
            withRole(Role.ADMIN, Role.TEACHER) {
                route("/export/course/{$format}/{$course_id}") {
                    get {
                        parameters(course_id, format) { (courseID, format) ->
                            val course =
                                courseDao.getOne(courseID) { Course::_id eq it } ?: return@get notFoundResponse()
                            val studentsInCourse = dao.getAll(User::_id `in` course.students)

                            val exporter = Exporter(course, studentsInCourse)

                            val byteArray: ByteArray = when (format) {
                                "csv", "CSV" -> exporter.exportCSV()
                                "xls", "XLS", "xlsx", "XLSX" -> exporter.exportXLS()
                                else -> return@get wrongIDResponse()
                            }

                            call.response.headers.append(
                                io.ktor.http.HttpHeaders.ContentDisposition,
                                "attachment; filename=" + course.name + '.' + format,
                                false
                            )

                            call.respondBytes(byteArray)
                        }
                    }
                }
            }
        }
    }
}



