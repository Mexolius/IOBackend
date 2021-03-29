package com.gumi.moodle.model

import java.security.SecureRandom

import java.security.spec.InvalidKeySpecException

import javax.crypto.SecretKeyFactory

import javax.crypto.spec.PBEKeySpec


class User(
    var firstName: String,
    var lastName: String,
    var email: String,
    var password: String,
    var roles: List<Role> = listOf(Role.STUDENT),
    var salt: ByteArray = ByteArray(0)
) {
    private val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")

    fun hashPassword(newPassword: String) {
        val random = SecureRandom()
        val hash: ByteArray
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        val spec = PBEKeySpec(newPassword.toCharArray(), saltBytes, 65536, 128)
        hash = try {
            keyFactory.generateSecret(spec).encoded
        } catch (invalidKeySpecException: InvalidKeySpecException) {
            invalidKeySpecException.printStackTrace()
            return
        }
        this.salt = saltBytes
        this.password = String(hash)
    }

    fun checkPassword(input: String): Boolean {
        val keySpec = PBEKeySpec(input.toCharArray(), salt, 65536, 128)
        try {
            val hash = keyFactory.generateSecret(keySpec).encoded
            return password == String(hash)
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return false
    }

    override fun toString(): String {
        return "$firstName $lastName  email: $email roles: $roles"
    }
}


enum class Role {
    ADMIN, STUDENT, TEACHER
}