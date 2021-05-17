package com.gumi.moodle.model

import kotlinx.serialization.json.*

object CourseTeachersSerializer : JsonTransformingSerializer<Course>(Course.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        val teacherNames = "teacherNames"
        val teachers = "teachers"
        val newTeacherNames = element.jsonObject[teacherNames]?.jsonArray?.map { elem ->
            JsonObject(elem.jsonObject.filterKeys { it == "_id" || it == "firstName" || it == "lastName" })
        } ?: listOf()
        val newElement = element.jsonObject.filterKeys { it != teachers && it != teacherNames }
        return JsonObject(newElement + (teachers to JsonArray(newTeacherNames)))
    }
}

class CourseSerializer(private val studentID: UserID) : JsonTransformingSerializer<Course>(CourseTeachersSerializer) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        val students = "students"
        val isEnrolled = element.jsonObject[students]?.jsonArray?.contains(JsonPrimitive(studentID))
        val newElement = element.jsonObject.filterKeys { k -> k != "students" }
        return JsonObject(newElement + ("isEnrolled" to JsonPrimitive(isEnrolled)))
    }
}

object UserSerializer : JsonTransformingSerializer<User>(User.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement =
        JsonObject(element.jsonObject.filterKeys { it != "password" && it != "salt" })
}