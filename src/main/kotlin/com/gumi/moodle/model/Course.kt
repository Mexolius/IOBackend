package com.gumi.moodle.model

data class Course(
    var _id: String?,
    var name: String,
    var description: String,
    var studentLimit: Int = 100,
    var students: MutableMap<String, List<Grade>> = mutableMapOf(),  //student id ->
    var teachers: List<String>,  //list of teacher ids
    var gradeModel: List<Grade>
) {
    fun filterStudents(id: String) {
        students.keys.retainAll { it == id }
    }
}