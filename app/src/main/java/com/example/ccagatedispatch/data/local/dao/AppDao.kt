package com.example.ccagatedispatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ccagatedispatch.data.local.entity.AcademicYearEntity
import com.example.ccagatedispatch.data.local.entity.CcaCoachCrossRef
import com.example.ccagatedispatch.data.local.entity.CcaEntity
import com.example.ccagatedispatch.data.local.entity.ClassSessionEntity
import com.example.ccagatedispatch.data.local.entity.CoachAttendanceEntity
import com.example.ccagatedispatch.data.local.entity.CoachEntity
import com.example.ccagatedispatch.data.local.entity.EntryLogEntity
import com.example.ccagatedispatch.data.local.entity.ExitLogEntity
import com.example.ccagatedispatch.data.local.entity.StudentAttendanceEntity
import com.example.ccagatedispatch.data.local.entity.StudentCcaEnrollmentCrossRef
import com.example.ccagatedispatch.data.local.entity.StudentEntity
import com.example.ccagatedispatch.data.local.entity.VanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Count Queries for Audit Dashboard
    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM entry_logs")
    fun getEntryLogsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM exit_logs")
    fun getExitLogsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM class_sessions")
    fun getClassSessionsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int

    // Seeding & Insert Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYear(year: AcademicYearEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoaches(coaches: List<CoachEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCcas(ccas: List<CcaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVans(vans: List<VanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCcaCoachCrossRefs(crossRefs: List<CcaCoachCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentCcaEnrollments(crossRefs: List<StudentCcaEnrollmentCrossRef>)

    // Entrance Logging Queries
    @Query("SELECT * FROM students WHERE student_id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntryLog(entryLog: EntryLogEntity)

    @Query("SELECT * FROM entry_logs ORDER BY timestamp DESC")
    fun getAllEntryLogsFlow(): Flow<List<EntryLogEntity>>

    // Coach Portal Queries
    @Query("SELECT * FROM coaches")
    suspend fun getAllCoaches(): List<CoachEntity>

    @Query("SELECT * FROM ccas")
    suspend fun getAllCcas(): List<CcaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoachAttendance(attendance: CoachAttendanceEntity)

    @Query("SELECT students.* FROM students INNER JOIN stu_cca_enroll ON students.student_id = stu_cca_enroll.student_id WHERE stu_cca_enroll.cca_id = :ccaId")
    suspend fun getStudentsEnrolledInCca(ccaId: String): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStudentAttendance(attendance: StudentAttendanceEntity)

    @Query("SELECT * FROM student_attendance WHERE cca_id = :ccaId AND date_key = :dateKey")
    suspend fun getStudentAttendanceForDate(ccaId: String, dateKey: String): List<StudentAttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassSession(session: ClassSessionEntity)

    @Query("SELECT * FROM class_sessions WHERE cca_id = :ccaId AND date = :dateKey LIMIT 1")
    suspend fun getClassSession(ccaId: String, dateKey: String): ClassSessionEntity?

    @Query("SELECT is_completed FROM class_sessions WHERE cca_id = :ccaId AND date = :dateKey LIMIT 1")
    suspend fun isClassSessionCompleted(ccaId: String, dateKey: String): Boolean?

    // Gate Dispatch Queries
    @Query("SELECT * FROM students WHERE parent_id = :parentId")
    suspend fun getStudentsByParentId(parentId: String): List<StudentEntity>

    @Query("SELECT * FROM vans WHERE drv_id = :driverId OR van_id = :driverId LIMIT 1")
    suspend fun getVanByDriverOrVanId(driverId: String): VanEntity?

    @Query("SELECT * FROM students WHERE van_id = :vanId")
    suspend fun getStudentsByVanId(vanId: String): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExitLog(exitLog: ExitLogEntity)

    @Query("SELECT * FROM exit_logs ORDER BY timestamp DESC")
    fun getAllExitLogsFlow(): Flow<List<ExitLogEntity>>
}
