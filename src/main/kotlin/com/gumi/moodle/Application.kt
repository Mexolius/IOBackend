package com.gumi.moodle

import com.gumi.moodle.rest_controllers.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.Koin
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(Koin) {
        modules(gumiModule())
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        basic(name = "basicAuth") {
            realm = "Ktor Server"
            validate { validateUser(it) }
        }
    }

    install(StatusPages) {
        exception<AuthorizationException> {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    install(RoleAuthorization) {
        getRoles = { (it as UserSession).roles }
    }

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Options)
        header(HttpHeaders.AccessControlAllowHeaders)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.AccessControlAllowMethods)
        header(HttpHeaders.Authorization)
        allowCredentials = false
        anyHost()
    }


    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK)
        }
        authenticate("basicAuth") {
            get("/logged") {
                call.respond(HttpStatusCode.OK)
            }
        }
        post("/generate") {
            try {
                Generator().insertToDB()
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                log.error("failed generating new data", e)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
        userRoutes()
        courseRoutes()
        gradeRoutes()
        exportRoutes()
        histogramRoutes()
    }

    if (!testing) runBlocking { migrations() }
}
