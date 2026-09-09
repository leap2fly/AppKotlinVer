package com.example.ccagatedispatch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ccagatedispatch.data.local.dao.AppDao
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

@Database(
    entities = [
        AcademicYearEntity::class,
        CoachEntity::class,
        CcaEntity::class,
        VanEntity::class,
        StudentEntity::class,
        CcaCoachCrossRef::class,
        StudentCcaEnrollmentCrossRef::class,
        EntryLogEntity::class,
        CoachAttendanceEntity::class,
        ClassSessionEntity::class,
        StudentAttendanceEntity::class,
        ExitLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
