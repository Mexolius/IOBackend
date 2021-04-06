package com.gumi.moodle.model

class Course(
    var _id: String?,
    var name: String,
    var description: String,
    var studentLimit: Int = 100,
    var students: Map<String, Unit> = mapOf<String, Unit>(),  //student id ->
    var teachers: List<String>,  //list of teacher ids
)