package com.example.planner

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

open class AIAnalyzer(private val apiKey: String, protected val uiMap: UiMapModel) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    open suspend fun analyzeIntent(naturalLanguageIntent: String, startPage: String): String {
        // 准备可见文本列表，用于GLM API参考
        val visibleTexts = uiMap.visible_text_index.keys.toList()
        
        // 构建GLM API请求
        val requestBody = GlmRequest(
            model = "glm-4-flash",
            messages = listOf(
                GlmMessage(
                    role = "system",
                    content = "你是一个UI路径规划助手，需要根据用户的自然语言意图和当前起始页面，从提供的可见文本列表中找到最合适的目标。只能从给定的列表中选择，不能生成列表外的内容。返回格式只能是纯文本，直接输出选择的可见文本。"
                ),
                GlmMessage(
                    role = "user",
                    content = "用户意图：$naturalLanguageIntent\n当前起始页面：$startPage\n\n可见文本列表：${visibleTexts.joinToString("\n- ", prefix = "- ")}"
                )
            ),
            temperature = 0.0,
            max_tokens = 100
        )

        try {
            // 调用GLM API
            println("Sending request to GLM API...")
            println("API Key: $apiKey")
            
            val response = httpClient.post("https://open.bigmodel.cn/api/paas/v4/chat/completions") {
                contentType(ContentType.Application.Json)
                // 直接设置headers，而不是使用函数
                headers["Authorization"] = "Bearer $apiKey"
                setBody(requestBody)
            }

            val responseText = response.bodyAsText()
            println("Response status: ${response.status}")
            println("Received response: $responseText")
            
            // 检查响应是否包含错误
            if (responseText.contains("\"error\":")) {
                throw RuntimeException("GLM API returned error: $responseText")
            }
            
            val glmResponse = Json.decodeFromString<GlmResponse>(responseText)
            
            // 提取目标visibleText
            val targetText = glmResponse.choices.firstOrNull()?.message?.content?.trim() ?: ""
            
            if (targetText.isEmpty()) {
                throw IllegalArgumentException("GLM API returned empty response")
            }
            
            // 验证返回的文本是否在visible_text_index中
            if (targetText !in uiMap.visible_text_index) {
                throw IllegalArgumentException("GLM API returned text not in visible_text_index: $targetText")
            }
            
            return targetText
        } catch (e: Exception) {
            println("Error calling GLM API:")
            e.printStackTrace()
            throw RuntimeException("Failed to analyze intent: ${e.message}")
        }
    }

    // 关闭HTTP客户端
    open fun close() {
        httpClient.close()
    }
}

// GLM API请求和响应模型
@Serializable
data class GlmRequest(
    val model: String,
    val messages: List<GlmMessage>,
    val temperature: Double,
    val max_tokens: Int
)

@Serializable
data class GlmMessage(
    val role: String,
    val content: String
)

@Serializable
data class GlmResponse(
    val id: String,
    val object_: String = "",
    val created: Long,
    val model: String,
    val choices: List<GlmChoice>,
    val usage: GlmUsage
) {
    @Serializable
    data class GlmChoice(
        val index: Int,
        val message: GlmMessage,
        val finish_reason: String
    )

    @Serializable
    data class GlmUsage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}