package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CricketLogoBadge
import com.example.ui.theme.*

@Composable
fun FirstRunScreen(
    onCreateGroup: (name: String, season: String) -> Unit,
    onRestoreBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf("") }
    var seasonName by remember { mutableStateOf("Season 1 · 2026") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgMidnight)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("first_run_card"),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardNavyBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CricketLogoBadge(size = 64.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WELCOME TO THE ARENA",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 11.sp,
                    color = ElectricBlue,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "CREATE FIRST GROUP",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = TrophyGold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A group is a private tournament or gully cricket club with its own roster, matches, and ICC-style ranking table.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it.take(40) },
                    label = { Text("Group / Tournament Name *") },
                    placeholder = { Text("e.g. Cronix Premier League") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = ElectricBlue,
                        unfocusedContainerColor = InputBg,
                        focusedContainerColor = InputBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = seasonName,
                    onValueChange = { seasonName = it.take(30) },
                    label = { Text("Season Name (optional)") },
                    placeholder = { Text("e.g. Season 1 · 2026") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("season_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = BorderDark,
                        focusedLabelColor = ElectricBlue,
                        unfocusedContainerColor = InputBg,
                        focusedContainerColor = InputBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onCreateGroup(groupName.trim(), seasonName.trim())
                        }
                    },
                    enabled = groupName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("create_group_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "CREATE GROUP",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF04111F)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = TrophyGold.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrophyGold.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Storage note: Tournament records and rankings are securely saved to your local device. Use Backup in the menu to export or transfer your data anytime.",
                        color = TextPrimary.copy(alpha = 0.85f),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = onRestoreBackup,
                    modifier = Modifier.testTag("restore_backup_btn")
                ) {
                    Text(
                        text = "Restore from JSON backup file",
                        fontSize = 12.sp,
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}
