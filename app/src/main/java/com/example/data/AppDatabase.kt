package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CustomQuestionDao
import com.example.data.dao.GameDao
import com.example.data.dao.LeaderboardDao
import com.example.data.dao.SmsDao
import com.example.data.model.CustomQuestionEntity
import com.example.data.model.GameRecordEntity
import com.example.data.model.LeaderboardEntity
import com.example.data.model.SmsMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SmsMessageEntity::class,
        GameRecordEntity::class,
        LeaderboardEntity::class,
        CustomQuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsDao(): SmsDao
    abstract fun gameDao(): GameDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun customQuestionDao(): CustomQuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sms_game_arena_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate rival leaderboard & default questions
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            seedInitialData(database)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            val rivals = listOf(
                LeaderboardEntity(
                    playerName = "PixelGladiator",
                    playerPhone = "+1 (555) ***-8291",
                    gameType = "TRIVIA",
                    score = 1450,
                    streak = 9,
                    rankBadge = "Grandmaster"
                ),
                LeaderboardEntity(
                    playerName = "TwilioNinja",
                    playerPhone = "+1 (555) ***-3194",
                    gameType = "WORD_SCRAMBLE",
                    score = 1120,
                    streak = 7,
                    rankBadge = "Gladiator"
                ),
                LeaderboardEntity(
                    playerName = "SMS_Cipher",
                    playerPhone = "+1 (555) ***-7721",
                    gameType = "RIDDLE",
                    score = 980,
                    streak = 6,
                    rankBadge = "Gladiator"
                ),
                LeaderboardEntity(
                    playerName = "SpeedCoder404",
                    playerPhone = "+1 (555) ***-1049",
                    gameType = "MATH_BLITZ",
                    score = 860,
                    streak = 5,
                    rankBadge = "Veteran"
                ),
                LeaderboardEntity(
                    playerName = "RetroTexter",
                    playerPhone = "+1 (555) ***-9982",
                    gameType = "DUNGEON_RPG",
                    score = 750,
                    streak = 4,
                    rankBadge = "Veteran"
                )
            )
            database.leaderboardDao().insertLeaderboardEntries(rivals)

            // Seed default welcome SMS from Arena Shortcode 224-GAME
            val welcomeSms = SmsMessageEntity(
                sender = "ARENA_BOT",
                phoneNumber = "224-GAME (Twilio Gateway)",
                text = "⚡ WELCOME TO SMS GAME ARENA! 🎮\nReply 'START' to pick a game, 'HELP' for commands, or 'TRIVIA' to jump right in! (Std SMS rates apply)",
                messageType = "MENU",
                segmentCount = 1
            )
            database.smsDao().insertMessage(welcomeSms)
        }
    }
}
