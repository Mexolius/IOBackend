package com.gumi.moodle

import com.gumi.moodle.model.Course
import io.ktor.application.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.MongoOperator.exists
import org.litote.kmongo.MongoOperator.rename
import org.litote.kmongo.coroutine.CoroutineDatabase

suspend fun Application.migrations() {

    val database: CoroutineDatabase by inject()
    val migrations: MutableMap<String, suspend () -> Unit> = mutableMapOf()

    migrations += ("gradeModel to grades" to {
        database.getCollection<Course>(COURSE_COLLECTION)
            .updateMany("{'gradeModel': {$exists: true}}", "{$rename: {'gradeModel':'grades'}}")
    })

    migrations.forEach {
        environment.log.info("Running migration ${it.key}")
        it.value.invoke()
    }
}