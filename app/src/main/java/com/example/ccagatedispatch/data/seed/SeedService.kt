package com.example.ccagatedispatch.data.seed

import com.example.ccagatedispatch.data.local.dao.AppDao
import com.example.ccagatedispatch.data.local.entity.AcademicYearEntity
import com.example.ccagatedispatch.data.local.entity.CcaCoachCrossRef
import com.example.ccagatedispatch.data.local.entity.CcaEntity
import com.example.ccagatedispatch.data.local.entity.CoachEntity
import com.example.ccagatedispatch.data.local.entity.StudentCcaEnrollmentCrossRef
import com.example.ccagatedispatch.data.local.entity.StudentEntity
import com.example.ccagatedispatch.data.local.entity.VanEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedService @Inject constructor(
    private val appDao: AppDao
) {
    suspend fun seedDefaultDataIfEmpty() {
        if (appDao.getStudentCount() > 0) {
            // Data already seeded; skip to prevent overwriting active logs or records
            return
        }

        val acYear = "2024-2025"
        appDao.insertAcademicYear(AcademicYearEntity(year = acYear))

        val coaches = listOf(
            CoachEntity(coachId = "C101", name = "Coach Marcus Vance", acYear = acYear),
            CoachEntity(coachId = "C102", name = "Coach Sarah Connor", acYear = acYear),
            CoachEntity(coachId = "C103", name = "Coach David Zhang", acYear = acYear)
        )
        appDao.insertCoaches(coaches)

        val ccas = listOf(
            CcaEntity(ccaId = "CCA_BASKETBALL", name = "Varsity Basketball", schedule = "Mon/Wed 15:30", acYear = acYear),
            CcaEntity(ccaId = "CCA_ROBOTICS", name = "Robotics & AI Club", schedule = "Tue/Thu 15:30", acYear = acYear),
            CcaEntity(ccaId = "CCA_SWIMMING", name = "Competitive Swimming", schedule = "Mon/Fri 16:00", acYear = acYear)
        )
        appDao.insertCcas(ccas)

        val ccaCoachCrossRefs = listOf(
            CcaCoachCrossRef(ccaId = "CCA_BASKETBALL", coachId = "C101"),
            CcaCoachCrossRef(ccaId = "CCA_ROBOTICS", coachId = "C102"),
            CcaCoachCrossRef(ccaId = "CCA_SWIMMING", coachId = "C103")
        )
        appDao.insertCcaCoachCrossRefs(ccaCoachCrossRefs)

        val vans = listOf(
            VanEntity(vanId = "VAN_01", driverName = "Alan Miller", driverId = "DRV_901", route = "Green Route - North Hills", acYear = acYear),
            VanEntity(vanId = "VAN_02", driverName = "Elena Rostova", driverId = "DRV_902", route = "Blue Route - Downtown Express", acYear = acYear)
        )
        appDao.insertVans(vans)

        val students = listOf(
            // Self commute students
            StudentEntity(
                studentId = "STU_1001",
                name = "Alex Mercer",
                std = "Grade 10",
                division = "10-A",
                commuteMode = "Self",
                parentId = "P_801",
                parentName = "Robert Mercer",
                vanId = null,
                selfAuth = true,
                acYear = acYear
            ),
            StudentEntity(
                studentId = "STU_1002",
                name = "Jordan Lee",
                std = "Grade 11",
                division = "11-B",
                commuteMode = "Self",
                parentId = "P_802",
                parentName = "Grace Lee",
                vanId = null,
                selfAuth = false, // Not pre-authed!
                acYear = acYear
            ),
            // Parent pickup sibling group 1
            StudentEntity(
                studentId = "STU_2001",
                name = "Ethan Thorne",
                std = "Grade 6",
                division = "6-A",
                commuteMode = "Parent",
                parentId = "P_PAR_THORNE",
                parentName = "Eleanor Thorne",
                vanId = null,
                selfAuth = false,
                acYear = acYear
            ),
            StudentEntity(
                studentId = "STU_2002",
                name = "Maya Thorne",
                std = "Grade 8",
                division = "8-C",
                commuteMode = "Parent",
                parentId = "P_PAR_THORNE",
                parentName = "Eleanor Thorne",
                vanId = null,
                selfAuth = false,
                acYear = acYear
            ),
            // Van commute students
            StudentEntity(
                studentId = "STU_3001",
                name = "Lucas Scott",
                std = "Grade 7",
                division = "7-B",
                commuteMode = "Van",
                parentId = "P_805",
                parentName = "Thomas Scott",
                vanId = "VAN_01",
                selfAuth = false,
                acYear = acYear
            ),
            StudentEntity(
                studentId = "STU_3002",
                name = "Chloe Bennett",
                std = "Grade 9",
                division = "9-A",
                commuteMode = "Van",
                parentId = "P_806",
                parentName = "Maria Bennett",
                vanId = "VAN_01",
                selfAuth = false,
                acYear = acYear
            )
        )
        appDao.insertStudents(students)

        val enrollments = listOf(
            StudentCcaEnrollmentCrossRef(studentId = "STU_1001", ccaId = "CCA_BASKETBALL"),
            StudentCcaEnrollmentCrossRef(studentId = "STU_1002", ccaId = "CCA_ROBOTICS"),
            StudentCcaEnrollmentCrossRef(studentId = "STU_2001", ccaId = "CCA_BASKETBALL"),
            StudentCcaEnrollmentCrossRef(studentId = "STU_2002", ccaId = "CCA_SWIMMING"),
            StudentCcaEnrollmentCrossRef(studentId = "STU_3001", ccaId = "CCA_BASKETBALL"),
            StudentCcaEnrollmentCrossRef(studentId = "STU_3002", ccaId = "CCA_ROBOTICS")
        )
        appDao.insertStudentCcaEnrollments(enrollments)
    }
}
