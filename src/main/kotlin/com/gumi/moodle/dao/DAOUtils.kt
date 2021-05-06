package com.gumi.moodle.dao

import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.GradeID
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.property.KPropertyPath
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1


infix fun <K, T> KProperty1<out Any?, Map<out K, T>?>.atKey(key: K): KPropertyPath<out Any?, T?> =
    this.keyProjection(key)

infix fun <K, T> KProperty1<out Any?, Map<out K, T>?>.containsKey(key: K): Bson =
    this.keyProjection(key) exists true

infix fun <T> KProperty<T>.setTo(value: T): Bson =
    set(SetTo(this, value))

infix fun Bson.withGradeID(id: GradeID): Bson =
    and(this, Course::grades / Grade::_id eq id)