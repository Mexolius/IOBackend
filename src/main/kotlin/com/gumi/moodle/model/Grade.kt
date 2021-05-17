package com.gumi.moodle.model

import kotlinx.serialization.Serializable

typealias GradeID = String

@Serializable
data class Grade(
    val _id: GradeID,
    val name: String,
    var isLeaf: Boolean,
    var level: Int,
    val maxPoints: Int = 0,
    val aggregation: Aggregation = Aggregation.SUM,
    val thresholds: MutableSet<Float> = mutableSetOf(),
    val studentPoints: MutableMap<UserID, Int> = mutableMapOf(),
    var parentID: GradeID? = null,
)

enum class Aggregation { SUM, MEAN, MEDIAN }

@Serializable
data class Bucket(val from: Int, val to: Int, val number: Int)