package com.gumi.moodle

import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.Role
import com.gumi.moodle.model.User
import kotlinx.coroutines.runBlocking
import kotlin.random.Random


fun main() {
    runBlocking { Generator().insertToDB() }
}

class Generator {


    suspend fun insertToDB() {
        var students = (1..20).map { newUser(it, true, false) }
        var teachers = (21..30).map { newUser(it, false, true) }
        val admin = User.createUserWithPlaintextInput("aa", "bb", "aa@aa.aa", "aa", setOf(Role.ADMIN))
        UserDAO().apply { drop() }.addAll(students + teachers + admin)

        students = UserDAO().getAll()
            .filter { Role.STUDENT in it.roles } //redownloading from DB to have id fields not null - db autofills them
        teachers = UserDAO().getAll().filter { Role.TEACHER in it.roles }
        val courses = (1..10).map { newCourse(it, students, teachers) }
        CourseDAO().apply { drop() }.addAll(courses)
    }

    private fun newUser(number: Int, isStudent: Boolean, isTeacher: Boolean): User {
        val roles = mutableSetOf<Role>()
        if (isStudent) roles.add(Role.STUDENT)
        if (isTeacher) roles.add(Role.TEACHER)
        return User.createUserWithPlaintextInput(
            "firstname$number",
            "lastname$number",
            "email$number@aa.aa",
            "aa",
            roles
        )
    }

    private fun newCourse(number: Int, students: List<User>, teachers: List<User>): Course {
        val teachersSublist = getRandomSublist(teachers, Random.nextInt(1, 3))
        val studentsSublist = getRandomSublist(students, Random.nextInt(0, 10))
        return Course(
            null,
            "course$number",
            "example description",
            100,
            studentsSublist.map { it._id!! }.toMutableSet(),
            teachersSublist.map { it._id!! }.toMutableSet(),
            mutableSetOf(Grade("grade1", "grade1", 0, Random.nextInt(100)))
        )
    }

    private fun <T> getRandomElement(list: List<T>): T {
        val n: Int = Random.nextInt(list.size)
        return list[n]
    }

    private fun <T> getRandomSublist(list: List<T>, size: Int): List<T> {
        assert(size <= list.size) { "Trying to get more elements from the list that is in the list" }
        return list.shuffled().subList(0, size)
    }

}

