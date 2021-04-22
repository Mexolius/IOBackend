package com.gumi.moodle.dao

import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.GradeID
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class CourseDAO : AbstractDAO<Course, String>({ Course::name eq it }) {

    override fun getCollection(): CoroutineCollection<Course> =
        database.getCollection("Course")

    override suspend fun exists(obj: Course): Boolean =
        getCollection().find(Course::name eq obj.name).toList().isNotEmpty()
}

infix fun Bson.withGradeID(id: GradeID): Bson =
    and(this, Course::gradeModel / Grade::_id eq id)