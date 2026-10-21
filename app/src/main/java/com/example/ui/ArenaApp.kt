package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GameMode
import com.example.ui.screens.ArenaChatScreen
import com.example.ui.screens.GameHubScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.QuestionLabScreen
import com.example.ui.screens.TwilioGatewayScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.ArenaViewModel

enum class ArenaScreen(val title: String, val icon: ImageVector) {
    ARENA("Arena", Icons.Default.Chat),
    HUB("Games", Icons.Default.SportsEsports),
    LEADERBOARD("Ranks", Icons.Default.EmojiEvents),
    LAB("Lab", Icons.Default.Science),
    GATEWAY("Gateway", Icons.Default.CellTower)
}

@Composable
fun ArenaApp(
    viewModel: ArenaViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(ArenaScreen.ARENA) }

    // Collect states
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val activeMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val currentScore by viewModel.currentScore.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val isBotTyping by viewModel.isBotTyping.collectAsStateWithLifecycle()
    val quickReplies by viewModel.quickReplies.collectAsStateWithLifecycle()

    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val gameHistory by viewModel.gameHistory.collectAsStateWithLifecycle()
    val customQuestions by viewModel.customQuestions.collectAsStateWithLifecycle()
    val playerHandle by viewModel.playerHandle.collectAsStateWithLifecycle()
    val totalScore by viewModel.totalScore.collectAsStateWithLifecycle()
    val bestStreak by viewModel.bestStreak.collectAsStateWithLifecycle()

    val twilioSid by viewModel.twilioSid.collectAsStateWithLifecycle()
    val twilioAuth by viewModel.twilioAuth.collectAsStateWithLifecycle()
    val twilioPhone by viewModel.twilioPhone.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticEnabled.collectAsStateWithLifecycle()
    val webhookTwiMl by viewModel.webhookTwiMl.collectAsStateWithLifecycle()
    val dispatchStatus by viewModel.twilioDispatchStatus.collectAsStateWithLifecycle()

    // BackHandler: Return to ARENA screen if on sub-screen
    BackHandler(enabled = currentScreen != ArenaScreen.ARENA) {
        currentScreen = ArenaScreen.ARENA
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("arena_bottom_navigation")
            ) {
                ArenaScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkSurface,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            ArenaScreen.ARENA -> {
                ArenaChatScreen(
                    messages = messages,
                    activeMode = activeMode,
                    currentScore = currentScore,
                    currentStreak = currentStreak,
                    isBotTyping = isBotTyping,
                    quickReplies = quickReplies,
                    onSendMessage = { text -> viewModel.sendUserMessage(text) },
                    onClearChat = { viewModel.clearChat() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ArenaScreen.HUB -> {
                GameHubScreen(
                    onLaunchGame = { mode ->
                        viewModel.startSpecificGame(mode)
                        currentScreen = ArenaScreen.ARENA
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ArenaScreen.LEADERBOARD -> {
                LeaderboardScreen(
                    leaderboardEntries = leaderboard,
                    matchHistory = gameHistory,
                    playerHandle = playerHandle,
                    totalScore = totalScore,
                    bestStreak = bestStreak,
                    onUpdateHandle = { viewModel.updatePlayerHandle(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ArenaScreen.LAB -> {
                QuestionLabScreen(
                    customQuestions = customQuestions,
                    onAddQuestion = { category, prompt, options, answer, hint ->
                        viewModel.addCustomQuestion(category, prompt, options, answer, hint)
                    },
                    onDeleteQuestion = { viewModel.deleteCustomQuestion(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            ArenaScreen.GATEWAY -> {
                TwilioGatewayScreen(
                    currentSid = twilioSid,
                    currentAuth = twilioAuth,
                    currentPhone = twilioPhone,
                    hapticEnabled = hapticEnabled,
                    lastTwiMl = webhookTwiMl,
                    dispatchStatus = dispatchStatus,
                    onSaveTwilioConfig = { sid, auth, phone ->
                        viewModel.updateTwilioConfig(sid, auth, phone)
                    },
                    onToggleHaptic = { viewModel.toggleHaptic(it) },
                    onSendTwilioSms = { toNumber, message ->
                        viewModel.sendRealTwilioSms(toNumber, message)
                    },
                    onClearDispatchStatus = { viewModel.clearTwilioDispatchStatus() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
