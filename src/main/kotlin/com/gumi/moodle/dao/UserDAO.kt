package com.gumi.moodle.dao

import com.gumi.moodle.model.User
import org.litote.kmongo.coroutine.CoroutineCollection

class UserDAO : AbstractDAO<User, String>({ it.email }) {

    override fun getCollection(): CoroutineCollection<User> {
        return database.getCollection("User")
    }

}