package com.example.pathplanner

import com.example.pathplanner.models.UIMap
import com.example.pathplanner.resolver.GLMTargetResolverFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Scanner

/**
 * 路径规划器人机交互测试程序
 */
fun main() {
    // 创建Scanner用于读取用户输入
    val scanner = Scanner(System.`in`)
    
    println("=== Android UI自动操作系统路径规划器 ===")
    println("本程序使用GLM API进行目标解析，输出结构化的路径规划结果")
    println()
    
    // 从用户提供的API密钥创建GLM目标解析器
    val apiKey = "17fb3e69b36f471ab107c7605797f8bf.wMPUTygxdEPkBQi3"
    val glmResolver = GLMTargetResolverFactory.create(apiKey)
    
    // 创建路径规划器
    val pathPlanner = PathPlanner(targetResolver = glmResolver)
    
    // 加载实际的UI地图文件 - 使用map-recognizer模块生成的path-planner格式地图
    val uiMap = loadUIMapFromFile("c:\\Users\\13210\\AndroidStudioProjects\\GuideSystemTest\\ui_map.json")
    
    // 定义符合实际应用的测试用例，增加跨层级组件交互测试
    val testCases = listOf(
        TestCase(1, "点击普通按钮", "点击普通按钮"),
        TestCase(2, "点击跳转到第二层级1", "跳转到第二层级1"),
        TestCase(3, "点击第二层级1的返回按钮", "点击第二层级1的返回按钮"),
        TestCase(4, "点击已完成任务按钮", "点击已完成任务按钮"),
        TestCase(5, "点击待办任务按钮", "点击待办任务按钮"),
        TestCase(6, "从第二层级跳转到第三层级", "从第二层级跳转到第三层级"),
        TestCase(7, "从第三层级返回第二层级", "从第三层级返回第二层级"),
        TestCase(8, "点击第二层级2的返回按钮", "点击第二层级2的返回按钮")
    )
    
    // 自动运行所有测试，无需用户输入
    println("\n开始自动运行所有测试...")
    var passed = 0
    var failed = 0
    
    for (testCase in testCases) {
        println("\n=== 测试：${testCase.description} ===")
        println("操作目的：${testCase.userIntent}")
        try {
            runBlocking {
                executePlanning(testCase.userIntent, pathPlanner, uiMap)
            }
            passed++
            println("? 测试通过")
        } catch (e: Exception) {
            failed++
            println("? 测试失败：${e.message}")
            e.printStackTrace()
        }
        
        // 添加延迟，避免并发调用API导致限额问题
        println("\n等待2秒后继续下一个测试...")
        Thread.sleep(2000)
    }
    
    println("\n=== 测试结果汇总 ===")
    println("总测试数：${testCases.size}")
    println("通过：$passed")
    println("失败：$failed")
    println("通过率：${(passed.toDouble() / testCases.size * 100).toInt()}%")
    
    // 循环接收用户输入（可选）
    println("\n是否需要手动测试？(y/n)")
    val option = scanner.nextLine().trim().toLowerCase()
    
    if (option == "y") {
        while (true) {
            // 显示菜单
            println("\n请选择操作：")
            println("1. 输入自定义操作目的")
            println("2. 运行默认测试用例")
            println("3. 退出程序")
            print("请输入选项(1-3)：")
            
            val userOption = scanner.nextLine().trim()
            
            when (userOption) {
                "1" -> {
                    // 自定义操作目的
                    println("\n请输入您的操作目的：")
                    val userIntent = scanner.nextLine().trim()
                    if (userIntent.isNotBlank()) {
                        runBlocking {
                            executePlanning(userIntent, pathPlanner, uiMap)
                        }
                    }
                }
                "2" -> {
                    // 运行默认测试用例
                    println("\n可用测试用例：")
                    testCases.forEach { testCase ->
                        println("${testCase.id}. ${testCase.description}")
                    }
                    println("0. 返回上一级")
                    print("请选择测试用例(0-${testCases.size})：")
                    
                    val testOption = scanner.nextLine().trim()
                    if (testOption == "0") continue
                    
                    val testCase = testCases.find { it.id.toString() == testOption }
                    if (testCase != null) {
                        println("\n正在执行测试：${testCase.description}")
                        println("操作目的：${testCase.userIntent}")
                        runBlocking {
                            executePlanning(testCase.userIntent, pathPlanner, uiMap)
                        }
                    } else {
                        println("无效的测试用例编号！")
                    }
                }
                "3" -> {
                    // 退出程序
                    println("程序结束，谢谢使用！")
                    break
                }
                else -> {
                    println("无效的选项，请重新输入！")
                }
            }
            
            println()
        }
    }
    
    scanner.close()
}

/**
 * 测试用例数据类
 */
data class TestCase(
    val id: Int,
    val description: String,
    val userIntent: String
)

/**
 * 从JSON文件加载UI地图
 */
fun loadUIMapFromFile(filePath: String): UIMap {
    println("[InteractiveTest] 加载UI地图: $filePath")
    
    // 读取JSON文件内容
    val jsonContent = File(filePath).readText()
    
    // 简化JSON配置，忽略未知键
    val json = Json {
        coerceInputValues = true
        ignoreUnknownKeys = true
    }
    
    // 解析JSON为UIMapData结构
    val uiMapData = json.decodeFromString<UIMapData>(jsonContent)
    
    // 转换为UIMap模型
    val statesMap = mutableMapOf<String, com.example.pathplanner.models.State>()
    
    // 遍历所有页面，转换为State对象
    for (page in uiMapData.pages) {
        val stateId = page.pageId
        
        // 转换组件
        val components = page.components.map { component ->
            // 转换触发器为Intent对象
            val intents = component.triggers.mapIndexed { index, trigger ->
                // 获取触发器效果
                val effect = trigger.effect
                
                // 确定目标状态ID
                val targetStateId = when (effect.effectType) {
                    "NAVIGATION" -> {
                        // 导航效果
                        when (effect.navigationRole) {
                            "BACK" -> {
                                // 对于返回操作，尝试从possibleTargetPageIds获取目标页面
                                // 如果没有，则使用上一个页面（这里简化处理，使用MainActivity）
                                effect.possibleTargetPageIds?.firstOrNull() ?: "MainActivity"
                            }
                            "ENTER_FLOW" -> {
                                // 对于进入新流程，使用目标页面ID
                                effect.targetPageId ?: stateId
                            }
                            else -> {
                                // 默认情况下，状态不变
                                stateId
                            }
                        }
                    }
                    else -> {
                        // 非导航操作，状态不变
                        stateId
                    }
                }
                
                com.example.pathplanner.models.Intent(
                    intentId = "${component.componentId}_${trigger.triggerType}_$index",
                    type = trigger.triggerType,
                    targetStateId = targetStateId,
                    description = "${trigger.triggerType} on ${component.componentId}",
                    parameters = emptyMap()
                )
            }
            
            com.example.pathplanner.models.Component(
                componentId = component.componentId,
                type = component.viewType,
                text = component.canonicalIntent,
                properties = mapOf(
                    "semanticRole" to component.semanticRole,
                    "intentTags" to component.intentTags.joinToString(",")
                ),
                intents = intents
            )
        }
        
        // 创建State对象
        val state = com.example.pathplanner.models.State(
            stateId = stateId,
            components = components,
            intents = emptyList(),
            description = stateId
        )
        
        statesMap[stateId] = state
    }
    
    // 创建并返回UIMap对象
    return UIMap(
        states = statesMap,
        version = "1.0"
    )
}

/**
 * UI地图数据类，用于解析JSON文件
 */
@kotlinx.serialization.Serializable
data class UIMapData(
    val pages: List<Page>
)

@kotlinx.serialization.Serializable
data class Page(
    val pageId: String,
    val components: List<ComponentData>,
    val entryPoint: Boolean
)

@kotlinx.serialization.Serializable
data class ComponentData(
    val componentId: String,
    val viewType: String,
    val semanticRole: String,
    val intentTags: List<String>,
    val triggers: List<Trigger>,
    val canonicalIntent: String
)

@kotlinx.serialization.Serializable
data class Trigger(
    val triggerType: String,
    val effect: Effect
)

@kotlinx.serialization.Serializable
data class Effect(
    val effectType: String,
    val stateScope: String? = null,
    val stateKey: String? = null,
    val stateDelta: String? = null,
    val stateType: String? = null,
    val isReversible: Boolean? = null,
    val stateImpact: String? = null,
    val navigationRole: String? = null,
    val targetPageId: String? = null,
    val possibleTargetPageIds: List<String>? = null,
    val interactionRole: String? = null
)


/**
 * 创建符合实际应用的UI地图
 */
fun createRealisticUIMap(): UIMap {
    // 创建Main状态的组件
    val btnNormal = com.example.pathplanner.models.Component(
        componentId = "btnNormal",
        type = "BUTTON",
        text = "普通按钮",
        intents = listOf(
            com.example.pathplanner.models.Intent(
                intentId = "btnNormal_click_fromMain",
                type = "CLICK",
                targetStateId = "Main",
                description = "点击普通按钮"
            )
        )
    )
    
    val btnIcon = com.example.pathplanner.models.Component(
        componentId = "btnIcon",
        type = "BUTTON",
        text = "图标按钮",
        intents = listOf(
            com.example.pathplanner.models.Intent(
                intentId = "btnIcon_click_fromMain",
                type = "CLICK",
                targetStateId = "Main",
                description = "点击图标按钮"
            )
        )
    )
    
    val btnToSecond1 = com.example.pathplanner.models.Component(
        componentId = "btnToSecond1",
        type = "BUTTON",
        text = "跳转到第二层级1",
        intents = listOf(
            com.example.pathplanner.models.Intent(
                intentId = "btnToSecond1_click_fromMain",
                type = "CLICK",
                targetStateId = "Second",
                description = "跳转到第二层级1"
            )
        )
    )
    
    val btnToSecond2 = com.example.pathplanner.models.Component(
        componentId = "btnToSecond2",
        type = "BUTTON",
        text = "跳转到第二层级2",
        intents = listOf(
            com.example.pathplanner.models.Intent(
                intentId = "btnToSecond2_click_fromMain",
                type = "CLICK",
                targetStateId = "Second",
                description = "跳转到第二层级2"
            )
        )
    )
    
    // 创建Main状态
    val mainState = com.example.pathplanner.models.State(
        stateId = "Main",
        components = listOf(btnNormal, btnIcon, btnToSecond1, btnToSecond2),
        intents = emptyList(),
        description = "主页面"
    )
    
    // 创建Second状态的组件
    val btnBack = com.example.pathplanner.models.Component(
        componentId = "btnBack",
        type = "BUTTON",
        text = "返回第一层级",
        intents = listOf(
            com.example.pathplanner.models.Intent(
                intentId = "btnBack_click_fromSecond",
                type = "CLICK",
                targetStateId = "Main",
                description = "返回第一层级"
            )
        )
    )
    
    val btnToThird1 = com.example.pathplanner.models.Component(
        componentId = "btnToThird1",
        type = "BUTTON",
        text = "跳转到第三层级1",
        intents = listOf(
            com.example.pathplanner.models.Intent(
                intentId = "btnToThird1_click_fromSecond",
                type = "CLICK",
                targetStateId = "Third",
                description = "跳转到第三层级1"
            )
        )
    )
    
    // 创建Second状态
    val secondState = com.example.pathplanner.models.State(
        stateId = "Second",
        components = listOf(btnBack, btnToThird1),
        intents = emptyList(),
        description = "第二层级页面"
    )
    
    // 创建Third状态的组件
    val btnBackFromThird = com.example.pathplanner.models.Component(
        componentId = "btnBack",
        type = "BUTTON",
        text = "返回第二层级",
        intents = listOf(
            com.example.pathplanner.models.Intent(
                intentId = "btnBack_click_fromThird",
                type = "CLICK",
                targetStateId = "Second",
                description = "返回第二层级"
            )
        )
    )
    
    // 创建Third状态
    val thirdState = com.example.pathplanner.models.State(
        stateId = "Third",
        components = listOf(btnBackFromThird),
        intents = emptyList(),
        description = "第三层级页面"
    )
    
    // 创建UI地图
    return UIMap(
        states = mapOf(
            "Main" to mainState,
            "Second" to secondState,
            "Third" to thirdState
        )
    )
}

/**
 * 执行路径规划
 */
suspend fun executePlanning(userIntent: String, pathPlanner: PathPlanner, uiMap: UIMap) {
    println("\n正在规划路径，请稍候...")
    
    val (result, allPathResults) = pathPlanner.plan(userIntent, uiMap)
    
    // 输出所有可达成目的路径结果
    println("\n=== 可达成目的的路径结果 ===")
    println()
    
    if (allPathResults.isEmpty()) {
        println("没有找到可达成目的的路径")
    } else {
        for ((pathIndex, pathResult) in allPathResults.withIndex()) {
            println("路径 ${pathIndex + 1}：")
            println("   路径：${pathResult.path.joinToString { it.intentId }}")
            println("   状态序列：${pathResult.stateSequence.joinToString(" → ")}")
            println("   步骤数：${pathResult.path.size}")
            println()
        }
    }
    
    println("=== 路径规划结果 ===")
    println()
    
    // 输出目标规范
    println("1. 目标规范：")
    println("   ${result.target}")
    println()
    
    // 输出规划假设
    println("2. 规划假设：")
    println("   ${result.assumptions}")
    println()
    
    // 输出规划路径
    println("3. 规划路径：")
    if (result.plannedPath.isEmpty()) {
        println("   已在目标状态，无需操作")
    } else {
        for ((index, step) in result.plannedPath.withIndex()) {
            println("   步骤 ${index + 1}：")
            println("   - 意图：${step.intent}")
            println("   - 从状态：${step.fromStateId}")
            println("   - 期望状态：${step.expectedStateId}")
            println("   - UI绑定：")
            println("     * 组件ID：${step.uiBinding.componentId}")
            println("     * 触发方式：${step.uiBinding.trigger}")
            if (step.uiBinding.parameters.isNotEmpty()) {
                println("     * 参数：${step.uiBinding.parameters}")
            }
            println()
        }
    }
    
    println("=== 规划完成 ===")
}

