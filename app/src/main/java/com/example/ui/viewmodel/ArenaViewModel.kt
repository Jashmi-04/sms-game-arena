package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.CustomQuestionEntity
import com.example.data.model.GameRecordEntity
import com.example.data.model.LeaderboardEntity
import com.example.data.model.SmsMessageEntity
import com.example.data.repository.ArenaRepository
import com.example.game.GameMode
import com.example.game.SmsGameEngine
import com.example.gateway.TwilioGatewayService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArenaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ArenaRepository
    private val gameEngine = SmsGameEngine()
    private val twilioGateway = TwilioGatewayService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ArenaRepository(
            smsDao = db.smsDao(),
            gameDao = db.gameDao(),
            leaderboardDao = db.leaderboardDao(),
            customQuestionDao = db.customQuestionDao(),
            context = application
        )
    }

    val messages: StateFlow<List<SmsMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntity>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gameHistory: StateFlow<List<GameRecordEntity>> = repository.allGameRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customQuestions: StateFlow<List<CustomQuestionEntity>> = repository.customQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerHandle = repository.playerHandle
    val twilioSid = repository.twilioSid
    val twilioAuth = repository.twilioAuth
    val twilioPhone = repository.twilioPhone
    val hapticEnabled = repository.hapticEnabled

    val totalScore: StateFlow<Int> = repository.totalScore
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val bestStreak: StateFlow<Int> = repository.bestStreak
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Current Game State
    private val _currentMode = MutableStateFlow(GameMode.NONE)
    val currentMode: StateFlow<GameMode> = _currentMode.asStateFlow()

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _isBotTyping = MutableStateFlow(false)
    val isBotTyping: StateFlow<Boolean> = _isBotTyping.asStateFlow()

    private val _quickReplies = MutableStateFlow<List<String>>(listOf("START", "HELP", "SCORE", "TOP"))
    val quickReplies: StateFlow<List<String>> = _quickReplies.asStateFlow()

    private val _webhookTwiMl = MutableStateFlow<String?>(null)
    val webhookTwiMl: StateFlow<String?> = _webhookTwiMl.asStateFlow()

    private val _twilioDispatchStatus = MutableStateFlow<String?>(null)
    val twilioDispatchStatus: StateFlow<String?> = _twilioDispatchStatus.asStateFlow()

    fun sendUserMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            // Save user message to Room
            repository.saveSmsMessage(
                sender = "USER",
                phoneNumber = playerHandle.value,
                text = text,
                messageType = "USER_COMMAND"
            )

            // Simulate realistic SMS delivery network delay & bot typing indicator
            _isBotTyping.value = true
            delay(400)

            // Process via SMS Game Engine
            val output = gameEngine.processIncomingSms(text)

            // Sync state
            _currentMode.value = gameEngine.currentMode
            _currentScore.value = gameEngine.currentScore
            _currentStreak.value = gameEngine.currentStreak
            _quickReplies.value = output.quickReplies
            _webhookTwiMl.value = twilioGateway.generateTwilioResponseXml(output.botSmsText)

            delay(350)
            _isBotTyping.value = false

            // Save bot SMS reply to Room
            repository.saveSmsMessage(
                sender = "ARENA_BOT",
                phoneNumber = "224-GAME (Twilio Gateway)",
                text = output.botSmsText,
                messageType = output.messageType
            )

            triggerHaptic()

            // If game finished, record stats and update leaderboard
            if (output.isGameFinished) {
                repository.recordGameFinished(
                    gameType = _currentMode.value.name,
                    score = output.finalScore,
                    questionsAnswered = output.questionsAnswered,
                    correctAnswers = output.correctAnswers,
                    highestStreak = output.maxStreak,
                    durationSeconds = output.durationSeconds
                )
            }
        }
    }

    fun startSpecificGame(mode: GameMode) {
        sendUserMessage("START ${mode.shortCommand}")
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            // Reset to welcome prompt
            val welcomeSms = SmsMessageEntity(
                sender = "ARENA_BOT",
                phoneNumber = "224-GAME (Twilio Gateway)",
                text = "⚡ SMS GAME ARENA RESET! 🎮\nReply 'START' to pick a game or tap any game button below!",
                messageType = "MENU",
                segmentCount = 1
            )
            repository.saveSmsMessage(welcomeSms.sender, welcomeSms.phoneNumber, welcomeSms.text, welcomeSms.messageType)
            _quickReplies.value = listOf("START TRIVIA", "START SCRAMBLE", "START RIDDLE", "START MATH", "START RPG")
        }
    }

    fun updatePlayerHandle(name: String) {
        repository.updatePlayerHandle(name)
    }

    fun updateTwilioConfig(sid: String, auth: String, phone: String) {
        repository.updateTwilioConfig(sid, auth, phone)
    }

    fun toggleHaptic(enabled: Boolean) {
        repository.setHapticEnabled(enabled)
    }

    fun addCustomQuestion(
        category: String,
        prompt: String,
        options: List<String>,
        correctAnswer: String,
        hint: String
    ) {
        viewModelScope.launch {
            repository.addCustomQuestion(category, prompt, options, correctAnswer, hint)
        }
    }

    fun deleteCustomQuestion(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomQuestion(id)
        }
    }

    fun sendRealTwilioSms(toNumber: String, message: String) {
        viewModelScope.launch {
            _twilioDispatchStatus.value = "Sending SMS via Twilio API..."
            val result = twilioGateway.sendTwilioSms(
                accountSid = twilioSid.value,
                authToken = twilioAuth.value,
                fromNumber = twilioPhone.value,
                toNumber = toNumber,
                body = message
            )
            if (result.success) {
                _twilioDispatchStatus.value = "✅ SMS Sent successfully! (SID: ${result.messageSid})"
            } else {
                _twilioDispatchStatus.value = "❌ Twilio error: ${result.errorMessage}"
            }
        }
    }

    fun clearTwilioDispatchStatus() {
        _twilioDispatchStatus.value = null
    }

    private fun triggerHaptic() {
        if (!hapticEnabled.value) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(45)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibrator unavailable in container
        }
    }
}
