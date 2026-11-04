package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CinematicSynth
import com.example.data.CricketRepository
import com.example.engine.CricketEngine
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ActiveModal {
    object None : ActiveModal()
    data class PlayerForm(val player: Player?) : ActiveModal()
    data class PlayerProfile(val playerId: String) : ActiveModal()
    data class MatchDetail(val matchId: String) : ActiveModal()
    data class MatchWizard(val editMatchId: String?) : ActiveModal()
    data class GroupManage(val editGroupId: String? = null) : ActiveModal()
    object AdminPin : ActiveModal()
    object BackupRestore : ActiveModal()
    object ShareDialog : ActiveModal()
    object RatingInfo : ActiveModal()
}

data class WizardDraft(
    val editMatchId: String? = null,
    val step: Int = 1,
    val title: String = "",
    val format: String = "T20",
    val date: String = "",
    val selectedPlayerIds: List<String> = emptyList(),
    val currentPlayerIndex: Int = 0,
    // Map of Player ID -> Player Stats Input Form
    val entries: Map<String, WizardPlayerForm> = emptyMap()
)

data class WizardPlayerForm(
    val batted: Boolean = false,
    val runs: String = "",
    val balls: String = "",
    val fours: String = "",
    val sixes: String = "",
    val notOut: Boolean = false,
    val overs: String = "",
    val conceded: String = "",
    val wickets: String = "",
    val maidens: String = "",
    val catches: String = "",
    val runouts: String = "",
    val stumpings: String = "",
    val fair: Int = 0 // -5 to +5
)

class CricketViewModel(application: Application) : AndroidViewModel(application) {
    val repository = CricketRepository(application)

    val dbFlow = repository.dbFlow
    val adminUnlocked = repository.adminUnlocked

    private val _selectedTab = MutableStateFlow("rankings") // rankings, players, matches, compare, awards
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _selectedFormat = MutableStateFlow("T20") // T20, ODI, TEST, ALL
    val selectedFormat: StateFlow<String> = _selectedFormat.asStateFlow()

    private val _selectedCategory = MutableStateFlow("overall") // overall, batting, bowling, allround, fair
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _rankSearch = MutableStateFlow("")
    val rankSearch: StateFlow<String> = _rankSearch.asStateFlow()

    private val _playerSearch = MutableStateFlow("")
    val playerSearch: StateFlow<String> = _playerSearch.asStateFlow()

    private val _activeModal = MutableStateFlow<ActiveModal>(ActiveModal.None)
    val activeModal: StateFlow<ActiveModal> = _activeModal.asStateFlow()

    // H2H Compare
    private val _cmpPlayerAId = MutableStateFlow<String?>(null)
    val cmpPlayerAId: StateFlow<String?> = _cmpPlayerAId.asStateFlow()

    private val _cmpPlayerBId = MutableStateFlow<String?>(null)
    val cmpPlayerBId: StateFlow<String?> = _cmpPlayerBId.asStateFlow()

    // Awards offset
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    private val _monthOffset = MutableStateFlow(0)
    val monthOffset: StateFlow<Int> = _monthOffset.asStateFlow()

    // Wizard state
    private val _wizardDraft = MutableStateFlow<WizardDraft?>(null)
    val wizardDraft: StateFlow<WizardDraft?> = _wizardDraft.asStateFlow()

    // Intro screen
    private val _showIntro = MutableStateFlow(true)
    val showIntro: StateFlow<Boolean> = _showIntro.asStateFlow()

    // Toast message trigger
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun dismissIntro() {
        _showIntro.value = false
    }

    fun playStartupSound() {
        viewModelScope.launch {
            CinematicSynth.playStartupSound()
        }
    }

    fun setTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setFormat(format: String) {
        _selectedFormat.value = format
    }

    fun setCategory(cat: String) {
        _selectedCategory.value = cat
    }

    fun setRankSearch(query: String) {
        _rankSearch.value = query
    }

    fun setPlayerSearch(query: String) {
        _playerSearch.value = query
    }

    fun openModal(modal: ActiveModal) {
        _activeModal.value = modal
    }

    fun closeModal() {
        _activeModal.value = ActiveModal.None
        _wizardDraft.value = null
    }

    fun setComparePlayers(idA: String?, idB: String?) {
        _cmpPlayerAId.value = idA
        _cmpPlayerBId.value = idB
    }

    fun shiftAwardWeek(delta: Int) {
        _weekOffset.value = minOf(0, _weekOffset.value + delta)
    }

    fun shiftAwardMonth(delta: Int) {
        _monthOffset.value = minOf(0, _monthOffset.value + delta)
    }

    fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }

    // Wizard actions
    fun startMatchWizard(editMatchId: String?) {
        val group = repository.getActiveGroup() ?: return
        val existing = editMatchId?.let { id -> group.matches.find { it.id == id } }

        val format = existing?.format ?: if (_selectedFormat.value == "ALL") "T20" else _selectedFormat.value
        val date = existing?.date ?: CricketRepository.todayStr()
        val title = existing?.title ?: "$format Match – ${formatDate(date)}"
        val selectedIds = existing?.entries?.map { it.pid } ?: emptyList()

        val entriesMap = mutableMapOf<String, WizardPlayerForm>()
        existing?.entries?.forEach { e ->
            entriesMap[e.pid] = WizardPlayerForm(
                batted = e.bat.batted,
                runs = if (e.bat.runs > 0 || e.bat.batted) e.bat.runs.toString() else "",
                balls = if (e.bat.balls > 0 || e.bat.batted) e.bat.balls.toString() else "",
                fours = if (e.bat.fours > 0) e.bat.fours.toString() else "",
                sixes = if (e.bat.sixes > 0) e.bat.sixes.toString() else "",
                notOut = e.bat.notOut,
                overs = if (e.bowl.legal > 0) CricketEngine.oversStr(e.bowl.legal) else "",
                conceded = if (e.bowl.legal > 0) e.bowl.conceded.toString() else "",
                wickets = if (e.bowl.legal > 0) e.bowl.wickets.toString() else "",
                maidens = if (e.bowl.maidens > 0) e.bowl.maidens.toString() else "",
                catches = if (e.field.catches > 0) e.field.catches.toString() else "",
                runouts = if (e.field.runouts > 0) e.field.runouts.toString() else "",
                stumpings = if (e.field.stumpings > 0) e.field.stumpings.toString() else "",
                fair = e.fair
            )
        }

        _wizardDraft.value = WizardDraft(
            editMatchId = editMatchId,
            step = 1,
            title = title,
            format = format,
            date = date,
            selectedPlayerIds = selectedIds,
            currentPlayerIndex = 0,
            entries = entriesMap
        )
        _activeModal.value = ActiveModal.MatchWizard(editMatchId)
    }

    fun updateWizardMeta(title: String, format: String, date: String) {
        _wizardDraft.value = _wizardDraft.value?.copy(
            title = title,
            format = format,
            date = date
        )
    }

    fun toggleWizardPlayer(playerId: String) {
        val draft = _wizardDraft.value ?: return
        val current = draft.selectedPlayerIds.toMutableList()
        if (current.contains(playerId)) {
            current.remove(playerId)
        } else {
            current.add(playerId)
        }
        _wizardDraft.value = draft.copy(selectedPlayerIds = current)
    }

    fun selectAllWizardPlayers(allIds: List<String>) {
        val draft = _wizardDraft.value ?: return
        _wizardDraft.value = draft.copy(selectedPlayerIds = allIds)
    }

    fun clearWizardPlayers() {
        val draft = _wizardDraft.value ?: return
        _wizardDraft.value = draft.copy(selectedPlayerIds = emptyList())
    }

    fun wizardGoToStep2() {
        val draft = _wizardDraft.value ?: return
        if (draft.selectedPlayerIds.isEmpty()) {
            emitToast("Select at least one player")
            return
        }
        if (draft.title.isBlank()) {
            emitToast("Please enter match title")
            return
        }
        _wizardDraft.value = draft.copy(step = 2, currentPlayerIndex = 0)
    }

    fun wizardSetCurrentPlayerIndex(index: Int) {
        val draft = _wizardDraft.value ?: return
        if (index in draft.selectedPlayerIds.indices) {
            _wizardDraft.value = draft.copy(currentPlayerIndex = index)
        }
    }

    fun updateWizardPlayerEntry(playerId: String, form: WizardPlayerForm) {
        val draft = _wizardDraft.value ?: return
        val map = draft.entries.toMutableMap()
        map[playerId] = form
        _wizardDraft.value = draft.copy(entries = map)
    }

    fun wizardGoToStep3() {
        _wizardDraft.value = _wizardDraft.value?.copy(step = 3)
    }

    fun wizardGoBack() {
        val draft = _wizardDraft.value ?: return
        when (draft.step) {
            3 -> _wizardDraft.value = draft.copy(step = 2, currentPlayerIndex = draft.selectedPlayerIds.size - 1)
            2 -> {
                if (draft.currentPlayerIndex > 0) {
                    _wizardDraft.value = draft.copy(currentPlayerIndex = draft.currentPlayerIndex - 1)
                } else {
                    _wizardDraft.value = draft.copy(step = 1)
                }
            }
        }
    }

    fun wizardCommit() {
        val draft = _wizardDraft.value ?: return
        val activeGroup = repository.getActiveGroup() ?: return

        // Build MatchPlayerEntries
        val matchEntries = mutableListOf<MatchPlayerEntry>()
        for (pid in draft.selectedPlayerIds) {
            val f = draft.entries[pid] ?: WizardPlayerForm()
            val runs = f.runs.toIntOrNull() ?: 0
            val balls = f.balls.toIntOrNull() ?: 0
            val fours = f.fours.toIntOrNull() ?: 0
            val sixes = f.sixes.toIntOrNull() ?: 0
            val legalBalls = CricketEngine.parseOvers(f.overs) ?: 0
            val conceded = f.conceded.toIntOrNull() ?: 0
            val wickets = (f.wickets.toIntOrNull() ?: 0).coerceIn(0, 10)
            val maidens = f.maidens.toIntOrNull() ?: 0
            val catches = f.catches.toIntOrNull() ?: 0
            val runouts = f.runouts.toIntOrNull() ?: 0
            val stumpings = f.stumpings.toIntOrNull() ?: 0

            matchEntries.add(
                MatchPlayerEntry(
                    pid = pid,
                    bat = BattingEntry(
                        batted = f.batted,
                        runs = runs,
                        balls = balls,
                        fours = fours,
                        sixes = sixes,
                        notOut = f.batted && f.notOut
                    ),
                    bowl = BowlingEntry(
                        legal = legalBalls,
                        conceded = conceded,
                        wickets = wickets,
                        maidens = maidens
                    ),
                    field = FieldingEntry(
                        catches = catches,
                        runouts = runouts,
                        stumpings = stumpings
                    ),
                    fair = f.fair.coerceIn(-5, 5)
                )
            )
        }

        val isEdit = draft.editMatchId != null
        val match = CricketMatch(
            id = draft.editMatchId ?: java.util.UUID.randomUUID().toString(),
            title = draft.title.trim(),
            format = draft.format,
            date = draft.date,
            createdAt = CricketRepository.todayStr(),
            entries = matchEntries
        )

        viewModelScope.launch {
            repository.saveMatch(match, isEdit)
            _selectedFormat.value = match.format
            _selectedTab.value = "rankings"
            closeModal()
            emitToast(if (isEdit) "Match updated — rankings recalculating..." else "Match recorded — rankings updated!")
        }
    }

    private fun formatDate(d: String): String {
        return try {
            val inSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outSdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            val date = inSdf.parse(d)
            if (date != null) outSdf.format(date) else d
        } catch (_: Exception) {
            d
        }
    }
}
