package com.gumi.moodle.model

data class Course(
    var _id: String?,
    var name: String,
    var description: String,
    var studentLimit: Int = 100,
    var students: MutableSet<UserID> = mutableSetOf(),
    var teachers: MutableSet<UserID> = mutableSetOf(),
    var gradeModel: MutableSet<Grade> = mutableSetOf(),
)