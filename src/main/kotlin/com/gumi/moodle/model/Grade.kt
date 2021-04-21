package com.gumi.moodle.model

typealias GradeID = String?

data class StudentGrade(
    val gradeID: GradeID,
    val points: Int,
)

data class Grade(
    var _id: GradeID,
    val name: String,
    val level: Int,
    val maxPoints: Int = 0,
    val studentPoints: MutableMap<UserID, Int> = mutableMapOf(),
    val thresholds: MutableSet<Float> = mutableSetOf(),
    val parentID: GradeID = null,
)
