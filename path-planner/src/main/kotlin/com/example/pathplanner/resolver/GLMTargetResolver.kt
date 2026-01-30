package com.example.pathplanner.resolver

import com.example.pathplanner.models.ComponentTarget
import com.example.pathplanner.models.StateTarget
import com.example.pathplanner.models.TargetSpec
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/**
 * GLM API的响应数据类
 */
@Serializable
private data class GLMResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<GLMChoice>? = emptyList(),
    val usage: GLMUsage? = null
)

@Serializable
private data class GLMChoice(
    val message: GLMMessage,
    val finish_reason: String? = null
)

@Serializable
private data class GLMUsage(
    val prompt_tokens: Int? = 0,
    val completion_tokens: Int? = 0,
    val total_tokens: Int? = 0
)

@Serializable
private data class GLMMessage(
    @SerialName("role")
    val role: String,
    val content: String
)

/**
 * GLM API的请求数据类
 */
@Serializable
private data class GLMRequest(
    val model: String,
    val messages: List<GLMMessage>,
    val stream: Boolean = false
)

/**
 * GLM API的配置数据类
 */
data class GLMConfig(
    val apiKey: String,
    val model: String = "glm-4.6V-Flash",
    val baseUrl: String = "https://open.bigmodel.cn/api/paas/v4/chat/completions"
)

/**
 * 使用GLM API的目标解析器
 */
class GLMTargetResolver(
    private val config: GLMConfig
) : TargetResolver {
    
    private val httpClient = HttpClient(CIO)
    
    private val json = Json {
        ignoreUnknownKeys = true
    }
    
    override suspend fun resolve(userIntent: String): TargetSpec {
        // 构建提示词
        val prompt = buildPrompt(userIntent)
        
        // 构建请求
        val request = GLMRequest(
            model = config.model,
            messages = listOf(
                GLMMessage(
                    role = "user",
                    content = prompt
                )
            ),
            stream = false
        )
        
        try {
            println("[GLM API] 发送请求：")
            println("- 模型：${config.model}")
            println("- 端点：${config.baseUrl}")
            println("- 用户意图：$userIntent")
            
            // 发送请求到GLM API
            val response: HttpResponse = httpClient.post(config.baseUrl) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer ${config.apiKey}")
                }
                setBody(json.encodeToString(request))
            }
            
            // 解析响应
            val responseText = response.bodyAsText()
            println("[GLM API] 响应状态：${response.status}")
            println("[GLM API] 响应内容：$responseText")
            
            // 解析GLM响应
            val glmResponse = json.decodeFromString<GLMResponse>(responseText)
            
            // 解析GLM返回的内容为TargetSpec
            val content = glmResponse.choices?.firstOrNull()?.message?.content ?: ""
            if (content.isBlank()) {
                println("[GLM API] 警告：返回内容为空，使用默认解析")
                return parseDefaultTargetSpec(userIntent)
            }
            
            return parseGLMContent(content)
        } catch (e: Exception) {
            // 异常处理，打印详细错误信息
            println("[GLM API] 错误：${e.message}")
            e.printStackTrace()
            // 使用默认解析
            return parseDefaultTargetSpec(userIntent)
        }
    }
    
    /**
     * 解析用户意图为默认的TargetSpec（当GLM API调用失败时使用）
     */
    private fun parseDefaultTargetSpec(userIntent: String): TargetSpec {
        println("[默认解析] 处理用户意图：$userIntent")
        
        // 根据用户意图关键词返回相应的TargetSpec
        return when {
            userIntent.contains("设置") || userIntent.contains("settings") -> {
                StateTarget("Settings", 0.8)
            }
            userIntent.contains("搜索") || userIntent.contains("search") -> {
                ComponentTarget(
                    componentId = "search_bar",
                    componentType = "EditText",
                    confidence = 0.8
                )
            }
            userIntent.contains("提交") || userIntent.contains("submit") -> {
                ComponentTarget(
                    componentId = "submit_button",
                    componentType = "Button",
                    confidence = 0.8
                )
            }
            userIntent.contains("刷新") || userIntent.contains("refresh") -> {
                ComponentTarget(
                    componentId = "refresh_button",
                    componentType = "Button",
                    confidence = 0.8
                )
            }
            userIntent.contains("主页") || userIntent.contains("home") -> {
                StateTarget("Main", 0.8)
            }
            userIntent.contains("第二层级") || userIntent.contains("second level") -> {
                ComponentTarget(
                    componentId = "btnToSecond",
                    componentType = "BUTTON",
                    componentText = "跳转到第二层级",
                    confidence = 0.8
                )
            }
            userIntent.contains("第三层级") || userIntent.contains("third level") -> {
                ComponentTarget(
                    componentId = "btnToThird",
                    componentType = "BUTTON",
                    componentText = "跳转到第三层级",
                    confidence = 0.8
                )
            }
            userIntent.contains("返回") || userIntent.contains("back") -> {
                ComponentTarget(
                    componentId = "btnBack",
                    componentType = "BUTTON",
                    componentText = "返回",
                    confidence = 0.8
                )
            }
            userIntent.contains("普通按钮") -> {
                ComponentTarget(
                    componentId = "btnNormal",
                    componentType = "BUTTON",
                    componentText = "普通按钮",
                    confidence = 0.8
                )
            }
            userIntent.contains("图标按钮") -> {
                ComponentTarget(
                    componentId = "btnIcon",
                    componentType = "BUTTON",
                    componentText = "图标按钮",
                    confidence = 0.8
                )
            }
            else -> {
                // 默认返回Main状态
                StateTarget("Main", 0.5)
            }
        }
    }
    
    /**
     * 构建GLM API的提示词
     */
    private fun buildPrompt(userIntent: String): String {
        return """请将用户的自然语言指令转换为结构化的TargetSpec，用于Android UI自动操作的路径规划。

TargetSpec有两种类型：
1. StateTarget：用于描述目标状态
2. ComponentTarget：用于描述目标组件

StateTarget格式：
{"type":"StateTarget","stateId":"目标状态ID","confidence":置信度}

ComponentTarget格式：
{"type":"ComponentTarget","componentId":"组件ID","componentType":"组件类型","componentText":"组件文本","componentRole":"组件角色","confidence":置信度}

请严格按照以下要求生成TargetSpec：

1. 状态ID必须是UI地图中实际存在的状态，包括：
   - Main：主页面状态
   - Second：第二层级页面状态
   - Third：第三层级页面状态

2. 组件ID必须是UI地图中实际存在的组件，包括：
   - btnNormal：普通按钮，文本为"普通按钮"，位于Main状态
   - btnIcon：图标按钮，文本为"图标按钮"，位于Main状态
   - btnToSecond1：跳转到第二层级1的按钮，文本为"跳转到第二层级1"，位于Main状态
   - btnToSecond2：跳转到第二层级2的按钮，文本为"跳转到第二层级2"，位于Main状态
   - btnBack：返回按钮，文本为"返回第一层级"或"返回第二层级"，位于Second或Third状态
   - btnToThird1：跳转到第三层级1的按钮，文本为"跳转到第三层级1"，位于Second状态
   - 待办任务按钮：用于查看待办任务，位于Second状态

3. 组件类型必须使用以下英文名称：
   - BUTTON：按钮组件
   - TEXT_VIEW：文本组件
   - IMAGE_BUTTON：图标按钮组件

4. 置信度必须是0-1之间的数字

5. 只返回JSON格式，不要包含其他内容

6. 请根据用户意图的实际含义，返回最匹配的TargetSpec：
   - 如果用户意图是查看待办任务，请返回待办任务按钮的ComponentTarget，该按钮位于Second状态
   - 如果用户意图是点击普通按钮，请返回componentId为btnNormal的ComponentTarget
   - 如果用户意图是跳转到第二层级，请返回componentId为btnToSecond1或btnToSecond2的ComponentTarget
   - 如果用户意图是查看列表，请返回列表组件的ComponentTarget

7. 重要提示：
   - 路径规划需要到达最终的按钮，而不是到达某一页面就结束
   - 例如，查看待办任务需要先从Main状态跳转到Second状态，然后在Second状态点击待办任务按钮
   - 请确保返回的TargetSpec能够引导路径规划器找到最终的目标按钮

用户意图：$userIntent
"""
    }
    
    /**
     * 解析GLM返回的内容为TargetSpec
     */
    private fun parseGLMContent(content: String): TargetSpec {
        // 清理内容，确保只包含JSON
        val cleanedContent = content.trim().removePrefix("```json").removeSuffix("```").trim()
        
        try {
            // 尝试直接解析为TargetSpec
            return json.decodeFromString<TargetSpec>(cleanedContent)
        } catch (e: Exception) {
            // 如果直接解析失败，返回默认目标
            e.printStackTrace()
            return StateTarget("Main", 0.5)
        }
    }
}

/**
 * GLM API的工厂类，用于创建GLMTargetResolver
 */
object GLMTargetResolverFactory {
    /**
     * 创建GLMTargetResolver实例
     */
    fun create(apiKey: String, model: String = "glm-4.6V-Flash"): GLMTargetResolver {
        val config = GLMConfig(
            apiKey = apiKey,
            model = model
        )
        return GLMTargetResolver(config)
    }
}