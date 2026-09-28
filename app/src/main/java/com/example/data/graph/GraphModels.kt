package com.example.data.graph

enum class NodeType {
    NOTE,
    UNRESOLVED,
    TAG,
    ATTACHMENT
}

data class GraphNode(
    val id: String, // Stable unique path identifier (e.g. "guides/architecture.md", "tag:#android", "unresolved:missing.md")
    val label: String,
    val filePath: String,
    val type: NodeType = NodeType.NOTE,
    val inDegree: Int = 0,
    val outDegree: Int = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isPinned: Boolean = false,
    val colorHex: String? = null,
    val creationTimestamp: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val contentSnippet: String = ""
) {
    val totalDegree: Int get() = inDegree + outDegree

    /**
     * Obsidian formula: notes with more backlinks are visibly larger.
     */
    fun getBaseRadius(multiplier: Float = 1.0f): Float {
        val base = when (type) {
            NodeType.NOTE -> 8f + (inDegree * 2.5f).coerceAtMost(28f)
            NodeType.UNRESOLVED -> 6f + (inDegree * 1.5f).coerceAtMost(16f)
            NodeType.TAG -> 5f + (inDegree * 1.2f).coerceAtMost(14f)
            NodeType.ATTACHMENT -> 6f
        }
        return (base * multiplier).coerceIn(4f, 48f)
    }
}

data class GraphEdge(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val isUnresolved: Boolean = false
)

data class ColorGroup(
    val id: String,
    val query: String, // e.g. "path:guides", "tag:#architecture", "type:unresolved"
    val colorHex: String
)

data class GraphFilterConfig(
    val searchQuery: String = "",
    val showTags: Boolean = true,
    val showAttachments: Boolean = true,
    val showUnresolved: Boolean = true,
    val showOrphans: Boolean = true
)

data class GraphForcesConfig(
    val centerForce: Float = 0.18f,     // Center gravity pull (0.01f - 1.0f)
    val repelForce: Float = 260f,      // Many-body node repulsion (50f - 800f)
    val linkForce: Float = 0.28f,      // Spring link stiffness (0.01f - 1.0f)
    val linkDistance: Float = 110f,    // Rest length of links (30f - 300f)
    val damping: Float = 0.88f         // Friction / decay
)

data class GraphDisplayConfig(
    val nodeSizeMultiplier: Float = 1.0f,
    val lineThickness: Float = 1.5f,
    val showArrows: Boolean = true,
    val textFadeThreshold: Float = 0.70f,
    val animateTimeline: Boolean = false
)

enum class GraphMode {
    GLOBAL,
    LOCAL
}

data class LocalGraphSettings(
    val activeNodeId: String? = null,
    val depth: Int = 1, // 1, 2, 3 hops from active node
    val incoming: Boolean = true,
    val outgoing: Boolean = true
)

data class VaultNote(
    val filePath: String,
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val creationTimestamp: Long = System.currentTimeMillis()
)
