package com.example.maprecognizer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * 数据模型单元测试
 */
class DataModelTest {
    
    /**
     * 测试AppAutomationMap数据模型
     */
    @Test
    fun testAppAutomationMap() {
        // 创建测试数据
        val appMeta = AppMeta(
            appName = "TestApp",
            packageName = "com.example.testapp",
            versionName = "1.0.0",
            versionCode = 1,
            uiFramework = UiFramework.COMPOSE
        )
        
        val uiModel = UiModel(
            pages = emptyList()
        )
        
        val stateModel = StateModel(
            states = emptyList(),
            initialStateId = "Home"
        )
        
        val intentModel = IntentModel(
            intents = emptyList()
        )
        
        // 创建AppAutomationMap
        val map = AppAutomationMap(appMeta, uiModel, stateModel, intentModel)
        
        // 验证数据
        assertNotNull(map)
        assertEquals("TestApp", map.appMeta.appName)
        assertNotNull(map.uiModel)
        assertNotNull(map.stateModel)
        assertNotNull(map.intentModel)
    }
    
    /**
     * 测试State数据模型
     */
    @Test
    fun testState() {
        // 创建测试数据
        val signals = listOf(
            StateSignal(
                type = SignalType.PAGE_ACTIVE,
                target = "Home",
                expectedValue = null,
                matcher = SignalMatcher.EQUALS
            )
        )
        
        // 创建State
        val state = State(
            stateId = "Home",
            name = "Home",
            description = "首页",
            signals = signals,
            relatedPageIds = listOf("Home") // 1:1映射，只有一个元素
        )
        
        // 验证数据
        assertNotNull(state)
        assertEquals("Home", state.stateId)
        assertEquals(1, state.relatedPageIds.size) // 验证1:1映射
        assertEquals("Home", state.relatedPageIds[0])
        assertNotNull(state.signals)
        assertEquals(1, state.signals.size)
    }
    
    /**
     * 测试Intent数据模型
     */
    @Test
    fun testIntent() {
        // 创建测试数据
        val uiBindings = listOf(
            UIActionBinding(
                componentId = "test_button",
                trigger = TriggerType.CLICK,
                parameters = emptyMap()
            )
        )
        
        // 创建Intent
        val intent = Intent(
            intentId = "test_intent",
            type = IntentType.CLICK,
            description = "测试意图",
            uiBindings = uiBindings,
            expectedNextStateIds = listOf("TargetPage")
        )
        
        // 验证数据
        assertNotNull(intent)
        assertEquals("test_intent", intent.intentId)
        assertNotNull(intent.uiBindings)
        assertEquals(1, intent.uiBindings.size)
        assertEquals("test_button", intent.uiBindings[0].componentId)
        assertEquals(TriggerType.CLICK, intent.uiBindings[0].trigger)
        assertEquals(listOf("TargetPage"), intent.expectedNextStateIds)
    }
    
    /**
     * 测试空expectedNextStateIds的Intent
     */
    @Test
    fun testIntentWithEmptyExpectedNextStateIds() {
        // 创建测试数据
        val uiBindings = listOf(
            UIActionBinding(
                componentId = "test_button",
                trigger = TriggerType.CLICK,
                parameters = emptyMap()
            )
        )
        
        // 创建Intent（expectedNextStateIds为空）
        val intent = Intent(
            intentId = "test_intent",
            type = IntentType.CLICK,
            description = "测试意图",
            uiBindings = uiBindings,
            expectedNextStateIds = emptyList() // 空expectedNextStateIds，符合规则
        )
        
        // 验证数据
        assertNotNull(intent)
        assertEquals(emptyList<String>(), intent.expectedNextStateIds)
    }
}
