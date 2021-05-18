package com.gumi.moodle.model

import kotlinx.serialization.json.*

val STUDENTS: String = Course::students.name
val TEACHERS: String = Course::teachers.name
val TEACHER_NAMES: String = Course::teacherNames.name
val FIRST_NAME: String = User::firstName.name
val LAST_NAME: String = User::lastName.name
val PASSWORD: String = User::password.name
val SALT: String = User::salt.name
val NOTIFICATIONS: String = User::notifications.name
val ID: String = User::_id.name
const val IS_ENROLLED: String = "isEnrolled"

object CourseTeachersSerializer : JsonTransformingSerializer<Course>(Course.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        val newTeacherNames = element.jsonObject[TEACHER_NAMES]?.jsonArray?.map { elem ->
            JsonObject(elem.jsonObject.filterKeys { it == ID || it == FIRST_NAME || it == LAST_NAME })
        } ?: listOf()
        val newElement = element.jsonObject.filterKeys { it != TEACHER_NAMES }
        return if (newTeacherNames.isEmpty()) JsonObject(newElement)
        else JsonObject(newElement + (TEACHERS to JsonArray(newTeacherNames)))
    }
}

class CourseSerializer(private val studentID: UserID) : JsonTransformingSerializer<Course>(CourseTeachersSerializer) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        val isEnrolled = element.jsonObject[STUDENTS]?.jsonArray?.contains(JsonPrimitive(studentID))
        val newElement = element.jsonObject.filterKeys { k -> k != STUDENTS }
        return JsonObject(newElement + (IS_ENROLLED to JsonPrimitive(isEnrolled)))
    }
}

object UserSerializer : JsonTransformingSerializer<User>(User.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement =
        JsonObject(element.jsonObject.filterKeys { it != PASSWORD && it != SALT && it != NOTIFICATIONS })
}