package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.CricketEngine
import com.example.model.CricketGroup
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.*
import com.example.viewmodel.CricketViewModel
import com.example.viewmodel.WizardPlayerForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchWizardModal(
    viewModel: CricketViewModel,
    group: CricketGroup
) {
    val draft by viewModel.wizardDraft.collectAsState()
    if (draft == null) return

    val d = draft!!
    var discardConfirm by remember { mutableStateOf(false) }

    if (discardConfirm) {
        AlertDialog(
            onDismissRequest = { discardConfirm = false },
            title = { Text("Discard match entry?", fontFamily = OrbitronFontFamily) },
            text = { Text("Nothing has been saved yet. Your entered statistics will be lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        discardConfirm = false
                        viewModel.closeModal()
                    }
                ) {
                    Text("Discard", color = NegRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { discardConfirm = false }) {
                    Text("Keep editing")
                }
            },
            containerColor = CardNavy
        )
    }

    Dialog(
        onDismissRequest = { discardConfirm = true },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("match_wizard_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CardNavyBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1E33))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (d.editMatchId != null) "EDIT MATCH" else "RECORD MATCH",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = TrophyGold
                        )
                        Text(
                            text = "Step ${d.step} of 3 · ${if (d.step == 1) "Match Info & Roster" else if (d.step == 2) "Player Stats Entry" else "Review & Commit"}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    IconButton(
                        onClick = { discardConfirm = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Step progress bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..3).forEach { stepNum ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (stepNum <= d.step) ElectricBlue else BorderDark)
                        )
                    }
                }

                // Step Body
                Box(modifier = Modifier.weight(1f)) {
                    when (d.step) {
                        1 -> WizardStep1(viewModel, group, d)
                        2 -> WizardStep2(viewModel, group, d)
                        3 -> WizardStep3(viewModel, group, d)
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStep1(
    viewModel: CricketViewModel,
    group: CricketGroup,
    draft: com.example.viewmodel.WizardDraft
) {
    var search by remember { mutableStateOf("") }
    val filteredPlayers = remember(group.players, search) {
        if (search.isBlank()) group.players
        else group.players.filter { it.name.contains(search.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        // Form meta
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = draft.title,
                onValueChange = { viewModel.updateWizardMeta(it, draft.format, draft.date) },
                label = { Text("Match Title *", fontSize = 11.sp) },
                placeholder = { Text("e.g. Sunday Final Clash") },
                singleLine = true,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("wizard_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = BorderDark,
                    unfocusedContainerColor = InputBg,
                    focusedContainerColor = InputBg
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Format dropdown
            var formatExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(0.7f)) {
                OutlinedTextField(
                    value = draft.format,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Format", fontSize = 11.sp) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formatExpanded = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = BorderDark,
                        unfocusedContainerColor = InputBg,
                        focusedContainerColor = InputBg
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                DropdownMenu(
                    expanded = formatExpanded,
                    onDismissRequest = { formatExpanded = false },
                    modifier = Modifier.background(CardNavy)
                ) {
                    CricketEngine.FORMATS.forEach { fmt ->
                        DropdownMenuItem(
                            text = { Text(fmt, fontFamily = OrbitronFontFamily, color = TextPrimary) },
                            onClick = {
                                viewModel.updateWizardMeta(draft.title, fmt, draft.date)
                                formatExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = draft.date,
            onValueChange = { viewModel.updateWizardMeta(draft.title, draft.format, it) },
            label = { Text("Match Date (YYYY-MM-DD) *", fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wizard_date_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = BorderDark,
                unfocusedContainerColor = InputBg,
                focusedContainerColor = InputBg
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Selective roster header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SELECT PLAYERS (${draft.selectedPlayerIds.size}/${group.players.size})",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = ElectricBlue
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = { viewModel.selectAllWizardPlayers(group.players.map { it.id }) },
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text("Select All", fontSize = 11.sp, color = TrophyGold)
                }
                TextButton(
                    onClick = { viewModel.clearWizardPlayers() },
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text("Clear", fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        // Roster selector list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredPlayers, key = { it.id }) { p ->
                val isSelected = draft.selectedPlayerIds.contains(p.id)
                Surface(
                    color = if (isSelected) ElectricBlue.copy(alpha = 0.12f) else InputBg,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) ElectricBlue else BorderDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleWizardPlayer(p.id) }
                        .testTag("wizard_select_player_${p.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { viewModel.toggleWizardPlayer(p.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ElectricBlue,
                                    uncheckedColor = BorderDark
                                )
                            )
                            PlayerAvatar(player = p, size = 30.dp)
                            Text(
                                text = p.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = p.role,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Bottom continue button
        Button(
            onClick = { viewModel.wizardGoToStep2() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("wizard_step1_next_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "CONTINUE TO PLAYER STATS →",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF04111F)
            )
        }
    }
}

@Composable
fun WizardStep2(
    viewModel: CricketViewModel,
    group: CricketGroup,
    draft: com.example.viewmodel.WizardDraft
) {
    val currentPid = draft.selectedPlayerIds.getOrNull(draft.currentPlayerIndex) ?: return
    val player = group.players.find { it.id == currentPid } ?: return
    val form = draft.entries[currentPid] ?: WizardPlayerForm()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Player Jump Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            draft.selectedPlayerIds.forEachIndexed { idx, pid ->
                val p = group.players.find { it.id == pid }
                val isActive = idx == draft.currentPlayerIndex
                val pForm = draft.entries[pid]
                val isFilled = pForm != null && (pForm.batted || pForm.overs.isNotBlank() || pForm.catches.isNotBlank())

                Surface(
                    color = if (isActive) TrophyGold.copy(alpha = 0.2f) else if (isFilled) PosGreen.copy(alpha = 0.12f) else InputBg,
                    shape = RoundedCornerShape(999.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) TrophyGold else if (isFilled) PosGreen else BorderDark
                    ),
                    modifier = Modifier.clickable { viewModel.wizardSetCurrentPlayerIndex(idx) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = p?.name ?: "P${idx + 1}",
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) TrophyGold else TextPrimary
                        )
                        if (isFilled) {
                            Text(text = "✓", fontSize = 10.sp, color = PosGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Player Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlayerAvatar(player = player, size = 44.dp)
            Column {
                Text(
                    text = player.name,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${player.role} · Player ${draft.currentPlayerIndex + 1} of ${draft.selectedPlayerIds.size}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable Input Fields
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Batting Section
            Card(
                colors = CardDefaults.cardColors(containerColor = InputBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🏏 BATTING",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ElectricBlue
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = form.batted,
                                onCheckedChange = {
                                    viewModel.updateWizardPlayerEntry(currentPid, form.copy(batted = it))
                                },
                                colors = CheckboxDefaults.colors(checkedColor = ElectricBlue)
                            )
                            Text(text = "Batted", fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    if (form.batted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = form.runs,
                                onValueChange = {
                                    viewModel.updateWizardPlayerEntry(currentPid, form.copy(runs = it, batted = true))
                                },
                                label = { Text("Runs", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = form.balls,
                                onValueChange = {
                                    viewModel.updateWizardPlayerEntry(currentPid, form.copy(balls = it, batted = true))
                                },
                                label = { Text("Balls", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = form.fours,
                                onValueChange = {
                                    viewModel.updateWizardPlayerEntry(currentPid, form.copy(fours = it, batted = true))
                                },
                                label = { Text("4s", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = form.sixes,
                                onValueChange = {
                                    viewModel.updateWizardPlayerEntry(currentPid, form.copy(sixes = it, batted = true))
                                },
                                label = { Text("6s", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Switch(
                                checked = form.notOut,
                                onCheckedChange = {
                                    viewModel.updateWizardPlayerEntry(currentPid, form.copy(notOut = it, batted = true))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = TrophyGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (form.notOut) "Not Out (*)" else "Dismissed / Out",
                                fontSize = 11.5.sp,
                                color = if (form.notOut) TrophyGold else TextMuted
                            )
                        }
                    }
                }
            }

            // Bowling Section
            Card(
                colors = CardDefaults.cardColors(containerColor = InputBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🎯 BOWLING",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TrophyGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = form.overs,
                            onValueChange = {
                                viewModel.updateWizardPlayerEntry(currentPid, form.copy(overs = it))
                            },
                            label = { Text("Overs (e.g. 4.2)", fontSize = 10.sp) },
                            placeholder = { Text("4.2") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = form.conceded,
                            onValueChange = {
                                viewModel.updateWizardPlayerEntry(currentPid, form.copy(conceded = it))
                            },
                            label = { Text("Runs Conceded", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = form.wickets,
                            onValueChange = {
                                viewModel.updateWizardPlayerEntry(currentPid, form.copy(wickets = it))
                            },
                            label = { Text("Wickets", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = form.maidens,
                            onValueChange = {
                                viewModel.updateWizardPlayerEntry(currentPid, form.copy(maidens = it))
                            },
                            label = { Text("Maidens", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Live Overs Validation Hint
                    val legal = CricketEngine.parseOvers(form.overs)
                    if (form.overs.isNotBlank()) {
                        if (legal == null) {
                            Text(
                                text = "⚠️ Invalid notation: balls digit must be 0–5 (e.g. 4.2 ✓, 4.7 ✗)",
                                fontSize = 11.sp,
                                color = NegRed,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = "= ${legal / 6} overs ${legal % 6} balls ($legal legal balls)",
                                fontSize = 11.sp,
                                color = PosGreen,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Fielding & Fair Play Section
            Card(
                colors = CardDefaults.cardColors(containerColor = InputBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🧤 FIELDING & FAIR PLAY",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = form.catches,
                            onValueChange = {
                                viewModel.updateWizardPlayerEntry(currentPid, form.copy(catches = it))
                            },
                            label = { Text("Catches", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = form.runouts,
                            onValueChange = {
                                viewModel.updateWizardPlayerEntry(currentPid, form.copy(runouts = it))
                            },
                            label = { Text("Run Outs", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = form.stumpings,
                            onValueChange = {
                                viewModel.updateWizardPlayerEntry(currentPid, form.copy(stumpings = it))
                            },
                            label = { Text("Stumpings", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Fair Play Points: ${if (form.fair > 0) "+${form.fair}" else "${form.fair}"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (form.fair > 0) PosGreen else if (form.fair < 0) NegRed else TextMuted
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (-5..5 step 2).forEach { pts ->
                                Surface(
                                    color = if (form.fair == pts) ElectricBlue else Color(0xFF14243B),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        viewModel.updateWizardPlayerEntry(currentPid, form.copy(fair = pts))
                                    }
                                ) {
                                    Text(
                                        text = if (pts > 0) "+$pts" else "$pts",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (form.fair == pts) Color(0xFF04111F) else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.wizardGoBack() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("← Back")
            }

            Button(
                onClick = {
                    if (draft.currentPlayerIndex < draft.selectedPlayerIds.size - 1) {
                        viewModel.wizardSetCurrentPlayerIndex(draft.currentPlayerIndex + 1)
                    } else {
                        viewModel.wizardGoToStep3()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("wizard_step2_next_btn")
            ) {
                Text(
                    text = if (draft.currentPlayerIndex < draft.selectedPlayerIds.size - 1) "Next Player →" else "Review Summary →",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF04111F)
                )
            }
        }
    }
}

@Composable
fun WizardStep3(
    viewModel: CricketViewModel,
    group: CricketGroup,
    draft: com.example.viewmodel.WizardDraft
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = "MATCH SUMMARY REVIEW",
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TrophyGold
        )
        Text(
            text = "${draft.title} · ${draft.format} · ${draft.date} (${draft.selectedPlayerIds.size} players)",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, BorderDark, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
        ) {
            items(draft.selectedPlayerIds) { pid ->
                val p = group.players.find { it.id == pid }
                val f = draft.entries[pid] ?: WizardPlayerForm()

                Surface(
                    color = CardNavy,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (p != null) PlayerAvatar(player = p, size = 26.dp)
                            Text(
                                text = p?.name ?: "Unknown",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (f.batted) "${f.runs}${if (f.notOut) "*" else ""} (${f.balls}b, ${f.fours}x4, ${f.sixes}x6)" else "Did not bat",
                                fontSize = 11.sp,
                                color = if (f.batted) ElectricBlue else TextMuted
                            )
                            if (f.overs.isNotBlank()) {
                                Text(
                                    text = "${f.wickets}w / ${f.conceded}r (${f.overs} ov)",
                                    fontSize = 10.5.sp,
                                    color = TrophyGold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.wizardGoBack() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("← Edit Stats")
            }

            Button(
                onClick = { viewModel.wizardCommit() },
                colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("wizard_commit_btn")
            ) {
                Text(
                    text = "COMMIT & RECALCULATE ★",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color(0xFF10151E)
                )
            }
        }
    }
}
