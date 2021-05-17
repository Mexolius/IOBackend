package com.gumi.moodle.model

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val courseID: String,
    val gradeID: String,
    val createdTimestamp: Long,
)
