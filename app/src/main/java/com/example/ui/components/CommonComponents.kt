package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CareerAgg
import com.example.model.Player
import com.example.model.PlayerRankItem
import com.example.model.Trend
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun CricketLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = ElectricBlue.copy(alpha = glowAlpha),
                spotColor = TrophyGold.copy(alpha = glowAlpha)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF14305A),
                        Color(0xFF0C1626)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricBlue, TrophyGold)
                ),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CR",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.42).sp,
                color = ElectricBlue
            )
        }
    }
}

@Composable
fun PlayerAvatar(
    player: Player,
    size: Dp = 44.dp,
    borderColor: Color = CardNavyBorder,
    borderWidth: Dp = 1.5.dp,
    modifier: Modifier = Modifier
) {
    val initials = remember(player.name) {
        val parts = player.name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (parts.isEmpty()) "?"
        else if (parts.size == 1) parts[0].take(2).uppercase()
        else (parts[0].take(1) + parts.last().take(1)).uppercase()
    }

    val hash = remember(player.name) {
        abs(player.name.hashCode())
    }

    val gradientColors = remember(hash) {
        val hue1 = (hash % 360).toFloat()
        val hue2 = ((hash + 50) % 360).toFloat()
        listOf(
            Color.hsl(hue1, 0.70f, 0.40f),
            Color.hsl(hue2, 0.75f, 0.25f)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(gradientColors))
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (player.photo.isNotBlank()) {
            AsyncImage(
                model = player.photo,
                contentDescription = player.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38).sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun TrendBadge(trend: Trend, modifier: Modifier = Modifier) {
    when (trend) {
        is Trend.Up -> {
            Surface(
                color = PosGreen.copy(alpha = 0.16f),
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PosGreen.copy(alpha = 0.5f)),
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "▲", color = PosGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${trend.delta}", color = PosGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        is Trend.Down -> {
            Surface(
                color = NegRed.copy(alpha = 0.16f),
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NegRed.copy(alpha = 0.5f)),
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "▼", color = NegRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${trend.delta}", color = NegRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        is Trend.New -> {
            Surface(
                color = TrophyGold.copy(alpha = 0.16f),
                shape = RoundedCornerShape(999.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrophyGold.copy(alpha = 0.5f)),
                modifier = modifier
            ) {
                Text(
                    text = "NEW",
                    color = TrophyGold,
                    fontSize = 10.sp,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Trend.Same, Trend.None -> {
            Text(
                text = "—",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

@Composable
fun PodiumSection(
    topThree: List<PlayerRankItem>,
    categoryLabel: String,
    onPlayerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val p1 = topThree.getOrNull(0)
    val p2 = topThree.getOrNull(1)
    val p3 = topThree.getOrNull(2)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place (Silver)
        PodiumCard(
            item = p2,
            rank = 2,
            title = "2ND",
            borderColor = SilverColor,
            avatarSize = 64.dp,
            cardHeight = 220.dp,
            categoryLabel = categoryLabel,
            modifier = Modifier.weight(1f),
            onPlayerClick = onPlayerClick
        )

        // 1st Place (Gold, elevated)
        PodiumCard(
            item = p1,
            rank = 1,
            title = "1ST",
            borderColor = TrophyGold,
            avatarSize = 78.dp,
            cardHeight = 250.dp,
            categoryLabel = categoryLabel,
            modifier = Modifier.weight(1.15f),
            onPlayerClick = onPlayerClick
        )

        // 3rd Place (Bronze)
        PodiumCard(
            item = p3,
            rank = 3,
            title = "3RD",
            borderColor = BronzeColor,
            avatarSize = 60.dp,
            cardHeight = 205.dp,
            categoryLabel = categoryLabel,
            modifier = Modifier.weight(1f),
            onPlayerClick = onPlayerClick
        )
    }
}

@Composable
fun PodiumCard(
    item: PlayerRankItem?,
    rank: Int,
    title: String,
    borderColor: Color,
    avatarSize: Dp,
    cardHeight: Dp,
    categoryLabel: String,
    modifier: Modifier = Modifier,
    onPlayerClick: (String) -> Unit
) {
    Card(
        modifier = modifier
            .height(cardHeight)
            .clickable(enabled = item != null) { item?.let { onPlayerClick(it.player.id) } }
            .testTag("podium_card_$rank"),
        colors = CardDefaults.cardColors(containerColor = CardNavy),
        border = androidx.compose.foundation.BorderStroke(
            width = if (rank == 1) 2.dp else 1.2.dp,
            color = borderColor.copy(alpha = if (rank == 1) 0.85f else 0.5f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontFamily = OrbitronFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Awaiting\nplayer",
                        color = TextMuted.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (rank == 1) "🥇" else if (rank == 2) "🥈" else "🥉",
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$title PLACE",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = borderColor,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    PlayerAvatar(
                        player = item.player,
                        size = avatarSize,
                        borderColor = borderColor,
                        borderWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.player.name,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = item.player.role + (if (item.player.jersey.isNotBlank()) " · #${item.player.jersey}" else ""),
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f".format(item.rating),
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = if (rank == 1) 22.sp else 18.sp,
                        color = if (rank == 1) TrophyGold else TextPrimary
                    )
                    Text(
                        text = "pts",
                        fontSize = 9.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${item.agg.runs}R",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricBlue
                        )
                        Text(
                            text = "${item.agg.wickets}W",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TrophyGold
                        )
                        Text(
                            text = "${item.agg.matches}M",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = InputBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                fontFamily = OrbitronFontFamily,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}
