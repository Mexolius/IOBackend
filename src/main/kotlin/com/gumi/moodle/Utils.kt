package com.gumi.moodle

import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.UserDAO
import io.ktor.application.*
import io.ktor.auth.*
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


suspend fun Application.validateUser(credentials: UserPasswordCredential): UserSession? {
    val userDAO: UserDAO by inject()
    val user = userDAO.getOne(credentials.name)
    return if (user != null && user.checkPassword(credentials.password)) UserSession(
        credentials.name,
        user._id!!,
        user.roles
    ) else null
}

const val MONGO_URI = "mongodb://localhost:27017"
const val MONGO_DB_NAME = "IOtest"
const val USER_COLLECTION = "User"
const val COURSE_COLLECTION = "Course"

const val user_id = "user_id"
const val course_id = "course_id"
const val grade_id = "grade_id"
const val email = "email"
const val format = "format"

fun Application.gumiModule() = module {
    val mongoURI = environment.config.propertyOrNull("ktor.mongodb.connectionString")?.getString() ?: MONGO_URI
    single { KMongo.createClient(MONGO_URI).coroutine.getDatabase(MONGO_DB_NAME) }
    single { UserDAO(mongoURI) }
    single { CourseDAO(mongoURI) }
}