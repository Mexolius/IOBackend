package com.gumi.moodle.rest_controllers

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

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
    body: suspend (List<String>) -> Unit
) = body(names.map {
    call.parameters[it] ?: return malformedRouteResponse(it)
})
