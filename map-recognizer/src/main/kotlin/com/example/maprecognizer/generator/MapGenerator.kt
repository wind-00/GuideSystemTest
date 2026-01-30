package com.example.maprecognizer.generator

import com.example.maprecognizer.analyzer.*
import com.example.maprecognizer.data.*
import com.example.maprecognizer.data.Position
import com.example.maprecognizer.data.Size

/**
 * 地图生成器，负责将分析器提取的信息转换为最终的AppAutomationMap结构
 */
class MapGenerator {
    
    /**
     * 生成应用自动化地图
     * @param navigationInfo 导航信息列表
     * @param uiComponents UI组件信息列表
     * @return 完整的应用自动化地图
     */
    fun generateAppAutomationMap(
        navigationInfo: List<NavigationInfo>,
        uiComponents: List<UIComponentInfo>
    ): AppAutomationMap {
        return generateAppAutomationMap(navigationInfo, uiComponents, "GuideSystemTest", "com.example.guidesystemtest")
    }
    
    /**
     * 生成应用自动化地图
     * @param navigationInfo 导航信息列表
     * @param uiComponents UI组件信息列表
     * @param appName 应用名称
     * @param packageName 应用包名
     * @return 完整的应用自动化地图
     */
    fun generateAppAutomationMap(
        navigationInfo: List<NavigationInfo>,
        uiComponents: List<UIComponentInfo>,
        appName: String,
        packageName: String
    ): AppAutomationMap {
        // 1. 构建应用元信息
        val appMeta = AppMeta(
            appName = appName,
            packageName = packageName,
            versionName = "1.0.0",
            versionCode = 1,
            uiFramework = UiFramework.VIEW
        )
        
        // 2. 构建UI模型
        val uiModel = buildUIModel(navigationInfo, uiComponents)
        
        // 3. 构建状态模型
        val stateModel = buildStateModel(navigationInfo)
        
        // 4. 构建意图模型
        val intentModel = buildIntentModel(navigationInfo, uiComponents)
        
        // 5. 生成完整的AppAutomationMap
        return AppAutomationMap(
            appMeta = appMeta,
            uiModel = uiModel,
            stateModel = stateModel,
            intentModel = intentModel
        )
    }
    
    /**
     * 生成应用自动化地图（使用ScreenInfo）
     * @param screenInfos 屏幕信息列表
     * @return 完整的应用自动化地图
     */
    fun generateAppAutomationMap(
        screenInfos: List<ScreenInfo>
    ): AppAutomationMap {
        return generateAppAutomationMap(screenInfos, "GuideSystemTest", "com.example.guidesystemtest")
    }
    
    /**
     * 生成应用自动化地图（使用ScreenInfo）
     * @param screenInfos 屏幕信息列表
     * @param appName 应用名称
     * @param packageName 应用包名
     * @return 完整的应用自动化地图
     */
    fun generateAppAutomationMap(
        screenInfos: List<ScreenInfo>,
        appName: String,
        packageName: String
    ): AppAutomationMap {
        // 1. 构建应用元信息
        val appMeta = AppMeta(
            appName = appName,
            packageName = packageName,
            versionName = "1.0.0",
            versionCode = 1,
            uiFramework = UiFramework.VIEW
        )
        
        // 2. 构建UI模型
        val uiModel = buildUIModelFromScreenInfos(screenInfos)
        
        // 3. 构建状态模型
        val stateModel = buildStateModelFromScreenInfos(screenInfos)
        
        // 4. 构建意图模型
        val intentModel = buildIntentModelFromScreenInfos(screenInfos)
        
        // 5. 生成完整的AppAutomationMap
        return AppAutomationMap(
            appMeta = appMeta,
            uiModel = uiModel,
            stateModel = stateModel,
            intentModel = intentModel
        )
    }
    
    /**
     * 构建UI模型
     * 将导航信息和UI组件信息转换为UiModel结构
     * 每个NavigationInfo对应一个Page
     * 每个UIComponentInfo对应一个Component
     * @param navigationInfo 导航信息列表
     * @param uiComponents UI组件信息列表
     * @return 构建好的UI模型
     */
    private fun buildUIModel(
        navigationInfo: List<NavigationInfo>,
        uiComponents: List<UIComponentInfo>
    ): UiModel {
        val pages = mutableListOf<Page>()
        
        // 遍历导航信息，为每个页面创建Page对象
        for (navInfo in navigationInfo) {
            // 查找该页面的所有UI组件
            val pageComponents = uiComponents.filter { it.screenName == navInfo.screenName }
            
            // 将UIComponentInfo转换为Component对象
            val components = pageComponents.map { uiComponent ->
                // 确定组件类型
                val viewType = ViewType.valueOf(uiComponent.componentType.uppercase())
                
                // 确定支持的触发类型
                val supportedTriggers = uiComponent.events.map { event ->
                    TriggerType.valueOf(event.eventType.uppercase())
                }
                
                Component(
                    componentId = uiComponent.componentId,
                    viewType = viewType,
                    text = uiComponent.properties["text"] as? String,
                    contentDescription = uiComponent.properties["contentDescription"] as? String,
                    position = Position(0, 0), // 规则型描述，运行时通过positionFormula计算
                    size = Size(0, 0), // 规则型描述，运行时通过sizeFormula计算
                    enabled = uiComponent.properties["enabled"] as? Boolean ?: true,
                    supportedTriggers = supportedTriggers
                )
            }
            
            // 创建Page对象
            val page = Page(
                pageId = navInfo.screenName,
                pageName = navInfo.screenName,
                route = navInfo.route,
                layoutType = LayoutType.VIEW,
                components = components
            )
            
            pages.add(page)
        }
        
        return UiModel(pages = pages)
    }
    
    /**
     * 构建UI模型（使用ScreenInfo）
     */
    private fun buildUIModelFromScreenInfos(screenInfos: List<ScreenInfo>): UiModel {
        val pages = mutableListOf<Page>()
        
        // 遍历屏幕信息，为每个页面创建Page对象
        for (screenInfo in screenInfos) {
            // 将UIComponentInfo转换为Component对象
            val components = screenInfo.components.map { uiComponent ->
                // 确定组件类型
                val viewType = ViewType.valueOf(uiComponent.componentType.uppercase())
                
                // 确定支持的触发类型
                val supportedTriggers = uiComponent.events.map { event ->
                    TriggerType.valueOf(event.eventType.uppercase())
                }
                
                Component(
                    componentId = uiComponent.componentId,
                    viewType = viewType,
                    text = uiComponent.properties["text"] as? String,
                    contentDescription = uiComponent.properties["contentDescription"] as? String,
                    position = Position(0, 0), // 初始值，运行时可补充
                    size = Size(0, 0), // 初始值，运行时可补充
                    enabled = uiComponent.properties["enabled"] as? Boolean ?: true,
                    supportedTriggers = supportedTriggers
                )
            }
            
            // 创建Page对象
            val page = Page(
                pageId = screenInfo.screenName,
                pageName = screenInfo.screenName,
                route = screenInfo.route,
                layoutType = LayoutType.VIEW,
                components = components
            )
            
            pages.add(page)
        }
        
        return UiModel(pages = pages)
    }
    
    /**
     * 构建状态模型
     * 注意：State与Page是1:1映射关系，使用强信号优先策略
     */
    private fun buildStateModel(navigationInfo: List<NavigationInfo>): StateModel {
        val states = mutableListOf<State>()
        
        // 遍历导航信息，为每个页面创建一个状态
        for (navInfo in navigationInfo) {
            // 根据页面名称生成不同的强信号
            val signals = when (navInfo.screenName) {
                "Main" -> {
                    // Main页面有多个组件，生成多个强信号
                    listOf(
                        // 组件可见信号（强信号）
                        StateSignal(
                            type = SignalType.COMPONENT_VISIBLE,
                            target = "btnNormal",
                            expectedValue = true,
                            matcher = SignalMatcher.EQUALS
                        ),
                        // 文本可见信号（中等信号）
                        StateSignal(
                            type = SignalType.TEXT_VISIBLE,
                            target = "普通按钮",
                            expectedValue = true,
                            matcher = SignalMatcher.EQUALS
                        ),
                        // 组件可见信号（强信号）
                        StateSignal(
                            type = SignalType.COMPONENT_VISIBLE,
                            target = "btnIcon",
                            expectedValue = true,
                            matcher = SignalMatcher.EQUALS
                        ),
                        // 内容描述可见信号（中等信号）
                        StateSignal(
                            type = SignalType.CONTENT_DESC_VISIBLE,
                            target = "图标按钮",
                            expectedValue = true,
                            matcher = SignalMatcher.EQUALS
                        ),
                        // 页面激活信号（补充）
                        StateSignal(
                            type = SignalType.PAGE_ACTIVE,
                            target = navInfo.screenName,
                            expectedValue = null,
                            matcher = SignalMatcher.EQUALS
                        )
                    )
                }
                "Second", "Second2" -> {
                    // 二级页面，使用路由匹配和页面激活信号
                    listOf(
                        // 路由匹配信号
                        StateSignal(
                            type = SignalType.ROUTE_MATCH,
                            target = navInfo.route,
                            expectedValue = navInfo.route,
                            matcher = SignalMatcher.EQUALS
                        ),
                        // 页面激活信号
                        StateSignal(
                            type = SignalType.PAGE_ACTIVE,
                            target = navInfo.screenName,
                            expectedValue = null,
                            matcher = SignalMatcher.EQUALS
                        )
                    )
                }
                else -> {
                    // 其他页面，至少包含一个页面激活信号
                    listOf(
                        StateSignal(
                            type = SignalType.PAGE_ACTIVE,
                            target = navInfo.screenName,
                            expectedValue = null,
                            matcher = SignalMatcher.EQUALS
                        )
                    )
                }
            }
            
            // 创建状态
            val state = State(
                stateId = navInfo.screenName,
                name = navInfo.screenName,
                description = "页面 ${navInfo.screenName}",
                signals = signals,
                relatedPageIds = listOf(navInfo.screenName) // 1:1映射，只有一个元素
            )
            
            states.add(state)
        }
        
        // 设置初始状态ID
        val initialStateId = "Main"
        
        return StateModel(
            states = states,
            initialStateId = initialStateId
        )
    }
    
    /**
     * 构建状态模型（使用ScreenInfo）
     * 注意：State与Page是1:1映射关系，使用强信号优先策略
     */
    private fun buildStateModelFromScreenInfos(screenInfos: List<ScreenInfo>): StateModel {
        val states = mutableListOf<State>()
        
        // 遍历屏幕信息，为每个页面创建一个状态
        for (screenInfo in screenInfos) {
            // 生成信号列表，优先使用强信号
            val signals = mutableListOf<StateSignal>()
            
            // 优先添加组件可见信号（强信号）
            if (screenInfo.components.isNotEmpty()) {
                // 添加第一个组件的可见信号
                val firstComponent = screenInfo.components.first()
                signals.add(
                    StateSignal(
                        type = SignalType.COMPONENT_VISIBLE,
                        target = firstComponent.componentId,
                        expectedValue = true,
                        matcher = SignalMatcher.EQUALS
                    )
                )
                
                // 如果组件有文本，添加文本可见信号
                if (firstComponent.properties["text"] is String && (firstComponent.properties["text"] as String).isNotEmpty()) {
                    signals.add(
                        StateSignal(
                            type = SignalType.TEXT_VISIBLE,
                            target = firstComponent.properties["text"] as String,
                            expectedValue = true,
                            matcher = SignalMatcher.EQUALS
                        )
                    )
                }
                
                // 如果组件有内容描述，添加内容描述可见信号
                if (firstComponent.properties["contentDescription"] is String && (firstComponent.properties["contentDescription"] as String).isNotEmpty()) {
                    signals.add(
                        StateSignal(
                            type = SignalType.CONTENT_DESC_VISIBLE,
                            target = firstComponent.properties["contentDescription"] as String,
                            expectedValue = true,
                            matcher = SignalMatcher.EQUALS
                        )
                    )
                }
            }
            
            // 始终添加路由匹配信号
            signals.add(
                StateSignal(
                    type = SignalType.ROUTE_MATCH,
                    target = screenInfo.route,
                    expectedValue = screenInfo.route,
                    matcher = SignalMatcher.EQUALS
                )
            )
            
            // 添加页面激活信号（补充）
            signals.add(
                StateSignal(
                    type = SignalType.PAGE_ACTIVE,
                    target = screenInfo.screenName,
                    expectedValue = null,
                    matcher = SignalMatcher.EQUALS
                )
            )
            
            // 创建状态
            val state = State(
                stateId = screenInfo.screenName,
                name = screenInfo.screenName,
                description = "页面 ${screenInfo.screenName}",
                signals = signals,
                relatedPageIds = listOf(screenInfo.screenName) // 1:1映射，只有一个元素
            )
            
            states.add(state)
        }
        
        // 设置初始状态ID
        val initialStateId = "Main"
        
        return StateModel(
            states = states,
            initialStateId = initialStateId
        )
    }
    
    /**
     * 构建意图模型
     */
    private fun buildIntentModel(
        navigationInfo: List<NavigationInfo>,
        uiComponents: List<UIComponentInfo>
    ): IntentModel {
        // 这个方法目前未被使用，可以移除或修改以适应新的Intent结构
        // 暂时返回空的IntentModel
        return IntentModel(intents = emptyList())
    }
    
    /**
     * 构建意图模型（使用ScreenInfo）
     * 实现确定性的状态转移语义：fromStateId -> toStateId + postConditions
     */
    private fun buildIntentModelFromScreenInfos(screenInfos: List<ScreenInfo>): IntentModel {
        val intents = mutableListOf<Intent>()
        
        // 遍历屏幕信息
        for (screenInfo in screenInfos) {
            // 为每个组件创建意图
            for (component in screenInfo.components) {
                // 获取组件支持的触发类型
                val componentTriggers = component.events.map { 
                    TriggerType.valueOf(it.eventType.uppercase()) 
                }
                
                for (event in component.events) {
                    // 验证trigger是否在组件的supportedTriggers中
                    val eventTrigger = TriggerType.valueOf(event.eventType.uppercase())
                    if (eventTrigger !in componentTriggers) {
                        continue // 跳过不支持的触发类型
                    }
                    
                    // 确定起始状态ID：当前屏幕的screenName
                    val fromStateId = screenInfo.screenName
                    
                    // 检查是否是返回按钮
                val isBackButton = (component.properties["text"] as? String ?: "").contains("返回", ignoreCase = true) || 
                                  (component.properties["text"] as? String ?: "").contains("back", ignoreCase = true)
                
                // 确定目标状态ID
                val toStateId = when {
                    // 处理返回按钮或FINISH操作
                    isBackButton || event.target == "FINISH" -> {
                        when {
                            // 第二层级页面返回Main
                            screenInfo.screenName.matches(Regex("Second\\d*")) -> "Main"
                            // 第三层级页面返回Second
                            screenInfo.screenName.matches(Regex("Third\\d*")) -> "Second"
                            else -> screenInfo.screenName // 默认保持当前状态
                        }
                    }
                    // 处理带有明确target的导航
                    event.target != null && event.target!!.isNotEmpty() && event.target != "FINISH" -> {
                        // 将Activity类名转换为状态ID（移除Activity后缀）
                        event.target!!.replace("Activity", "")
                    }
                    // 检查按钮文本是否包含导航相关词汇
                    component.properties["text"] is String -> {
                        val buttonText = component.properties["text"] as String
                        when {
                            buttonText.contains("第二层级", ignoreCase = true) || buttonText.contains("second", ignoreCase = true) -> "Second"
                            buttonText.contains("第三层级", ignoreCase = true) || buttonText.contains("third", ignoreCase = true) -> "Third"
                            else -> screenInfo.screenName // 保持在当前状态
                        }
                    }
                    else -> screenInfo.screenName // 保持在当前状态
                }
                    
                    // 确定意图类型，细化为三类
                    val refinedIntentType = when {
                        // 返回按钮
                        isBackButton -> {
                            IntentType.NAVIGATE_BACK
                        }
                        // 导航行为：State发生变化
                        fromStateId != toStateId -> {
                            IntentType.NAVIGATION
                        }
                        // 内部状态变化：如开关、滑块等
                        event.eventType.uppercase() in listOf("CHECKED_CHANGE", "PROGRESS_CHANGE", "TEXT_CHANGE") -> {
                            IntentType.STATE_INTERNAL
                        }
                        // 无状态变化：普通点击等
                        else -> {
                            IntentType.NO_STATE_CHANGE
                        }
                    }
                    
                    // 生成状态转移后验证条件
                    val postConditions = generatePostConditions(screenInfos, toStateId)
                    
                    // 确定UI操作绑定
                    val uiBindings = listOf(
                        UIActionBinding(
                            componentId = component.componentId,
                            trigger = eventTrigger,
                            parameters = event.parameters
                        )
                    )
                    
                    // 生成唯一的intentId，格式：componentId_trigger_fromStateId
                    // 确保同名组件在不同State下的Intent有不同的ID
                    val intentId = "${component.componentId}_${event.eventType.lowercase()}_from${fromStateId}"
                    
                    // 创建意图
                    val intent = Intent(
                        intentId = intentId,
                        type = refinedIntentType,
                        description = "在${fromStateId}状态下，${component.componentName}的${event.eventType}事件",
                        fromStateId = fromStateId,
                        uiBindings = uiBindings,
                        toStateId = toStateId,
                        postConditions = postConditions
                    )
                    
                    intents.add(intent)
                }
            }
        }
        
        return IntentModel(intents = intents)
    }
    
    /**
     * 生成状态转移后验证条件
     * 根据目标状态ID对应的屏幕信息生成验证条件
     */
    private fun generatePostConditions(screenInfos: List<ScreenInfo>, toStateId: String): List<PostCondition> {
        // 查找目标状态对应的屏幕信息
        val targetScreenInfo = screenInfos.find { it.screenName == toStateId }
        
        if (targetScreenInfo == null) {
            // 如果找不到目标屏幕信息，返回空列表
            return emptyList()
        }
        
        val postConditions = mutableListOf<PostCondition>()
        
        // 添加路由匹配验证条件
        postConditions.add(
            PostCondition(
                type = PostConditionType.ROUTE_MATCH,
                target = targetScreenInfo.route,
                expectedValue = targetScreenInfo.route
            )
        )
        
        // 添加页面激活验证条件
        postConditions.add(
            PostCondition(
                type = PostConditionType.PAGE_ACTIVE,
                target = toStateId
            )
        )
        
        // 添加组件可见性验证条件（使用页面的第一个组件）
        if (targetScreenInfo.components.isNotEmpty()) {
            val firstComponent = targetScreenInfo.components.first()
            postConditions.add(
                PostCondition(
                    type = PostConditionType.COMPONENT_VISIBLE,
                    target = firstComponent.componentId,
                    expectedValue = true
                )
            )
            
            // 如果第一个组件有文本，添加文本可见性验证条件
            if (firstComponent.properties["text"] is String && (firstComponent.properties["text"] as String).isNotEmpty()) {
                postConditions.add(
                    PostCondition(
                        type = PostConditionType.TEXT_VISIBLE,
                        target = firstComponent.properties["text"] as String,
                        expectedValue = true
                    )
                )
            }
        }
        
        return postConditions
    }
}
