package com.gumi.moodle.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val courseID: CourseID,
    val courseName: String,
    val gradeID: GradeID,
    val gradeName: String,
    val createdTimestamp: Long,
) {
    constructor(course: Course, grade: Grade, createdTimestamp: Long) :
            this(course._id!!, course.name, grade._id, grade.name, createdTimestamp)
}
