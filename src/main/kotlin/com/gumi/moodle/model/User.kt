package com.gumi.moodle.model

import io.ktor.util.*
import java.security.SecureRandom


class User(
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String = "",
    var salt: String = "",
    var roles: List<Role> = listOf(Role.STUDENT)
) {
    private val digestFunction = getDigestFunction("SHA-256") { salt }

    fun hashPassword(plaintext: String) {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        this.salt = String(saltBytes)
        val hash = digestFunction(plaintext)
        this.password = String(hash)
    }

    fun checkPassword(input: String): Boolean {
        val hash = digestFunction(input)
        return password == String(hash)
    }

    override fun toString(): String {
        return "$firstName $lastName  email: $email roles: $roles"
    }
}


enum class Role {
    ADMIN, STUDENT, TEACHER
}