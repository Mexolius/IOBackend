package com.gumi.moodle.model

import io.ktor.util.*
import java.security.SecureRandom

typealias UserID = String

data class User(
    var _id: UserID?,
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String = "",
    var salt: String = "",
    var roles: Set<Role> = setOf(Role.STUDENT),
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

    companion object {
        fun createUserWithPlaintextInput(
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            roles: Set<Role> = setOf(Role.STUDENT),
        ): User {
            return User(_id = null, firstName, lastName, email, roles = roles).apply { hashPassword(password) }
        }

        fun createUserWithPlaintextInput(
            user: User,
        ): User {
            return createUserWithPlaintextInput(user.firstName, user.lastName, user.email, user.password, user.roles)
        }
    }
}