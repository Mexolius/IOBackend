package com.gumi.moodle.model

typealias GradeID = String

data class Grade(
    val _id: GradeID,
    val name: String,
    val level: Int,
    val maxPoints: Int = 0,
    val aggregation: Aggregation = Aggregation.SUM,
    val thresholds: MutableSet<Float> = mutableSetOf(),
    val studentPoints: MutableMap<UserID, Int> = mutableMapOf(),
    val parentID: GradeID? = null,
)

enum class Aggregation { SUM, MEAN, MEDIAN }