package com.example.ccagatedispatch.ui.entry

import androidx.lifecycle.viewModelScope
import com.example.ccagatedispatch.data.local.entity.EntryLogEntity
import com.example.ccagatedispatch.data.local.entity.StudentEntity
import com.example.ccagatedispatch.data.repository.AppRepository
import com.example.ccagatedispatch.data.seed.SeedService
import com.example.ccagatedispatch.ui.base.BaseViewModel
import com.example.ccagatedispatch.ui.base.UiEffect
import com.example.ccagatedispatch.ui.base.UiEvent
import com.example.ccagatedispatch.ui.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EntryState(
    val scannedStudentId: String = "",
    val matchedStudent: StudentEntity? = null,
    val isScanning: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
) : UiState

sealed interface EntryEvent : UiEvent {
    data class ScanStudentCard(val cardInput: String) : EntryEvent
    data object ClearMessage : EntryEvent
    data object ClearScan : EntryEvent
}

sealed interface EntryEffect : UiEffect {
    data class ShowToast(val text: String) : EntryEffect
}

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: AppRepository,
    private val seedService: SeedService
) : BaseViewModel<EntryState, EntryEvent, EntryEffect>(EntryState()) {

    val recentEntryLogs: StateFlow<List<EntryLogEntity>> = repository.allEntryLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val studentCount = repository.studentCountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val entryCount = repository.entryLogsCountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val exitCount = repository.exitLogsCountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val sessionCount = repository.classSessionsCountFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            seedService.seedDefaultDataIfEmpty()
        }
    }

    override fun handleEvent(event: EntryEvent) {
        when (event) {
            is EntryEvent.ScanStudentCard -> processStudentScan(event.cardInput)
            is EntryEvent.ClearMessage -> updateState { copy(message = null, isError = false) }
            is EntryEvent.ClearScan -> updateState { copy(scannedStudentId = "", matchedStudent = null, message = null) }
        }
    }

    private fun processStudentScan(cardInput: String) {
        val cleanId = cardInput.trim()
        if (cleanId.isBlank()) return

        updateState { copy(isScanning = true, scannedStudentId = cleanId) }

        viewModelScope.launch {
            val student = repository.getStudentById(cleanId)
            if (student != null) {
                repository.logStudentEntry(cleanId)
                updateState {
                    copy(
                        isScanning = false,
                        matchedStudent = student,
                        message = "ACCESS GRANTED: ${student.name} (${student.std} - ${student.division}) logged into school.",
                        isError = false
                    )
                }
                sendEffect(EntryEffect.ShowToast("Entry Logged for ${student.name}"))
            } else {
                updateState {
                    copy(
                        isScanning = false,
                        matchedStudent = null,
                        message = "ACCESS DENIED: Invalid or Unknown Student Card ID '$cleanId'",
                        isError = true
                    )
                }
                sendEffect(EntryEffect.ShowToast("Unknown Student Card ID"))
            }
        }
    }
}
