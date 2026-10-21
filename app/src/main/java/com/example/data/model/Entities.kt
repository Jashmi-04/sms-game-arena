package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "ARENA_BOT"
    val phoneNumber: String, // "+1 (800) 224-GAME"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isDelivered: Boolean = true,
    val segmentCount: Int = 1,
    val messageType: String = "CHAT" // "CHAT", "QUESTION", "CORRECT", "INCORRECT", "HINT", "SCORE", "MENU"
)

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: String, // "TRIVIA", "WORD_SCRAMBLE", "RIDDLE", "MATH_BLITZ", "DUNGEON_RPG"
    val score: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val highestStreak: Int,
    val durationSeconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String,
    val playerPhone: String,
    val gameType: String, // "ALL", "TRIVIA", "WORD_SCRAMBLE", "RIDDLE", "MATH_BLITZ", "DUNGEON_RPG"
    val score: Int,
    val streak: Int,
    val rankBadge: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_questions")
data class CustomQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Trivia", "Riddle", "Word Scramble", "Math"
    val prompt: String,
    val optionsList: String, // Comma or pipe separated options
    val correctAnswer: String,
    val hint: String,
    val points: Int = 100,
    val createdTimestamp: Long = System.currentTimeMillis()
)
