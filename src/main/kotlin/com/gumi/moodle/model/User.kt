package com.gumi.moodle.model

class User(
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String,
    var roles: List<Role> = listOf(Role.STUDENT),
    var salt: String = ""
) {

    override fun toString(): String {
        return "$firstName $lastName  email: $email roles: $roles"
    }
}


enum class Role {
    ADMIN, STUDENT, TEACHER
}