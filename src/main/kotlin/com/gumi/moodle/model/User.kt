package com.gumi.moodle.model

import io.ktor.util.*
import java.security.SecureRandom


enum class Role { ADMIN, STUDENT, TEACHER }

class User(
    var _id: String?,
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

    override fun equals(other: Any?): Boolean { //this equals is for finding if user already exists in db, if there is a need to use traditional comparison please feel free to make this a separate method and swap it
        if (this === other) return true
        if (other !is User) return false
        return email == other.email
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }

    companion object {
        fun createUserWithPlaintextInput(
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            roles: Set<Role>,
        ): User {
            return User(null, firstName, lastName, email, "", "", roles).apply { hashPassword(password) }
        }

        fun createUserWithPlaintextInput(
            user: User,
        ): User {
            return createUserWithPlaintextInput(user.firstName, user.lastName, user.email, user.password, user.roles)
        }
    }
}