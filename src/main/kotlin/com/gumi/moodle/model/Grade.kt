package com.gumi.moodle.model

sealed class GradeTree(
    val level: Int,
    val name: String = ""
) {
    abstract fun filterStudents(id: UserID)

    override fun equals(other: Any?): Boolean {
        return other is GradeTree && other.name == this.name
    }

    override fun hashCode(): Int {
        var result = level
        result = 31 * result + name.hashCode()
        return result
    }
}

class GradeNode(
    level: Int,
    name: String,
    var children: MutableSet<GradeTree> = mutableSetOf()
) : GradeTree(level, name) {
    override fun filterStudents(id: UserID) {
        children.forEach { it.filterStudents(id) }
    }
}

class GradeLeaf(
    level: Int,
    name: String,
    val maxPoints: Int,
    val studentPoints: MutableMap<UserID, Int>
) : GradeTree(level, name) {
    override fun filterStudents(id: UserID) {
        studentPoints.keys.retainAll { it == id }
    }
}