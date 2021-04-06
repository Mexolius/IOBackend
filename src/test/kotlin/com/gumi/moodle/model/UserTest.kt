package com.gumi.moodle.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UserTest {
    @Test
    fun passwordHashTest() {
        val password = "secret"
        val user = User("Fname", "Lname", "email")
        user.hashPassword(password)
        assertNotEquals(password, user.password)
    }

    @Test
    fun rightPasswordTest() {
        val password = "secret"
        val user = User("Fname", "Lname", "email")
        user.hashPassword(password)
        assertTrue(user.checkPassword(password))
    }

    @Test
    fun wrongPasswordTest() {
        val password = "secret"
        val other = "not secret"
        val user = User("Fname", "Lname", "email")
        user.hashPassword(password)
        assertFalse(user.checkPassword(other))
    }


}