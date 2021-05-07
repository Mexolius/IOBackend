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
    private val userDAO = UserDAO()
    private val courseDAO = CourseDAO()

    suspend fun insertToDB() {
        var students = (1..20).map { newUser(it, isStudent = true, isTeacher = false) }
        var teachers = (21..30).map { newUser(it, isStudent = false, isTeacher = true) }
        val admin = User.createUserWithPlaintextInput(
            firstName = "aa",
            lastName = "bb",
            email = "aa@aa.aa",
            password = "aa",
            roles = setOf(Role.ADMIN)
        )
        userDAO.apply { drop() }.addAll(students + teachers + admin)

        students = userDAO.getAll()
            .filter { Role.STUDENT in it.roles } //redownloading from DB to have id fields not null - db autofills them
        teachers = userDAO.getAll().filter { Role.TEACHER in it.roles }
        val courses = (1..10).map { newCourse(it, students, teachers) }
        courseDAO.apply { drop() }.addAll(courses)
    }

    private fun newUser(number: Int, isStudent: Boolean, isTeacher: Boolean): User {
        val roles = mutableSetOf<Role>()
        if (isStudent) roles.add(Role.STUDENT)
        if (isTeacher) roles.add(Role.TEACHER)
        return User.createUserWithPlaintextInput(
            firstName = "firstname$number",
            lastName = "lastname$number",
            email = "email$number@aa.aa",
            password = "aa",
            roles = roles
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
            newGrading(studentsSublist)
        )
    }

    private fun newGrading(students: List<User>): MutableSet<Grade> {
        val parentGrades = (1..10)
            .map { newGrade(it, false) }
        val leafGrades = (11..20)
            .map { newGrade(it, true) }
        val loneGrades = (21..25)
            .map { newGrade(it, true) }
        leafGrades.forEach { assignGrades(it, students) }
        loneGrades.forEach { assignGrades(it, students) }

        assignParentsToParents(parentGrades, 3)

        addParentsToLeaves(leafGrades, parentGrades)

        val grades = parentGrades + leafGrades + loneGrades
        return grades.toMutableSet()
    }

    private fun assignParentsToParents(parentGrades: List<Grade>, avgChildren: Int) {
        val starting = parentGrades.toMutableList()
        val toPop = mutableListOf(popRandomElement(starting), popRandomElement(starting))
        val finished = mutableListOf<Grade>()

        while (starting.isNotEmpty()) {
            val node = popRandomElement(toPop)
            for (i in 1..Random.nextInt(1, avgChildren * 2)) {
                if (starting.isEmpty()) {
                    break
                }
                val childNode = popRandomElement(starting)
                childNode.parentID = node._id
                childNode.level = node.level + 1
                finished.add(childNode)
            }
            toPop.addAll(finished)
            finished.clear()
        }
    }

    private fun addParentsToLeaves(
        leafGrades: List<Grade>,
        parentGrades: List<Grade>,
    ) {
        leafGrades.forEach {
            val parent = getRandomElement(parentGrades)
            it.parentID = parent._id
            it.level = parent.level + 1
        }
    }

    private fun assignGrades(grade: Grade, students: List<User>) {
        students.filter { Random.nextBoolean() }
            .forEach { grade.studentPoints[it._id!!] = Random.nextInt(grade.maxPoints) }
    }

    private fun newGrade(number: Int, isLeaf: Boolean): Grade {
        return Grade("grade$number", "grade$number", isLeaf, 0, Random.nextInt(1, 100))
    }

    private fun <T> getRandomElement(list: List<T>): T {
        val n: Int = Random.nextInt(list.size)
        return list[n]
    }

    private fun <T> popRandomElement(list: MutableList<T>): T {
        val n: Int = Random.nextInt(list.size)
        return list.removeAt(n)
    }

    private fun <T> getRandomSublist(list: List<T>, size: Int): List<T> {
        assert(size <= list.size) { "Trying to get more elements from the list that is in the list" }
        return list.shuffled().subList(0, size)
    }

}

