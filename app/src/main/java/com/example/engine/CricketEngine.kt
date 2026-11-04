package com.example.engine

import com.example.model.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object CricketEngine {
    val FORMATS = listOf("T20", "ODI", "TEST")
    val ALL_FORMATS = listOf("T20", "ODI", "TEST", "ALL")

    val CATEGORIES = listOf(
        "overall" to "Overall Rating",
        "batting" to "Best Batters",
        "bowling" to "Best Bowlers",
        "allround" to "All-Rounders",
        "fair" to "Fair Play"
    )

    data class Benchmark(
        val avg: Double,
        val sr: Double,
        val vol: Double,
        val econBest: Double,
        val econWorst: Double,
        val bAvgBest: Double,
        val bAvgWorst: Double,
        val bSRBest: Double,
        val bSRWorst: Double,
        val wk: Double
    )

    private val BENCHMARKS = mapOf(
        "T20" to Benchmark(
            avg = 30.0, sr = 140.0, vol = 150.0,
            econBest = 5.0, econWorst = 12.0,
            bAvgBest = 12.0, bAvgWorst = 35.0,
            bSRBest = 10.0, bSRWorst = 30.0,
            wk = 8.0
        ),
        "ODI" to Benchmark(
            avg = 40.0, sr = 95.0, vol = 250.0,
            econBest = 4.0, econWorst = 7.5,
            bAvgBest = 20.0, bAvgWorst = 50.0,
            bSRBest = 20.0, bSRWorst = 60.0,
            wk = 8.0
        ),
        "TEST" to Benchmark(
            avg = 40.0, sr = 55.0, vol = 400.0,
            econBest = 2.0, econWorst = 4.5,
            bAvgBest = 20.0, bAvgWorst = 55.0,
            bSRBest = 40.0, bSRWorst = 100.0,
            wk = 10.0
        )
    )

    private val WEIGHT_BAT = 0.45
    private val WEIGHT_BOWL = 0.40
    private val WEIGHT_FIELD = 0.10
    private val WEIGHT_FAIR = 0.05
    private val NO_CONTRIB_PENALTY = 0.60

    /**
     * Parses cricket overs string (e.g. "4.2" -> 4*6 + 2 = 26 legal balls).
     * Returns null if string is invalid (balls > 5 or malformed).
     */
    fun parseOvers(s: String?): Int? {
        val str = s?.trim() ?: return 0
        if (str.isEmpty()) return 0
        val regex = Regex("""^(\d{1,3})(?:\.(\d))?$""")
        val match = regex.find(str) ?: return null
        val overs = match.groupValues[1].toIntOrNull() ?: return null
        val balls = match.groupValues.getOrNull(2)?.ifEmpty { "0" }?.toIntOrNull() ?: 0
        if (balls > 5) return null
        return (overs * 6) + balls
    }

    fun oversStr(legalBalls: Int): String {
        return "${legalBalls / 6}.${legalBalls % 6}"
    }

    fun dismissals(a: CareerAgg): Int = max(0, a.inns - a.notOuts)

    fun batAvg(a: CareerAgg): Double? {
        val d = dismissals(a)
        return if (d > 0) a.runs.toDouble() / d else null
    }

    fun strikeRate(a: CareerAgg): Double? {
        return if (a.balls > 0) (a.runs.toDouble() / a.balls) * 100.0 else null
    }

    fun economy(a: CareerAgg): Double? {
        return if (a.legal > 0) a.conceded.toDouble() / (a.legal.toDouble() / 6.0) else null
    }

    fun bowlAvg(a: CareerAgg): Double? {
        return if (a.wickets > 0) a.conceded.toDouble() / a.wickets else null
    }

    fun bowlSR(a: CareerAgg): Double? {
        return if (a.wickets > 0) a.legal.toDouble() / a.wickets else null
    }

    private fun clamp01(x: Double): Double = max(0.0, min(1.0, x))
    private fun up(v: Double, target: Double): Double = clamp01(v / target)
    private fun down(v: Double, best: Double, worst: Double): Double = clamp01((worst - v) / (worst - best))

    fun addEntryToAgg(acc: CareerAgg, entry: MatchPlayerEntry): CareerAgg {
        var matches = acc.matches + 1
        var inns = acc.inns
        var runs = acc.runs
        var balls = acc.balls
        var fours = acc.fours
        var sixes = acc.sixes
        var notOuts = acc.notOuts
        var fifties = acc.fifties
        var hundreds = acc.hundreds
        var hs = acc.hs
        var hsNO = acc.hsNO

        val b = entry.bat
        if (b.batted) {
            inns++
            runs += b.runs
            balls += b.balls
            fours += b.fours
            sixes += b.sixes
            if (b.notOut) notOuts++
            if (b.runs >= 100) hundreds++ else if (b.runs >= 50) fifties++
            if (b.runs > hs || (b.runs == hs && b.notOut && !hsNO)) {
                hs = b.runs
                hsNO = b.notOut
            }
        }

        var bowlInns = acc.bowlInns
        var legal = acc.legal
        var maidens = acc.maidens
        var conceded = acc.conceded
        var wickets = acc.wickets
        var bestW = acc.bestW
        var bestR = acc.bestR

        val w = entry.bowl
        if (w.legal > 0) {
            bowlInns++
            legal += w.legal
            maidens += w.maidens
            conceded += w.conceded
            wickets += w.wickets
            if (w.wickets > bestW || (w.wickets == bestW && (bestR == null || w.conceded < bestR))) {
                bestW = w.wickets
                bestR = w.conceded
            }
        }

        val catches = acc.catches + entry.field.catches
        val runouts = acc.runouts + entry.field.runouts
        val stumpings = acc.stumpings + entry.field.stumpings
        val fair = acc.fair + entry.fair

        return CareerAgg(
            matches = matches,
            inns = inns,
            runs = runs,
            balls = balls,
            fours = fours,
            sixes = sixes,
            notOuts = notOuts,
            fifties = fifties,
            hundreds = hundreds,
            hs = hs,
            hsNO = hsNO,
            bowlInns = bowlInns,
            legal = legal,
            maidens = maidens,
            conceded = conceded,
            wickets = wickets,
            bestW = bestW,
            bestR = bestR,
            catches = catches,
            runouts = runouts,
            stumpings = stumpings,
            fair = fair
        )
    }

    fun mergeAgg(a: CareerAgg, b: CareerAgg): CareerAgg {
        var hs = max(a.hs, b.hs)
        var hsNO = if (a.hs == b.hs) (a.hsNO || b.hsNO) else if (a.hs > b.hs) a.hsNO else b.hsNO

        var bestW = 0
        var bestR: Int? = null
        val candidates = listOf(a, b).filter { it.bowlInns > 0 }
            .sortedWith(compareByDescending<CareerAgg> { it.bestW }.thenBy { it.bestR ?: 9999 })
        if (candidates.isNotEmpty()) {
            bestW = candidates[0].bestW
            bestR = candidates[0].bestR
        }

        return CareerAgg(
            matches = a.matches + b.matches,
            inns = a.inns + b.inns,
            runs = a.runs + b.runs,
            balls = a.balls + b.balls,
            fours = a.fours + b.fours,
            sixes = a.sixes + b.sixes,
            notOuts = a.notOuts + b.notOuts,
            fifties = a.fifties + b.fifties,
            hundreds = a.hundreds + b.hundreds,
            hs = hs,
            hsNO = hsNO,
            bowlInns = a.bowlInns + b.bowlInns,
            legal = a.legal + b.legal,
            maidens = a.maidens + b.maidens,
            conceded = a.conceded + b.conceded,
            wickets = a.wickets + b.wickets,
            bestW = bestW,
            bestR = bestR,
            catches = a.catches + b.catches,
            runouts = a.runouts + b.runouts,
            stumpings = a.stumpings + b.stumpings,
            fair = a.fair + b.fair
        )
    }

    fun buildAggsByFormat(matches: List<CricketMatch>): Map<String, Map<String, CareerAgg>> {
        val out = mutableMapOf<String, MutableMap<String, CareerAgg>>(
            "T20" to mutableMapOf(),
            "ODI" to mutableMapOf(),
            "TEST" to mutableMapOf()
        )
        for (m in matches) {
            val fmtMap = out[m.format] ?: continue
            for (e in m.entries) {
                val current = fmtMap[e.pid] ?: CareerAgg()
                fmtMap[e.pid] = addEntryToAgg(current, e)
            }
        }
        return out
    }

    fun getPlayerAggForFormat(
        aggsByFormat: Map<String, Map<String, CareerAgg>>,
        pid: String,
        format: String
    ): CareerAgg {
        if (format != "ALL") {
            return aggsByFormat[format]?.get(pid) ?: CareerAgg()
        }
        var merged = CareerAgg()
        for (f in FORMATS) {
            val a = aggsByFormat[f]?.get(pid)
            if (a != null) {
                merged = mergeAgg(merged, a)
            }
        }
        return merged
    }

    fun calculateComponentScores(a: CareerAgg, format: String): ComponentScores {
        val bench = BENCHMARKS[format] ?: BENCHMARKS["T20"]!!
        if (a.matches == 0) return ComponentScores(null, null, null, null)

        var batScore: Double? = null
        if (a.inns > 0) {
            val d = dismissals(a)
            val base = if (d > 0) a.runs.toDouble() / d else a.runs.toDouble() / a.inns
            val avgN = up(base, bench.avg)
            val sr = strikeRate(a)
            val srN = if (sr == null) avgN else up(sr, bench.sr)
            val volN = up(a.runs.toDouble(), bench.vol)
            val mile = clamp01(a.fifties * 0.4 + a.hundreds * 0.8)
            batScore = 100.0 * (0.35 * avgN + 0.25 * srN + 0.25 * volN + 0.15 * mile)
        }

        var bowlScore: Double? = null
        if (a.legal > 0) {
            val wkN = up(a.wickets.toDouble(), bench.wk)
            val econ = economy(a) ?: bench.econWorst
            val econN = down(econ, bench.econBest, bench.econWorst)
            val bAvg = bowlAvg(a)
            val avgN = if (a.wickets > 0 && bAvg != null) down(bAvg, bench.bAvgBest, bench.bAvgWorst) else 0.0
            val bSR = bowlSR(a)
            val srN = if (a.wickets > 0 && bSR != null) down(bSR, bench.bSRBest, bench.bSRWorst) else 0.0
            bowlScore = 100.0 * (0.35 * wkN + 0.30 * econN + 0.20 * avgN + 0.15 * srN)
        }

        val fieldScore = 100.0 * clamp01((a.catches + 1.5 * (a.runouts + a.stumpings)) / (a.matches * 1.5))
        val fairScore = clamp01((50.0 + (a.fair.toDouble() / a.matches) * 10.0) / 100.0) * 100.0

        return ComponentScores(bat = batScore, bowl = bowlScore, field = fieldScore, fair = fairScore)
    }

    fun ratingForSingleFormat(a: CareerAgg, format: String, category: String): Double? {
        if (a.matches == 0) return null
        val c = calculateComponentScores(a, format)
        return when (category) {
            "batting" -> c.bat
            "bowling" -> c.bowl
            "allround" -> {
                if (c.bat != null && c.bowl != null) {
                    val sum = c.bat + c.bowl
                    if (sum == 0.0) 0.0 else (2.0 * c.bat * c.bowl) / sum
                } else null
            }
            "fair" -> c.fair
            else -> { // "overall"
                var sum = 0.0
                var weight = 0.0
                if (c.bat != null) {
                    sum += c.bat * WEIGHT_BAT
                    weight += WEIGHT_BAT
                }
                if (c.bowl != null) {
                    sum += c.bowl * WEIGHT_BOWL
                    weight += WEIGHT_BOWL
                }
                if (c.field != null) {
                    sum += c.field * WEIGHT_FIELD
                    weight += WEIGHT_FIELD
                }
                if (c.fair != null) {
                    sum += c.fair * WEIGHT_FAIR
                    weight += WEIGHT_FAIR
                }
                var rating = if (weight > 0) sum / weight else 0.0
                if (c.bat == null && c.bowl == null) {
                    rating *= NO_CONTRIB_PENALTY
                }
                rating
            }
        }
    }

    fun calculatePlayerRating(
        aggsByFormat: Map<String, Map<String, CareerAgg>>,
        pid: String,
        format: String,
        category: String
    ): Double? {
        if (format != "ALL") {
            val a = aggsByFormat[format]?.get(pid) ?: return null
            return ratingForSingleFormat(a, format, category)
        }
        // ALL-FORMAT: Average ONLY over formats the player has actually played!
        // Unplayed formats are never penalized or counted as zero.
        val ratings = mutableListOf<Double>()
        for (f in FORMATS) {
            val a = aggsByFormat[f]?.get(pid)
            if (a != null && a.matches > 0) {
                val r = ratingForSingleFormat(a, f, category)
                if (r != null) ratings.add(r)
            }
        }
        return if (ratings.isNotEmpty()) ratings.average() else null
    }

    fun buildRanking(
        format: String,
        category: String,
        matches: List<CricketMatch>,
        players: List<Player>,
        prevRanks: Map<String, Int>? = null
    ): RankingResult {
        val aggs = buildAggsByFormat(matches)
        val ranked = mutableListOf<PlayerRankItem>()
        val unranked = mutableListOf<UnrankedItem>()

        for (p in players) {
            val a = getPlayerAggForFormat(aggs, p.id, format)
            if (a.matches == 0) {
                unranked.add(UnrankedItem(p, "Not played ${if (format == "ALL") "any format" else format}"))
                continue
            }
            val r = calculatePlayerRating(aggs, p.id, format, category)
            if (r == null) {
                val reason = when (category) {
                    "batting" -> "No batting innings"
                    "bowling" -> "No bowling innings"
                    "allround" -> "Requires both batting & bowling"
                    else -> "No contributions"
                }
                unranked.add(UnrankedItem(p, reason))
            } else {
                ranked.add(
                    PlayerRankItem(
                        rank = 0,
                        player = p,
                        agg = a,
                        rating = r,
                        trend = Trend.None
                    )
                )
            }
        }

        // Sort by Rating DESC, Runs DESC, Wickets DESC, Name ASC
        ranked.sortWith(
            compareByDescending<PlayerRankItem> { it.rating }
                .thenByDescending { it.agg.runs }
                .thenByDescending { it.agg.wickets }
                .thenBy { it.player.name }
        )

        // Assign ranks & calculate trend compared to prevRanks snapshot
        val resultRanked = ranked.mapIndexed { idx, item ->
            val rank = idx + 1
            val trendKey = "$format|$category|${item.player.id}"
            val prevRank = prevRanks?.get(trendKey)
            val trend = when {
                prevRanks == null || prevRanks.isEmpty() -> Trend.None
                prevRank == null -> Trend.New
                prevRank > rank -> Trend.Up(prevRank - rank)
                prevRank < rank -> Trend.Down(rank - prevRank)
                else -> Trend.Same
            }
            item.copy(rank = rank, trend = trend)
        }

        return RankingResult(ranked = resultRanked, unranked = unranked)
    }

    fun snapshotRanks(matches: List<CricketMatch>, players: List<Player>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        for (f in ALL_FORMATS) {
            for ((catKey, _) in CATEGORIES) {
                val res = buildRanking(f, catKey, matches, players, prevRanks = null)
                for (item in res.ranked) {
                    map["$f|$catKey|${item.player.id}"] = item.rank
                }
            }
        }
        return map
    }

    // Awards Period Range (week / month)
    data class AwardPeriod(
        val start: Date,
        val end: Date,
        val label: String
    )

    fun getPeriodRange(kind: String, offset: Int): AwardPeriod {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val monthSdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        return if (kind == "week") {
            // Monday-Sunday week
            val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
            cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek + (offset * 7))
            val startDate = cal.time
            cal.add(Calendar.DAY_OF_MONTH, 7)
            val endDate = cal.time
            cal.add(Calendar.DAY_OF_MONTH, -1)
            val labelEnd = cal.time
            AwardPeriod(
                start = startDate,
                end = endDate,
                label = "${sdf.format(startDate)} – ${sdf.format(labelEnd)}"
            )
        } else {
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, offset)
            val startDate = cal.time
            cal.add(Calendar.MONTH, 1)
            val endDate = cal.time
            AwardPeriod(
                start = startDate,
                end = endDate,
                label = monthSdf.format(startDate)
            )
        }
    }
}
