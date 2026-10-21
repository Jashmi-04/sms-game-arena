package com.example.game

import kotlin.random.Random

enum class GameMode(val displayName: String, val shortCommand: String) {
    NONE("Lobby", "LOBBY"),
    TRIVIA("Trivia Master", "TRIVIA"),
    WORD_SCRAMBLE("Word Scramble", "SCRAMBLE"),
    RIDDLE("Riddle Realm", "RIDDLE"),
    MATH_BLITZ("Math Blitz", "MATH"),
    DUNGEON_RPG("Twilio Dungeon Quest", "RPG")
}

data class TriviaQuestion(
    val question: String,
    val options: List<String>, // e.g. ["A) Python", "B) Java", "C) Kotlin", "D) C++"]
    val correctLetter: String, // "C"
    val correctText: String, // "Kotlin"
    val hint: String,
    val explanation: String
)

data class WordScrambleItem(
    val originalWord: String,
    val scrambledWord: String,
    val clue: String,
    val hint: String
)

data class RiddleItem(
    val riddle: String,
    val answers: List<String>, // Accepted variants
    val hint: String
)

data class RpgNode(
    val id: String,
    val text: String,
    val choices: List<Pair<String, String>>, // Pair("1", "NextNodeId"), Pair("2", "OtherNodeId")
    val isTerminal: Boolean = false,
    val scoreDelta: Int = 0
)

data class GameEngineOutput(
    val botSmsText: String,
    val messageType: String, // "QUESTION", "CORRECT", "INCORRECT", "HINT", "SCORE", "MENU", "SYSTEM"
    val isGameFinished: Boolean = false,
    val finalScore: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val maxStreak: Int = 0,
    val durationSeconds: Long = 0,
    val quickReplies: List<String> = emptyList()
)

class SmsGameEngine {

    var currentMode: GameMode = GameMode.NONE
        private set

    var currentScore: Int = 0
        private set

    var currentStreak: Int = 0
        private set

    var highestStreak: Int = 0
        private set

    var questionsAnswered: Int = 0
        private set

    var correctAnswers: Int = 0
        private set

    private var questionStartTime: Long = 0L
    private var gameStartTime: Long = 0L
    private var currentQuestionIndex: Int = 0
    private val totalQuestionsPerRound = 5

    // Active question states
    private var activeTriviaList: List<TriviaQuestion> = emptyList()
    private var activeScrambleList: List<WordScrambleItem> = emptyList()
    private var scrambleAttemptsLeft: Int = 3
    private var activeRiddleList: List<RiddleItem> = emptyList()
    private var currentRpgNode: RpgNode? = null

    // Comprehensive Trivia Bank
    private val triviaBank = listOf(
        TriviaQuestion(
            question = "Which programming language was created by JetBrains?",
            options = listOf("A) Swift", "B) Kotlin", "C) Rust", "D) Go"),
            correctLetter = "B",
            correctText = "Kotlin",
            hint = "Named after an island near St. Petersburg.",
            explanation = "Kotlin was unveiled by JetBrains in 2011 and is now Google's preferred Android language!"
        ),
        TriviaQuestion(
            question = "What does 'SMS' stand for?",
            options = listOf("A) Short Message Service", "B) Simple Mobile System", "C) Secure Mail Socket", "D) Shared Media Stream"),
            correctLetter = "A",
            correctText = "Short Message Service",
            hint = "Short M______ S______",
            explanation = "SMS was invented in 1984 by Friedhelm Hillebrand and Bernard Ghillebaert."
        ),
        TriviaQuestion(
            question = "What is the maximum standard GSM 7-bit character count for 1 SMS?",
            options = listOf("A) 120", "B) 140", "C) 160", "D) 256"),
            correctLetter = "C",
            correctText = "160",
            hint = "Between 150 and 170.",
            explanation = "160 characters was determined after researching typical postcard and telex lengths!"
        ),
        TriviaQuestion(
            question = "Which company acquired GitHub in 2018?",
            options = listOf("A) Google", "B) Apple", "C) Microsoft", "D) Meta"),
            correctLetter = "C",
            correctText = "Microsoft",
            hint = "Creators of Windows and Xbox.",
            explanation = "Microsoft acquired GitHub for $7.5 billion in June 2018."
        ),
        TriviaQuestion(
            question = "In the original 1980 Pac-Man, what is the red ghost's nickname?",
            options = listOf("A) Blinky", "B) Pinky", "C) Inky", "D) Clyde"),
            correctLetter = "A",
            correctText = "Blinky",
            hint = "Rhymes with Inky and starts with B.",
            explanation = "Blinky relentlessly chases Pac-Man directly!"
        ),
        TriviaQuestion(
            question = "What was the very first SMS message ever sent in 1992?",
            options = listOf("A) Hello World", "B) Merry Christmas", "C) Can you hear me?", "D) Test 123"),
            correctLetter = "B",
            correctText = "Merry Christmas",
            hint = "A festive holiday greeting.",
            explanation = "Neil Papworth sent 'Merry Christmas' via Vodafone network on Dec 3, 1992!"
        ),
        TriviaQuestion(
            question = "Which protocol is the backbone of the World Wide Web?",
            options = listOf("A) FTP", "B) SMTP", "C) HTTP", "D) SSH"),
            correctLetter = "C",
            correctText = "HTTP",
            hint = "HyperText Transfer Protocol.",
            explanation = "HTTP was formulated by Tim Berners-Lee at CERN."
        ),
        TriviaQuestion(
            question = "What year was Android version 1.0 released to the public?",
            options = listOf("A) 2006", "B) 2008", "C) 2010", "D) 2012"),
            correctLetter = "B",
            correctText = "2008",
            hint = "Launched alongside the HTC Dream (T-Mobile G1).",
            explanation = "Android 1.0 launched in September 2008!"
        )
    )

    // Word Scramble Bank
    private val wordScrambleBank = listOf(
        WordScrambleItem("TWILIO", "IWTLIO", "Cloud communication API platform", "Starts with 'T'"),
        WordScrambleItem("KOTLIN", "TOKILN", "Modern expressive JVM language", "Starts with 'K'"),
        WordScrambleItem("AVATAR", "RAATAV", "Player profile graphic or digital persona", "Starts with 'A'"),
        WordScrambleItem("ARENA", "ENARA", "A battleground for champions", "Starts with 'A'"),
        WordScrambleItem("MESSAGE", "SSAGEME", "Text communication transmitted over networks", "Starts with 'M'"),
        WordScrambleItem("ARCADE", "EDACRA", "Classic coin-op video game palace", "Starts with 'A'"),
        WordScrambleItem("STREAK", "KAERTS", "Consecutive wins without failing", "Starts with 'S'"),
        WordScrambleItem("SERVER", "RESERV", "Computers serving API requests", "Starts with 'S'")
    )

    // Riddle Bank
    private val riddleBank = listOf(
        RiddleItem(
            riddle = "I speak without a mouth and hear without ears. I have no body, but I come alive with wind. What am I?",
            answers = listOf("echo", "an echo", "the echo"),
            hint = "You hear me bounce off canyon walls."
        ),
        RiddleItem(
            riddle = "I have keys but no locks. I have space but no rooms. You can enter, but you can't go outside. What am I?",
            answers = listOf("keyboard", "a keyboard", "computer keyboard"),
            hint = "You type on me every single day."
        ),
        RiddleItem(
            riddle = "What has cities, but no houses; forests, but no trees; and water, but no fish?",
            answers = listOf("map", "a map", "the map", "atlas", "an atlas"),
            hint = "Cartographers draw them."
        ),
        RiddleItem(
            riddle = "The more you take, the more you leave behind. What am I?",
            answers = listOf("footsteps", "footstep", "foot steps", "steps"),
            hint = "Think about walking on sand or snow."
        ),
        RiddleItem(
            riddle = "What runs all around a backyard yet never moves?",
            answers = listOf("fence", "a fence", "the fence"),
            hint = "Keeps pets in and marks property boundaries."
        )
    )

    // Text RPG Map
    private val rpgNodes = mapOf(
        "start" to RpgNode(
            id = "start",
            text = "🏰 [QUEST: The Twilio Dungeon]\nYou stand before an ancient stone dungeon. A heavy Oak Gate is on the left (1), and a glowing Crystal Tunnel is on the right (2). Reply 1 or 2.",
            choices = listOf("1" to "oak_gate", "2" to "crystal_tunnel")
        ),
        "oak_gate" to RpgNode(
            id = "oak_gate",
            text = "🚪 The oak door creaks open into a torchlit armory! A sleeping goblin guards a steel chest. Reply 1 to sneak and loot, 2 to attack with your sword.",
            choices = listOf("1" to "loot_goblin", "2" to "fight_goblin")
        ),
        "crystal_tunnel" to RpgNode(
            id = "crystal_tunnel",
            text = "💎 Glowing blue crystals hum with arcane power. You reach a chasm with a rickety bridge (1) or a dark cavern pool (2). Reply 1 or 2.",
            choices = listOf("1" to "bridge_cross", "2" to "pool_dive")
        ),
        "loot_goblin" to RpgNode(
            id = "loot_goblin",
            text = "🗡️ Success! You quietly pocket 250 Gold & a Mythic SMS Relic! You slip through a secret escape hatch back to safety! 🎉 VICTORY!",
            choices = emptyList(),
            isTerminal = true,
            scoreDelta = 350
        ),
        "fight_goblin" to RpgNode(
            id = "fight_goblin",
            text = "⚔️ You strike boldly! The goblin drops a Ruby Ring (+150 pts), but the alarm rings! You barely sprint out with your life! VICTORY!",
            choices = emptyList(),
            isTerminal = true,
            scoreDelta = 200
        ),
        "bridge_cross" to RpgNode(
            id = "bridge_cross",
            text = "🌉 The bridge sways wildly, but your agile footwork prevails! Across the chasm sits the Golden Chalice of Arena (+300 pts)! 🏆 VICTORY!",
            choices = emptyList(),
            isTerminal = true,
            scoreDelta = 300
        ),
        "pool_dive" to RpgNode(
            id = "pool_dive",
            text = "🏊 You dive into the pool and find an underwater passage leading into the Arena Hall of Champions (+220 pts)! 🌟 VICTORY!",
            choices = emptyList(),
            isTerminal = true,
            scoreDelta = 220
        )
    )

    fun processIncomingSms(rawText: String): GameEngineOutput {
        val input = rawText.trim()
        val upper = input.uppercase()

        // Global Command overrides
        when {
            upper in listOf("HELP", "?", "COMMANDS", "MENU") -> {
                return buildMenuOutput()
            }
            upper in listOf("QUIT", "STOP", "EXIT", "LEAVE") -> {
                return abortCurrentGame()
            }
            upper in listOf("SCORE", "STATS", "POINTS") -> {
                return buildScoreOutput()
            }
            upper.startsWith("START ") || upper.startsWith("PLAY ") -> {
                val modeKey = upper.removePrefix("START ").removePrefix("PLAY ").trim()
                return startGameFromKey(modeKey)
            }
            upper in listOf("START", "PLAY") -> {
                return GameEngineOutput(
                    botSmsText = "🎮 Pick your SMS game:\n1) TRIVIA\n2) SCRAMBLE\n3) RIDDLE\n4) MATH\n5) RPG\nReply with game name or number!",
                    messageType = "MENU",
                    quickReplies = listOf("TRIVIA", "SCRAMBLE", "RIDDLE", "MATH", "RPG")
                )
            }
            upper in listOf("1", "TRIVIA") && currentMode == GameMode.NONE -> {
                return startTrivia()
            }
            upper in listOf("2", "SCRAMBLE", "WORD") && currentMode == GameMode.NONE -> {
                return startWordScramble()
            }
            upper in listOf("3", "RIDDLE") && currentMode == GameMode.NONE -> {
                return startRiddle()
            }
            upper in listOf("4", "MATH") && currentMode == GameMode.NONE -> {
                return startMathBlitz()
            }
            upper in listOf("5", "RPG", "QUEST") && currentMode == GameMode.NONE -> {
                return startDungeonRpg()
            }
            upper in listOf("LEADERBOARD", "TOP", "RANKS") -> {
                return GameEngineOutput(
                    botSmsText = "🏆 ARENA TOP 5 SMS GLADIATORS:\n1. PixelGladiator - 1,450 pts (x9)\n2. TwilioNinja - 1,120 pts (x7)\n3. SMS_Cipher - 980 pts (x6)\n4. SpeedCoder404 - 860 pts (x5)\n5. RetroTexter - 750 pts (x4)\nReply START to duel!",
                    messageType = "SCORE",
                    quickReplies = listOf("START", "SCORE", "HELP")
                )
            }
            upper in listOf("GATEWAY", "TWILIO", "STATUS") -> {
                return GameEngineOutput(
                    botSmsText = "📡 SMS GATEWAY DIAGNOSTIC:\n• Status: ONLINE (Twilio Rest v2010)\n• Sender: 224-GAME\n• Encoding: GSM 7-bit (160 char)\n• Latency: <120ms\n• Ready for commands!",
                    messageType = "SYSTEM",
                    quickReplies = listOf("START", "HELP")
                )
            }
        }

        // Active Game Handling
        return when (currentMode) {
            GameMode.NONE -> {
                GameEngineOutput(
                    botSmsText = "🤖 Command '$input' unknown. Reply 'START' to launch a game, or 'HELP' for SMS Arena commands!",
                    messageType = "CHAT",
                    quickReplies = listOf("START", "HELP", "SCORE", "TOP")
                )
            }
            GameMode.TRIVIA -> handleTriviaInput(input)
            GameMode.WORD_SCRAMBLE -> handleScrambleInput(input)
            GameMode.RIDDLE -> handleRiddleInput(input)
            GameMode.MATH_BLITZ -> handleMathInput(input)
            GameMode.DUNGEON_RPG -> handleRpgInput(input)
        }
    }

    private fun buildMenuOutput(): GameEngineOutput {
        return GameEngineOutput(
            botSmsText = "⚡ SMS GAME ARENA COMMANDS:\n• START <GAME> (TRIVIA, SCRAMBLE, RIDDLE, MATH, RPG)\n• HINT (Get a clue)\n• SKIP (Pass question)\n• SCORE (View stats)\n• TOP (Leaderboard)\n• QUIT (Exit game)",
            messageType = "MENU",
            quickReplies = listOf("START TRIVIA", "START SCRAMBLE", "START RIDDLE", "START MATH", "TOP")
        )
    }

    private fun buildScoreOutput(): GameEngineOutput {
        return GameEngineOutput(
            botSmsText = "📊 CURRENT ARENA STATS:\n• Game: ${currentMode.displayName}\n• Score: $currentScore pts\n• Streak: x$currentStreak (Best: x$highestStreak)\n• Answered: $correctAnswers/$questionsAnswered\nReply to keep playing!",
            messageType = "SCORE",
            quickReplies = if (currentMode != GameMode.NONE) listOf("HINT", "SKIP", "QUIT") else listOf("START", "TOP")
        )
    }

    private fun startGameFromKey(key: String): GameEngineOutput {
        return when (key) {
            "TRIVIA", "1" -> startTrivia()
            "SCRAMBLE", "WORD", "2" -> startWordScramble()
            "RIDDLE", "3" -> startRiddle()
            "MATH", "4" -> startMathBlitz()
            "RPG", "QUEST", "5" -> startDungeonRpg()
            else -> GameEngineOutput(
                botSmsText = "Unknown game '$key'. Valid options: TRIVIA, SCRAMBLE, RIDDLE, MATH, RPG. Reply 'START TRIVIA'!",
                messageType = "CHAT",
                quickReplies = listOf("START TRIVIA", "START SCRAMBLE", "START RIDDLE", "START MATH")
            )
        }
    }

    // --- TRIVIA MODE ---
    private fun startTrivia(): GameEngineOutput {
        resetGameVariables(GameMode.TRIVIA)
        activeTriviaList = triviaBank.shuffled().take(totalQuestionsPerRound)
        return deliverTriviaQuestion(0)
    }

    private fun deliverTriviaQuestion(index: Int): GameEngineOutput {
        currentQuestionIndex = index
        questionStartTime = System.currentTimeMillis()
        val q = activeTriviaList[index]
        val optionsFormatted = q.options.joinToString(" ")
        val text = "🧠 [TRIVIA Q${index + 1}/$totalQuestionsPerRound] ${q.question}\n$optionsFormatted\nReply A, B, C, or D (or HINT)!"
        return GameEngineOutput(
            botSmsText = text,
            messageType = "QUESTION",
            quickReplies = listOf("A", "B", "C", "D", "HINT", "SKIP")
        )
    }

    private fun handleTriviaInput(input: String): GameEngineOutput {
        val upper = input.trim().uppercase()
        val currentQ = activeTriviaList[currentQuestionIndex]

        if (upper == "HINT") {
            return GameEngineOutput(
                botSmsText = "💡 HINT: ${currentQ.hint}\nReply with A, B, C, or D!",
                messageType = "HINT",
                quickReplies = listOf("A", "B", "C", "D", "SKIP")
            )
        }

        if (upper in listOf("SKIP", "PASS", "NEXT")) {
            questionsAnswered++
            currentStreak = 0
            return moveToNextTriviaQuestion("⏩ Question skipped. Correct answer was ${currentQ.correctLetter}) ${currentQ.correctText}.")
        }

        // Check answer
        val isCorrect = upper == currentQ.correctLetter ||
                upper == currentQ.correctText.uppercase() ||
                upper.startsWith(currentQ.correctLetter + ")")

        questionsAnswered++
        if (isCorrect) {
            correctAnswers++
            currentStreak++
            if (currentStreak > highestStreak) highestStreak = currentStreak
            val streakBonus = (currentStreak - 1) * 25
            val pointsGained = 100 + streakBonus
            currentScore += pointsGained

            val feedback = "✅ BINGO! +$pointsGained pts! (Streak x$currentStreak 🔥)\n${currentQ.explanation}"
            return moveToNextTriviaQuestion(feedback)
        } else {
            currentStreak = 0
            val feedback = "❌ INCORRECT! The correct answer was ${currentQ.correctLetter}) ${currentQ.correctText}.\n${currentQ.explanation}"
            return moveToNextTriviaQuestion(feedback)
        }
    }

    private fun moveToNextTriviaQuestion(prefixFeedback: String): GameEngineOutput {
        val nextIdx = currentQuestionIndex + 1
        return if (nextIdx < totalQuestionsPerRound) {
            val nextQ = activeTriviaList[nextIdx]
            currentQuestionIndex = nextIdx
            val optionsFormatted = nextQ.options.joinToString(" ")
            val text = "$prefixFeedback\n\n🧠 [Q${nextIdx + 1}/$totalQuestionsPerRound] ${nextQ.question}\n$optionsFormatted"
            GameEngineOutput(
                botSmsText = text,
                messageType = "QUESTION",
                quickReplies = listOf("A", "B", "C", "D", "HINT", "QUIT")
            )
        } else {
            finishGame(prefixFeedback)
        }
    }

    // --- WORD SCRAMBLE MODE ---
    private fun startWordScramble(): GameEngineOutput {
        resetGameVariables(GameMode.WORD_SCRAMBLE)
        activeScrambleList = wordScrambleBank.shuffled().take(totalQuestionsPerRound)
        return deliverScrambleItem(0)
    }

    private fun deliverScrambleItem(index: Int): GameEngineOutput {
        currentQuestionIndex = index
        scrambleAttemptsLeft = 3
        val item = activeScrambleList[index]
        val text = "🔤 [SCRAMBLE ${index + 1}/$totalQuestionsPerRound] Unscramble: '${item.scrambledWord}'\nClue: ${item.clue}\n(3 tries) Reply your guess or 'HINT'!"
        return GameEngineOutput(
            botSmsText = text,
            messageType = "QUESTION",
            quickReplies = listOf("HINT", "SKIP", "QUIT")
        )
    }

    private fun handleScrambleInput(input: String): GameEngineOutput {
        val upper = input.trim().uppercase()
        val item = activeScrambleList[currentQuestionIndex]

        if (upper == "HINT") {
            return GameEngineOutput(
                botSmsText = "💡 HINT: ${item.hint} (${item.originalWord.length} letters)\nReply your guess!",
                messageType = "HINT",
                quickReplies = listOf("SKIP", "QUIT")
            )
        }

        if (upper in listOf("SKIP", "PASS")) {
            questionsAnswered++
            currentStreak = 0
            return moveToNextScramble("⏩ Skipped! The word was '${item.originalWord}'.")
        }

        if (upper == item.originalWord) {
            questionsAnswered++
            correctAnswers++
            currentStreak++
            if (currentStreak > highestStreak) highestStreak = currentStreak
            val pts = 120 + (scrambleAttemptsLeft * 20)
            currentScore += pts
            return moveToNextScramble("🎉 SPOT ON! +$pts pts for '${item.originalWord}'! (Streak x$currentStreak 🔥)")
        } else {
            scrambleAttemptsLeft--
            if (scrambleAttemptsLeft > 0) {
                return GameEngineOutput(
                    botSmsText = "❌ Not '$input'! You have $scrambleAttemptsLeft attempt(s) left. Unscramble: '${item.scrambledWord}'. Reply guess or HINT!",
                    messageType = "INCORRECT",
                    quickReplies = listOf("HINT", "SKIP", "QUIT")
                )
            } else {
                questionsAnswered++
                currentStreak = 0
                return moveToNextScramble("❌ Out of tries! The word was '${item.originalWord}'.")
            }
        }
    }

    private fun moveToNextScramble(prefixFeedback: String): GameEngineOutput {
        val nextIdx = currentQuestionIndex + 1
        return if (nextIdx < totalQuestionsPerRound) {
            val item = activeScrambleList[nextIdx]
            currentQuestionIndex = nextIdx
            scrambleAttemptsLeft = 3
            val text = "$prefixFeedback\n\n🔤 [${nextIdx + 1}/$totalQuestionsPerRound] Unscramble: '${item.scrambledWord}'\nClue: ${item.clue}\nReply guess or HINT!"
            GameEngineOutput(
                botSmsText = text,
                messageType = "QUESTION",
                quickReplies = listOf("HINT", "SKIP", "QUIT")
            )
        } else {
            finishGame(prefixFeedback)
        }
    }

    // --- RIDDLE MODE ---
    private fun startRiddle(): GameEngineOutput {
        resetGameVariables(GameMode.RIDDLE)
        activeRiddleList = riddleBank.shuffled().take(totalQuestionsPerRound)
        return deliverRiddleItem(0)
    }

    private fun deliverRiddleItem(index: Int): GameEngineOutput {
        currentQuestionIndex = index
        val item = activeRiddleList[index]
        val text = "🧙‍♂️ [RIDDLE ${index + 1}/$totalQuestionsPerRound] \"${item.riddle}\"\nReply with your answer or 'HINT'!"
        return GameEngineOutput(
            botSmsText = text,
            messageType = "QUESTION",
            quickReplies = listOf("HINT", "SKIP", "QUIT")
        )
    }

    private fun handleRiddleInput(input: String): GameEngineOutput {
        val clean = input.trim().lowercase()
        val item = activeRiddleList[currentQuestionIndex]

        if (clean == "hint") {
            return GameEngineOutput(
                botSmsText = "💡 RIDDLE CLUE: ${item.hint}\nReply your answer!",
                messageType = "HINT",
                quickReplies = listOf("SKIP", "QUIT")
            )
        }

        if (clean in listOf("skip", "pass")) {
            questionsAnswered++
            currentStreak = 0
            return moveToNextRiddle("⏩ Skipped! The answer was '${item.answers.first()}'.")
        }

        val isMatch = item.answers.any { accepted ->
            clean == accepted || clean.contains(accepted) || accepted.contains(clean)
        }

        questionsAnswered++
        if (isMatch) {
            correctAnswers++
            currentStreak++
            if (currentStreak > highestStreak) highestStreak = currentStreak
            val pts = 150 + (currentStreak * 25)
            currentScore += pts
            return moveToNextRiddle("✨ BRILLIANT! Answer: '${item.answers.first()}'. +$pts pts! (Streak x$currentStreak 🔥)")
        } else {
            currentStreak = 0
            return moveToNextRiddle("❌ Not quite! The answer was '${item.answers.first()}'.")
        }
    }

    private fun moveToNextRiddle(prefixFeedback: String): GameEngineOutput {
        val nextIdx = currentQuestionIndex + 1
        return if (nextIdx < totalQuestionsPerRound) {
            val item = activeRiddleList[nextIdx]
            currentQuestionIndex = nextIdx
            val text = "$prefixFeedback\n\n🧙‍♂️ [${nextIdx + 1}/$totalQuestionsPerRound] \"${item.riddle}\"\nReply your answer or HINT!"
            GameEngineOutput(
                botSmsText = text,
                messageType = "QUESTION",
                quickReplies = listOf("HINT", "SKIP", "QUIT")
            )
        } else {
            finishGame(prefixFeedback)
        }
    }

    // --- MATH BLITZ MODE ---
    private var mathAnswer: Int = 0

    private fun startMathBlitz(): GameEngineOutput {
        resetGameVariables(GameMode.MATH_BLITZ)
        return deliverMathQuestion(0)
    }

    private fun deliverMathQuestion(index: Int): GameEngineOutput {
        currentQuestionIndex = index
        val op = listOf("+", "-", "*").random()
        val (a, b) = when (op) {
            "*" -> Pair(Random.nextInt(4, 13), Random.nextInt(3, 11))
            "-" -> {
                val n1 = Random.nextInt(20, 99)
                val n2 = Random.nextInt(5, n1)
                Pair(n1, n2)
            }
            else -> Pair(Random.nextInt(15, 89), Random.nextInt(12, 89))
        }
        mathAnswer = when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            else -> a + b
        }
        val text = "⚡ [MATH BLITZ ${index + 1}/$totalQuestionsPerRound] Solve fast:\n$a $op $b = ?\nReply with the number!"
        return GameEngineOutput(
            botSmsText = text,
            messageType = "QUESTION",
            quickReplies = listOf(mathAnswer.toString(), (mathAnswer + 2).toString(), (mathAnswer - 3).toString(), "SKIP")
        )
    }

    private fun handleMathInput(input: String): GameEngineOutput {
        val upper = input.trim().uppercase()
        if (upper in listOf("SKIP", "PASS")) {
            questionsAnswered++
            currentStreak = 0
            return moveToNextMath("⏩ Skipped! The answer was $mathAnswer.")
        }

        val parsed = input.trim().toIntOrNull()
        questionsAnswered++
        if (parsed != null && parsed == mathAnswer) {
            correctAnswers++
            currentStreak++
            if (currentStreak > highestStreak) highestStreak = currentStreak
            val pts = 90 + (currentStreak * 15)
            currentScore += pts
            return moveToNextMath("⚡ CORRECT! $mathAnswer. +$pts pts! (Streak x$currentStreak 🔥)")
        } else {
            currentStreak = 0
            return moveToNextMath("❌ INCORRECT! Correct answer was $mathAnswer.")
        }
    }

    private fun moveToNextMath(prefixFeedback: String): GameEngineOutput {
        val nextIdx = currentQuestionIndex + 1
        return if (nextIdx < totalQuestionsPerRound) {
            currentQuestionIndex = nextIdx
            val op = listOf("+", "-", "*").random()
            val (a, b) = when (op) {
                "*" -> Pair(Random.nextInt(4, 13), Random.nextInt(3, 11))
                "-" -> {
                    val n1 = Random.nextInt(20, 99)
                    val n2 = Random.nextInt(5, n1)
                    Pair(n1, n2)
                }
                else -> Pair(Random.nextInt(15, 89), Random.nextInt(12, 89))
            }
            mathAnswer = when (op) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                else -> a + b
            }
            val text = "$prefixFeedback\n\n⚡ [${nextIdx + 1}/$totalQuestionsPerRound] Solve: $a $op $b = ?\nReply with the number!"
            GameEngineOutput(
                botSmsText = text,
                messageType = "QUESTION",
                quickReplies = listOf(mathAnswer.toString(), (mathAnswer + 4).toString(), "SKIP", "QUIT")
            )
        } else {
            finishGame(prefixFeedback)
        }
    }

    // --- DUNGEON TEXT RPG MODE ---
    private fun startDungeonRpg(): GameEngineOutput {
        resetGameVariables(GameMode.DUNGEON_RPG)
        val node = rpgNodes["start"]!!
        currentRpgNode = node
        return GameEngineOutput(
            botSmsText = node.text,
            messageType = "QUESTION",
            quickReplies = listOf("1", "2", "QUIT")
        )
    }

    private fun handleRpgInput(input: String): GameEngineOutput {
        val node = currentRpgNode ?: return abortCurrentGame()
        val choice = input.trim()

        val nextNodeId = node.choices.find { it.first == choice }?.second
        if (nextNodeId == null) {
            val valid = node.choices.joinToString(" or ") { it.first }
            return GameEngineOutput(
                botSmsText = "Invalid choice. Reply $valid or QUIT!",
                messageType = "INCORRECT",
                quickReplies = node.choices.map { it.first } + "QUIT"
            )
        }

        val nextNode = rpgNodes[nextNodeId] ?: return abortCurrentGame()
        currentRpgNode = nextNode
        questionsAnswered++
        correctAnswers++

        if (nextNode.isTerminal) {
            currentScore += nextNode.scoreDelta
            currentStreak = 1
            highestStreak = 1
            return finishGame(nextNode.text)
        } else {
            return GameEngineOutput(
                botSmsText = nextNode.text,
                messageType = "QUESTION",
                quickReplies = nextNode.choices.map { it.first } + "QUIT"
            )
        }
    }

    // --- Helpers ---
    private fun resetGameVariables(mode: GameMode) {
        currentMode = mode
        currentScore = 0
        currentStreak = 0
        highestStreak = 0
        questionsAnswered = 0
        correctAnswers = 0
        currentQuestionIndex = 0
        gameStartTime = System.currentTimeMillis()
    }

    private fun finishGame(prefixFeedback: String): GameEngineOutput {
        val durationSec = ((System.currentTimeMillis() - gameStartTime) / 1000).coerceAtLeast(1)
        val finishedMode = currentMode
        val finalScoreVal = currentScore
        val qAnswered = questionsAnswered
        val cAnswers = correctAnswers
        val hStreak = highestStreak

        currentMode = GameMode.NONE

        val summaryText = "$prefixFeedback\n\n🏁 GAME OVER! [${finishedMode.displayName}]\n• Total Score: $finalScoreVal pts\n• Accuracy: $cAnswers/$qAnswered\n• Best Streak: x$hStreak 🔥\n• Time: ${durationSec}s\nSaved to Arena Leaderboard! Reply 'START' to battle again."

        return GameEngineOutput(
            botSmsText = summaryText,
            messageType = "SCORE",
            isGameFinished = true,
            finalScore = finalScoreVal,
            questionsAnswered = qAnswered,
            correctAnswers = cAnswers,
            maxStreak = hStreak,
            durationSeconds = durationSec,
            quickReplies = listOf("START", "SCORE", "TOP", "HELP")
        )
    }

    private fun abortCurrentGame(): GameEngineOutput {
        val prev = currentMode
        currentMode = GameMode.NONE
        return GameEngineOutput(
            botSmsText = "🛑 Left ${prev.displayName}. Game session ended. Reply 'START' to pick a new challenge or 'HELP' for menu.",
            messageType = "SYSTEM",
            quickReplies = listOf("START", "TOP", "HELP")
        )
    }
}
