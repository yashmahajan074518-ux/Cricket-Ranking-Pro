package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CricketEngine
import com.example.model.CricketGroup
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.*
import com.example.viewmodel.CricketViewModel

@Composable
fun CompareScreen(
    viewModel: CricketViewModel,
    group: CricketGroup,
    modifier: Modifier = Modifier
) {
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val cmpAId by viewModel.cmpPlayerAId.collectAsState()
    val cmpBId by viewModel.cmpPlayerBId.collectAsState()

    val playerA = remember(group.players, cmpAId) {
        group.players.find { it.id == cmpAId } ?: group.players.getOrNull(0)
    }
    val playerB = remember(group.players, cmpBId) {
        group.players.find { it.id == cmpBId } ?: group.players.getOrNull(1) ?: group.players.getOrNull(0)
    }

    if (group.players.size < 2 || playerA == null || playerB == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add at least 2 players to the group roster to use head-to-head comparison.",
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
        return
    }

    val aggs = remember(group.matches) {
        CricketEngine.buildAggsByFormat(group.matches)
    }

    val aggA = remember(playerA, selectedFormat, aggs) {
        CricketEngine.getPlayerAggForFormat(aggs, playerA.id, selectedFormat)
    }
    val aggB = remember(playerB, selectedFormat, aggs) {
        CricketEngine.getPlayerAggForFormat(aggs, playerB.id, selectedFormat)
    }

    val ratingA = remember(playerA, selectedFormat, aggs) {
        CricketEngine.calculatePlayerRating(aggs, playerA.id, selectedFormat, "overall")
    }
    val ratingB = remember(playerB, selectedFormat, aggs) {
        CricketEngine.calculatePlayerRating(aggs, playerB.id, selectedFormat, "overall")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("compare_screen"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PLAYER INTELLIGENCE",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "HEAD-TO-HEAD COMPARISON",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = ElectricBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (selectedFormat == "ALL") "ALL-FORMAT" else selectedFormat,
                        fontSize = 11.sp,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Two player headers
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Player A
                PlayerSelectHeader(
                    player = playerA,
                    allPlayers = group.players,
                    accentColor = ElectricBlue,
                    onSelect = { viewModel.setComparePlayers(it.id, playerB.id) },
                    modifier = Modifier.weight(1f)
                )

                // Player B
                PlayerSelectHeader(
                    player = playerB,
                    allPlayers = group.players,
                    accentColor = TrophyGold,
                    onSelect = { viewModel.setComparePlayers(playerA.id, it.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Metric Comparison Rows
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardNavy),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ComparisonMetricRow("Rating Points", ratingA, ratingB, lowerIsBetter = false, decimals = 1)
                    ComparisonMetricRow("Matches Played", aggA.matches.toDouble(), aggB.matches.toDouble(), false, 0)
                    ComparisonMetricRow("Total Runs", aggA.runs.toDouble(), aggB.runs.toDouble(), false, 0)
                    ComparisonMetricRow("Batting Average", CricketEngine.batAvg(aggA), CricketEngine.batAvg(aggB), false, 2)
                    ComparisonMetricRow("Strike Rate", CricketEngine.strikeRate(aggA), CricketEngine.strikeRate(aggB), false, 1)
                    ComparisonMetricRow("Fours (4s)", aggA.fours.toDouble(), aggB.fours.toDouble(), false, 0)
                    ComparisonMetricRow("Sixes (6s)", aggA.sixes.toDouble(), aggB.sixes.toDouble(), false, 0)
                    ComparisonMetricRow("Wickets Taken", aggA.wickets.toDouble(), aggB.wickets.toDouble(), false, 0)
                    ComparisonMetricRow("Economy Rate", CricketEngine.economy(aggA), CricketEngine.economy(aggB), lowerIsBetter = true, decimals = 2)
                    ComparisonMetricRow("Bowling Average", CricketEngine.bowlAvg(aggA), CricketEngine.bowlAvg(aggB), lowerIsBetter = true, decimals = 2)
                    ComparisonMetricRow("Catches", aggA.catches.toDouble(), aggB.catches.toDouble(), false, 0)
                    ComparisonMetricRow("Run Outs", aggA.runouts.toDouble(), aggB.runouts.toDouble(), false, 0)
                    ComparisonMetricRow("Fair Play", aggA.fair.toDouble(), aggB.fair.toDouble(), false, 0)
                }
            }
        }
    }
}

@Composable
fun PlayerSelectHeader(
    player: com.example.model.Player,
    allPlayers: List<com.example.model.Player>,
    accentColor: Color,
    onSelect: (com.example.model.Player) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlayerAvatar(player = player, size = 52.dp, borderColor = accentColor)
            Spacer(modifier = Modifier.height(6.dp))

            Box {
                Surface(
                    color = InputBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = player.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(CardNavy)
                ) {
                    allPlayers.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.name, fontSize = 12.sp, color = TextPrimary) },
                            onClick = {
                                onSelect(p)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "${player.role}${if (player.jersey.isNotBlank()) " #${player.jersey}" else ""}",
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ComparisonMetricRow(
    label: String,
    valA: Double?,
    valB: Double?,
    lowerIsBetter: Boolean,
    decimals: Int
) {
    val winA = if (valA != null && valB != null && valA != valB) {
        if (lowerIsBetter) valA < valB else valA > valB
    } else false

    val winB = if (valA != null && valB != null && valA != valB) {
        if (lowerIsBetter) valB < valA else valB > valA
    } else false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Value A
        Surface(
            color = if (winA) ElectricBlue.copy(alpha = 0.2f) else Color.Transparent,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.width(70.dp)
        ) {
            Text(
                text = if (valA == null) "N/A" else "%.${decimals}f".format(valA),
                fontFamily = OrbitronFontFamily,
                fontWeight = if (winA) FontWeight.Black else FontWeight.Normal,
                fontSize = 12.sp,
                color = if (winA) ElectricBlue else TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Metric Label
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp,
            modifier = Modifier.weight(1f)
        )

        // Value B
        Surface(
            color = if (winB) TrophyGold.copy(alpha = 0.2f) else Color.Transparent,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.width(70.dp)
        ) {
            Text(
                text = if (valB == null) "N/A" else "%.${decimals}f".format(valB),
                fontFamily = OrbitronFontFamily,
                fontWeight = if (winB) FontWeight.Black else FontWeight.Normal,
                fontSize = 12.sp,
                color = if (winB) TrophyGold else TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
