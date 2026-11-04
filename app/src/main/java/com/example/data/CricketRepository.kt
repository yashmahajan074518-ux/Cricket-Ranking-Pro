package com.example.data

import android.content.Context
import com.example.engine.CricketEngine
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CricketRepository(private val context: Context) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val dbAdapter = moshi.adapter(AppDatabase::class.java)

    private val dbFile = File(context.filesDir, "cricket_ranking_pro_db.json")

    private val _dbFlow = MutableStateFlow(loadInitial())
    val dbFlow: StateFlow<AppDatabase> = _dbFlow.asStateFlow()

    private val _adminUnlocked = MutableStateFlow(false)
    val adminUnlocked: StateFlow<Boolean> = _adminUnlocked.asStateFlow()

    companion object {
        const val MAX_PLAYERS_PER_GROUP = 51

        fun hashPin(pin: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest("crp_salt_$pin".toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun todayStr(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
    }

    private fun loadInitial(): AppDatabase {
        return try {
            if (dbFile.exists()) {
                val json = dbFile.readText()
                val parsed = dbAdapter.fromJson(json)
                if (parsed != null && parsed.groups.isNotEmpty()) {
                    val activeId = parsed.settings.activeGroupId
                    val validActiveId = if (parsed.groups.any { it.id == activeId }) {
                        activeId
                    } else {
                        parsed.groups.first().id
                    }
                    parsed.copy(settings = parsed.settings.copy(activeGroupId = validActiveId))
                } else {
                    createDefaultDatabase()
                }
            } else {
                createDefaultDatabase()
            }
        } catch (_: Exception) {
            createDefaultDatabase()
        }
    }

    private fun createDefaultDatabase(): AppDatabase {
        val p1 = Player(id = "p1", name = "Rohit S.", role = "Batter")
        val p2 = Player(id = "p2", name = "Virat K.", role = "Batter")
        val p3 = Player(id = "p3", name = "Jasprit B.", role = "Bowler")
        val p4 = Player(id = "p4", name = "Hardik P.", role = "All-rounder")
        val p5 = Player(id = "p5", name = "Ravindra J.", role = "All-rounder")
        val p6 = Player(id = "p6", name = "KL Rahul", role = "Wicketkeeper")
        val p7 = Player(id = "p7", name = "Mohammed S.", role = "Bowler")
        val p8 = Player(id = "p8", name = "Suryakumar Y.", role = "Batter")
        val p9 = Player(id = "p9", name = "Kuldeep Y.", role = "Bowler")
        val p10 = Player(id = "p10", name = "Shubman G.", role = "Batter")
        val players = listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)

        val m1 = CricketMatch(
            id = "m1",
            title = "Match 1 · Weekend Gully Clásico",
            format = "T20",
            date = "2026-11-01",
            entries = listOf(
                MatchPlayerEntry("p1", BattingEntry(batted = true, runs = 68, balls = 42, fours = 7, sixes = 3), BowlingEntry(legal = 12, conceded = 16, wickets = 1), FieldingEntry(catches = 1), fair = 4),
                MatchPlayerEntry("p2", BattingEntry(batted = true, runs = 82, balls = 49, fours = 9, sixes = 4, notOut = true), FieldingEntry(catches = 2, runouts = 1), fair = 5),
                MatchPlayerEntry("p3", BattingEntry(batted = true, runs = 4, balls = 2, fours = 1, notOut = true), BowlingEntry(legal = 24, conceded = 18, wickets = 3, maidens = 1), FieldingEntry(catches = 1), fair = 4),
                MatchPlayerEntry("p4", BattingEntry(batted = true, runs = 34, balls = 18, fours = 3, sixes = 2), BowlingEntry(legal = 18, conceded = 24, wickets = 2), FieldingEntry(catches = 1), fair = 4),
                MatchPlayerEntry("p5", BattingEntry(batted = true, runs = 28, balls = 15, fours = 2, sixes = 1), BowlingEntry(legal = 24, conceded = 22, wickets = 2), FieldingEntry(catches = 2), fair = 5),
                MatchPlayerEntry("p7", BattingEntry(batted = true, runs = 2, balls = 3), BowlingEntry(legal = 24, conceded = 26, wickets = 2), fair = 4)
            )
        )
        val m2 = CricketMatch(
            id = "m2",
            title = "Match 2 · Titans vs Warriors T20",
            format = "T20",
            date = "2026-11-02",
            entries = listOf(
                MatchPlayerEntry("p1", BattingEntry(batted = true, runs = 45, balls = 30, fours = 5, sixes = 2), BowlingEntry(legal = 6, conceded = 11), fair = 4),
                MatchPlayerEntry("p2", BattingEntry(batted = true, runs = 56, balls = 38, fours = 6, sixes = 1), FieldingEntry(catches = 1), fair = 5),
                MatchPlayerEntry("p3", BowlingEntry(legal = 24, conceded = 15, wickets = 4, maidens = 1), fair = 5),
                MatchPlayerEntry("p4", BattingEntry(batted = true, runs = 41, balls = 22, fours = 4, sixes = 2, notOut = true), BowlingEntry(legal = 18, conceded = 28, wickets = 1), FieldingEntry(catches = 1), fair = 4),
                MatchPlayerEntry("p6", BattingEntry(batted = true, runs = 52, balls = 35, fours = 5, sixes = 2), FieldingEntry(catches = 2, stumpings = 1), fair = 4),
                MatchPlayerEntry("p8", BattingEntry(batted = true, runs = 64, balls = 29, fours = 6, sixes = 4), FieldingEntry(catches = 1), fair = 4)
            )
        )
        val m3 = CricketMatch(
            id = "m3",
            title = "Match 3 · 40-Over Championship Derby",
            format = "ODI",
            date = "2026-11-03",
            entries = listOf(
                MatchPlayerEntry("p1", BattingEntry(batted = true, runs = 94, balls = 88, fours = 10, sixes = 3), BowlingEntry(legal = 18, conceded = 20, wickets = 1), FieldingEntry(catches = 1), fair = 5),
                MatchPlayerEntry("p2", BattingEntry(batted = true, runs = 110, balls = 96, fours = 11, sixes = 2, notOut = true), BowlingEntry(legal = 12, conceded = 12), FieldingEntry(catches = 1, runouts = 1), fair = 5),
                MatchPlayerEntry("p3", BattingEntry(batted = true, runs = 12, balls = 8, fours = 2, notOut = true), BowlingEntry(legal = 48, conceded = 32, wickets = 4, maidens = 2), FieldingEntry(catches = 1), fair = 5),
                MatchPlayerEntry("p5", BattingEntry(batted = true, runs = 55, balls = 44, fours = 5, sixes = 1), BowlingEntry(legal = 48, conceded = 38, wickets = 3, maidens = 1), FieldingEntry(catches = 2), fair = 5),
                MatchPlayerEntry("p7", BattingEntry(batted = true, runs = 8, balls = 10, fours = 1), BowlingEntry(legal = 46, conceded = 36, wickets = 3, maidens = 1), fair = 4),
                MatchPlayerEntry("p9", BattingEntry(batted = true, runs = 15, balls = 14, fours = 1, notOut = true), BowlingEntry(legal = 42, conceded = 30, wickets = 2), fair = 4)
            )
        )

        val defaultGroup = CricketGroup(
            id = "grp_gully_demo",
            name = "Gully Premier League",
            season = "Season 1 · 2026",
            createdAt = "2026-11-01",
            players = players,
            matches = listOf(m1, m2, m3),
            prevRanks = mapOf("ALL|OVERALL|p1" to 2, "ALL|OVERALL|p2" to 1, "ALL|OVERALL|p3" to 3)
        )
        return AppDatabase(
            settings = AppSettings(pinHash = hashPin("1234"), activeGroupId = defaultGroup.id),
            groups = listOf(defaultGroup)
        )
    }

    private suspend fun persist(db: AppDatabase) = withContext(Dispatchers.IO) {
        try {
            val json = dbAdapter.indent("  ").toJson(db)
            dbFile.writeText(json)
            _dbFlow.value = db
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveGroup(): CricketGroup? {
        val db = _dbFlow.value
        val activeId = db.settings.activeGroupId
        return db.groups.find { it.id == activeId } ?: db.groups.firstOrNull()
    }

    suspend fun createGroup(name: String, season: String): CricketGroup {
        val currentDb = _dbFlow.value
        val newGroup = CricketGroup(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            season = season.trim(),
            createdAt = todayStr(),
            players = emptyList(),
            matches = emptyList(),
            prevRanks = null
        )
        val updatedGroups = currentDb.groups + newGroup
        val updatedDb = currentDb.copy(
            settings = currentDb.settings.copy(activeGroupId = newGroup.id),
            groups = updatedGroups
        )
        persist(updatedDb)
        return newGroup
    }

    suspend fun switchGroup(groupId: String) {
        val currentDb = _dbFlow.value
        if (currentDb.groups.any { it.id == groupId }) {
            persist(currentDb.copy(settings = currentDb.settings.copy(activeGroupId = groupId)))
        }
    }

    suspend fun updateGroup(groupId: String, name: String, season: String) {
        val currentDb = _dbFlow.value
        val updatedGroups = currentDb.groups.map { g ->
            if (g.id == groupId) g.copy(name = name.trim(), season = season.trim()) else g
        }
        persist(currentDb.copy(groups = updatedGroups))
    }

    suspend fun deleteGroup(groupId: String) {
        val currentDb = _dbFlow.value
        val updatedGroups = currentDb.groups.filter { it.id != groupId }
        val newActiveId = if (currentDb.settings.activeGroupId == groupId) {
            updatedGroups.firstOrNull()?.id
        } else {
            currentDb.settings.activeGroupId
        }
        persist(currentDb.copy(settings = currentDb.settings.copy(activeGroupId = newActiveId), groups = updatedGroups))
    }

    // Players
    suspend fun addPlayer(name: String, role: String, jersey: String, photo: String): Boolean {
        val activeGroup = getActiveGroup() ?: return false
        if (activeGroup.players.size >= MAX_PLAYERS_PER_GROUP) return false

        val newPlayer = Player(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            role = role,
            jersey = jersey.trim(),
            photo = photo,
            createdAt = todayStr()
        )
        val updatedGroup = activeGroup.copy(players = activeGroup.players + newPlayer)
        updateActiveGroup(updatedGroup)
        return true
    }

    suspend fun updatePlayer(playerId: String, name: String, role: String, jersey: String, photo: String) {
        val activeGroup = getActiveGroup() ?: return
        val updatedPlayers = activeGroup.players.map { p ->
            if (p.id == playerId) p.copy(name = name.trim(), role = role, jersey = jersey.trim(), photo = photo) else p
        }
        updateActiveGroup(activeGroup.copy(players = updatedPlayers))
    }

    suspend fun deletePlayer(playerId: String) {
        val activeGroup = getActiveGroup() ?: return
        val snap = CricketEngine.snapshotRanks(activeGroup.matches, activeGroup.players)
        val updatedPlayers = activeGroup.players.filter { it.id != playerId }
        // Clean up performances from matches
        val updatedMatches = activeGroup.matches.map { m ->
            m.copy(entries = m.entries.filter { it.pid != playerId })
        }.filter { it.entries.isNotEmpty() }

        val updatedGroup = activeGroup.copy(
            players = updatedPlayers,
            matches = updatedMatches,
            prevRanks = snap
        )
        updateActiveGroup(updatedGroup)
    }

    // Matches & Atomic Commit
    suspend fun saveMatch(match: CricketMatch, isEdit: Boolean) {
        val activeGroup = getActiveGroup() ?: return
        // 1. Snapshot previous ranks before commit to calculate movement trends
        val currentSnap = CricketEngine.snapshotRanks(activeGroup.matches, activeGroup.players)

        val updatedMatches = if (isEdit) {
            activeGroup.matches.map { if (it.id == match.id) match else it }
        } else {
            activeGroup.matches + match
        }

        val updatedGroup = activeGroup.copy(
            matches = updatedMatches,
            prevRanks = currentSnap
        )
        updateActiveGroup(updatedGroup)
    }

    suspend fun deleteMatch(matchId: String) {
        val activeGroup = getActiveGroup() ?: return
        val currentSnap = CricketEngine.snapshotRanks(activeGroup.matches, activeGroup.players)
        val updatedMatches = activeGroup.matches.filter { it.id != matchId }
        val updatedGroup = activeGroup.copy(
            matches = updatedMatches,
            prevRanks = currentSnap
        )
        updateActiveGroup(updatedGroup)
    }

    private suspend fun updateActiveGroup(group: CricketGroup) {
        val currentDb = _dbFlow.value
        val updatedGroups = currentDb.groups.map { if (it.id == group.id) group else it }
        persist(currentDb.copy(groups = updatedGroups))
    }

    // Admin PIN Controls
    fun hasPin(): Boolean = !(_dbFlow.value.settings.pinHash.isNullOrEmpty())

    fun isUnlocked(): Boolean {
        return !hasPin() || _adminUnlocked.value
    }

    fun verifyPin(pin: String): Boolean {
        val target = _dbFlow.value.settings.pinHash ?: return true
        val matches = (hashPin(pin) == target)
        if (matches) {
            _adminUnlocked.value = true
        }
        return matches
    }

    suspend fun setPin(pin: String) {
        val currentDb = _dbFlow.value
        val hash = hashPin(pin)
        persist(currentDb.copy(settings = currentDb.settings.copy(pinHash = hash)))
        _adminUnlocked.value = true
    }

    suspend fun removePin() {
        val currentDb = _dbFlow.value
        persist(currentDb.copy(settings = currentDb.settings.copy(pinHash = null)))
        _adminUnlocked.value = false
    }

    fun lockAdmin() {
        _adminUnlocked.value = false
    }

    // JSON Export / Import
    fun exportJsonString(): String {
        return dbAdapter.indent("  ").toJson(_dbFlow.value)
    }

    suspend fun importJsonString(json: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val parsed = dbAdapter.fromJson(json)
                ?: return@withContext Result.failure(Exception("Invalid JSON schema"))
            if (parsed.groups.isEmpty()) {
                return@withContext Result.failure(Exception("Backup contains no groups"))
            }
            // Enforce max players check
            for (g in parsed.groups) {
                if (g.players.size > MAX_PLAYERS_PER_GROUP) {
                    return@withContext Result.failure(Exception("Group '${g.name}' exceeds $MAX_PLAYERS_PER_GROUP players"))
                }
            }
            val activeId = parsed.settings.activeGroupId
            val validActiveId = if (parsed.groups.any { it.id == activeId }) {
                activeId
            } else {
                parsed.groups.first().id
            }
            val normalized = parsed.copy(
                settings = parsed.settings.copy(activeGroupId = validActiveId)
            )
            persist(normalized)
            Result.success(normalized.groups.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
