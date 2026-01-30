# Executor模块实现计划

## 1. 模块结构

创建Executor模块，按照要求划分为以下子模块：

```
executor/
 ├─ core/           // 执行主流程
 ├─ state/          // 页面状态感知
 ├─ action/         // 原子动作执行
 ├─ result/         // 执行结果定义
 ├─ planner/        // 与Planner的通信接口
 └─ util/           // 工具与同步控制
```

## 2. 核心数据结构定义

### 2.1 输入数据结构（planner包）

```kotlin
data class ActionStep(
    val actionId: Int,
    val componentId: String,
    val triggerType: TriggerType
)

enum class TriggerType {
    CLICK,
    LONG_CLICK,
    CHECKED_CHANGE,
    PROGRESS_CHANGE,
    TOUCH
}

data class ActionPath(
    val startPageId: String,
    val steps: List<ActionStep>
)
```

### 2.2 输出结果结构（result包）

```kotlin
sealed class ExecuteResult {
    object Success : ExecuteResult()
    data class Failed(
        val stepIndex: Int,
        val action: ActionStep,
        val reason: ExecuteFailReason
    ) : ExecuteResult()
}

enum class ExecuteFailReason {
    PAGE_MISMATCH,
    COMPONENT_NOT_FOUND,
    COMPONENT_NOT_INTERACTABLE,
    TRIGGER_NOT_SUPPORTED,
    PAGE_NOT_CHANGED,
    TIMEOUT
}
```

## 3. 核心模块实现

### 3.1 页面状态感知（state包）

```kotlin
interface PageStateProvider {
    fun getCurrentPageId(): String?
}

// 实现类：基于ActivityLifecycleCallbacks的页面状态感知
class ActivityPageStateProvider(private val application: Application) : PageStateProvider {
    // 实现获取当前页面ID的逻辑
}
```

### 3.2 原子动作执行（action包）

```kotlin
interface AtomicActionExecutor {
    fun execute(action: ActionStep): AtomicExecuteResult
}

sealed class AtomicExecuteResult {
    object Success : AtomicExecuteResult()
    data class Fail(val reason: ExecuteFailReason) : AtomicExecuteResult()
}

// 实现类：基于View操作的原子动作执行
class ViewAtomicActionExecutor(private val stateProvider: PageStateProvider) : AtomicActionExecutor {
    // 实现各种原子动作的执行逻辑
}
```

### 3.3 UI同步与稳定性控制（util包）

```kotlin
interface UiStabilizer {
    fun waitForIdle(timeoutMs: Long = 1500): Boolean
}

// 实现类：基于主线程Looper的UI稳定器
class LooperUiStabilizer : UiStabilizer {
    // 实现等待UI稳定的逻辑
}
```

### 3.4 执行主流程（core包）

```kotlin
interface Executor {
    fun execute(actionPath: ActionPath): ExecuteResult
}

// 实现类：执行引擎
class ExecutionEngine(
    private val stateProvider: PageStateProvider,
    private val atomicActionExecutor: AtomicActionExecutor,
    private val uiStabilizer: UiStabilizer
) : Executor {
    override fun execute(actionPath: ActionPath): ExecuteResult {
        // 1. 校验起始页面
        // 2. 顺序执行actionPath中的每个步骤
        // 3. 每一步后等待UI稳定
        // 4. 校验页面状态变化
        // 5. 失败即中断并返回结果
    }
}
```

### 3.5 与Planner通信接口（planner包）

```kotlin
interface PlannerBridge {
    fun onExecutionFinished(result: ExecuteResult)
}
```

## 4. 实现顺序

1. 首先创建所有的接口和数据结构定义
2. 实现工具类和辅助功能
3. 实现页面状态感知模块
4. 实现原子动作执行模块
5. 实现执行主流程
6. 编写单元测试

## 5. 关键技术点

1. **页面状态感知**：使用ActivityLifecycleCallbacks跟踪当前活动的Activity，并映射为pageId
2. **View定位**：通过componentId在当前Activity中查找对应的View
3. **动作执行**：根据triggerType执行相应的View操作
4. **UI同步**：确保动作执行后UI有足够时间稳定
5. **错误处理**：严格按照要求处理各种失败情况，不自动重试

## 6. 依赖关系

- 仅依赖Android SDK，不依赖其他第三方库
- 与Planner模块仅通过接口通信，不直接依赖实现类

## 7. 测试策略

- 单元测试：测试每个模块的核心逻辑
- 集成测试：测试完整的执行流程
- 模拟测试：模拟各种边界情况和失败场景