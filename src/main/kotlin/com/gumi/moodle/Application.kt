package com.gumi.moodle

import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.rest_controllers.courseRoutes
import com.gumi.moodle.rest_controllers.userRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.slf4j.event.Level

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(CallLogging) {
        level = Level.INFO
    }

    install(ContentNegotiation) {
        jackson()
    }

    install(Authentication) {
        basic(name = "basicAuth") {
            realm = "Ktor Server"
            validate { credentials -> validateUser(credentials) }
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
        get("/") {
            data class Jedi(val name: String, val age: Int)

            val client = KMongo.createClient("mongodb://localhost:27017").coroutine
            val users = client.getDatabase("test")
                .getCollection<Jedi>()
                .find()
                .toList()
            call.respondText("users: $users")
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
    }
}

suspend fun validateUser(credentials: UserPasswordCredential): UserSession? {
    val user = UserDAO().getOne(credentials.name) ?: return null
    return if (user.checkPassword(credentials.password)) UserSession(credentials.name, user.roles) else null
}
