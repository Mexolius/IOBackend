package com.gumi.moodle.dao

import com.gumi.moodle.MONGO_DB_NAME
import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

abstract class AbstractDAO<T : Any, U>(mongoURI: String, protected val defaultQueryCreator: (U) -> Bson) {

    private val client = KMongo.createClient(mongoURI).coroutine
    protected val database = client.getDatabase(MONGO_DB_NAME)

    protected abstract fun getCollection(): CoroutineCollection<T>
    abstract suspend fun exists(obj: T): Boolean

    open suspend fun getAll(query: Bson = EMPTY_BSON): List<T> =
        getCollection().find(query).toList()

    suspend fun add(obj: T): Boolean =
        if (exists(obj)) false
        else getCollection().insertOne(obj).wasAcknowledged()

    suspend fun addAll(obj: List<T>): Boolean =
        getCollection().insertMany(obj).wasAcknowledged()

    suspend fun getOne(
        value: U,
        queryCreator: (U) -> Bson = defaultQueryCreator
    ): T? =
        getCollection().findOne(queryCreator(value))

    suspend fun updateOne(
        value: U,
        update: Bson,
        queryCreator: (U) -> Bson = defaultQueryCreator
    ): Boolean =
        getCollection().updateOne(queryCreator(value), update).wasAcknowledged()

    suspend fun drop() =
        getCollection().drop()
}
