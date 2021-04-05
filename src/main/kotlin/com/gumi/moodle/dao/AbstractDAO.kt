package com.gumi.moodle.com.gumi.moodle.dao

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

abstract class AbstractDAO<T : Any, U>(val extract: (T) -> U) {

    private val client = KMongo.createClient("mongodb://localhost:27017").coroutine
    val database = client.getDatabase("IOtest")

    abstract fun getCollection(): CoroutineCollection<T> //retified generic in collection was breaking stuff when tried to be generified

    suspend fun getAll(): List<T> {
        return getCollection().find().toList()
    }

    suspend fun add(obj: T): Boolean {
        if (exists(obj)) {
            return false
        }
        return getCollection().insertOne(obj).wasAcknowledged()
    }

    suspend fun addAll(obj: List<T>): Boolean {
        return getCollection().insertMany(obj).wasAcknowledged()
    }

    private suspend fun exists(obj: T): Boolean { //this should get optimized in the future
        return obj in getAll()
    }

    suspend fun getOne(arg: U): T? { //this should get optimized in the future
        return getAll().find { extract(it) == arg }
    }

    suspend fun drop() {
        getCollection().drop()
    }
}