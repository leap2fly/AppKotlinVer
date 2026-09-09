package com.example.ccagatedispatch.ui.dispatch

import androidx.lifecycle.viewModelScope
import com.example.ccagatedispatch.data.local.entity.StudentEntity
import com.example.ccagatedispatch.data.local.entity.VanEntity
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

enum class DispatchTab {
    SELF_COMMUTE,
    PARENT_PICKUP,
    VAN_HANDOVER
}

data class DispatchState(
    val activeTab: DispatchTab = DispatchTab.SELF_COMMUTE,
    val searchInput: String = "",
    val activeGateId: String = "GATE_MAIN_01",
    val currentDateKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),

    // Self Commute State
    val selfStudent: StudentEntity? = null,
    val selfClassCompleted: Boolean = false,
    val selfOverrideReason: String = "",
    val showSelfOverrideDialog: Boolean = false,

    // Parent Pickup State
    val parentGroupStudents: List<StudentEntity> = emptyList(),
    val parentClassCompletionMap: Map<String, Boolean> = emptyMap(), // ccaId -> isCompleted
    val parentOverrideReason: String = "",
    val showParentOverrideDialogForStudent: StudentEntity? = null,

    // Van Handover State
    val activeVan: VanEntity? = null,
    val vanRoster: List<StudentEntity> = emptyList(),
    val vanCheckedStudents: Set<String> = emptySet(),
    val digitalSignatureSvg: String? = null,

    // Status Message & Feedback
    val message: String? = null,
    val isError: Boolean = false
) : UiState

sealed interface DispatchEvent : UiEvent {
    data class SelectTab(val tab: DispatchTab) : DispatchEvent
    data class UpdateSearchInput(val input: String) : DispatchEvent
    data class ExecuteSearch(val scanInput: String) : DispatchEvent

    // Self Commute
    data object ReleaseSelfStudent : DispatchEvent
    data class SubmitSelfOverride(val reason: String) : DispatchEvent

    // Parent Pickup
    data class ReleaseParentStudent(val student: StudentEntity) : DispatchEvent
    data class SubmitParentOverride(val student: StudentEntity, val reason: String) : DispatchEvent

    // Van Handover
    data class ToggleVanStudentCheck(val studentId: String) : DispatchEvent
    data class SaveDigitalSignature(val svgData: String) : DispatchEvent
    data object CompleteVanHandover : DispatchEvent

    data object ClearMessage : DispatchEvent
}

sealed interface DispatchEffect : UiEffect {
    data class ShowToast(val message: String) : DispatchEffect
}

@HiltViewModel
class DispatchViewModel @Inject constructor(
    private val repository: AppRepository
) : BaseViewModel<DispatchState, DispatchEvent, DispatchEffect>(DispatchState()) {

    override fun handleEvent(event: DispatchEvent) {
        when (event) {
            is DispatchEvent.SelectTab -> updateState { copy(activeTab = event.tab, searchInput = "", message = null) }
            is DispatchEvent.UpdateSearchInput -> updateState { copy(searchInput = event.input) }
            is DispatchEvent.ExecuteSearch -> performSearch(event.scanInput)

            // Self Commute
            is DispatchEvent.ReleaseSelfStudent -> handleSelfRelease()
            is DispatchEvent.SubmitSelfOverride -> handleSelfOverride(event.reason)

            // Parent Pickup
            is DispatchEvent.ReleaseParentStudent -> handleParentRelease(event.student)
            is DispatchEvent.SubmitParentOverride -> handleParentOverride(event.student, event.reason)

            // Van Handover
            is DispatchEvent.ToggleVanStudentCheck -> toggleVanCheck(event.studentId)
            is DispatchEvent.SaveDigitalSignature -> updateState { copy(digitalSignatureSvg = event.svgData) }
            is DispatchEvent.CompleteVanHandover -> handleVanHandoverCompletion()

            is DispatchEvent.ClearMessage -> updateState { copy(message = null, isError = false) }
        }
    }

    private fun performSearch(rawInput: String) {
        val input = rawInput.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            when (currentState.activeTab) {
                DispatchTab.SELF_COMMUTE -> {
                    val student = repository.getStudentById(input)
                    if (student != null) {
                        val isClassCompleted = repository.isClassSessionCompleted("CCA_BASKETBALL", currentState.currentDateKey) // Check general/assigned CCA
                        updateState {
                            copy(
                                selfStudent = student,
                                selfClassCompleted = isClassCompleted,
                                message = if (!student.selfAuth) "WARNING: Student lacks Pre-Auth signature!" else null,
                                isError = !student.selfAuth
                            )
                        }
                    } else {
                        updateState { copy(selfStudent = null, message = "Student not found for ID: $input", isError = true) }
                    }
                }
                DispatchTab.PARENT_PICKUP -> {
                    val students = repository.getStudentsByParentId(input)
                    if (students.isNotEmpty()) {
                        updateState {
                            copy(
                                parentGroupStudents = students,
                                message = "Found ${students.size} sibling(s) for Parent ID: $input",
                                isError = false
                            )
                        }
                    } else {
                        updateState { copy(parentGroupStudents = emptyList(), message = "No students associated with Parent ID: $input", isError = true) }
                    }
                }
                DispatchTab.VAN_HANDOVER -> {
                    val van = repository.getVanByDriverOrVanId(input)
                    if (van != null) {
                        val roster = repository.getStudentsByVanId(van.vanId)
                        updateState {
                            copy(
                                activeVan = van,
                                vanRoster = roster,
                                vanCheckedStudents = emptySet(),
                                digitalSignatureSvg = null,
                                message = "Van Route '${van.route}' loaded. ${roster.size} student(s) scheduled.",
                                isError = false
                            )
                        }
                    } else {
                        updateState { copy(activeVan = null, vanRoster = emptyList(), message = "No van route found for Driver / Van ID: $input", isError = true) }
                    }
                }
            }
        }
    }

    private fun handleSelfRelease() {
        val student = currentState.selfStudent ?: return
        if (!student.selfAuth) {
            updateState { copy(showSelfOverrideDialog = true) }
            return
        }

        viewModelScope.launch {
            repository.logExitGate(
                studentId = student.studentId,
                gateId = currentState.activeGateId,
                commuteMode = "Self"
            )
            updateState {
                copy(
                    selfStudent = null,
                    message = "DISPATCH SUCCESS: ${student.name} dispatched via Self Commute.",
                    isError = false
                )
            }
            sendEffect(DispatchEffect.ShowToast("${student.name} Dispatched"))
        }
    }

    private fun handleSelfOverride(reason: String) {
        val student = currentState.selfStudent ?: return
        viewModelScope.launch {
            repository.logExitGate(
                studentId = student.studentId,
                gateId = currentState.activeGateId,
                commuteMode = "Self",
                overrideReason = "OVERRIDE: $reason"
            )
            updateState {
                copy(
                    selfStudent = null,
                    showSelfOverrideDialog = false,
                    message = "OVERRIDE DISPATCH: ${student.name} released under Guard Override.",
                    isError = false
                )
            }
            sendEffect(DispatchEffect.ShowToast("Guard Override Dispatched"))
        }
    }

    private fun handleParentRelease(student: StudentEntity) {
        viewModelScope.launch {
            repository.logExitGate(
                studentId = student.studentId,
                gateId = currentState.activeGateId,
                commuteMode = "Parent"
            )
            val updatedList = currentState.parentGroupStudents.filterNot { it.studentId == student.studentId }
            updateState {
                copy(
                    parentGroupStudents = updatedList,
                    message = "${student.name} released to Parent (${student.parentName}).",
                    isError = false
                )
            }
            sendEffect(DispatchEffect.ShowToast("${student.name} Released"))
        }
    }

    private fun handleParentOverride(student: StudentEntity, reason: String) {
        viewModelScope.launch {
            repository.logExitGate(
                studentId = student.studentId,
                gateId = currentState.activeGateId,
                commuteMode = "Parent",
                overrideReason = "OVERRIDE: $reason"
            )
            val updatedList = currentState.parentGroupStudents.filterNot { it.studentId == student.studentId }
            updateState {
                copy(
                    parentGroupStudents = updatedList,
                    showParentOverrideDialogForStudent = null,
                    message = "${student.name} released under Guard Override ($reason).",
                    isError = false
                )
            }
        }
    }

    private fun toggleVanCheck(studentId: String) {
        val currentSet = currentState.vanCheckedStudents.toMutableSet()
        if (currentSet.contains(studentId)) {
            currentSet.remove(studentId)
        } else {
            currentSet.add(studentId)
        }
        updateState { copy(vanCheckedStudents = currentSet) }
    }

    private fun handleVanHandoverCompletion() {
        val van = currentState.activeVan ?: return
        val signature = currentState.digitalSignatureSvg

        if (signature == null) {
            updateState { copy(message = "Driver signature is mandatory before dispatching van batch!", isError = true) }
            return
        }

        viewModelScope.launch {
            currentState.vanCheckedStudents.forEach { studentId ->
                repository.logExitGate(
                    studentId = studentId,
                    gateId = currentState.activeGateId,
                    commuteMode = "Van",
                    digitalSignatureSvg = signature
                )
            }
            val count = currentState.vanCheckedStudents.size
            updateState {
                copy(
                    activeVan = null,
                    vanRoster = emptyList(),
                    vanCheckedStudents = emptySet(),
                    digitalSignatureSvg = null,
                    message = "VAN DISPATCH COMPLETE: $count student(s) dispatched with Driver ${van.driverName} (${van.route}).",
                    isError = false
                )
            }
            sendEffect(DispatchEffect.ShowToast("Van Handover Completed"))
        }
    }
}
