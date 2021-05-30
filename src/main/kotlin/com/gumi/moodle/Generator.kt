package com.gumi.moodle

import com.gumi.moodle.dao.CourseDAO
import com.gumi.moodle.dao.UserDAO
import com.gumi.moodle.dao.setTo
import com.gumi.moodle.model.*
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.eq
import kotlin.random.Random


fun main() {
    runBlocking { Generator().insertToDB() }
}

class Generator {
    private val userDAO = UserDAO()
    private val courseDAO = CourseDAO()

    suspend fun insertToDB() {
        var students = (1..20).map { newUser(it, isStudent = true, isTeacher = false) }
        var teachers = (1..10).map { newUser(it, isStudent = false, isTeacher = true) }
        val admin = User.createUserWithPlaintextInput(
            firstName = "aa",
            lastName = "bb",
            email = "aa@aa.aa",
            password = "aa",
            roles = setOf(Role.ADMIN)
        )
        userDAO.apply { drop() }.addAll(students + teachers + admin)

        //redownloading from DB to have id fields not null - db autofills them
        students = userDAO.getAll().filter { Role.STUDENT in it.roles }
        teachers = userDAO.getAll().filter { Role.TEACHER in it.roles }
        val courses = (1..10).map { newCourse(it, students, teachers) }
        courseDAO.apply { drop() }.addAll(courses)

        courseDAO.getAll().forEach { addNotificationsToStudents(it, students) }

        students.forEach { updateNotifications(it) } //couldn't find a way to do it in one update
    }

    private fun newUser(number: Int, isStudent: Boolean, isTeacher: Boolean): User {
        val roles = mutableSetOf<Role>()
        if (isStudent) roles.add(Role.STUDENT)
        if (isTeacher) roles.add(Role.TEACHER)
        val roleName = if (isStudent) "student" else "teacher"
        return User.createUserWithPlaintextInput(
            firstName = "firstname_$roleName$number",
            lastName = "lastname_$roleName$number",
            email = "$roleName$number@aa.aa",
            password = "aa",
            roles = roles
        )
    }

    private fun newCourse(number: Int, students: List<User>, teachers: List<User>): Course {
        val teachersSublist = getRandomSublist(teachers, Random.nextInt(1, 3))
        val studentsSublist = getRandomSublist(students, Random.nextInt(5, 15))
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

        val parentsToHaveChildrenAdded = chooseParentsToHaveChildrenAdded(parentGrades, leafGrades)
        addParentsToLeaves(parentsToHaveChildrenAdded, leafGrades)
        updateParentPoints(parentGrades, leafGrades)

        val grades = parentGrades + leafGrades + loneGrades
        return grades.toMutableSet()
    }

    private fun chooseParentsToHaveChildrenAdded(parentGrades: List<Grade>, leafGrades: List<Grade>): List<Grade> {
        val parentToChildrenMap = mapIdToGradeAndChildren(parentGrades, leafGrades)

        val withChildren = parentToChildrenMap.filter { it.value.second.size != 0 }.values.map { it.first }
        val withoutChildren = parentToChildrenMap.filter { it.value.second.size == 0 }.values.map { it.first }

        return withoutChildren + getRandomSublist(withChildren, withChildren.size / 2)
    }

    private fun updateParentPoints(parentGrades: List<Grade>, leafGrades: List<Grade>) {
        val parentToChildrenMap = mapIdToGradeAndChildren(parentGrades, leafGrades)

        parentToChildrenMap.values
            .associate { it.first to it.second.sumOf { it.maxPoints } }
            .forEach { it.key.maxPoints = it.value }
    }

    private fun mapIdToGradeAndChildren(
        parentGrades: List<Grade>,
        leafGrades: List<Grade>
    ): Map<GradeID, Pair<Grade, MutableList<Grade>>> {
        val parentToChildrenMap = parentGrades.associate { it._id to Pair(it, mutableListOf<Grade>()) }
        (parentGrades + leafGrades)
            .filter { it.parentID != null }
            .forEach { parentToChildrenMap[it.parentID]!!.second.add(it) }
        return parentToChildrenMap
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
        parentGrades: List<Grade>,
        leafGrades: List<Grade>,
    ) {
        val initialParents = parentGrades.toMutableList()
        leafGrades.forEach {
            val parent =
                if (initialParents.isNotEmpty()) initialParents.removeLast() else getRandomElement(parentGrades)
            it.parentID = parent._id
            it.level = parent.level + 1
        }
    }

    private fun assignGrades(grade: Grade, students: List<User>) {
        if (Random.nextInt(4) == 0) { // 1/4 chance of not adding any grades (i.e teacher specified for later)
            return
        }

        students.filter { Random.nextInt(4) != 0 }  // 3/4 chance of student having grade added
            .forEach { grade.studentPoints[it._id!!] = Random.nextInt(grade.maxPoints) }
    }

    private fun newGrade(number: Int, isLeaf: Boolean): Grade {
        return Grade("grade$number", "grade$number", isLeaf, 0, Random.nextInt(1, 100))
    }

    private fun addNotificationsToStudents(course: Course, students: List<User>) {
        val now = System.currentTimeMillis()
        val studentMap = students.associateBy { it._id }
        val notificationsMap = course.grades
            .map { g -> g.studentPoints.keys.associateWith { Notification(course, g, now) } }
            .flatMap { it.entries }
            .groupBy { it.key }
            .mapValues { entry -> entry.value.map { it.value } }

        notificationsMap.forEach { studentMap[it.key]?.notifications?.addAll(it.value) }
    }

    private suspend fun updateNotifications(s: User) {
        userDAO.updateOne(
            s._id as String,
            User::notifications setTo s.notifications
        ) { User::_id eq it }
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

