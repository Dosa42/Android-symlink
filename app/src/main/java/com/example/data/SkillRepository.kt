package com.example.data

import com.example.data.local.AppDatabase
import com.example.data.local.AuditResultEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.RecipeEntity
import com.example.data.local.SkillEntity
import com.example.data.local.SystemCommandEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkillRepository(private val database: AppDatabase) {
    private val skillDao = database.skillDao()
    private val recipeDao = database.recipeDao()
    private val chatDao = database.chatDao()
    private val auditDao = database.auditDao()
    private val systemCommandDao = database.systemCommandDao()

    init {
        // Pre-populate skills, recipes, and system commands if database is empty
        CoroutineScope(Dispatchers.IO).launch {
            if (skillDao.getSkillCount() == 0) {
                skillDao.insertSkills(SkillsData.ALL_SKILLS)
                SkillsData.INITIAL_RECIPES.forEach { recipe ->
                    recipeDao.insertRecipe(recipe)
                }
            }
            if (systemCommandDao.getCommandCount() == 0) {
                systemCommandDao.insertCommands(SystemCommandsData.ALL_SYSTEM_COMMANDS)
            }
        }
    }

    val allSkills: Flow<List<SkillEntity>> = skillDao.getAllSkills()
    val allCommands: Flow<List<SystemCommandEntity>> = systemCommandDao.getAllCommands()

    suspend fun getCommandByName(name: String): SystemCommandEntity? = withContext(Dispatchers.IO) {
        systemCommandDao.getCommandByName(name)
    }

    fun getSkillsByCategory(category: String): Flow<List<SkillEntity>> =
        skillDao.getSkillsByCategory(category)

    fun getSkillById(id: String): Flow<SkillEntity?> =
        skillDao.getSkillById(id)

    suspend fun updateSkill(skill: SkillEntity) = withContext(Dispatchers.IO) {
        skillDao.updateSkill(skill)
    }

    val allRecipes: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()

    suspend fun saveRecipe(recipe: RecipeEntity): Long = withContext(Dispatchers.IO) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun deleteRecipe(id: Long) = withContext(Dispatchers.IO) {
        recipeDao.deleteRecipe(id)
    }

    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun addChatMessage(role: String, content: String, model: String, extractedCode: String? = null): Long =
        withContext(Dispatchers.IO) {
            val message = ChatMessageEntity(
                role = role,
                content = content,
                modelName = model,
                extractedCode = extractedCode
            )
            chatDao.insertMessage(message)
        }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
    }

    val auditHistory: Flow<List<AuditResultEntity>> = auditDao.getAllAudits()

    suspend fun recordAudit(audit: AuditResultEntity): Long = withContext(Dispatchers.IO) {
        auditDao.insertAudit(audit)
    }
}
