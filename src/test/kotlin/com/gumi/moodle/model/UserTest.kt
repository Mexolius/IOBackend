package com.gumi.moodle.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserTest {
    @Test
    fun properPasswordTest(){
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