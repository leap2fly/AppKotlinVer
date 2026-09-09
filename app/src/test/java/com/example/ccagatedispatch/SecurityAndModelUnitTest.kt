package com.example.ccagatedispatch

import com.example.ccagatedispatch.data.local.entity.StudentEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAndModelUnitTest {

    @Test
    fun testStudentEntityValidation() {
        val student = StudentEntity(
            studentId = "STU_1001",
            name = "Alex Mercer",
            std = "Grade 10",
            division = "10-A",
            commuteMode = "Self",
            parentId = "P_801",
            parentName = "Robert Mercer",
            vanId = null,
            selfAuth = true,
            acYear = "2024-2025"
        )

        assertEquals("STU_1001", student.studentId)
        assertTrue(student.selfAuth)
        assertEquals("Self", student.commuteMode)
    }

    @Test
    fun testSelfAuthRequirement() {
        val unauthStudent = StudentEntity(
            studentId = "STU_1002",
            name = "Jordan Lee",
            std = "Grade 11",
            division = "11-B",
            commuteMode = "Self",
            parentId = null,
            parentName = null,
            vanId = null,
            selfAuth = false,
            acYear = "2024-2025"
        )

        assertFalse(unauthStudent.selfAuth)
    }
}
