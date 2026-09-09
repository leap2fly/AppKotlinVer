package com.example.ccagatedispatch.ui.coach

import androidx.lifecycle.viewModelScope
import com.example.ccagatedispatch.data.local.entity.CcaEntity
import com.example.ccagatedispatch.data.local.entity.CoachEntity
import com.example.ccagatedispatch.data.local.entity.StudentEntity
import com.example.ccagatedispatch.data.repository.AppRepository
import com.example.ccagatedispatch.ui.base.BaseViewModel
import com.example.ccagatedispatch.ui.base.UiEffect
import com.example.ccagatedispatch.ui.base.UiEvent
import com.example.ccagatedispatch.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CoachState(
    val coaches: List<CoachEntity> = emptyList(),
    val ccas: List<CcaEntity> = emptyList(),
    val selectedCoach: CoachEntity? = null,
    val selectedCca: CcaEntity? = null,
    val enrolledStudents: List<StudentEntity> = emptyList(),
    val studentAttendanceMap: Map<String, String> = emptyMap(), // studentId -> status (Present/Absent/Excused)
    val isCoachCheckedIn: Boolean = false,
    val isClassCompleted: Boolean = false,
    val message: String? = null,
    val currentDateKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
) : UiState

sealed interface CoachEvent : UiEvent {
    data class SelectCoach(val coach: CoachEntity) : CoachEvent
    data class SelectCca(val cca: CcaEntity) : CoachEvent
    data object CheckInCoach : CoachEvent
    data class ToggleStudentAttendance(val studentId: String, val status: String) : CoachEvent
    data object CompleteClassSession : CoachEvent
    data object ClearMessage : CoachEvent
}

sealed interface CoachEffect : UiEffect {
    data class ShowNotification(val text: String) : CoachEffect
}

@HiltViewModel
class CoachViewModel @Inject constructor(
    private val repository: AppRepository
) : BaseViewModel<CoachState, CoachEvent, CoachEffect>(CoachState()) {

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val coaches = repository.getAllCoaches()
            val ccas = repository.getAllCcas()
            val initialCoach = coaches.firstOrNull()
            val initialCca = ccas.firstOrNull()

            updateState {
                copy(
                    coaches = coaches,
                    ccas = ccas,
                    selectedCoach = initialCoach,
                    selectedCca = initialCca
                )
            }

            if (initialCca != null) {
                loadRosterAndAttendance(initialCca.ccaId, currentState.currentDateKey)
            }
        }
    }

    override fun handleEvent(event: CoachEvent) {
        when (event) {
            is CoachEvent.SelectCoach -> updateState { copy(selectedCoach = event.coach, isCoachCheckedIn = false) }
            is CoachEvent.SelectCca -> {
                updateState { copy(selectedCca = event.cca, isCoachCheckedIn = false) }
                loadRosterAndAttendance(event.cca.ccaId, currentState.currentDateKey)
            }
            is CoachEvent.CheckInCoach -> handleCoachCheckIn()
            is CoachEvent.ToggleStudentAttendance -> handleAttendanceToggle(event.studentId, event.status)
            is CoachEvent.CompleteClassSession -> handleClassCompletion()
            is CoachEvent.ClearMessage -> updateState { copy(message = null) }
        }
    }

    private fun loadRosterAndAttendance(ccaId: String, dateKey: String) {
        viewModelScope.launch {
            val students = repository.getEnrolledStudents(ccaId)
            val attendance = repository.getStudentAttendanceForDate(ccaId, dateKey)
            val isCompleted = repository.isClassSessionCompleted(ccaId, dateKey)

            updateState {
                copy(
                    enrolledStudents = students,
                    studentAttendanceMap = attendance,
                    isClassCompleted = isCompleted
                )
            }
        }
    }

    private fun handleCoachCheckIn() {
        val coach = currentState.selectedCoach ?: return
        val cca = currentState.selectedCca ?: return

        viewModelScope.launch {
            repository.logCoachAttendance(coach.coachId, cca.ccaId)
            updateState {
                copy(
                    isCoachCheckedIn = true,
                    message = "${coach.name} checked in for ${cca.name}."
                )
            }
            sendEffect(CoachEffect.ShowNotification("${coach.name} Checked In"))
        }
    }

    private fun handleAttendanceToggle(studentId: String, status: String) {
        val cca = currentState.selectedCca ?: return
        val dateKey = currentState.currentDateKey

        viewModelScope.launch {
            repository.updateStudentAttendance(studentId, cca.ccaId, dateKey, status)
            val updatedMap = currentState.studentAttendanceMap.toMutableMap().apply {
                put(studentId, status)
            }
            updateState { copy(studentAttendanceMap = updatedMap) }
        }
    }

    private fun handleClassCompletion() {
        val coach = currentState.selectedCoach
        val cca = currentState.selectedCca
        if (coach == null || cca == null) return

        viewModelScope.launch {
            repository.completeClassSession(cca.ccaId, currentState.currentDateKey, coach.coachId)
            updateState {
                copy(
                    isClassCompleted = true,
                    message = "Class Session for '${cca.name}' completed! Gate departure authorization window is now OPEN."
                )
            }
            sendEffect(CoachEffect.ShowNotification("Class Completed! Exit Gate Opened."))
        }
    }
}
