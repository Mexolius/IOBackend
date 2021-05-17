package com.gumi.moodle.dao

import com.gumi.moodle.MONGO_URI
import com.gumi.moodle.USER_COLLECTION
import com.gumi.moodle.model.User
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import org.litote.kmongo.eq
import org.litote.kmongo.exclude
import kotlin.reflect.KProperty

class UserDAO(mongoURI: String = MONGO_URI) : AbstractDAO<User, String>(mongoURI, { User::email eq it }) {

    override fun getCollection(): CoroutineCollection<User> =
        database.getCollection(USER_COLLECTION)

    override suspend fun exists(obj: User): Boolean =
        getCollection().find(User::email eq obj.email).toList().isNotEmpty()

    override suspend fun getAll(query: Bson): List<User> =
        getCollection().find(query).projection(exclude(User::password, User::salt, User::notifications)).toList()

    suspend fun getAll(
        query: Bson = EMPTY_BSON,
        includeCrypto: Boolean = false,
        includeNotifications: Boolean = false,
    ): List<User> =
        getCollection()
            .find(query)
            .applyExcludes(includeCrypto, includeNotifications)
            .toList()


    suspend fun getOne(
        value: String,
        includeCrypto: Boolean = false,
        includeNotifications: Boolean = false,
        queryCreator: (String) -> Bson = defaultQueryCreator,
    ): User? =
        getCollection()
            .find(queryCreator(value))
            .applyExcludes(includeCrypto, includeNotifications)
            .first()


    private fun CoroutineFindPublisher<User>.applyExcludes(includeCrypto: Boolean, includeNotifications: Boolean): CoroutineFindPublisher<User> {
        val fields = mutableListOf<KProperty<*>>()
        if (!includeCrypto) fields.addAll(listOf(User::password, User::salt))
        if (!includeNotifications) fields.add(User::notifications)

        if (fields.isNotEmpty()) {
            projection(exclude(fields))
        }

        return this
    }


}