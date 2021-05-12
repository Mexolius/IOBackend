package com.gumi.moodle.model

import kotlinx.serialization.Serializable

typealias CourseID = String

@Serializable
data class Course(
    var _id: CourseID? = null,
    var name: String,
    var description: String,
    var studentLimit: Int = 100,
    var students: MutableSet<UserID> = mutableSetOf(),
    var teachers: MutableSet<UserID> = mutableSetOf(),
    var grades: MutableSet<Grade> = mutableSetOf(),
    var isEnrolled: Boolean = false,
)