package com.gumi.moodle

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        jackson()
    }

    routing {
        get("/") {
            val client = KMongo.createClient("mongodb://localhost:27017").coroutine
            val users = client.getDatabase("test")
                .getCollection<Jedi>()
                .find()
                .toList()
            call.respondText("users: $users")
        }
    }
}

data class Jedi(val name: String, val age: Int)