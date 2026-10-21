package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmsMessageEntity
import com.example.game.GameMode
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SmsGreenBubble
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArenaChatScreen(
    messages: List<SmsMessageEntity>,
    activeMode: GameMode,
    currentScore: Int,
    currentStreak: Int,
    isBotTyping: Boolean,
    quickReplies: List<String>,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive or bot starts typing
    LaunchedEffect(messages.size, isBotTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Arena Gateway Header & Live Stats Bar
        ArenaHeader(
            activeMode = activeMode,
            currentScore = currentScore,
            currentStreak = currentStreak,
            onClearChat = onClearChat
        )

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    SmsMessageBubble(message = message)
                }

                if (isBotTyping) {
                    item {
                        BotTypingIndicator()
                    }
                }
            }
        }

        // Quick Reply Action Chips
        if (quickReplies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickReplies.forEach { reply ->
                    AssistChip(
                        onClick = { onSendMessage(reply) },
                        label = {
                            Text(
                                text = reply,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (reply.startsWith("A") || reply.startsWith("B") || reply.startsWith("C") || reply.startsWith("D"))
                                    NeonCyan else NeonLime
                            )
                        },
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = DarkBorder
                        ),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("quick_chip_$reply")
                    )
                }
            }
        }

        // SMS Composer Box
        SmsInputBar(
            inputText = inputText,
            onInputChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    onSendMessage(inputText)
                    inputText = ""
                }
            }
        )
    }
}

@Composable
fun ArenaHeader(
    activeMode: GameMode,
    currentScore: Int,
    currentStreak: Int,
    onClearChat: () -> Unit
) {
    Surface(
        color = DarkSurface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS Bot",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "224-GAME",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Status Dot
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Online",
                                tint = NeonLime,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Twilio Bot",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonLime
                            )
                        }

                        Text(
                            text = if (activeMode != GameMode.NONE) "Active: ${activeMode.displayName}" else "Mode: Standby Lobby",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (activeMode != GameMode.NONE) NeonCyan else TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = TextMuted
                        )
                    }
                }
            }

            // Streak & Score Banner if game active or score > 0
            if (activeMode != GameMode.NONE || currentScore > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Score",
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SCORE: $currentScore PTS",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonAmber
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Streak",
                            tint = if (currentStreak > 0) NeonLime else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "STREAK: x$currentStreak 🔥",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (currentStreak > 0) NeonLime else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmsMessageBubble(message: SmsMessageEntity) {
    val isUser = message.sender == "USER"
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }
    val charCount = message.text.length
    val segmentText = if (charCount <= 160) "1/1 (${charCount}/160)" else "${message.segmentCount} segments (${charCount} chars)"

    val bubbleColor = when {
        isUser -> SmsGreenBubble
        message.messageType == "CORRECT" -> Color(0xFF0F382A)
        message.messageType == "INCORRECT" -> Color(0xFF38141D)
        message.messageType == "HINT" -> Color(0xFF33290E)
        message.messageType == "SCORE" -> Color(0xFF1B2A4A)
        else -> DarkSurfaceVariant
    }

    val borderColor = when {
        isUser -> Color(0xFF00A884)
        message.messageType == "CORRECT" -> NeonLime
        message.messageType == "INCORRECT" -> NeonCoral
        message.messageType == "HINT" -> NeonAmber
        message.messageType == "SCORE" -> NeonCyan
        else -> DarkBorder
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender and segment header label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isUser) "You (SMS)" else "224-GAME",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUser) NeonLime else NeonCyan
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• $segmentText",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        // Bubble Container
        Box(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 21.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✓✓",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BotTypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Twilio Bot is texting",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = alpha1))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = alpha2))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = alpha3))
        )
    }
}

@Composable
fun SmsInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val charCount = inputText.length
    val segmentCount = if (charCount <= 160) 1 else kotlin.math.ceil(charCount / 153.0).toInt()
    val isOverLimit = charCount > 160

    Surface(
        color = DarkSurface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = "Text START, A, B, HINT or answer...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sms_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() })
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSend,
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) NeonCyan else DarkSurfaceVariant)
                        .testTag("send_sms_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send SMS",
                        tint = if (inputText.isNotBlank()) Color(0xFF0F172A) else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 160-Character GSM Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SMS Arena Engine • Twilio v2010",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Text(
                    text = "$charCount/160 chars ($segmentCount segment${if (segmentCount > 1) "s" else ""})",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverLimit) NeonCoral else TextSecondary
                )
            }
        }
    }
}
