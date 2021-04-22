package com.gumi.moodle.model

typealias GradeID = String

data class GradeStudent(
    val gradeID: GradeID,
    val points: Int,
)

data class GradeThresholds(
    val gradeID: GradeID,
    val thresholds: MutableSet<Float>
)

data class Grade(
    val _id: GradeID,
    val name: String,
    val level: Int,
    val maxPoints: Int = 0,
    val thresholds: MutableSet<Float> = mutableSetOf(),
    val studentPoints: MutableMap<UserID, Int> = mutableMapOf(),
    val parentID: GradeID? = null,
)
