package com.gumi.moodle.model

data class Course(
    var _id: String?,
    var name: String,
    var description: String,
    var studentLimit: Int = 100,
    var students: MutableSet<String> = mutableSetOf(),
    var teachers: List<String>,
    var gradeModel: GradeNode
) {
    fun filterStudents(id: UserID) {
        gradeModel.filterStudents(id)
    }
}