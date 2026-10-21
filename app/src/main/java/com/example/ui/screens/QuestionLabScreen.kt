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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomQuestionEntity
import com.example.data.repository.ArenaRepository
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonLime
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuestionLabScreen(
    customQuestions: List<CustomQuestionEntity>,
    onAddQuestion: (category: String, prompt: String, options: List<String>, answer: String, hint: String) -> Unit,
    onDeleteQuestion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Trivia", "Riddle", "Word Scramble", "Math")
    var selectedCategory by remember { mutableStateOf("Trivia") }
    var promptText by remember { mutableStateOf("") }
    var optionsText by remember { mutableStateOf("A) ..., B) ..., C) ..., D) ...") }
    var answerText by remember { mutableStateOf("") }
    var hintText by remember { mutableStateOf("") }

    // Live SMS Simulation String
    val simulatedSms = remember(selectedCategory, promptText, optionsText, hintText) {
        when (selectedCategory) {
            "Trivia" -> "[$selectedCategory] ${promptText.ifBlank { "Question..." }}\n${if (optionsText.isNotBlank()) optionsText else ""}\nReply A, B, C or D"
            "Riddle" -> "[$selectedCategory] \"${promptText.ifBlank { "Riddle..." }}\"\nReply with your answer or HINT"
            "Word Scramble" -> "[SCRAMBLE] Unscramble: '${promptText.ifBlank { "SCRAMBLED" }}'\nReply guess or HINT"
            else -> "[MATH] Solve: ${promptText.ifBlank { "12 * 8 = ?" }}\nReply numeric answer"
        }
    }

    val charLength = simulatedSms.length
    val segmentCount = ArenaRepository.calculateSmsSegments(simulatedSms)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = DarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SMS QUESTION LAB",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonCyan
                    )
                }
                Text(
                    text = "Craft challenges & preview 160-character GSM SMS formatting in real time",
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
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Create Challenge",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = Color(0xFF0F172A)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedCategory == cat,
                                        borderColor = if (selectedCategory == cat) NeonCyan else DarkBorder
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            label = { Text("Question / Riddle Prompt") },
                            modifier = Modifier.fillMaxWidth().testTag("lab_prompt_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        if (selectedCategory == "Trivia") {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = optionsText,
                                onValueChange = { optionsText = it },
                                label = { Text("Options (A, B, C, D)") },
                                modifier = Modifier.fillMaxWidth().testTag("lab_options_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = answerText,
                                onValueChange = { answerText = it },
                                label = { Text("Correct Answer") },
                                modifier = Modifier.weight(1f).testTag("lab_answer_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = hintText,
                                onValueChange = { hintText = it },
                                label = { Text("Hint (Optional)") },
                                modifier = Modifier.weight(1f).testTag("lab_hint_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live SMS Simulator Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (charLength > 160) NeonCoral else NeonCyan.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📱 TWILIO SMS LIVE PREVIEW",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                    Text(
                                        text = "$charLength/160 chars ($segmentCount segment${if (segmentCount > 1) "s" else ""})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (charLength > 160) NeonCoral else NeonLime
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = simulatedSms,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )

                                if (charLength > 160) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = NeonCoral,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Message exceeds single 160 GSM segment limit (will incur 2 SMS credits on Twilio)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonCoral
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (promptText.isNotBlank() && answerText.isNotBlank()) {
                                    onAddQuestion(
                                        selectedCategory,
                                        promptText,
                                        listOf(optionsText),
                                        answerText,
                                        hintText
                                    )
                                    promptText = ""
                                    answerText = ""
                                    hintText = ""
                                }
                            },
                            enabled = promptText.isNotBlank() && answerText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_question_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = Color(0xFF0F172A)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Arena Bank", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Saved Custom Questions
            item {
                Text(
                    text = "Saved Questions (${customQuestions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (customQuestions.isEmpty()) {
                item {
                    Text(
                        text = "No custom questions yet. Build your first SMS challenge above!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                items(customQuestions, key = { it.id }) { question ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${question.category}] ${question.prompt}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Answer: ${question.correctAnswer} • Hint: ${question.hint.ifBlank { "None" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonLime
                                )
                            }
                            IconButton(
                                onClick = { onDeleteQuestion(question.id) },
                                modifier = Modifier.testTag("delete_question_${question.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = NeonCoral
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
