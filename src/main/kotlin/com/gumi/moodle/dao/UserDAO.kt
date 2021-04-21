package com.gumi.moodle.dao

import com.gumi.moodle.model.User
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.exclude

class UserDAO : AbstractDAO<User, String>({ User::email eq it }) {

    override fun getCollection(): CoroutineCollection<User> =
        database.getCollection("User")

    override suspend fun exists(obj: User): Boolean =
        getCollection().find(User::email eq obj.email).toList().isNotEmpty()

    override suspend fun getAll(query: Bson): List<User> =
        getCollection().find(query).projection(exclude(User::password, User::salt)).toList()

    suspend fun getOne(
        value: String,
        includeCrypto: Boolean,
        queryCreator: (String) -> Bson = defaultQueryCreator
    ): User? =
        if (includeCrypto)
            getCollection().findOne(queryCreator(value))
        else
            getCollection().find(queryCreator(value))
                .projection(exclude(User::password, User::salt))
                .first()
}