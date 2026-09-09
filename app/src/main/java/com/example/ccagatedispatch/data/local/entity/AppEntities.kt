package com.example.ccagatedispatch.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "academic_years")
data class AcademicYearEntity(
    @PrimaryKey
    @ColumnInfo(name = "year")
    val year: String
)

@Entity(
    tableName = "coaches",
    foreignKeys = [
        ForeignKey(
            entity = AcademicYearEntity::class,
            parentColumns = ["year"],
            childColumns = ["ac_year"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ac_year"])]
)
data class CoachEntity(
    @PrimaryKey
    @ColumnInfo(name = "coach_id")
    val coachId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "ac_year")
    val acYear: String
)

@Entity(
    tableName = "ccas",
    foreignKeys = [
        ForeignKey(
            entity = AcademicYearEntity::class,
            parentColumns = ["year"],
            childColumns = ["ac_year"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ac_year"])]
)
data class CcaEntity(
    @PrimaryKey
    @ColumnInfo(name = "cca_id")
    val ccaId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "schedule")
    val schedule: String,
    @ColumnInfo(name = "ac_year")
    val acYear: String
)

@Entity(
    tableName = "vans",
    foreignKeys = [
        ForeignKey(
            entity = AcademicYearEntity::class,
            parentColumns = ["year"],
            childColumns = ["ac_year"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["drv_id"], unique = true), Index(value = ["ac_year"])]
)
data class VanEntity(
    @PrimaryKey
    @ColumnInfo(name = "van_id")
    val vanId: String,
    @ColumnInfo(name = "drv_name")
    val driverName: String,
    @ColumnInfo(name = "drv_id")
    val driverId: String,
    @ColumnInfo(name = "route")
    val route: String,
    @ColumnInfo(name = "ac_year")
    val acYear: String
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = VanEntity::class,
            parentColumns = ["van_id"],
            childColumns = ["van_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AcademicYearEntity::class,
            parentColumns = ["year"],
            childColumns = ["ac_year"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["van_id"]), Index(value = ["parent_id"]), Index(value = ["ac_year"])]
)
data class StudentEntity(
    @PrimaryKey
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "std")
    val std: String,
    @ColumnInfo(name = "division")
    val division: String,
    @ColumnInfo(name = "commute_mode")
    val commuteMode: String, // Self, Parent, Van
    @ColumnInfo(name = "parent_id")
    val parentId: String?,
    @ColumnInfo(name = "parent_name")
    val parentName: String?,
    @ColumnInfo(name = "van_id")
    val vanId: String?,
    @ColumnInfo(name = "self_auth")
    val selfAuth: Boolean, // Pre-Auth signed for self commute
    @ColumnInfo(name = "ac_year")
    val acYear: String
)

@Entity(
    tableName = "cca_coaches",
    primaryKeys = ["cca_id", "coach_id"],
    foreignKeys = [
        ForeignKey(
            entity = CcaEntity::class,
            parentColumns = ["cca_id"],
            childColumns = ["cca_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CoachEntity::class,
            parentColumns = ["coach_id"],
            childColumns = ["coach_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["coach_id"])]
)
data class CcaCoachCrossRef(
    @ColumnInfo(name = "cca_id")
    val ccaId: String,
    @ColumnInfo(name = "coach_id")
    val coachId: String
)

@Entity(
    tableName = "stu_cca_enroll",
    primaryKeys = ["student_id", "cca_id"],
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["student_id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CcaEntity::class,
            parentColumns = ["cca_id"],
            childColumns = ["cca_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cca_id"])]
)
data class StudentCcaEnrollmentCrossRef(
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "cca_id")
    val ccaId: String
)

@Entity(
    tableName = "entry_logs",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["student_id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["student_id"])]
)
data class EntryLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "status")
    val status: String = "Entered"
)

@Entity(
    tableName = "coach_attendance",
    foreignKeys = [
        ForeignKey(
            entity = CoachEntity::class,
            parentColumns = ["coach_id"],
            childColumns = ["coach_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CcaEntity::class,
            parentColumns = ["cca_id"],
            childColumns = ["cca_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["coach_id"]), Index(value = ["cca_id"])]
)
data class CoachAttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "coach_id")
    val coachId: String,
    @ColumnInfo(name = "cca_id")
    val ccaId: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "status")
    val status: String = "Present"
)

@Entity(
    tableName = "class_sessions",
    foreignKeys = [
        ForeignKey(
            entity = CcaEntity::class,
            parentColumns = ["cca_id"],
            childColumns = ["cca_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CoachEntity::class,
            parentColumns = ["coach_id"],
            childColumns = ["completed_by_coach_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["date"], unique = true), Index(value = ["cca_id"]), Index(value = ["completed_by_coach_id"])]
)
data class ClassSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "cca_id")
    val ccaId: String,
    @ColumnInfo(name = "date")
    val dateKey: String, // YYYY-MM-DD
    @ColumnInfo(name = "completed_by_coach_id")
    val completedByCoachId: String?,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "student_attendance",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["student_id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CcaEntity::class,
            parentColumns = ["cca_id"],
            childColumns = ["cca_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["student_id"]), Index(value = ["cca_id"]), Index(value = ["date_key"])]
)
data class StudentAttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "cca_id")
    val ccaId: String,
    @ColumnInfo(name = "date_key")
    val dateKey: String,
    @ColumnInfo(name = "status")
    val status: String // Present, Absent, Excused
)

@Entity(
    tableName = "exit_logs",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["student_id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["student_id"])]
)
data class ExitLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "gate_id")
    val gateId: String,
    @ColumnInfo(name = "commute_mode")
    val commuteMode: String,
    @ColumnInfo(name = "override_reason")
    val overrideReason: String? = null,
    @ColumnInfo(name = "digital_signature_svg")
    val digitalSignatureSvg: String? = null,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
