package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: String = "Batter", // Batter, Bowler, All-rounder, Wicketkeeper
    val jersey: String = "",
    val photo: String = "", // Base64 or URI or empty
    val createdAt: String = ""
)

@JsonClass(generateAdapter = true)
data class BattingEntry(
    val batted: Boolean = false,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val notOut: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BowlingEntry(
    val legal: Int = 0, // Stored as total legal balls (e.g. 4.2 overs = 26 balls)
    val conceded: Int = 0,
    val wickets: Int = 0,
    val maidens: Int = 0
)

@JsonClass(generateAdapter = true)
data class FieldingEntry(
    val catches: Int = 0,
    val runouts: Int = 0,
    val stumpings: Int = 0
)

@JsonClass(generateAdapter = true)
data class MatchPlayerEntry(
    val pid: String,
    val bat: BattingEntry = BattingEntry(),
    val bowl: BowlingEntry = BowlingEntry(),
    val field: FieldingEntry = FieldingEntry(),
    val fair: Int = 0 // -5 to +5
)

@JsonClass(generateAdapter = true)
data class CricketMatch(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val format: String = "T20", // T20, ODI, TEST
    val date: String, // YYYY-MM-DD
    val createdAt: String = "",
    val entries: List<MatchPlayerEntry> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PreviousRanks(
    val at: Long = 0L,
    val ranks: Map<String, Int> = emptyMap() // Key format: "FORMAT|CATEGORY|PID"
)

@JsonClass(generateAdapter = true)
data class CricketGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val season: String = "",
    val createdAt: String = "",
    val players: List<Player> = emptyList(),
    val matches: List<CricketMatch> = emptyList(),
    val prevRanks: Map<String, Int>? = null
)

@JsonClass(generateAdapter = true)
data class AppSettings(
    val pinHash: String? = null,
    val activeGroupId: String? = null
)

@JsonClass(generateAdapter = true)
data class AppDatabase(
    val app: String = "CRICKET RANKING PRO",
    val version: Int = 2,
    val settings: AppSettings = AppSettings(),
    val groups: List<CricketGroup> = emptyList()
)

// In-memory aggregations and derived career statistics
data class CareerAgg(
    val matches: Int = 0,
    val inns: Int = 0,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val notOuts: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val hs: Int = 0,
    val hsNO: Boolean = false,
    val bowlInns: Int = 0,
    val legal: Int = 0,
    val maidens: Int = 0,
    val conceded: Int = 0,
    val wickets: Int = 0,
    val bestW: Int = 0,
    val bestR: Int? = null,
    val catches: Int = 0,
    val runouts: Int = 0,
    val stumpings: Int = 0,
    val fair: Int = 0
)

data class ComponentScores(
    val bat: Double?,
    val bowl: Double?,
    val field: Double?,
    val fair: Double?
)

data class PlayerRankItem(
    val rank: Int,
    val player: Player,
    val agg: CareerAgg,
    val rating: Double,
    val trend: Trend
)

sealed class Trend {
    object None : Trend()
    object Same : Trend()
    object New : Trend()
    data class Up(val delta: Int) : Trend()
    data class Down(val delta: Int) : Trend()
}

data class UnrankedItem(
    val player: Player,
    val reason: String
)

data class RankingResult(
    val ranked: List<PlayerRankItem>,
    val unranked: List<UnrankedItem>
)
