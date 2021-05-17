package com.gumi.moodle.rest_controllers

import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.dao.and
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.Notification
import com.gumi.moodle.model.User
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.litote.kmongo.eq
import org.litote.kmongo.pullByFilter
import org.litote.kmongo.push

suspend fun PipelineContext<Unit, ApplicationCall>.wrongIDResponse() {
    call.respondText(
        text = "Missing or malformed id",
        status = HttpStatusCode.BadRequest
    )
}


suspend fun PipelineContext<Unit, ApplicationCall>.notFoundResponse() {
    call.respondText(
        text = "Not found in database",
        status = HttpStatusCode.NotFound
    )
}

suspend fun PipelineContext<Unit, ApplicationCall>.duplicateCourseNameResponse() {
    call.respondText(
        text = "Duplicate course name",
        status = HttpStatusCode.Conflict
    )
}

suspend fun PipelineContext<Unit, ApplicationCall>.malformedRouteResponse(name: String) {
    call.respondText(
        text = "Missing or malformed $name",
        status = HttpStatusCode.BadRequest
    )
}

@ContextDsl
suspend fun PipelineContext<Unit, ApplicationCall>.parameters(
    vararg names: String,
    body: suspend (List<String>) -> Unit,
) = body(names.map {
    call.parameters[it] ?: return malformedRouteResponse(it)
})

suspend fun createNotification(userDao: UserDAO, courseID: String, gradeID: String, studentID: String) {
    val notification = Notification(courseID, gradeID, System.currentTimeMillis())

    userDao.updateOne(
        studentID,
        pullByFilter(User::notifications, (Notification::courseID eq courseID) and (Notification::gradeID eq gradeID))
    ) { User::_id eq it }

    userDao.updateOne(
        studentID,
        push(User::notifications, notification)
    ) { User::_id eq it }
}

suspend fun getGrade(dao: CourseDAO, courseID: String, gradeID: String): Grade? {
    val course = dao.getOne(courseID) { Course::_id eq it } ?: return null
    return course.grades.find { it._id == gradeID }
}