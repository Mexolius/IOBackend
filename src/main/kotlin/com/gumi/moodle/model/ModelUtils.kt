package com.gumi.moodle.model

import kotlinx.serialization.json.*

class CourseSerializer(private val studentID: UserID) : JsonTransformingSerializer<Course>(Course.serializer()) {
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
