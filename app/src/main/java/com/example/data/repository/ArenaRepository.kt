package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.dao.CustomQuestionDao
import com.example.data.dao.GameDao
import com.example.data.dao.LeaderboardDao
import com.example.data.dao.SmsDao
import com.example.data.model.CustomQuestionEntity
import com.example.data.model.GameRecordEntity
import com.example.data.model.LeaderboardEntity
import com.example.data.model.SmsMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.ceil

class ArenaRepository(
    private val smsDao: SmsDao,
    private val gameDao: GameDao,
    private val leaderboardDao: LeaderboardDao,
    private val customQuestionDao: CustomQuestionDao,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sms_arena_prefs", Context.MODE_PRIVATE)

    // Reactive streams
    val allMessages: Flow<List<SmsMessageEntity>> = smsDao.getAllMessages()
    val allGameRecords: Flow<List<GameRecordEntity>> = gameDao.getAllRecords()
    val leaderboard: Flow<List<LeaderboardEntity>> = leaderboardDao.getTopLeaderboard()
    val customQuestions: Flow<List<CustomQuestionEntity>> = customQuestionDao.getAllQuestions()
    val totalScore: Flow<Int?> = gameDao.getTotalScore()
    val bestStreak: Flow<Int?> = gameDao.getBestStreak()

    // Config state
    private val _playerHandle = MutableStateFlow(prefs.getString("player_handle", "Gladiator_One") ?: "Gladiator_One")
    val playerHandle = _playerHandle.asStateFlow()

    private val _twilioSid = MutableStateFlow(prefs.getString("twilio_sid", "") ?: "")
    val twilioSid = _twilioSid.asStateFlow()

    private val _twilioAuth = MutableStateFlow(prefs.getString("twilio_auth", "") ?: "")
    val twilioAuth = _twilioAuth.asStateFlow()

    private val _twilioPhone = MutableStateFlow(prefs.getString("twilio_phone", "+18005554263") ?: "+18005554263")
    val twilioPhone = _twilioPhone.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val hapticEnabled = _hapticEnabled.asStateFlow()

    fun updatePlayerHandle(name: String) {
        val clean = name.trim().ifEmpty { "Gladiator_One" }
        prefs.edit().putString("player_handle", clean).apply()
        _playerHandle.value = clean
    }

    fun updateTwilioConfig(sid: String, auth: String, phone: String) {
        prefs.edit()
            .putString("twilio_sid", sid.trim())
            .putString("twilio_auth", auth.trim())
            .putString("twilio_phone", phone.trim())
            .apply()
        _twilioSid.value = sid.trim()
        _twilioAuth.value = auth.trim()
        _twilioPhone.value = phone.trim()
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
        _hapticEnabled.value = enabled
    }

    suspend fun saveSmsMessage(
        sender: String,
        phoneNumber: String,
        text: String,
        messageType: String = "CHAT"
    ): Long {
        val segmentCount = calculateSmsSegments(text)
        val entity = SmsMessageEntity(
            sender = sender,
            phoneNumber = phoneNumber,
            text = text,
            messageType = messageType,
            segmentCount = segmentCount
        )
        return smsDao.insertMessage(entity)
    }

    suspend fun clearChat() {
        smsDao.clearAllMessages()
    }

    suspend fun recordGameFinished(
        gameType: String,
        score: Int,
        questionsAnswered: Int,
        correctAnswers: Int,
        highestStreak: Int,
        durationSeconds: Long
    ) {
        val record = GameRecordEntity(
            gameType = gameType,
            score = score,
            questionsAnswered = questionsAnswered,
            correctAnswers = correctAnswers,
            highestStreak = highestStreak,
            durationSeconds = durationSeconds
        )
        gameDao.insertRecord(record)

        if (score > 0) {
            val badge = when {
                score >= 1200 -> "Grandmaster"
                score >= 800 -> "Gladiator"
                score >= 400 -> "Veteran"
                else -> "Rookie"
            }
            val entry = LeaderboardEntity(
                playerName = _playerHandle.value,
                playerPhone = "+1 (555) ***-YOU",
                gameType = gameType,
                score = score,
                streak = highestStreak,
                rankBadge = badge
            )
            leaderboardDao.insertLeaderboardEntry(entry)
        }
    }

    suspend fun addCustomQuestion(
        category: String,
        prompt: String,
        options: List<String>,
        correctAnswer: String,
        hint: String,
        points: Int = 100
    ): Long {
        val entity = CustomQuestionEntity(
            category = category,
            prompt = prompt,
            optionsList = options.joinToString("|"),
            correctAnswer = correctAnswer,
            hint = hint,
            points = points
        )
        return customQuestionDao.insertQuestion(entity)
    }

    suspend fun deleteCustomQuestion(id: Long) {
        customQuestionDao.deleteQuestion(id)
    }

    companion object {
        fun calculateSmsSegments(text: String): Int {
            val length = text.length
            return if (length <= 160) 1 else ceil(length / 153.0).toInt()
        }
    }
}
