package com.gumi.moodle

import com.gumi.moodle.model.Course
import io.ktor.application.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.MongoOperator.rename
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun Application.migrations() {

    val database: CoroutineDatabase by inject()
    val migrations: MutableList<suspend () -> Unit> = mutableListOf()

    migrations += {
        database.getCollection<Course>(COURSE_COLLECTION)
            .updateMany("{'gradeModel': {$exists: true}}", "{$rename: {'gradeModel':'grades'}}")
    }

    migrations.forEach { it.invoke() }
}