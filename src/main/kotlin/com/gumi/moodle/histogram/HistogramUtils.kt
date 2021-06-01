package com.gumi.moodle.histogram

import com.gumi.moodle.model.Bucket
import com.gumi.moodle.model.Grade
import kotlinx.serialization.Serializable
import kotlin.math.min

fun bucketList(grade: Grade, buckets: String): List<Bucket> {
    val bucketRange = grade.maxPoints / buckets.toInt() + 1

    val result = gradeList(grade)
        .groupBy { (it / bucketRange) * bucketRange }
        .mapValues { it.value.size }
        .entries
        .map { Bucket(it.key, min(it.key + bucketRange - 1, grade.maxPoints), it.value) }
    return result
}

fun bucketListWithEmpty(grade: Grade, buckets: String): List<Bucket> {
    val bucketRange = grade.maxPoints / buckets.toInt() + 1

    val studentPoints = gradeList(grade)
        .groupBy { (it / bucketRange) * bucketRange }
        .mapValues { it.value.size }

    val result = (0..grade.maxPoints step bucketRange).associateWith { 0 }
        .plus(studentPoints)
        .entries
        .map { Bucket(it.key, min(it.key + bucketRange - 1, grade.maxPoints), it.value) }
    return result
}

fun gradeList(grade: Grade) = grade.studentPoints.values.sorted()


fun histogramResponse(grade: Grade, buckets: List<Bucket>, studentID: String): HistogramResponse<Bucket, Bucket> {
    return HistogramResponse(grade.name, buckets.find { it.from >= grade.studentPoints[studentID] ?: 2000000000 }, buckets)
}

@Serializable
data class HistogramResponse<T, U>(val gradeName: String, val studentPointsPosition: Int?, val studentPoints: U?, val points: List<T>) {
    constructor(gradeName: String, studentPoints: U?, points: List<T>) : this(gradeName, points.indexOf(studentPoints), studentPoints, points)
}




