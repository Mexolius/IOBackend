package com.gumi.moodle.rest_controllers

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*


suspend fun PipelineContext<Unit, ApplicationCall>.wrongIDResponse() {
    call.respondText(
        "Missing or malformed id",
        status = HttpStatusCode.BadRequest
    )
}


suspend fun PipelineContext<Unit, ApplicationCall>.notFoundResponse() {
    call.respondText(
        "Not found in database",
        status = HttpStatusCode.NotFound
    )
}

suspend fun PipelineContext<Unit, ApplicationCall>.duplicateCourseNameResponse() {
    call.respondText(
        "Duplicate course name",
        status = HttpStatusCode.Conflict
    )
}
