package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val mandates: String, // newline separated ALWAYS/NEVER rules
    val codeSnippet: String,
    val gotchas: String,
    val effectivenessRubric: String,
    val isFavorite: Boolean = false
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillId: String,
    val title: String,
    val code: String,
    val language: String,
    val description: String,
    val effectivenessScore: Int = 100,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String = "gemini-3.5-flash",
    val extractedCode: String? = null
)

@Entity(tableName = "audit_results")
data class AuditResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codeSnippet: String,
    val overallScore: Int,
    val securityScore: Int,
    val architectureScore: Int,
    val uiAccessibilityScore: Int,
    val offlineRobustnessScore: Int,
    val suggestions: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_commands")
data class SystemCommandEntity(
    @PrimaryKey val commandName: String,
    val executablePath: String,
    val category: String,
    val summary: String,
    val usageSyntax: String,
    val bashSource: String, // Single source of truth executable script
    val returnContractJson: String,
    val relatedSkillId: String
)
