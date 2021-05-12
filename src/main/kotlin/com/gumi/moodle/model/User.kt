package com.gumi.moodle.model

import io.ktor.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.security.SecureRandom

typealias UserID = String

@Serializable
data class User(
    var _id: UserID? = null,
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String = "",
    var salt: String = "",
    var roles: Set<Role> = setOf(Role.STUDENT),
) {
    @Transient
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
            id: UserID? = null,
            firstName: String,
            lastName: String,
            email: String,
            password: String,
            roles: Set<Role> = setOf(Role.STUDENT),
        ): User = User(id, firstName, lastName, email, roles = roles).apply { hashPassword(password) }


        fun createUserWithPlaintextInput(
            user: User,
        ): User = createUserWithPlaintextInput(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            password = user.password,
            roles = user.roles
        )
    }
}

enum class Role { ADMIN, STUDENT, TEACHER }
