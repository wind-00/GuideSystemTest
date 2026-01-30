package com.example.pathplanner

import com.example.pathplanner.generator.ExecutionFlowGenerator
import com.example.pathplanner.locator.TargetLocator
import com.example.pathplanner.models.*
import com.example.pathplanner.resolver.MockAITargetResolver
import com.example.pathplanner.resolver.TargetResolver
import com.example.pathplanner.search.PathResult
import com.example.pathplanner.search.PathSearcher
import com.example.pathplanner.selector.DefaultPathSelector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PathPlannerTest {
    
    private lateinit var uiMap: UIMap
    private lateinit var pathPlanner: PathPlanner
    
    @Before
    fun setup() {
        // 创建测试用的UI地图
        uiMap = createTestUIMap()
        
        // 初始化路径规划器
        pathPlanner = PathPlanner(
            targetResolver = MockAITargetResolver()
        )
    }
    
    /**
     * 创建测试用的UI地图
     */
    private fun createTestUIMap(): UIMap {
        // 创建组件
        val button1 = Component(
            componentId = "button1",
            type = "Button",
            text = "打开设置",
            intents = listOf(
                Intent(
                    intentId = "open_settings",
                    type = "CLICK",
                    targetStateId = "Settings",
                    description = "打开设置页面"
                )
            )
        )
        
        val button2 = Component(
            componentId = "button2",
            type = "Button",
            text = "返回主页",
            intents = listOf(
                Intent(
                    intentId = "go_home",
                    type = "CLICK",
                    targetStateId = "Main",
                    description = "返回主页面"
                )
            )
        )
        
        val searchBar = Component(
            componentId = "search_bar",
            type = "EditText",
            text = "搜索",
            intents = listOf(
                Intent(
                    intentId = "search",
                    type = "TEXT",
                    targetStateId = "SearchResults",
                    description = "执行搜索",
                    parameters = mapOf("text" to "测试")
                )
            )
        )
        
        // 创建状态
        val mainState = State(
            stateId = "Main",
            components = listOf(button1, searchBar),
            intents = emptyList(),
            description = "主页面"
        )
        
        val settingsState = State(
            stateId = "Settings",
            components = listOf(button2),
            intents = emptyList(),
            description = "设置页面"
        )
        
        val searchResultsState = State(
            stateId = "SearchResults",
            components = listOf(button2),
            intents = emptyList(),
            description = "搜索结果页面"
        )
        
        // 创建UI地图
        return UIMap(
            states = mapOf(
                "Main" to mainState,
                "Settings" to settingsState,
                "SearchResults" to searchResultsState
            )
        )
    }
    
    /**
     * 测试目标解析功能
     */
    @Test
    fun testTargetResolver() {
        val resolver = MockAITargetResolver()
        
        runBlocking {
            // 测试状态目标解析
            val settingsTarget = resolver.resolve("打开设置")
            assertTrue(settingsTarget is StateTarget)
            assertEquals("Settings", (settingsTarget as StateTarget).stateId)
            assertEquals("StateTarget", settingsTarget.type)
            
            // 测试状态目标解析（搜索意图现在返回StateTarget）
            val searchTarget = resolver.resolve("搜索")
            assertTrue(searchTarget is StateTarget)
            assertEquals("SearchResults", (searchTarget as StateTarget).stateId)
            assertEquals("StateTarget", searchTarget.type)
            
            // 测试组件目标解析（使用按钮意图）
            val buttonTarget = resolver.resolve("点击按钮")
            assertTrue(buttonTarget is ComponentTarget)
            assertEquals("Button", (buttonTarget as ComponentTarget).componentType)
            assertEquals("点击", buttonTarget.componentText)
            assertEquals("ComponentTarget", buttonTarget.type)
        }
    }
    
    /**
     * 测试目标定位功能
     */
    @Test
    fun testTargetLocator() {
        val locator = TargetLocator()
        
        // 测试状态目标定位
        val stateTarget = StateTarget("Settings", 0.9)
        val stateTargetIds = locator.locate(uiMap, stateTarget)
        assertEquals(setOf("Settings"), stateTargetIds)
        
        // 测试组件目标定位（使用componentType和componentText）
        val componentTarget = ComponentTarget(
            componentType = "EditText", 
            componentText = "搜索", 
            confidence = 0.85
        )
        val componentTargetIds = locator.locate(uiMap, componentTarget)
        assertEquals(setOf("Main"), componentTargetIds)
        
        // 测试组件目标定位（使用componentId）
        val componentIdTarget = ComponentTarget(
            componentId = "button1", 
            confidence = 0.9
        )
        val componentIdTargetIds = locator.locate(uiMap, componentIdTarget)
        assertEquals(setOf("Main"), componentIdTargetIds)
    }
    
    /**
     * 测试路径搜索功能
     */
    @Test
    fun testPathSearcher() {
        val searcher = PathSearcher()
        
        // 测试从Main到Settings的路径搜索
        val pathResults = searcher.search(uiMap, "Main", setOf("Settings"))
        assertEquals(1, pathResults.size)
        
        val pathResult = pathResults[0]
        assertEquals(1, pathResult.path.size)
        assertEquals("open_settings", pathResult.path[0].intentId)
        assertEquals(listOf("Main", "Settings"), pathResult.stateSequence)
        
        // 测试从Main到SearchResults的路径搜索
        val searchPathResults = searcher.search(uiMap, "Main", setOf("SearchResults"))
        assertEquals(1, searchPathResults.size)
        assertEquals(1, searchPathResults[0].path.size)
        assertEquals("search", searchPathResults[0].path[0].intentId)
        assertEquals(listOf("Main", "SearchResults"), searchPathResults[0].stateSequence)
        
        // 测试searchIntents方法
        val intentsPaths = searcher.searchIntents(uiMap, "Main", setOf("Settings"))
        assertEquals(1, intentsPaths.size)
        assertEquals(1, intentsPaths[0].size)
        
        // 测试searchShortestPath方法
        val shortestPathResult = searcher.searchShortestPath(uiMap, "Main", setOf("Settings"))
        assertNotNull(shortestPathResult)
        assertEquals(1, shortestPathResult!!.path.size)
        
        // 测试searchShortestPathIntents方法
        val shortestIntentsPath = searcher.searchShortestPathIntents(uiMap, "Main", setOf("Settings"))
        assertNotNull(shortestIntentsPath)
        assertEquals(1, shortestIntentsPath!!.size)
    }
    
    /**
     * 测试路径选择功能
     */
    @Test
    fun testPathSelector() {
        val selector = DefaultPathSelector()
        
        // 创建测试路径结果
        val path1 = listOf(
            Intent("intent1", "CLICK", "State2"),
            Intent("intent2", "CLICK", "State3")
        )
        val pathResult1 = PathResult(path1, listOf("State1", "State2", "State3"))
        
        val path2 = listOf(
            Intent("intent3", "CLICK", "State3")
        )
        val pathResult2 = PathResult(path2, listOf("State1", "State3"))
        
        val pathResults = listOf(pathResult1, pathResult2)
        
        // 测试选择最短路径
        val selectedPathResult = selector.select(pathResults)
        assertNotNull(selectedPathResult)
        assertEquals(1, selectedPathResult!!.path.size)
        assertEquals("intent3", selectedPathResult.path[0].intentId)
        assertEquals(listOf("State1", "State3"), selectedPathResult.stateSequence)
        
        // 测试兼容旧接口的select方法
        val selectedPath = selector.select(listOf(path1, path2))
        assertNotNull(selectedPath)
        assertEquals(1, selectedPath!!.size)
        assertEquals("intent3", selectedPath[0].intentId)
    }
    
    /**
     * 测试执行流程生成功能
     */
    @Test
    fun testExecutionFlowGenerator() {
        val generator = ExecutionFlowGenerator()
        
        // 创建测试路径
        val path = listOf(
            Intent("open_settings", "CLICK", "Settings")
        )
        
        // 测试使用意图路径生成执行流程
        val steps = generator.generate(uiMap, "Main", path)
        assertEquals(1, steps.size)
        
        val step = steps[0]
        assertEquals("open_settings", step.intent)
        assertEquals("Main", step.fromStateId)
        assertEquals("Settings", step.expectedStateId)
        assertEquals("button1", step.uiBinding.componentId)
        assertEquals("CLICK", step.uiBinding.trigger)
        
        // 测试使用PathResult生成执行流程
        val pathResult = PathResult(path, listOf("Main", "Settings"))
        val stepsFromResult = generator.generate(uiMap, pathResult)
        assertEquals(1, stepsFromResult.size)
        assertEquals(step, stepsFromResult[0])
        
        // 测试带有参数的意图
        val searchPath = listOf(
            Intent("search", "TEXT", "SearchResults", parameters = mapOf("text" to "测试"))
        )
        val searchSteps = generator.generate(uiMap, "Main", searchPath)
        assertEquals(1, searchSteps.size)
        assertEquals("search", searchSteps[0].intent)
        assertEquals("search_bar", searchSteps[0].uiBinding.componentId)
        assertEquals("TEXT", searchSteps[0].uiBinding.trigger)
        assertEquals(mapOf("text" to "测试"), searchSteps[0].uiBinding.parameters)
    }
    
    /**
     * 测试完整的规划流程
     */
    @Test
    fun testFullPlanningFlow() {
        runBlocking {
            // 测试规划从Main到Settings的路径
            val result = pathPlanner.plan("打开设置", uiMap)
            
            assertNotNull("规划结果不应为null", result)
            assertTrue("目标应为StateTarget", result.target is StateTarget)
            
            val stateTarget = result.target as StateTarget
            assertEquals("Settings", stateTarget.stateId)
            assertEquals("StateTarget", stateTarget.type)
            assertEquals("规划路径应包含1个步骤", 1, result.plannedPath.size)
            
            val step = result.plannedPath[0]
            assertEquals("open_settings", step.intent)
            assertEquals("Main", step.fromStateId)
            assertEquals("Settings", step.expectedStateId)
            assertEquals("button1", step.uiBinding.componentId)
            assertEquals("CLICK", step.uiBinding.trigger)
            
            assertEquals("Main", result.assumptions.startState)
            assertEquals(0.9, result.assumptions.confidence, 0.01)
        }
    }
    
    /**
     * 测试规划器异常处理
     */
    @Test
    fun testPlannerExceptions() {
        runBlocking {
            // 创建一个自定义的TargetResolver来测试异常
            val customResolver = object : TargetResolver {
                override suspend fun resolve(userIntent: String): TargetSpec {
                    return StateTarget("NonExistentState", 0.9)
                }
            }
            
            val invalidTargetPlanner = PathPlanner(
                targetResolver = customResolver
            )
            
            try {
                invalidTargetPlanner.plan("无效目标", uiMap)
                fail("应该抛出TargetNotFoundException异常")
            } catch (e: PlannerException) {
                // 验证异常类型
                assertTrue(e is TargetNotFoundException)
            }
            
            // 测试planSafe方法
            val safeResult = invalidTargetPlanner.planSafe("无效目标", uiMap)
            assertNotNull(safeResult)
            assertTrue(safeResult.target is StateTarget)
            assertEquals("ERROR", (safeResult.target as StateTarget).stateId)
            assertTrue(safeResult.plannedPath.isEmpty())
            assertEquals(0.0, safeResult.assumptions.confidence, 0.01)
        }
    }
    
    /**
     * 测试优化后的ExecutorStep结构
     */
    @Test
    fun testExecutorStepStructure() {
        runBlocking {
            // 测试规划从Main到SearchResults的路径
            val result = pathPlanner.plan("搜索", uiMap)
            
            assertNotNull(result)
            assertEquals(1, result.plannedPath.size)
            
            val step = result.plannedPath[0]
            // 验证ExecutorStep包含所有必要字段
            assertNotNull(step.intent)
            assertNotNull(step.fromStateId)
            assertNotNull(step.expectedStateId)
            assertNotNull(step.uiBinding)
            
            // 验证uiBinding包含所有必要字段
            assertNotNull(step.uiBinding.componentId)
            assertNotNull(step.uiBinding.trigger)
            assertNotNull(step.uiBinding.parameters)
            
            // 验证状态转换信息完整
            assertEquals("Main", step.fromStateId)
            assertEquals("SearchResults", step.expectedStateId)
        }
    }
}