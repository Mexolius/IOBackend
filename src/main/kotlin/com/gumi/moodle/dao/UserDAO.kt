package com.gumi.moodle.dao

import com.gumi.moodle.MONGO_URI
import com.gumi.moodle.USER_COLLECTION
import com.gumi.moodle.model.User
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
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
            .aggregate<User>(pipeline(query, includeCrypto, includeNotifications))
            .toList()

    suspend fun getOne(
        value: String,
        includeCrypto: Boolean = false,
        includeNotifications: Boolean = false,
        queryCreator: (String) -> Bson = defaultQueryCreator,
    ): User? =
        getCollection()
            .aggregate<User>(pipeline(queryCreator(value), includeCrypto, includeNotifications))
            .first()

    private fun pipeline(
        query: Bson,
        includeCrypto: Boolean,
        includeNotifications: Boolean
    ): List<Bson> =
        listOf(
            match(query),
            project(userProjection(includeCrypto, includeNotifications))
        )

    private fun userProjection(
        includeCrypto: Boolean,
        includeNotifications: Boolean
    ): Bson = exclude(mutableListOf<KProperty<*>>().apply {
        if (!includeCrypto) addAll(listOf(User::password, User::salt))
        if (!includeNotifications) add(User::notifications)
    })

}