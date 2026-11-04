package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CricketEngine
import com.example.model.CricketGroup
import com.example.model.CricketMatch
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.*
import com.example.viewmodel.ActiveModal
import com.example.viewmodel.CricketViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AwardsScreen(
    viewModel: CricketViewModel,
    group: CricketGroup,
    modifier: Modifier = Modifier
) {
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val weekOffset by viewModel.weekOffset.collectAsState()
    val monthOffset by viewModel.monthOffset.collectAsState()

    val weekPeriod = remember(weekOffset) {
        CricketEngine.getPeriodRange("week", weekOffset)
    }
    val monthPeriod = remember(monthOffset) {
        CricketEngine.getPeriodRange("month", monthOffset)
    }

    val inSdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    fun filterMatchesInPeriod(start: java.util.Date, end: java.util.Date): List<CricketMatch> {
        return group.matches.filter { m ->
            val matchDate = try { inSdf.parse(m.date) } catch (_: Exception) { null }
            matchDate != null && !matchDate.before(start) && matchDate.before(end) &&
                    (selectedFormat == "ALL" || m.format == selectedFormat)
        }
    }

    val weekMatches = remember(group.matches, weekPeriod, selectedFormat) {
        filterMatchesInPeriod(weekPeriod.start, weekPeriod.end)
    }
    val monthMatches = remember(group.matches, monthPeriod, selectedFormat) {
        filterMatchesInPeriod(monthPeriod.start, monthPeriod.end)
    }

    val weekRanking = remember(weekMatches, group.players, selectedFormat) {
        CricketEngine.buildRanking(selectedFormat, "overall", weekMatches, group.players)
    }
    val monthRanking = remember(monthMatches, group.players, selectedFormat) {
        CricketEngine.buildRanking(selectedFormat, "overall", monthMatches, group.players)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("awards_screen"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SEASON SPOTLIGHT",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "PERIOD AWARDS",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = TrophyGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrophyGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (selectedFormat == "ALL") "ALL-FORMAT" else selectedFormat,
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TrophyGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Player of the Week Card
        item {
            PeriodAwardCard(
                title = "PLAYER OF THE WEEK",
                periodLabel = weekPeriod.label + (if (weekOffset == 0) " · Current" else ""),
                matchesCount = weekMatches.size,
                rankedPlayers = weekRanking.ranked,
                onPrev = { viewModel.shiftAwardWeek(-1) },
                onNext = { viewModel.shiftAwardWeek(1) },
                canNext = weekOffset < 0,
                onPlayerClick = { pid -> viewModel.openModal(ActiveModal.PlayerProfile(pid)) }
            )
        }

        // Player of the Month Card
        item {
            PeriodAwardCard(
                title = "PLAYER OF THE MONTH",
                periodLabel = monthPeriod.label + (if (monthOffset == 0) " · Current" else ""),
                matchesCount = monthMatches.size,
                rankedPlayers = monthRanking.ranked,
                onPrev = { viewModel.shiftAwardMonth(-1) },
                onNext = { viewModel.shiftAwardMonth(1) },
                canNext = monthOffset < 0,
                onPlayerClick = { pid -> viewModel.openModal(ActiveModal.PlayerProfile(pid)) }
            )
        }
    }
}

@Composable
fun PeriodAwardCard(
    title: String,
    periodLabel: String,
    matchesCount: Int,
    rankedPlayers: List<com.example.model.PlayerRankItem>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    canNext: Boolean,
    onPlayerClick: (String) -> Unit
) {
    val winner = rankedPlayers.firstOrNull()
    val runnersUp = rankedPlayers.drop(1).take(2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = TrophyGold
                    )
                    Text(
                        text = "$periodLabel · $matchesCount match${if (matchesCount == 1) "" else "es"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous",
                            tint = ElectricBlue
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        enabled = canNext,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next",
                            tint = if (canNext) ElectricBlue else TextMuted.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (winner != null) {
                Surface(
                    color = TrophyGold.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrophyGold.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayerClick(winner.player.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PlayerAvatar(
                                player = winner.player,
                                size = 52.dp,
                                borderColor = TrophyGold
                            )
                            Column {
                                Text(
                                    text = winner.player.name,
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "${winner.agg.runs} Runs",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ElectricBlue
                                    )
                                    Text(
                                        text = "${winner.agg.wickets} Wickets",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TrophyGold
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "🏆",
                                fontSize = 18.sp
                            )
                            Text(
                                text = "%.1f".format(winner.rating),
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = TrophyGold
                            )
                            Text(
                                text = "rating",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                if (runnersUp.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Runners-up: " + runnersUp.joinToString(" · ") { "${it.player.name} (%.1f)".format(it.rating) },
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matches played in this period.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
