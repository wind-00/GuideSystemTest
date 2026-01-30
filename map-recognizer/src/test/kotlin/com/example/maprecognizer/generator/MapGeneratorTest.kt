package com.example.maprecognizer.generator

import com.example.maprecognizer.analyzer.NavigationInfo
import com.example.maprecognizer.analyzer.NavigationTarget
import com.example.maprecognizer.analyzer.NavigationTargetType
import com.example.maprecognizer.analyzer.UIComponentInfo
import com.example.maprecognizer.analyzer.ComponentEvent
import com.example.maprecognizer.data.AppAutomationMap
import com.example.maprecognizer.data.UiFramework
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 地图生成器单元测试
 */
class MapGeneratorTest {
    
    /**
     * 测试地图生成器生成AppAutomationMap
     */
    @Test
    fun testGenerateAppAutomationMap() {
        // 创建测试数据
        val navigationInfo = listOf(
            NavigationInfo(
                screenName = "Home",
                route = "/",
                navigationTargets = listOf(
                    NavigationTarget(
                        type = NavigationTargetType.NAVIGATE,
                        target = "DoctorList"
                    )
                )
            ),
            NavigationInfo(
                screenName = "DoctorList",
                route = "/doctor_list",
                navigationTargets = emptyList()
            )
        )
        
        val uiComponents = listOf(
            UIComponentInfo(
                screenName = "Home",
                componentId = "home_button",
                componentName = "HomeButton",
                componentType = "BUTTON",
                properties = mapOf(
                    "text" to "医生管理",
                    "enabled" to true
                ),
                events = listOf(
                    ComponentEvent(
                        eventType = "CLICK",
                        target = "DoctorList",
                        parameters = emptyMap()
                    )
                )
            )
        )
        
        // 创建地图生成器
        val generator = MapGenerator()
        
        // 生成地图
        val map = generator.generateAppAutomationMap(navigationInfo, uiComponents)
        
        // 验证数据
        assertNotNull(map)
        assertEquals("GuideSystemTest", map.appMeta.appName)
        assertEquals(UiFramework.COMPOSE, map.appMeta.uiFramework)
        
        // 验证UI模型
        assertNotNull(map.uiModel)
        assertEquals(2, map.uiModel.pages.size)
        
        // 验证状态模型
        assertNotNull(map.stateModel)
        assertEquals(2, map.stateModel.states.size)
        assertEquals("Home", map.stateModel.initialStateId)
        
        // 验证意图模型
        assertNotNull(map.intentModel)
        assertEquals(1, map.intentModel.intents.size)
        val intent = map.intentModel.intents[0]
        assertEquals(1, intent.expectedNextStateIds.size) // 显式导航有明确的预期状态
        assertEquals("DoctorList", intent.expectedNextStateIds[0])
    }
    
    /**
     * 测试地图生成器生成包含多个页面的地图
     */
    @Test
    fun testGenerateAppAutomationMapMultiplePages() {
        // 创建测试数据
        val navigationInfo = listOf(
            NavigationInfo(
                screenName = "Home",
                route = "/",
                navigationTargets = listOf(
                    NavigationTarget(
                        type = NavigationTargetType.NAVIGATE,
                        target = "Page1"
                    ),
                    NavigationTarget(
                        type = NavigationTargetType.NAVIGATE,
                        target = "Page2"
                    )
                )
            ),
            NavigationInfo(
                screenName = "Page1",
                route = "/page1",
                navigationTargets = listOf(
                    NavigationTarget(
                        type = NavigationTargetType.NAVIGATE_BACK,
                        target = ""
                    )
                )
            ),
            NavigationInfo(
                screenName = "Page2",
                route = "/page2",
                navigationTargets = listOf(
                    NavigationTarget(
                        type = NavigationTargetType.NAVIGATE_BACK,
                        target = ""
                    )
                )
            )
        )
        
        val uiComponents = listOf(
            UIComponentInfo(
                screenName = "Home",
                componentId = "home_to_page1",
                componentName = "HomeToPage1Button",
                componentType = "BUTTON",
                properties = mapOf(
                    "text" to "Go to Page1",
                    "enabled" to true
                ),
                events = listOf(
                    ComponentEvent(
                        eventType = "CLICK",
                        target = "Page1",
                        parameters = emptyMap()
                    )
                )
            ),
            UIComponentInfo(
                screenName = "Home",
                componentId = "home_to_page2",
                componentName = "HomeToPage2Button",
                componentType = "BUTTON",
                properties = mapOf(
                    "text" to "Go to Page2",
                    "enabled" to true
                ),
                events = listOf(
                    ComponentEvent(
                        eventType = "CLICK",
                        target = "Page2",
                        parameters = emptyMap()
                    )
                )
            )
        )
        
        // 创建地图生成器
        val generator = MapGenerator()
        
        // 生成地图
        val map = generator.generateAppAutomationMap(navigationInfo, uiComponents)
        
        // 验证数据
        assertNotNull(map)
        assertEquals(3, map.uiModel.pages.size) // 3个页面
        assertEquals(3, map.stateModel.states.size) // 3个状态（1:1映射）
        assertEquals(2, map.intentModel.intents.size) // 2个意图
        
        // 验证每个状态都关联了一个页面（1:1映射）
        for (state in map.stateModel.states) {
            assertEquals(1, state.relatedPageIds.size)
        }
        
        // 验证意图的expectedNextStateIds
        for (intent in map.intentModel.intents) {
            assertNotNull(intent.expectedNextStateIds)
            assertTrue(intent.expectedNextStateIds.size == 1) // 显式导航有明确的预期状态
        }
    }
}
