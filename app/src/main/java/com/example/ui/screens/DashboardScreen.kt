package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CricketEngine
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ActiveModal
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val db by viewModel.dbFlow.collectAsState()
    val group = db.groups.find { it.id == db.settings.activeGroupId } ?: db.groups.firstOrNull()

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val rankSearch by viewModel.rankSearch.collectAsState()
    val isUnlocked by viewModel.adminUnlocked.collectAsState()
    val hasPin = db.settings.pinHash != null

    if (group == null) {
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgMidnight,
        topBar = {
            Surface(
                color = Color(0xDD080D18),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CricketLogoBadge(size = 38.dp)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CRICKET RANKING ",
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "PRO",
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = ElectricBlue
                                )
                            }
                            Text(
                                text = group.name,
                                fontSize = 11.sp,
                                color = TrophyGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.openModal(ActiveModal.GroupManage()) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(InputBg)
                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                .testTag("groups_header_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Groups",
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openModal(ActiveModal.BackupRestore) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(InputBg)
                                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                                .testTag("backup_header_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Backup",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.openModal(ActiveModal.AdminPin) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasPin && !isUnlocked) InputBg else TrophyGold.copy(alpha = 0.2f))
                                .border(1.dp, if (hasPin && !isUnlocked) BorderDark else TrophyGold, RoundedCornerShape(8.dp))
                                .testTag("admin_header_btn")
                        ) {
                            Icon(
                                imageVector = if (hasPin && !isUnlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Admin",
                                tint = if (hasPin && !isUnlocked) TextMuted else TrophyGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Navigation Tabs
            val navTabs = listOf(
                "rankings" to "Rankings",
                "players" to "Players",
                "matches" to "Matches",
                "compare" to "Compare",
                "awards" to "Awards"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                navTabs.forEach { (key, label) ->
                    val isActive = selectedTab == key
                    Button(
                        onClick = { viewModel.setTab(key) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0xFF14456B) else InputBg,
                            contentColor = if (isActive) Color.White else TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) ElectricBlue else BorderDark
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("tab_$key")
                    ) {
                        Text(
                            text = label,
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Format Selector Pills (Shown in Rankings, Compare, Awards)
            if (selectedTab in listOf("rankings", "compare", "awards")) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CricketEngine.ALL_FORMATS.forEach { fmt ->
                        val isActive = selectedFormat == fmt
                        val label = if (fmt == "ALL") "ALL-FORMAT" else fmt
                        Button(
                            onClick = { viewModel.setFormat(fmt) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) TrophyGold else Color(0x660E1A2C),
                                contentColor = if (isActive) Color(0xFF10151E) else TextMuted
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isActive) TrophyGold else BorderDark
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("format_$fmt")
                        ) {
                            Text(
                                text = label,
                                fontFamily = OrbitronFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                "rankings" -> RankingsTabContent(viewModel, group)
                "players" -> PlayersScreen(viewModel, group)
                "matches" -> MatchesScreen(viewModel, group)
                "compare" -> CompareScreen(viewModel, group)
                "awards" -> AwardsScreen(viewModel, group)
            }
        }
    }
}

@Composable
fun RankingsTabContent(
    viewModel: CricketViewModel,
    group: CricketGroup
) {
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val rankSearch by viewModel.rankSearch.collectAsState()

    val rankingResult = remember(group, selectedFormat, selectedCategory) {
        CricketEngine.buildRanking(
            format = selectedFormat,
            category = selectedCategory,
            matches = group.matches,
            players = group.players,
            prevRanks = group.prevRanks
        )
    }

    val filteredRanked = remember(rankingResult.ranked, rankSearch) {
        if (rankSearch.isBlank()) rankingResult.ranked
        else rankingResult.ranked.filter {
            it.player.name.contains(rankSearch.trim(), ignoreCase = true)
        }
    }

    val categoryLabel = CricketEngine.CATEGORIES.find { it.first == selectedCategory }?.second ?: "Rating"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rankings_list"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Welcome Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_hero_card"),
                colors = CardDefaults.cardColors(containerColor = CardNavy),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOURNAMENT ARENA",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = group.name,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    if (group.season.isNotBlank()) {
                        Surface(
                            color = TrophyGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TrophyGold.copy(alpha = 0.4f)),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = group.season,
                                fontSize = 11.sp,
                                color = TrophyGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.startMatchWizard(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("record_match_hero_btn")
                        ) {
                            Text(
                                text = "＋ Record Match",
                                color = Color(0xFF10151E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.openModal(ActiveModal.PlayerForm(null)) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_player_hero_btn")
                        ) {
                            Text(
                                text = "＋ Add Player",
                                color = Color(0xFF04111F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.openModal(ActiveModal.ShareDialog) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("share_hero_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Share", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Summary Stats Row
        item {
            val formatMatches = if (selectedFormat == "ALL") group.matches.size
            else group.matches.count { it.format == selectedFormat }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(label = "Players", value = "${group.players.size}/51")
                StatChip(label = "Matches", value = "${group.matches.size}")
                StatChip(
                    label = if (selectedFormat == "ALL") "All Formats" else "$selectedFormat Matches",
                    value = "$formatMatches"
                )
                StatChip(
                    label = "Active Format",
                    value = if (selectedFormat == "ALL") "OVERALL" else selectedFormat
                )
            }
        }

        // Category Selector & Heading
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ICC-STYLE PODIUM",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "TOP THREE PLAYERS",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                // Category selector dropdown
                var catExpanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        color = InputBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .clickable { catExpanded = true }
                            .testTag("category_dropdown_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = categoryLabel,
                                fontSize = 11.sp,
                                fontFamily = OrbitronFontFamily,
                                color = TrophyGold,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = TrophyGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        modifier = Modifier.background(CardNavy)
                    ) {
                        CricketEngine.CATEGORIES.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 12.sp,
                                        color = if (selectedCategory == key) TrophyGold else TextPrimary
                                    )
                                },
                                onClick = {
                                    viewModel.setCategory(key)
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Top-3 Podium
        item {
            PodiumSection(
                topThree = rankingResult.ranked.take(3),
                categoryLabel = categoryLabel,
                onPlayerClick = { pid -> viewModel.openModal(ActiveModal.PlayerProfile(pid)) }
            )
        }

        // Leaderboard Section Header & Search
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE LEADERBOARD",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "PLAYER RANKINGS",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                TextButton(
                    onClick = { viewModel.openModal(ActiveModal.RatingInfo) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("how_ratings_work_btn")
                ) {
                    Text(
                        text = "How ratings work",
                        fontSize = 11.sp,
                        color = ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = rankSearch,
                onValueChange = { viewModel.setRankSearch(it) },
                placeholder = { Text("Search player by name...", fontSize = 12.sp) },
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
                    .testTag("rank_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = BorderDark,
                    unfocusedContainerColor = InputBg,
                    focusedContainerColor = InputBg
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Detailed Table Header
        item {
            Surface(
                color = Color(0xFF0B1525),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "RK", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = TextMuted, modifier = Modifier.width(28.dp))
                    Text(text = "TREND", fontFamily = OrbitronFontFamily, fontSize = 9.sp, color = TextMuted, modifier = Modifier.width(46.dp))
                    Text(text = "PLAYER", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = TextMuted, modifier = Modifier.weight(1f))
                    Text(text = "PTS", fontFamily = OrbitronFontFamily, fontSize = 10.sp, color = TrophyGold, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
                    Text(text = "RUNS", fontFamily = OrbitronFontFamily, fontSize = 9.sp, color = TextMuted, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                    Text(text = "WKTS", fontFamily = OrbitronFontFamily, fontSize = 9.sp, color = TextMuted, modifier = Modifier.width(38.dp), textAlign = TextAlign.End)
                    Text(text = "FAIR", fontFamily = OrbitronFontFamily, fontSize = 9.sp, color = TextMuted, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }
        }

        // Leaderboard Rows
        if (filteredRanked.isEmpty()) {
            item {
                Surface(
                    color = CardNavy,
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (group.players.isEmpty()) "No players in roster yet"
                            else if (group.matches.isEmpty()) "No matches recorded yet"
                            else "No player matches search filter",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (group.players.isEmpty()) {
                            Button(
                                onClick = { viewModel.openModal(ActiveModal.PlayerForm(null)) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Text("＋ Add First Player", color = Color(0xFF04111F))
                            }
                        } else if (group.matches.isEmpty()) {
                            Button(
                                onClick = { viewModel.startMatchWizard(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = TrophyGold)
                            ) {
                                Text("＋ Record First Match", color = Color(0xFF10151E))
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredRanked, key = { it.player.id }) { item ->
                val isTop1 = item.rank == 1
                Surface(
                    color = if (isTop1) Color(0x22F5C451) else CardNavy,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isTop1) TrophyGold.copy(alpha = 0.5f) else BorderDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openModal(ActiveModal.PlayerProfile(item.player.id)) }
                        .testTag("leaderboard_row_${item.rank}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank
                        Text(
                            text = "#${item.rank}",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (isTop1) TrophyGold else TextPrimary,
                            modifier = Modifier.width(28.dp)
                        )

                        // Trend
                        Box(modifier = Modifier.width(46.dp)) {
                            TrendBadge(trend = item.trend)
                        }

                        // Player info
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlayerAvatar(
                                player = item.player,
                                size = 32.dp,
                                borderColor = if (isTop1) TrophyGold else CardNavyBorder
                            )
                            Column {
                                Text(
                                    text = item.player.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.player.role}${if (item.player.jersey.isNotBlank()) " #${item.player.jersey}" else ""}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Rating Points
                        Text(
                            text = "%.1f".format(item.rating),
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (isTop1) TrophyGold else ElectricBlue,
                            modifier = Modifier.width(44.dp),
                            textAlign = TextAlign.End
                        )

                        // Runs
                        Text(
                            text = "${item.agg.runs}",
                            fontSize = 11.5.sp,
                            color = TextPrimary,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.End
                        )

                        // Wickets
                        Text(
                            text = "${item.agg.wickets}",
                            fontSize = 11.5.sp,
                            color = TextPrimary,
                            modifier = Modifier.width(38.dp),
                            textAlign = TextAlign.End
                        )

                        // Fair Play
                        val fair = item.agg.fair
                        Text(
                            text = if (fair > 0) "+$fair" else "$fair",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (fair > 0) PosGreen else if (fair < 0) NegRed else TextMuted,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // Unranked Section
        if (rankingResult.unranked.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = InputBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "UNRANKED IN $selectedFormat · $categoryLabel",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 10.sp,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rankingResult.unranked.forEach { u ->
                                Surface(
                                    color = CardNavy,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                                    modifier = Modifier.clickable {
                                        viewModel.openModal(ActiveModal.PlayerProfile(u.player.id))
                                    }
                                ) {
                                    Text(
                                        text = "${u.player.name} (${u.reason})",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
