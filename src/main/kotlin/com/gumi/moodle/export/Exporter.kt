package com.gumi.moodle.export

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.gumi.moodle.model.Course
import com.gumi.moodle.model.Grade
import com.gumi.moodle.model.User
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.xssf.usermodel.*
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class Exporter(val course: Course, val students: List<User>) {

    private val grades = course.grades
    private val leafGrades = grades.filter { it.isLeaf }.sortedWith(Comparator(this::compare))
    private val numberOfDescriptors = 2

    fun exportCSV(): ByteArray {
        val header = listOf("firstName", "lastName") + leafGrades.map { nameWithParents(it) }
        val rows2 = course.students.map { s -> studentProps(s) + leafGrades.map { it.studentPoints[s] } }

        val outputStream = ByteArrayOutputStream()
        CsvWriter().writeAll(listOf(header) + rows2, "export.csv")
        CsvWriter().writeAll(listOf(header) + rows2, outputStream)
        return outputStream.toByteArray()
    }

    fun exportXLS(): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Grades")

        val header = listOf("firstName", "lastName") + leafGrades.map { it.name }
        val rows = course.students.map { s -> studentProps(s) + leafGrades.map { it.studentPoints[s]?.toString() ?: "" } }

        val maxLevel = getMaxLevel()

        fillRowCells(sheet, maxLevel, header)
        rows.forEachIndexed { i, values -> fillRowCells(sheet, i + maxLevel + 1, values) }
        (0 until maxLevel).forEach { fillUpperHeaderRow(sheet, it) }

        applyStyling(workbook, sheet, maxLevel, rows)

        writeWorkbookToFile(workbook)
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        return outputStream.toByteArray()
    }

    fun nameWithParents(grade: Grade): String {
        var g = grade
        var out = grade.name
        while (g.parentID != null) {
            g = grades.first { it.name == g.parentID }
            out = g.name + ":" + out
        }
        return out
    }

    private fun applyStyling(workbook: XSSFWorkbook, sheet: XSSFSheet, maxLevel: Int, rows: List<List<String>>) {
        val style = workbook.createCellStyle()
        val boldFont = workbook.createFont()
        boldFont.bold = true
        style.setFont(boldFont)

        val headerRow = sheet.getRow(maxLevel)
        leafGrades.indices.map { it + 2 }.forEach { headerRow.getCell(it)!!.cellStyle = style }

        val (bgFillStyles, bgFillStylesBold) = createFilledBackgroundStyles(workbook, boldFont)
        val (style3, style4) = createNotFilledBackgroundStyles(workbook, boldFont)

        (0..rows.size).map { it + maxLevel }.forEach { rowNumber ->
            addStyleToRow(sheet, rowNumber, maxLevel, bgFillStyles, bgFillStylesBold, style4, style3)
        }
    }

    private fun addStyleToRow(
        sheet: XSSFSheet,
        rowNumber: Int,
        maxLevel: Int,
        bgFillStyles: List<XSSFCellStyle>,
        bgFillStylesBold: List<XSSFCellStyle>,
        style4: XSSFCellStyle,
        style3: XSSFCellStyle,
    ) {
        val row = sheet.getRow(rowNumber)

        (leafGrades.indices).map { it + numberOfDescriptors }
            .forEach { addColoredStyleToCell(row, it, rowNumber, maxLevel, bgFillStyles, bgFillStylesBold) }

        (0 until numberOfDescriptors).forEach { addNotColoredStyleToCell(row, it, rowNumber, maxLevel, style4, style3) }

    }

    private fun addNotColoredStyleToCell(row: XSSFRow, it: Int, rowNumber: Int, maxLevel: Int, style4: XSSFCellStyle, style3: XSSFCellStyle) {
        row.getCell(it, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)!!.cellStyle = if (rowNumber == maxLevel) style4 else style3
    }

    private fun addColoredStyleToCell(
        row: XSSFRow,
        it: Int,
        rowNumber: Int,
        maxLevel: Int,
        bgFillStyles: List<XSSFCellStyle>,
        bgFillStylesBold: List<XSSFCellStyle>,
    ) {
        row.getCell(it, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)!!.cellStyle = bgFillStyle(it, rowNumber == maxLevel, bgFillStyles, bgFillStylesBold)
    }

    private fun createNotFilledBackgroundStyles(
        workbook: XSSFWorkbook,
        boldFont: XSSFFont?,
    ): Pair<XSSFCellStyle, XSSFCellStyle> {
        val style = workbook.createCellStyle()
        style.borderBottom = BorderStyle.THIN
        style.setBottomBorderColor(XSSFColor(Color(80, 80, 80)))

        val styleBold = workbook.createCellStyle()
        styleBold.cloneStyleFrom(style)
        styleBold.setFont(boldFont)
        return Pair(style, styleBold)
    }

    private fun createFilledBackgroundStyles(workbook: XSSFWorkbook, boldFont: XSSFFont?): Pair<List<XSSFCellStyle>, List<XSSFCellStyle>> {
        val style1 = workbook.createCellStyle()
        style1.setFillForegroundColor(XSSFColor(Color(220, 220, 220)))
        style1.fillPattern = FillPatternType.SOLID_FOREGROUND
        style1.borderBottom = BorderStyle.THIN
        style1.setBottomBorderColor(XSSFColor(Color(80, 80, 80)))

        val style2 = workbook.createCellStyle()
        style2.cloneStyleFrom(style1)
        style2.setFillForegroundColor(XSSFColor(Color(240, 240, 240)))

        val bgFillStyles = listOf(style1, style2)
        val bgFillStylesBold = bgFillStyles
            .map { workbook.createCellStyle() to it }
            .onEach { it.first.cloneStyleFrom(it.second) }
            .map { it.first }
            .onEach { it.setFont(boldFont) }
        return Pair(bgFillStyles, bgFillStylesBold)
    }

    private fun bgFillStyle(
        i: Int,
        bold: Boolean,
        styles: List<XSSFCellStyle>,
        stylesBold: List<XSSFCellStyle>,
    ): XSSFCellStyle {
        val chosenStyles = if (bold) stylesBold else styles
        return chosenStyles[i % chosenStyles.size]
    }

    private fun writeWorkbookToFile(workbook: XSSFWorkbook) {
        val path = File(".").absolutePath
        val fileLocation = path.substring(0, path.length - 1) + "export.xlsx"

        val fileStream = FileOutputStream(fileLocation)
        workbook.write(fileStream)
        fileStream.close()
    }

    private fun fillUpperHeaderRow(sheet: XSSFSheet, rowNumber: Int) {
        val row = sheet.createRow(rowNumber)
        leafGrades.forEachIndexed { i, g ->
            if (rowNumber >= g.level) {
                return@forEachIndexed
            }
            fillUpperHeaderCell(row, g, rowNumber, i)
        }
        mergeSameCells(row, sheet, rowNumber)
    }

    private fun mergeSameCells(row: XSSFRow, sheet: XSSFSheet, rowNumber: Int) {
        var i = 0
        while (i < leafGrades.size) {
            val first = i + numberOfDescriptors
            var last = i + numberOfDescriptors
            while (last < leafGrades.size + numberOfDescriptors && bothCellsContainIdenticalValues(row, last, last + 1)) {
                last++
                i++
            }
            if (first != last) {
                sheet.addMergedRegion(CellRangeAddress(rowNumber, rowNumber, first, last))
                CellUtil.setAlignment(row.getCell(first), HorizontalAlignment.CENTER)
            }
            i++
        }
    }

    private fun bothCellsContainIdenticalValues(row: XSSFRow, col1: Int, col2: Int): Boolean {
        val cell1 = row.getCell(col1)
        val cell2 = row.getCell(col2)
        if (cell1 == null || cell2 == null) {
            return false
        }
        return cell1.toString() == cell2.toString()
    }

    private fun fillUpperHeaderCell(row: XSSFRow, leafGrade: Grade, rowNumber: Int, i: Int) {
        val stack = getStack(leafGrade)
        val grade = stack[leafGrade.level - rowNumber]
        row.createCell(i + numberOfDescriptors).setCellValue(grade.name)
    }

    private fun fillRowCells(sheet: XSSFSheet, rowNumber: Int, values: List<String>) {
        val row = sheet.createRow(rowNumber)
        values.forEachIndexed { i1, v -> row.createCell(i1).setCellValue(v) }
    }

    private fun studentProps(id: String): List<String> {
        val student = students.find { it._id == id }!!
        return listOf(student.firstName, student.lastName)
    }

    private fun getMaxLevel(): Int {
        return grades.map { it.level }.maxOrNull() ?: throw IllegalStateException("no grades to find levels")
    }

    private fun getStack(grade: Grade): List<Grade> {
        var root = grade
        val roots = mutableListOf(root)
        while (root.parentID != null) {
            root = grades.first { it.name == root.parentID }
            roots.add(root)
        }
        return roots
    }

    private fun compare(grade1: Grade, grade2: Grade): Int {
        val roots1 = getStack(grade1).toMutableList()
        val roots2 = getStack(grade2).toMutableList()

        while (roots1.last() == roots2.last()) {
            roots1.removeLast()
            roots2.removeLast()
        }
        return roots1.last().name.compareTo(roots2.last().name)
    }


}

