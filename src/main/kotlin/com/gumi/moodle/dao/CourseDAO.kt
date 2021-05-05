package com.gumi.moodle.dao

import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.GradeID
import com.gumi.moodle.model.UserID
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

class CourseDAO : AbstractDAO<Course, String>({ Course::name eq it }) {

    override fun getCollection(): CoroutineCollection<Course> =
        database.getCollection("Course")

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
        Course::gradeModel / Grade::_id,
        Course::gradeModel / Grade::name,
        Course::gradeModel / Grade::level,
        Course::gradeModel / Grade::maxPoints,
        Course::gradeModel / Grade::thresholds,
        Course::gradeModel / Grade::studentPoints atKey studentID,
    )
}

infix fun Bson.withGradeID(id: GradeID): Bson =
    and(this, Course::gradeModel / Grade::_id eq id)