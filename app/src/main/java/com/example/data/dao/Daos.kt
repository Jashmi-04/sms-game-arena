package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CustomQuestionEntity
import com.example.data.model.GameRecordEntity
import com.example.data.model.LeaderboardEntity
import com.example.data.model.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<SmsMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsMessageEntity): Long

    @Query("DELETE FROM sms_messages")
    suspend fun clearAllMessages()
}

@Dao
interface GameDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GameRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecordEntity): Long

    @Query("SELECT SUM(score) FROM game_records")
    fun getTotalScore(): Flow<Int?>

    @Query("SELECT MAX(highestStreak) FROM game_records")
    fun getBestStreak(): Flow<Int?>
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_entries ORDER BY score DESC LIMIT 50")
    fun getTopLeaderboard(): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard_entries WHERE gameType = :gameType ORDER BY score DESC LIMIT 50")
    fun getLeaderboardByGame(gameType: String): Flow<List<LeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<LeaderboardEntity>)
}

@Dao
interface CustomQuestionDao {
    @Query("SELECT * FROM custom_questions ORDER BY createdTimestamp DESC")
    fun getAllQuestions(): Flow<List<CustomQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: CustomQuestionEntity): Long

    @Query("DELETE FROM custom_questions WHERE id = :id")
    suspend fun deleteQuestion(id: Long)
}
