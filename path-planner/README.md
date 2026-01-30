# 路径规划器（Path Planner）模块

一个纯逻辑的路径规划器模块，用于Android UI自动操作系统，将用户的自然语言指令转换为结构化的执行计划。

## 功能介绍

路径规划器是一个纯逻辑组件，不直接执行UI操作，也不依赖Android运行环境，可以作为独立库存在和测试。它负责将用户的自然语言指令转换为结构化的执行计划。

### 核心功能

1. **目标解析**：将用户的自然语言指令解析为结构化的目标规范（仅使用AI）
2. **目标定位**：在UI地图中查找匹配目标规范的节点
3. **路径搜索**：使用BFS算法搜索从起始状态到目标状态的路径（完全确定性，不依赖AI）
4. **路径选择**：选择最优路径（默认最短路径，支持自定义策略）
5. **执行流程生成**：将意图路径转换为执行器可消费的步骤序列（完全确定性，不依赖AI）
6. **异常处理**：对各种异常情况进行处理，输出结构化错误信息

## 目录结构

```
path-planner/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── com/example/pathplanner/
│   │           ├── PathPlanner.kt          # 主类
│   │           ├── models/                # 数据模型
│   │           ├── resolver/              # 目标解析
│   │           ├── locator/               # 目标定位
│   │           ├── search/                # 路径搜索
│   │           ├── selector/              # 路径选择
│   │           └── generator/             # 执行流程生成
│   └── test/
│       └── kotlin/
│           └── com/example/pathplanner/   # 测试用例
├── build.gradle.kts
└── README.md
```

## 优化后的核心数据结构

### 1. TargetSpec（目标规范）

```kotlin
// 目标规范基类
open class PlannerException(message: String) : Exception(message) {
    abstract val errorCode: String
    abstract val details: Map<String, Any>
}

// 状态目标，表示用户希望到达的特定状态
sealed class TargetSpec {
    abstract val confidence: Double
    abstract val type: String
}

// 状态目标，表示用户希望到达的特定状态
class StateTarget(
    val stateId: String,
    override val confidence: Double
) : TargetSpec() {
    override val type: String = "StateTarget"
}

// 组件目标，表示用户希望与特定组件交互或到达包含该组件的状态
class ComponentTarget(
    val componentId: String? = null,      // 组件ID（优先匹配）
    val componentType: String? = null,    // 组件类型
    val componentText: String? = null,    // 组件文本
    val componentRole: String? = null,    // 组件角色
    val componentProperties: Map<String, Any> = emptyMap(), // 组件属性
    override val confidence: Double
) : TargetSpec() {
    override val type: String = "ComponentTarget"
}
```

### 2. ExecutorStep（执行器步骤）

```kotlin
// 执行器可消费的步骤
class ExecutorStep(
    val intent: String,                 // 意图ID
    val fromStateId: String,            // 起始状态ID
    val expectedStateId: String,        // 期望状态ID
    val uiBinding: UIBinding            // UI绑定信息
)

// UI组件绑定信息
class UIBinding(
    val componentId: String,            // 组件ID
    val trigger: String,                // 触发方式（如CLICK、TEXT等）
    val parameters: Map<String, Any> = emptyMap() // 可选参数
)
```

### 3. PlannerOutput（规划器输出）

```kotlin
// 规划器输出
class PlannerOutput(
    val target: TargetSpec,             // 目标规范
    val plannedPath: List<ExecutorStep>, // 执行器步骤序列
    val assumptions: Assumptions        // 规划假设
)

// 规划假设
class Assumptions(
    val startState: String,             // 起始状态ID
    val confidence: Double              // 置信度
)
```

### 4. 规划器异常

```kotlin
// 规划器异常基类
open class PlannerException(message: String) : Exception(message) {
    abstract val errorCode: String
    abstract val details: Map<String, Any>
}

// 找不到目标节点异常
class TargetNotFoundException(
    override val errorCode: String = "TARGET_NOT_FOUND",
    override val details: Map<String, Any>
) : PlannerException("找不到匹配的目标节点")

// 找不到路径异常
class PathNotFoundException(
    override val errorCode: String = "PATH_NOT_FOUND",
    override val details: Map<String, Any>
) : PlannerException("找不到从起始状态到目标状态的路径")

// 多条路径无优先级可选异常
class MultiplePathsException(
    override val errorCode: String = "MULTIPLE_PATHS",
    override val details: Map<String, Any>
) : PlannerException("存在多条路径，无法确定优先级")

// TargetSpec与UIMap不匹配异常
class TargetSpecMismatchException(
    override val errorCode: String = "TARGET_SPEC_MISMATCH",
    override val details: Map<String, Any>
) : PlannerException("TargetSpec与UIMap不匹配")

// 未知错误异常
class UnknownPlannerException(
    override val errorCode: String = "UNKNOWN_ERROR",
    override val details: Map<String, Any>
) : PlannerException("规划过程中发生未知错误")
```

## 优化后的核心流程

### 1. 目标解析流程

1. 用户输入自然语言指令
2. TargetResolver（仅使用AI）生成标准化的TargetSpec
3. TargetSpec包含明确的stateId或componentId等定位信息

### 2. 路径规划流程

1. TargetLocator在UIMap中查找匹配TargetSpec的目标节点
2. PathSearcher使用BFS算法搜索所有可能路径，记录完整状态序列
3. PathSelector选择最优路径或备用路径
4. ExecutionFlowGenerator生成执行器步骤序列，包含完整状态信息
5. 输出规范化的PlannerOutput

### 3. 异常处理流程

1. 检测异常情况
2. 抛出对应的PlannerException，包含结构化错误信息
3. 上层应用捕获异常，根据错误信息进行处理

## 优化后的输出示例

```json
{
  "target": {
    "type": "StateTarget",
    "stateId": "Settings",
    "confidence": 0.9
  },
  "plannedPath": [
    {
      "intent": "open_settings",
      "fromStateId": "Main",
      "expectedStateId": "Settings",
      "uiBinding": {
        "componentId": "button1",
        "trigger": "CLICK",
        "parameters": {}
      }
    }
  ],
  "assumptions": {
    "startState": "Main",
    "confidence": 0.9
  }
}
```

## 使用示例

### 基本使用

```kotlin
// 创建目标解析器
val targetResolver = MockAITargetResolver()

// 创建路径规划器
val pathPlanner = PathPlanner(
    targetResolver = targetResolver
)

// 执行路径规划
val userIntent = "打开设置"
val uiMap = createTestUIMap() // 从地图识别器获取UI地图

runBlocking {
    try {
        val result = pathPlanner.plan(userIntent, uiMap)
        
        // 处理规划结果
        println("目标：${result.target}")
        println("规划路径：${result.plannedPath}")
        println("假设：${result.assumptions}")
    } catch (e: PlannerException) {
        // 处理规划异常
        println("规划失败：${e.message}")
        println("错误代码：${e.errorCode}")
        println("错误详情：${e.details}")
    }
}
```

### 安全规划（不抛出异常）

```kotlin
runBlocking {
    val result = pathPlanner.planSafe(userIntent, uiMap)
    
    // 处理规划结果
    if (result.target is StateTarget && result.target.stateId == "ERROR") {
        println("规划失败")
    } else {
        println("规划成功：${result.plannedPath}")
    }
}
```

### 自定义路径选择策略

```kotlin
// 创建基于权重的路径选择器
val weightedSelector = WeightedPathSelector(
    riskAssessment = { intent -> 
        // 自定义风险评估逻辑
        if (intent.type == "TEXT") 0.5 else 1.0
    },
    stabilityWeight = { intent -> 
        // 自定义稳定性权重逻辑
        if (intent.targetStateId == "Main") 0.8 else 1.0
    }
)

// 使用自定义选择器创建路径规划器
val pathPlanner = PathPlanner(
    targetResolver = MockAITargetResolver(),
    pathSelector = weightedSelector
)
```

## 扩展开发

### 自定义目标解析器

```kotlin
class CustomAITargetResolver : TargetResolver {
    override suspend fun resolve(userIntent: String): TargetSpec {
        // 调用真实的AI API或实现自定义解析逻辑
        // ...
        return StateTarget("TargetState", 0.9)
    }
}
```

### 自定义路径选择策略

```kotlin
class CustomPathSelector : PathSelector {
    override fun select(pathResults: List<PathResult>): PathResult? {
        // 实现自定义路径选择逻辑
        // ...
        return pathResults.firstOrNull()
    }
    
    // 兼容旧接口
    override fun select(paths: List<List<Intent>>): List<Intent>? {
        // 实现自定义路径选择逻辑
        // ...
        return paths.firstOrNull()
    }
}
```

## 测试

运行单元测试：

```bash
./gradlew :path-planner:test
```

生成测试报告：

```bash
./gradlew :path-planner:test --info
```

测试报告位于：`path-planner/build/reports/tests/test/index.html`

## 构建

构建模块：

```bash
./gradlew :path-planner:build
```

生成AAR文件：

```bash
./gradlew :path-planner:assembleRelease
```

AAR文件位于：`path-planner/build/outputs/aar/`

## 依赖

- Kotlin Standard Library
- JUnit 4
- Kotlinx Coroutines (测试用)

## 设计原则

1. **纯逻辑性**：不直接执行UI操作，只做推理与规划
2. **可测试性**：所有组件均可独立测试
3. **可扩展性**：支持自定义路径选择策略、目标解析器等
4. **清晰分离**：明确区分AI推理阶段与确定性算法阶段
5. **无依赖**：不依赖Android运行环境，可作为独立库使用
6. **健壮性**：对各种异常情况进行处理，输出结构化错误信息
7. **规范化输出**：所有输出都有清晰的结构和完整的信息

## 优化内容

### 1. TargetSpec标准化
- ✅ 确保TargetSpec包含明确的stateId或componentId
- ✅ 支持componentType、componentText、componentRole等多种定位方式
- ✅ 明确TargetSpec格式为可直接用于地图节点定位

### 2. 路径搜索健壮性
- ✅ PathSearcher记录候选路径来源stateId
- ✅ 支持多条路径输出，以便PathSelector选择最优路径或备用路径
- ✅ 预留错误恢复机制，如果目标不可达，返回合理提示

### 3. ExecutorStep扩展
- ✅ 输出每一步时，包含intent、fromStateId、expectedStateId、uiBinding
- ✅ 支持可选参数（例如滑块数值、选择项）
- ✅ 每一步明确fromStateId与expectedStateId，保证跨页面路径可复用

### 4. 严格职责划分
- ✅ AI仅用于TargetResolver，生成TargetSpec
- ✅ 路径搜索、路径选择、执行流程生成完全确定性，不依赖AI

### 5. 异常处理
- ✅ 明确Planner对各种异常情况的处理策略
- ✅ 实现了结构化的异常类，包含错误代码和详细信息
- ✅ 所有异常输出结构化信息，用于后续调试

### 6. 输出结构规范化
- ✅ PlannerOutput中target、plannedPath、assumptions三部分完整、清晰
- ✅ plannedPath中每个ExecutorStep包含完整信息

### 7. 模块独立性与可测试性
- ✅ Planner不依赖Android环境
- ✅ 支持单元测试和集成测试
- ✅ 所有关键逻辑可独立测试

## 版本历史

- v1.0.0: 初始版本，实现基本功能
- v1.1.0: 优化版本，包含以下改进：
  - 标准化TargetSpec格式
  - 增强路径搜索健壮性
  - 扩展ExecutorStep结构
  - 实现完整异常处理
  - 规范化输出结构
  - 严格职责划分

## 许可证

[MIT License](LICENSE)

## 贡献

欢迎提交Issue和Pull Request！

## 联系方式

如有问题或建议，请联系：

- Email: example@example.com
- GitHub: https://github.com/example/path-planner