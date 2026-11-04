package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CricketEngine
import com.example.model.CricketGroup
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.*
import com.example.viewmodel.ActiveModal
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.launch

@Composable
fun PlayersScreen(
    viewModel: CricketViewModel,
    group: CricketGroup,
    modifier: Modifier = Modifier
) {
    val playerSearch by viewModel.playerSearch.collectAsState()
    val scope = rememberCoroutineScope()

    val filteredPlayers = remember(group.players, playerSearch) {
        if (playerSearch.isBlank()) group.players
        else group.players.filter {
            it.name.contains(playerSearch.trim(), ignoreCase = true) ||
            it.role.contains(playerSearch.trim(), ignoreCase = true)
        }
    }

    val aggs = remember(group.matches) {
        CricketEngine.buildAggsByFormat(group.matches)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("players_roster_screen"),
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
                        text = "SQUAD ROSTER",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "PLAYERS (${group.players.size}/51)",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.repository.isUnlocked()) {
                            if (group.players.size >= 51) {
                                viewModel.emitToast("Maximum 51 players reached")
                            } else {
                                viewModel.openModal(ActiveModal.PlayerForm(null))
                            }
                        } else {
                            viewModel.openModal(ActiveModal.AdminPin)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_player_btn")
                ) {
                    Text(
                        text = "＋ Add Player",
                        color = Color(0xFF04111F),
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = playerSearch,
                onValueChange = { viewModel.setPlayerSearch(it) },
                placeholder = { Text("Search squad...", fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = BorderDark,
                    unfocusedContainerColor = InputBg,
                    focusedContainerColor = InputBg
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        if (filteredPlayers.isEmpty()) {
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
                            text = if (group.players.isEmpty()) "Your roster is empty" else "No players found",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (group.players.isEmpty()) "Add up to 51 players to record their statistics." else "Try a different search term.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(filteredPlayers, key = { it.id }) { p ->
                val rating = CricketEngine.calculatePlayerRating(aggs, p.id, "ALL", "overall")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_card_${p.id}"),
                    colors = CardDefaults.cardColors(containerColor = CardNavy),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.openModal(ActiveModal.PlayerProfile(p.id)) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PlayerAvatar(player = p, size = 44.dp)
                            Column {
                                Text(
                                    text = p.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = p.role + (if (p.jersey.isNotBlank()) " · #${p.jersey}" else ""),
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    if (rating != null) {
                                        Surface(
                                            color = TrophyGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "%.1f pts".format(rating),
                                                fontSize = 10.sp,
                                                color = TrophyGold,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { viewModel.openModal(ActiveModal.PlayerProfile(p.id)) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "View",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (viewModel.repository.isUnlocked()) {
                                        viewModel.openModal(ActiveModal.PlayerForm(p))
                                    } else {
                                        viewModel.openModal(ActiveModal.AdminPin)
                                    }
                                },
                                modifier = Modifier.size(34.dp)
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
                                            viewModel.repository.deletePlayer(p.id)
                                            viewModel.emitToast("Player '${p.name}' removed")
                                        }
                                    } else {
                                        viewModel.openModal(ActiveModal.AdminPin)
                                    }
                                },
                                modifier = Modifier.size(34.dp)
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
