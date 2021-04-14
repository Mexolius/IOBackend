package com.gumi.moodle.dao

import com.gumi.moodle.model.User
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

class UserDAO : AbstractDAO<User, String>({ User::email eq it }) {

    override fun getCollection(): CoroutineCollection<User> =
        database.getCollection("User")

    override suspend fun exists(obj: User): Boolean =
        getCollection().find(User::email eq obj.email).toList().isNotEmpty()
}