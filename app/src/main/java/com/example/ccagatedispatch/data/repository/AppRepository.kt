package com.example.ccagatedispatch.data.repository

import com.example.ccagatedispatch.data.local.dao.AppDao
import com.example.ccagatedispatch.data.local.entity.ClassSessionEntity
import com.example.ccagatedispatch.data.local.entity.CoachAttendanceEntity
import com.example.ccagatedispatch.data.local.entity.CoachEntity
import com.example.ccagatedispatch.data.local.entity.EntryLogEntity
import com.example.ccagatedispatch.data.local.entity.ExitLogEntity
import com.example.ccagatedispatch.data.local.entity.StudentAttendanceEntity
import com.example.ccagatedispatch.data.local.entity.StudentEntity
import com.example.ccagatedispatch.data.local.entity.CcaEntity
import com.example.ccagatedispatch.data.local.entity.VanEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val appDao: AppDao
) {
    // Audit Counters
    val studentCountFlow: Flow<Int> = appDao.getStudentCountFlow()
    val entryLogsCountFlow: Flow<Int> = appDao.getEntryLogsCountFlow()
    val exitLogsCountFlow: Flow<Int> = appDao.getExitLogsCountFlow()
    val classSessionsCountFlow: Flow<Int> = appDao.getClassSessionsCountFlow()

    // Logs
    val allEntryLogs: Flow<List<EntryLogEntity>> = appDao.getAllEntryLogsFlow()
    val allExitLogs: Flow<List<ExitLogEntity>> = appDao.getAllExitLogsFlow()

    // Entrance Logging
    suspend fun getStudentById(studentId: String): StudentEntity? = appDao.getStudentById(studentId)

    suspend fun logStudentEntry(studentId: String): Boolean {
        val student = appDao.getStudentById(studentId) ?: return false
        appDao.insertEntryLog(EntryLogEntity(studentId = student.studentId))
        return true
    }

    // Coach Portal
    suspend fun getAllCoaches(): List<CoachEntity> = appDao.getAllCoaches()
    suspend fun getAllCcas(): List<CcaEntity> = appDao.getAllCcas()

    suspend fun logCoachAttendance(coachId: String, ccaId: String) {
        appDao.insertCoachAttendance(
            CoachAttendanceEntity(
                coachId = coachId,
                ccaId = ccaId,
                status = "Present"
            )
        )
    }

    suspend fun getEnrolledStudents(ccaId: String): List<StudentEntity> {
        return appDao.getStudentsEnrolledInCca(ccaId)
    }

    suspend fun updateStudentAttendance(studentId: String, ccaId: String, dateKey: String, status: String) {
        appDao.insertOrUpdateStudentAttendance(
            StudentAttendanceEntity(
                studentId = studentId,
                ccaId = ccaId,
                dateKey = dateKey,
                status = status
            )
        )
    }

    suspend fun getStudentAttendanceForDate(ccaId: String, dateKey: String): Map<String, String> {
        return appDao.getStudentAttendanceForDate(ccaId, dateKey).associate {
            it.studentId to it.status
        }
    }

    suspend fun completeClassSession(ccaId: String, dateKey: String, coachId: String) {
        val existing = appDao.getClassSession(ccaId, dateKey)
        val session = ClassSessionEntity(
            id = existing?.id ?: 0,
            ccaId = ccaId,
            dateKey = dateKey,
            completedByCoachId = coachId,
            isCompleted = true
        )
        appDao.insertClassSession(session)
    }

    suspend fun isClassSessionCompleted(ccaId: String, dateKey: String): Boolean {
        return appDao.isClassSessionCompleted(ccaId, dateKey) ?: false
    }

    // Gate Dispatch
    suspend fun getStudentsByParentId(parentId: String): List<StudentEntity> {
        return appDao.getStudentsByParentId(parentId)
    }

    suspend fun getVanByDriverOrVanId(driverId: String): VanEntity? {
        return appDao.getVanByDriverOrVanId(driverId)
    }

    suspend fun getStudentsByVanId(vanId: String): List<StudentEntity> {
        return appDao.getStudentsByVanId(vanId)
    }

    suspend fun logExitGate(
        studentId: String,
        gateId: String,
        commuteMode: String,
        overrideReason: String? = null,
        digitalSignatureSvg: String? = null
    ) {
        appDao.insertExitLog(
            ExitLogEntity(
                studentId = studentId,
                gateId = gateId,
                commuteMode = commuteMode,
                overrideReason = overrideReason,
                digitalSignatureSvg = digitalSignatureSvg
            )
        )
    }
}
