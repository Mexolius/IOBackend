package com.gumi.moodle.model

data class Grade(
    var name: String,
    var points: Int,
    var children: List<Grade> = listOf()
)