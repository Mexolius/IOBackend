package com.gumi.moodle.dao

import com.gumi.moodle.COURSE_COLLECTION
import com.gumi.moodle.MONGO_URI
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.UserID
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

class CourseDAO(mongoURI: String = MONGO_URI) : AbstractDAO<Course, String>(mongoURI, { Course::name eq it }) {

    override fun getCollection(): CoroutineCollection<Course> =
        database.getCollection(COURSE_COLLECTION)

    override suspend fun exists(obj: Course): Boolean =
        getCollection().find(Course::name eq obj.name).toList().isNotEmpty()

    suspend fun getAll(query: Bson = EMPTY_BSON, studentID: UserID): List<Course> =
        getCollection().aggregate<Course>(pipeline(query, studentID)).toList()

    override suspend fun getOne(value: String, queryCreator: (String) -> Bson): Course? =
        getOne(value, "", queryCreator)

    suspend fun getOne(
        value: String,
        studentID: UserID,
        queryCreator: (String) -> Bson = defaultQueryCreator
    ): Course? =
        getCollection().aggregate<Course>(pipeline(queryCreator(value), studentID)).first()

    private fun pipeline(query: Bson, studentID: UserID = ""): List<Bson> {
        val pipeline = listOf(
            match(query),
            lookup(from = "User", localField = "teachers", foreignField = "_id", newAs = "teacherNames"),
            lookup(from = "User", localField = "students", foreignField = "_id", newAs = "studentNames"),
        )
        return if (studentID.isNotEmpty()) pipeline + project(courseProjection(studentID)) else pipeline
    }

    private fun courseProjection(studentID: UserID): Bson = include(
        Course::_id,
        Course::name,
        Course::description,
        Course::studentLimit,
        Course::students,
        Course::teachers,
        Course::teacherNames,
        Course::studentNames,
        Course::grades / Grade::_id,
        Course::grades / Grade::name,
        Course::grades / Grade::level,
        Course::grades / Grade::maxPoints,
        Course::grades / Grade::thresholds,
        Course::grades / Grade::parentID,
        Course::grades / Grade::isLeaf,
        Course::grades / Grade::studentPoints atKey studentID,
    )

    suspend fun getGrade(courseID: String, gradeID: String): Grade? =
        getOne(courseID) { Course::_id eq it }?.grades?.find { it._id == gradeID }
}