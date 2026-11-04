package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.CricketEngine
import com.example.model.CricketGroup
import com.example.model.Player
import com.example.ui.components.PlayerAvatar
import com.example.ui.theme.*
import com.example.viewmodel.ActiveModal
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.launch

// 1. Player Add / Edit Modal
@Composable
fun PlayerFormModal(
    player: Player?,
    group: CricketGroup,
    viewModel: CricketViewModel
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(player?.name ?: "") }
    var role by remember { mutableStateOf(player?.role ?: "Batter") }
    var jersey by remember { mutableStateOf(player?.jersey ?: "") }
    var photo by remember { mutableStateOf(player?.photo ?: "") }

    val roles = listOf("Batter", "Bowler", "All-rounder", "Wicketkeeper")

    Dialog(onDismissRequest = { viewModel.closeModal() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("player_form_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (player != null) "EDIT PLAYER" else "ADD NEW PLAYER",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TrophyGold
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Player Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Player Role", fontSize = 11.sp, color = TextMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.forEach { r ->
                        val isSelected = role == r
                        Surface(
                            color = if (isSelected) ElectricBlue else InputBg,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ElectricBlue else BorderDark),
                            modifier = Modifier.clickable { role = r }
                        ) {
                            Text(
                                text = r,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF04111F) else TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = jersey,
                    onValueChange = { jersey = it.take(3) },
                    label = { Text("Jersey Number (optional)") },
                    placeholder = { Text("e.g. 18") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = photo,
                    onValueChange = { photo = it },
                    label = { Text("Photo URL (optional)") },
                    placeholder = { Text("https://example.com/photo.jpg") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.closeModal() }) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                viewModel.emitToast("Please enter player name")
                                return@Button
                            }
                            scope.launch {
                                if (player != null) {
                                    viewModel.repository.updatePlayer(player.id, name, role, jersey, photo)
                                    viewModel.emitToast("Player updated")
                                } else {
                                    val success = viewModel.repository.addPlayer(name, role, jersey, photo)
                                    if (success) viewModel.emitToast("Player added")
                                    else viewModel.emitToast("Maximum 51 players reached")
                                }
                                viewModel.closeModal()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("player_save_btn")
                    ) {
                        Text(
                            text = if (player != null) "Save Changes" else "Add Player",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF04111F)
                        )
                    }
                }
            }
        }
    }
}

// 2. Player Profile Modal
@Composable
fun PlayerProfileModal(
    playerId: String,
    group: CricketGroup,
    viewModel: CricketViewModel
) {
    val player = group.players.find { it.id == playerId } ?: return
    val aggs = remember(group.matches) {
        CricketEngine.buildAggsByFormat(group.matches)
    }

    val playerMatches = remember(group.matches, playerId) {
        group.matches.filter { m -> m.entries.any { it.pid == playerId } }
            .sortedByDescending { it.date }
    }

    Dialog(
        onDismissRequest = { viewModel.closeModal() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("player_profile_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1E33))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlayerAvatar(player = player, size = 48.dp, borderColor = TrophyGold)
                        Column {
                            Text(
                                text = player.name,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${player.role}${if (player.jersey.isNotBlank()) " · #${player.jersey}" else ""}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.closeModal() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Scrollable Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Career Stats for each format
                    listOf("T20", "ODI", "TEST", "ALL").forEach { fmt ->
                        item {
                            val a = CricketEngine.getPlayerAggForFormat(aggs, playerId, fmt)
                            val rating = CricketEngine.calculatePlayerRating(aggs, playerId, fmt, "overall")
                            val label = if (fmt == "ALL") "ALL-FORMAT CAREER" else "$fmt CAREER"

                            Card(
                                colors = CardDefaults.cardColors(containerColor = InputBg),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = OrbitronFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TrophyGold
                                        )
                                        if (rating != null) {
                                            Text(
                                                text = "%.1f pts".format(rating),
                                                fontFamily = OrbitronFontFamily,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = ElectricBlue
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (a.matches == 0) {
                                        Text(
                                            text = "No matches played in this format",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    } else {
                                        // Quick metrics grid
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = "Matches: ${a.matches}", fontSize = 11.sp, color = TextPrimary)
                                                Text(text = "Innings: ${a.inns}", fontSize = 11.sp, color = TextMuted)
                                                Text(text = "Runs: ${a.runs}", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                                                Text(text = "High Score: ${a.hs}${if (a.hsNO) "*" else ""}", fontSize = 11.sp, color = TextMuted)
                                            }
                                            Column {
                                                Text(text = "Bat Avg: ${CricketEngine.batAvg(a)?.let { "%.2f".format(it) } ?: "N/A"}", fontSize = 11.sp, color = TextPrimary)
                                                Text(text = "Strike Rate: ${CricketEngine.strikeRate(a)?.let { "%.1f".format(it) } ?: "N/A"}", fontSize = 11.sp, color = TextMuted)
                                                Text(text = "50s / 100s: ${a.fifties} / ${a.hundreds}", fontSize = 11.sp, color = TextMuted)
                                                Text(text = "4s / 6s: ${a.fours} / ${a.sixes}", fontSize = 11.sp, color = TextMuted)
                                            }
                                            Column {
                                                Text(text = "Wickets: ${a.wickets}", fontSize = 11.sp, color = TrophyGold, fontWeight = FontWeight.Bold)
                                                Text(text = "Overs: ${CricketEngine.oversStr(a.legal)}", fontSize = 11.sp, color = TextMuted)
                                                Text(text = "Economy: ${CricketEngine.economy(a)?.let { "%.2f".format(it) } ?: "N/A"}", fontSize = 11.sp, color = TextMuted)
                                                Text(text = "Fair Play: ${if (a.fair > 0) "+${a.fair}" else "${a.fair}"}", fontSize = 11.sp, color = PosGreen)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Match History Table
                    item {
                        Text(
                            text = "MATCH LOG (${playerMatches.size})",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ElectricBlue
                        )
                    }

                    items(playerMatches, key = { it.id }) { m ->
                        val entry = m.entries.find { it.pid == playerId } ?: return@items
                        Surface(
                            color = InputBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${m.format} · ${m.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                    Text(text = m.date, fontSize = 10.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (entry.bat.batted) "🏏 ${entry.bat.runs}${if (entry.bat.notOut) "*" else ""} (${entry.bat.balls}b)" else "Did not bat",
                                        fontSize = 11.sp,
                                        color = if (entry.bat.batted) ElectricBlue else TextMuted
                                    )
                                    if (entry.bowl.legal > 0) {
                                        Text(
                                            text = "🎯 ${entry.bowl.wickets}/${entry.bowl.conceded} (${CricketEngine.oversStr(entry.bowl.legal)} ov)",
                                            fontSize = 11.sp,
                                            color = TrophyGold
                                        )
                                    }
                                    Text(
                                        text = "Fair: ${if (entry.fair > 0) "+${entry.fair}" else "${entry.fair}"}",
                                        fontSize = 10.sp,
                                        color = if (entry.fair > 0) PosGreen else TextMuted
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

// 3. Match Detail Modal
@Composable
fun MatchDetailModal(
    matchId: String,
    group: CricketGroup,
    viewModel: CricketViewModel
) {
    val match = group.matches.find { it.id == matchId } ?: return

    Dialog(
        onDismissRequest = { viewModel.closeModal() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f)
                .testTag("match_detail_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1E33))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${match.format} · ${match.title}",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TrophyGold
                        )
                        Text(text = match.date, fontSize = 11.sp, color = TextMuted)
                    }

                    IconButton(onClick = { viewModel.closeModal() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(match.entries) { e ->
                        val p = group.players.find { it.id == e.pid }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = InputBg),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "Fair: ${if (e.fair > 0) "+${e.fair}" else "${e.fair}"}",
                                        fontSize = 11.sp,
                                        color = if (e.fair > 0) PosGreen else TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (e.bat.batted) "Bat: ${e.bat.runs}${if (e.bat.notOut) "*" else ""} (${e.bat.balls}b, ${e.bat.fours}x4, ${e.bat.sixes}x6)" else "Did not bat",
                                        fontSize = 11.sp,
                                        color = if (e.bat.batted) ElectricBlue else TextMuted
                                    )
                                    if (e.bowl.legal > 0) {
                                        Text(
                                            text = "Bowl: ${e.bowl.wickets}/${e.bowl.conceded} (${CricketEngine.oversStr(e.bowl.legal)} ov, ${e.bowl.maidens} M)",
                                            fontSize = 11.sp,
                                            color = TrophyGold
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
}

// 4. Group Management Modal
@Composable
fun GroupManageModal(
    viewModel: CricketViewModel
) {
    val db by viewModel.dbFlow.collectAsState()
    val scope = rememberCoroutineScope()
    var newGroupName by remember { mutableStateOf("") }
    var newSeason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { viewModel.closeModal() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("group_manage_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TOURNAMENT GROUPS",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TrophyGold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Create group form
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it.take(40) },
                    label = { Text("New Group / Tournament Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newSeason,
                    onValueChange = { newSeason = it.take(30) },
                    label = { Text("Season (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            scope.launch {
                                viewModel.repository.createGroup(newGroupName, newSeason)
                                viewModel.emitToast("Group '$newGroupName' created")
                                newGroupName = ""
                                newSeason = ""
                            }
                        }
                    },
                    enabled = newGroupName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("＋ Create Group", color = Color(0xFF04111F), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Existing Groups (${db.groups.size})", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(db.groups) { g ->
                        val isActive = g.id == db.settings.activeGroupId
                        Surface(
                            color = if (isActive) ElectricBlue.copy(alpha = 0.15f) else InputBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) ElectricBlue else BorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = g.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text(text = "${g.players.size} players · ${g.matches.size} matches", fontSize = 10.sp, color = TextMuted)
                                }

                                if (isActive) {
                                    Text(text = "ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                viewModel.repository.switchGroup(g.id)
                                                viewModel.emitToast("Switched to ${g.name}")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Switch", fontSize = 11.sp, color = Color(0xFF10151E))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.closeModal() }) {
                        Text("Done", color = ElectricBlue)
                    }
                }
            }
        }
    }
}

// 5. Admin PIN Modal
@Composable
fun AdminModal(
    viewModel: CricketViewModel
) {
    val db by viewModel.dbFlow.collectAsState()
    val isUnlocked by viewModel.adminUnlocked.collectAsState()
    val scope = rememberCoroutineScope()
    var pinInput by remember { mutableStateOf("") }
    val hasPin = db.settings.pinHash != null

    Dialog(onDismissRequest = { viewModel.closeModal() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_pin_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ADMIN CONTROL PANEL",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TrophyGold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (!hasPin) "No 4-digit PIN set. Set one to protect match and player records."
                    else if (isUnlocked) "Admin is currently UNLOCKED."
                    else "Enter 4-digit PIN to unlock admin privileges.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                    label = { Text(if (!hasPin) "Set 4-Digit PIN" else "4-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (hasPin && isUnlocked) {
                        Button(
                            onClick = {
                                viewModel.repository.lockAdmin()
                                viewModel.emitToast("Admin locked")
                                viewModel.closeModal()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = InputBg)
                        ) {
                            Text("Lock Now", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.repository.removePin()
                                    viewModel.emitToast("PIN removed")
                                    viewModel.closeModal()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NegRed)
                        ) {
                            Text("Remove PIN", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (pinInput.length != 4) {
                                    viewModel.emitToast("PIN must be exactly 4 digits")
                                    return@Button
                                }
                                if (!hasPin) {
                                    scope.launch {
                                        viewModel.repository.setPin(pinInput)
                                        viewModel.emitToast("PIN set successfully")
                                        viewModel.closeModal()
                                    }
                                } else {
                                    val valid = viewModel.repository.verifyPin(pinInput)
                                    if (valid) {
                                        viewModel.emitToast("Admin unlocked!")
                                        viewModel.closeModal()
                                    } else {
                                        viewModel.emitToast("Incorrect PIN")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (!hasPin) "Set PIN" else "Unlock", color = Color(0xFF04111F), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 6. Backup & Restore Modal
@Composable
fun BackupRestoreModal(
    viewModel: CricketViewModel
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importJsonText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { viewModel.closeModal() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backup_restore_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "BACKUP & RESTORE",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = TrophyGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Export tournament JSON to share or paste a backup JSON to restore data.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val json = viewModel.repository.exportJsonString()
                            clipboard.setText(AnnotatedString(json))
                            viewModel.emitToast("Backup JSON copied to clipboard!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy JSON", color = Color(0xFF04111F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val json = viewModel.repository.exportJsonString()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, json)
                                type = "application/json"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Tournament Backup JSON"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Share File", color = Color(0xFF10151E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = importJsonText,
                    onValueChange = { importJsonText = it },
                    label = { Text("Paste Backup JSON to Restore") },
                    placeholder = { Text("{\"app\": \"CRICKET RANKING PRO\"...}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (importJsonText.isBlank()) {
                            viewModel.emitToast("Please paste backup JSON")
                            return@Button
                        }
                        scope.launch {
                            val res = viewModel.repository.importJsonString(importJsonText)
                            if (res.isSuccess) {
                                viewModel.emitToast("Restored ${res.getOrNull()} groups successfully!")
                                viewModel.closeModal()
                            } else {
                                viewModel.emitToast("Restore failed: ${res.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Restore from Pasted JSON", color = Color(0xFF04111F), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { viewModel.closeModal() }, modifier = Modifier.align(Alignment.End)) {
                    Text("Close", color = TextMuted)
                }
            }
        }
    }
}

// 7. Share Modal
@Composable
fun ShareModal(
    group: CricketGroup,
    viewModel: CricketViewModel
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val selectedFormat by viewModel.selectedFormat.collectAsState()

    val rankingResult = remember(group, selectedFormat) {
        CricketEngine.buildRanking(selectedFormat, "overall", group.matches, group.players)
    }

    val shareText = remember(group, selectedFormat, rankingResult) {
        val top3 = rankingResult.ranked.take(3)
        val medals = listOf("🥇", "🥈", "🥉")
        buildString {
            append("🏏 CRICKET RANKING PRO\n")
            append("${group.name}${if (group.season.isNotBlank()) " · ${group.season}" else ""}\n")
            append("${if (selectedFormat == "ALL") "ALL-FORMAT" else selectedFormat} RANKINGS\n\n")
            top3.forEachIndexed { idx, r ->
                append("${medals.getOrElse(idx) { "#${idx + 1}" }} ${r.player.name} — %.1f pts | ${r.agg.runs} runs, ${r.agg.wickets} wkts\n".format(r.rating))
            }
            append("\nPowered by Customize AI")
        }
    }

    Dialog(onDismissRequest = { viewModel.closeModal() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("share_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CRICKET RANKING PRO",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TrophyGold
                )
                Text(
                    text = group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = InputBg,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = shareText,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboard.setText(AnnotatedString(shareText))
                            viewModel.emitToast("Ranking copied to clipboard!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InputBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy", color = TextPrimary)
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Rankings"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Share", color = Color(0xFF04111F), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 8. How Ratings Work Modal
@Composable
fun RatingInfoModal(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rating_info_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = CardNavy,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "HOW RATINGS WORK",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = TrophyGold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Every rating is 0–100, unified and derived from official career match performances:\n\n" +
                            "• Batting (45%): Batting average 35%, Strike Rate 25%, Run volume 25%, 50s/100s milestones 15%.\n\n" +
                            "• Bowling (40%): Wickets taken 35%, Economy rate 30%, Bowling average 20%, Bowling strike rate 15%.\n\n" +
                            "• Fielding (10%): Catches, run-outs, and stumpings per match.\n\n" +
                            "• Fair Play (5%): Average fair-play points (-5 to +5) normalized to 0–100.\n\n" +
                            "• Unpunished Specialization: Pure batters are not penalized for not bowling.\n\n" +
                            "• ALL-FORMAT: Averages only formats the player has actually played (unplayed formats are never counted as zero).\n\n" +
                            "• Real-Time Trend (▲/▼): Automatically reflects rank movements compared to the previous match commit snapshot.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Got It", color = Color(0xFF04111F), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
