package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CricketEngine
import com.example.model.CricketGroup
import com.example.model.CricketMatch
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.*
import com.example.viewmodel.ActiveModal
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MatchesScreen(
    viewModel: CricketViewModel,
    group: CricketGroup,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val sortedMatches = remember(group.matches) {
        group.matches.sortedWith(
            compareByDescending<CricketMatch> { it.date }.thenByDescending { it.createdAt }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("matches_screen_list"),
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
                        text = "MATCH ARCHIVE",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "MATCH HISTORY (${group.matches.size})",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.repository.isUnlocked()) {
                            if (group.players.isEmpty()) {
                                viewModel.emitToast("Add players to roster first")
                            } else {
                                viewModel.startMatchWizard(null)
                            }
                        } else {
                            viewModel.openModal(ActiveModal.AdminPin)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("record_match_header_btn")
                ) {
                    Text(
                        text = "＋ Record Match",
                        color = Color(0xFF10151E),
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (sortedMatches.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardNavy),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No matches recorded yet",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record match scores to automatically build career statistics and ICC rankings.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(sortedMatches, key = { it.id }) { match ->
                val participatingPlayers = remember(match, group.players) {
                    val pMap = group.players.associateBy { it.id }
                    match.entries.mapNotNull { pMap[it.pid] }
                }

                // Top batter & bowler in match
                val topBatter = remember(match) {
                    match.entries.filter { it.bat.batted }
                        .maxByOrNull { it.bat.runs }
                }
                val topBowler = remember(match) {
                    match.entries.filter { it.bowl.legal > 0 }
                        .maxWithOrNull(
                            compareBy<com.example.model.MatchPlayerEntry> { it.bowl.wickets }
                                .thenByDescending { -it.bowl.conceded }
                        )
                }

                val batterName = topBatter?.let { tb -> group.players.find { it.id == tb.pid }?.name }
                val bowlerName = topBowler?.let { tb -> group.players.find { it.id == tb.pid }?.name }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openModal(ActiveModal.MatchDetail(match.id)) }
                        .testTag("match_card_${match.id}"),
                    colors = CardDefaults.cardColors(containerColor = CardNavy),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = when (match.format) {
                                        "T20" -> ElectricBlue.copy(alpha = 0.2f)
                                        "ODI" -> TrophyGold.copy(alpha = 0.2f)
                                        else -> NegRed.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when (match.format) {
                                            "T20" -> ElectricBlue
                                            "ODI" -> TrophyGold
                                            else -> NegRed
                                        }
                                    )
                                ) {
                                    Text(
                                        text = match.format,
                                        fontFamily = OrbitronFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = when (match.format) {
                                            "T20" -> ElectricBlue
                                            "ODI" -> TrophyGold
                                            else -> NegRed
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = match.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = formatDate(match.date),
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Match Leaders highlight
                        if (topBatter != null || topBowler != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (topBatter != null && batterName != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = "🏏", fontSize = 12.sp)
                                        Text(
                                            text = "$batterName ${topBatter.bat.runs}${if (topBatter.bat.notOut) "*" else ""} (${topBatter.bat.balls})",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ElectricBlue
                                        )
                                    }
                                }
                                if (topBowler != null && bowlerName != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = "🎯", fontSize = 12.sp)
                                        Text(
                                            text = "$bowlerName ${topBowler.bowl.wickets}/${topBowler.bowl.conceded} (${CricketEngine.oversStr(topBowler.bowl.legal)} ov)",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TrophyGold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Participant avatars & action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                participatingPlayers.take(6).forEachIndexed { idx, p ->
                                    PlayerAvatar(
                                        player = p,
                                        size = 26.dp,
                                        modifier = Modifier.offset(x = (-6 * idx).dp)
                                    )
                                }
                                if (participatingPlayers.size > 6) {
                                    Text(
                                        text = "+${participatingPlayers.size - 6}",
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        modifier = Modifier.offset(x = (-30).dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { viewModel.openModal(ActiveModal.MatchDetail(match.id)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "View",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (viewModel.repository.isUnlocked()) {
                                            viewModel.startMatchWizard(match.id)
                                        } else {
                                            viewModel.openModal(ActiveModal.AdminPin)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (viewModel.repository.isUnlocked()) {
                                            scope.launch {
                                                viewModel.repository.deleteMatch(match.id)
                                                viewModel.emitToast("Match deleted — rankings recalculated")
                                            }
                                        } else {
                                            viewModel.openModal(ActiveModal.AdminPin)
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = NegRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
