package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY category ASC, title ASC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE category = :category ORDER BY title ASC")
    fun getSkillsByCategory(category: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id LIMIT 1")
    fun getSkillById(id: String): Flow<SkillEntity?>

    @Query("SELECT COUNT(*) FROM skills")
    suspend fun getSkillCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE skillId = :skillId ORDER BY createdAt DESC")
    fun getRecipesBySkill(skillId: String): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_results ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<AuditResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditResultEntity): Long
}

@Dao
interface SystemCommandDao {
    @Query("SELECT * FROM system_commands ORDER BY commandName ASC")
    fun getAllCommands(): Flow<List<SystemCommandEntity>>

    @Query("SELECT * FROM system_commands WHERE commandName = :name LIMIT 1")
    suspend fun getCommandByName(name: String): SystemCommandEntity?

    @Query("SELECT COUNT(*) FROM system_commands")
    suspend fun getCommandCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommands(commands: List<SystemCommandEntity>)
}
