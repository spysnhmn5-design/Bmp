package com.example.data.local

import androidx.room.*
import com.example.data.model.WebProject
import com.example.data.model.LocalMedia
import com.example.data.model.CommunityMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM web_projects ORDER BY scannedAt DESC")
    fun getAllProjects(): Flow<List<WebProject>>

    @Query("SELECT * FROM web_projects WHERE id = :id")
    suspend fun getProjectById(id: Int): WebProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: WebProject): Long

    @Update
    suspend fun updateProject(project: WebProject)

    @Delete
    suspend fun deleteProject(project: WebProject)
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM local_media WHERE projectId = :projectId")
    fun getMediaForProject(projectId: Int): Flow<List<LocalMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: LocalMedia)

    @Query("DELETE FROM local_media WHERE projectId = :projectId")
    suspend fun deleteMediaForProject(projectId: Int)
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<CommunityMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CommunityMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CommunityMessage>)

    @Query("DELETE FROM community_messages")
    suspend fun clearAll()
}

@Database(entities = [WebProject::class, LocalMedia::class, CommunityMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun mediaDao(): MediaDao
    abstract fun communityDao(): CommunityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "web_vault_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
