package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LlmWikiStorageService(
    private val reasoningDao: ReasoningCacheDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Saves or replaces a wiki page with specific entity metadata.
     */
    suspend fun savePage(
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        connections: List<String> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val pageId = "wiki-${title.lowercase().replace(" ", "-")}"
        
        // Convert connections to JSON array
        val connectionsJson = JSONArray(connections).toString()
        val tagsString = tags.distinct().joinToString(", ")

        val entity = WikiPageEntity(
            id = pageId,
            title = title.trim(),
            content = content.trim(),
            tags = tagsString,
            connectionsJson = connectionsJson,
            lastUpdated = System.currentTimeMillis()
        )
        reasoningDao.insertWikiPage(entity)
    }

    /**
     * Retrieves a page and formats it as a standardized Markdown string with Frontmatter.
     */
    suspend fun getPageAsMarkdown(title: String): String? = withContext(Dispatchers.IO) {
        val allPages = reasoningDao.getAllWikiPages().first()
        val page = allPages.find { it.title.equals(title.trim(), ignoreCase = true) } ?: return@withContext null

        return@withContext buildString {
            appendLine("---")
            appendLine("title: ${page.title}")
            if (page.tags.isNotBlank()) {
                appendLine("tags: [${page.tags}]")
            }
            appendLine("last_updated: ${dateFormat.format(Date(page.lastUpdated))}")
            
            // Format connections as standard wikilinks in frontmatter
            if (page.connectionsJson.isNotBlank() && page.connectionsJson != "[]") {
                val links = try {
                    JSONArray(page.connectionsJson)
                } catch (e: Exception) {
                    JSONArray()
                }
                val wikilinks = mutableListOf<String>()
                for (i in 0 until links.length()) {
                    val linkVal = links.optString(i)
                    val label = linkVal.replace("wiki-", "").replace("-", " ").capitalize()
                    wikilinks.add("[[$label]]")
                }
                appendLine("connections: ${wikilinks.joinToString(", ")}")
            }
            appendLine("---")
            appendLine()
            appendLine(page.content)
        }
    }

    /**
     * Retrieves the raw content page.
     */
    suspend fun getPageEntity(title: String): WikiPageEntity? = withContext(Dispatchers.IO) {
        val allPages = reasoningDao.getAllWikiPages().first()
        return@withContext allPages.find { it.title.equals(title.trim(), ignoreCase = true) }
    }

    /**
     * Intelligently merges and updates existing knowledge nodes to prevent contradiction
     * and achieve Compounding Intelligence upfront.
     */
    suspend fun mergeOrUpdatePage(
        title: String,
        newContent: String,
        newTags: List<String> = emptyList(),
        newConnections: List<String> = emptyList()
    ): Unit = withContext(Dispatchers.IO) {
        val existing = getPageEntity(title)

        if (existing == null) {
            // Topic does not exist, save as new node!
            savePage(title, newContent, newTags, newConnections)
            return@withContext
        }

        // Topic already exists, let's compound the intelligence!
        val timestampHeader = "\n\n### [Update: ${dateFormat.format(Date(System.currentTimeMillis()))}]\n"
        val compoundedContent = existing.content.trim() + timestampHeader + newContent.trim()

        // Merge tags
        val existingTags = existing.tags.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val mergedTags = (existingTags + newTags.map { it.trim() })
            .distinct()
            .filter { it.isNotBlank() }

        // Merge connections
        val existingConnections = try {
            val arr = JSONArray(existing.connectionsJson)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.optString(i))
            }
            list
        } catch (e: Exception) {
            emptyList<String>()
        }
        val mergedConnections = (existingConnections + newConnections).distinct()

        // Save updated page
        val mergedEntity = existing.copy(
            content = compoundedContent,
            tags = mergedTags.joinToString(", "),
            connectionsJson = JSONArray(mergedConnections).toString(),
            lastUpdated = System.currentTimeMillis()
        )
        reasoningDao.insertWikiPage(mergedEntity)
    }

    /**
     * Deletes a page by title.
     */
    suspend fun deletePage(title: String) = withContext(Dispatchers.IO) {
        val existing = getPageEntity(title)
        if (existing != null) {
            reasoningDao.deleteWikiPage(existing.id)
        }
    }
}
