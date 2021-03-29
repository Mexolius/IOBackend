package com.gumi.moodle.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UserTest {
    @Test
    fun passwordHashTest(){
        val password = "secret"
        val user = User("Fname", "Lname", "email", password)
        assertNotEquals(password, user.password)
    }

    @Test
    fun rightPasswordTest(){
        val password = "secret"
        val user = User("Fname", "Lname", "email", password)
        assertTrue(user.checkPassword(password))
    }

    @Test
    fun wrongPasswordTest(){
        val password = "secret"
        val other = "not secret"
        val user = User("Fname", "Lname", "email", password)
        assertFalse(user.checkPassword(other))
    }


}