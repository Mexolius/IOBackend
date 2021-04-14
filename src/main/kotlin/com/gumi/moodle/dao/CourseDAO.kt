package com.gumi.moodle.dao

import com.gumi.moodle.model.Course
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

class CourseDAO : AbstractDAO<Course, String>({ Course::name eq it }) {

    override fun getCollection(): CoroutineCollection<Course> =
        database.getCollection("Course")

    override suspend fun exists(obj: Course): Boolean =
        getCollection().find(Course::name eq obj.name).toList().isNotEmpty()

}