package com.gumi.moodle

import com.gumi.moodle.model.Course
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.MongoOperator.rename
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class Migrations {

    private val client = KMongo.createClient(MONGO_URI).coroutine
    private val database = client.getDatabase(MONGO_DB_NAME)

    init {
        runBlocking { gradeModelToGrades() }
    }

    private suspend fun gradeModelToGrades() {
        database.getCollection<Course>(COURSE_COLLECTION).updateMany("{}", "{$rename: {'gradeModel':'grades'}}")
    }
}