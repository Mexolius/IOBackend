package com.gumi.moodle.dao

import com.gumi.moodle.COURSE_COLLECTION
import com.gumi.moodle.MONGO_URI
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.UserID
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.include

class CourseDAO(mongoURI: String = MONGO_URI) : AbstractDAO<Course, String>(mongoURI, { Course::name eq it }) {

    override fun getCollection(): CoroutineCollection<Course> =
        database.getCollection(COURSE_COLLECTION)

    override suspend fun exists(obj: Course): Boolean =
        getCollection().find(Course::name eq obj.name).toList().isNotEmpty()

    suspend fun getAll(query: Bson = EMPTY_BSON, studentID: UserID): List<Course> =
        getCollection().find(query)
            .projection(studentProjection(studentID))
            .toList()

    suspend fun getOne(
        value: String,
        studentID: UserID,
        queryCreator: (String) -> Bson = defaultQueryCreator
    ): Course? =
        getCollection().find(queryCreator(value))
            .projection(studentProjection(studentID))
            .first()

    private fun studentProjection(studentID: UserID): Bson = include(
        Course::_id,
        Course::name,
        Course::description,
        Course::studentLimit,
        Course::teachers,
        Course::grades / Grade::_id,
        Course::grades / Grade::name,
        Course::grades / Grade::level,
        Course::grades / Grade::maxPoints,
        Course::grades / Grade::thresholds,
        Course::grades / Grade::parentID,
        Course::grades / Grade::isLeaf,
        Course::grades / Grade::studentPoints atKey studentID,
    )
}