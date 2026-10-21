package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameMode
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class GameCardItem(
    val mode: GameMode,
    val title: String,
    val tagline: String,
    val description: String,
    val sampleSms: String,
    val command: String,
    val difficulty: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun GameHubScreen(
    onLaunchGame: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val games = listOf(
        GameCardItem(
            mode = GameMode.TRIVIA,
            title = "Trivia Master",
            tagline = "General Knowledge & Pop Culture",
            description = "Answer 5 multiple-choice questions via SMS! Multipliers build up with each streak. Text HINT if you're stuck.",
            sampleSms = "[TRIVIA] What does SMS stand for? A) Short Message Service B) Simple Mobile System...",
            command = "START TRIVIA",
            difficulty = "Medium",
            icon = Icons.Default.Quiz,
            accentColor = NeonCyan
        ),
        GameCardItem(
            mode = GameMode.WORD_SCRAMBLE,
            title = "Word Scramble",
            tagline = "Anagram & Vocabulary Arena",
            description = "Unscramble 5-8 letter words within 3 attempts. Progressive clues reveal starting letters and categories.",
            sampleSms = "[SCRAMBLE] Unscramble 'IWTLIO'. Clue: Cloud communication API. 3 tries left!",
            command = "START SCRAMBLE",
            difficulty = "Tricky",
            icon = Icons.Default.Extension,
            accentColor = NeonLime
        ),
        GameCardItem(
            mode = GameMode.RIDDLE,
            title = "Riddle Realm",
            tagline = "Mind Bending Mysteries",
            description = "Clever riddles parsed with semantic and keyword matching. Test your lateral thinking without multiple choice crutches!",
            sampleSms = "[RIDDLE] I speak without a mouth and hear without ears... What am I?",
            command = "START RIDDLE",
            difficulty = "Hard",
            icon = Icons.Default.Psychology,
            accentColor = NeonPurple
        ),
        GameCardItem(
            mode = GameMode.MATH_BLITZ,
            title = "Math Blitz",
            tagline = "Rapid Mental Arithmetic",
            description = "Speed calculation challenges! Text the numeric answer as fast as possible to maximize streak bonuses.",
            sampleSms = "[MATH] Solve fast: 47 + 68 = ? Reply with your number!",
            command = "START MATH",
            difficulty = "Fast",
            icon = Icons.Default.Calculate,
            accentColor = NeonAmber
        ),
        GameCardItem(
            mode = GameMode.DUNGEON_RPG,
            title = "Twilio Dungeon Quest",
            tagline = "Branching Interactive SMS Adventure",
            description = "Text RPG story adventure! Reply with choice numbers (1, 2) to navigate dangerous halls, loot monsters, and escape with treasure.",
            sampleSms = "[QUEST] Heavy Oak door (1) or Glowing Crystal tunnel (2)? Reply 1 or 2.",
            command = "START RPG",
            difficulty = "Epic",
            icon = Icons.Default.Castle,
            accentColor = NeonCoral
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hub Top Bar
        Surface(
            color = DarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "🎮 SMS GAME CATALOG",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Text(
                    text = "Play by tapping below or texting the command to 224-GAME",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(games, key = { it.mode.name }) { game ->
                GameCatalogCard(
                    item = game,
                    onLaunch = { onLaunchGame(game.mode) }
                )
            }
        }
    }
}

@Composable
fun GameCatalogCard(
    item: GameCardItem,
    onLaunch: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("game_card_${item.mode.name}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.5.dp, item.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = item.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = item.tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, item.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.difficulty,
                        style = MaterialTheme.typography.labelSmall,
                        color = item.accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated SMS Bubble Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📨 SMS PREVIEW (160 GSM)",
                            style = MaterialTheme.typography.labelSmall,
                            color = item.accentColor
                        )
                        Text(
                            text = "Cmd: '${item.command}'",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.sampleSms,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onLaunch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("launch_button_${item.mode.name}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = item.accentColor,
                    contentColor = Color(0xFF0F172A)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Play in SMS Arena",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
