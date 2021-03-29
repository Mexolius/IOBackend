package com.gumi.moodle.DAO

import com.gumi.moodle.model.User
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class UserDAO {

    private val client = KMongo.createClient("mongodb://localhost:27017").coroutine
    private val database = client.getDatabase("IOtest")
    private val collection = database.getCollection<User>("User")

    suspend fun getUsers(): List<User> {
        return collection.find().toList()
    }

    suspend fun addUser(user: User): Boolean {
        if (getUser(user.email) != null) {
            return false
        }
        collection.insertOne(user)
        return true
    }

    suspend fun getUser(email: String): User? {
        return getUsers().find { it.email == email }
    }

}