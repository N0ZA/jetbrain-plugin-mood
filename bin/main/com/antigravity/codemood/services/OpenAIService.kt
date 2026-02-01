package com.antigravity.codemood.services

import com.antigravity.codemood.settings.CodeMoodSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service(Service.Level.PROJECT)
class OpenAIService(private val project: Project) {

    fun getMotivation(mood: String, callback: (String) -> Unit) {
        val settings = CodeMoodSettings.getInstance(project).state
        if (!settings.enableAiMotivation || settings.openAiKey.isBlank()) {
            callback("AI disabled. Go to Settings -> CodeMood to enable.")
            return
        }

        // We use Java 11 HTTP Client (standard in recent IntelliJ platforms)
        val client = HttpClient.newHttpClient()
        
        val prompt = "You are a supportive coding assistant. The developer is currently feeling: $mood. " +
                "Give them a very short (1 sentence), punchy, motivational tip or coding wisdom. " +
                "Do not be cheesy. Be pragmatic."

        val jsonBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [{"role": "user", "content": "$prompt"}],
                "max_tokens": 50
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${settings.openAiKey}")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build()
            
        // Async call
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { it.body() }
            .thenAccept { body ->
                // Super rough JSON parsing to avoid dependencies like Jackson/Gson for now
                // We just look for "content": "..."
                val content = extractContent(body)
                callback(content)
            }
            .exceptionally { e ->
                callback("Error contacting AI: ${e.message}")
                null
            }
    }

    private fun extractContent(json: String): String {
        return try {
            val startMarker = "\"content\": \""
            val startIndex = json.indexOf(startMarker)
            if (startIndex == -1) return "Keep coding!"
            
            val contentStart = startIndex + startMarker.length
            val endIndex = json.indexOf("\"", contentStart)
            val text = json.substring(contentStart, endIndex)
            
            // Clean up escaped newlines if any
            text.replace("\\n", " ").trim()
        } catch (e: Exception) {
            "Keep coding! (Parse Error)"
        }
    }
}
