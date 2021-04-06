package com.gumi.moodle.dao

import com.gumi.moodle.model.Course
import org.litote.kmongo.coroutine.CoroutineCollection

class CourseDAO : AbstractDAO<Course, String>({ it.name }) {

    override fun getCollection(): CoroutineCollection<Course> {
        return database.getCollection("Course")
    }

}