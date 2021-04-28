package com.gumi.moodle.dao

import org.bson.conversions.Bson
import org.litote.kmongo.EMPTY_BSON
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.exists
import org.litote.kmongo.keyProjection
import org.litote.kmongo.property.KPropertyPath
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.reflect.KProperty1

abstract class AbstractDAO<T : Any, U>(protected val defaultQueryCreator: (U) -> Bson) {

    private val client = KMongo.createClient("mongodb://localhost:27017").coroutine
    protected val database = client.getDatabase("IOtest")

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

infix fun <K, T> KProperty1<out Any?, Map<out K, T>?>.atKey(key: K): KPropertyPath<out Any?, T?> =
    this.keyProjection(key)

infix fun <K, T> KProperty1<out Any?, Map<out K, T>?>.containsKey(key: K): Bson =
    this.keyProjection(key) exists true
