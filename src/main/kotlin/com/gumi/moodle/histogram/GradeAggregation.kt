package com.gumi.moodle.histogram

import com.gumi.moodle.model.Grade

data class TreeNode(val grade: Grade, val children: MutableList<TreeNode> = mutableListOf())

fun calculateParentGrades(grades: MutableSet<Grade>) {
    val mappedGrades = grades.filter { !(it.isLeaf && it.parentID == null) }.toMutableList()
    val nodes = mutableListOf<TreeNode>()
    val nodeMap = mutableMapOf<String, TreeNode>()
    mappedGrades
        .filter { it.parentID == null }
        .onEach { mappedGrades.remove(it) }
        .map { TreeNode(it) }
        .onEach { nodes.add(it) }
        .forEach { nodeMap[it.grade._id] = it }

    while (mappedGrades.isNotEmpty()) {
        tryAddingGradeToParent(mappedGrades, nodeMap)
    }
    nodes.forEach { calculatePoints(it) }
}

private fun tryAddingGradeToParent(mappedGrades: MutableList<Grade>, nodeMap: MutableMap<String, TreeNode>) {
    val grade = mappedGrades.removeFirst()
    val node = nodeMap[grade.parentID]
    if (node != null) {
        val newNode = TreeNode(grade)
        node.children.add(newNode)
        nodeMap[grade._id] = newNode
    } else {
        mappedGrades.add(grade)
    }
}

private fun calculatePoints(node: TreeNode) {
    if (node.grade.isLeaf) return

    node.children.forEach { calculatePoints(it) }
    node.children.forEach { sumPoints(node.grade, it.grade) }
}

private fun sumPoints(parent: Grade, child: Grade) {
    child.studentPoints.entries.forEach {
        if (it.key in parent.studentPoints.keys) {
            parent.studentPoints[it.key] = parent.studentPoints[it.key]!! + it.value
        } else {
            parent.studentPoints[it.key] = it.value
        }
    }
}