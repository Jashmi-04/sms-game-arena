package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ArenaRepository
import com.example.game.GameMode
import com.example.game.SmsGameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SMS Game Arena", appName)
    }

    @Test
    fun `verify sms game engine menu and start commands`() {
        val engine = SmsGameEngine()
        assertEquals(GameMode.NONE, engine.currentMode)

        // Test HELP / MENU command
        val helpOutput = engine.processIncomingSms("HELP")
        assertTrue(helpOutput.botSmsText.contains("COMMANDS"))
        assertEquals("MENU", helpOutput.messageType)

        // Test START TRIVIA
        val startOutput = engine.processIncomingSms("START TRIVIA")
        assertEquals(GameMode.TRIVIA, engine.currentMode)
        assertTrue(startOutput.botSmsText.contains("[TRIVIA Q1/"))
        assertEquals("QUESTION", startOutput.messageType)
    }

    @Test
    fun `verify word scramble and hint logic`() {
        val engine = SmsGameEngine()
        val startOutput = engine.processIncomingSms("START SCRAMBLE")
        assertEquals(GameMode.WORD_SCRAMBLE, engine.currentMode)
        assertTrue(startOutput.botSmsText.contains("[SCRAMBLE 1/"))

        // Request hint
        val hintOutput = engine.processIncomingSms("HINT")
        assertTrue(hintOutput.botSmsText.contains("HINT"))
        assertEquals("HINT", hintOutput.messageType)
    }

    @Test
    fun `verify sms segment calculation`() {
        val shortSms = "Hello SMS Arena"
        assertEquals(1, ArenaRepository.calculateSmsSegments(shortSms))

        val longSms = "A".repeat(161)
        assertEquals(2, ArenaRepository.calculateSmsSegments(longSms))
    }
}
