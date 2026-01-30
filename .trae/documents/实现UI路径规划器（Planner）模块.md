# 实现UI路径规划器（Planner）模块

## 1. 项目结构设计

### 1.1 创建Planner文件夹
在项目根目录下创建`planner`文件夹，作为新模块的根目录。

### 1.2 模块配置
创建`build.gradle.kts`文件，配置Kotlin和序列化依赖：
- Kotlin标准库
- kotlinx.serialization（用于JSON反序列化）

## 2. 核心数据类设计

### 2.1 ActionMeta
```kotlin
data class ActionMeta(
    val page: String,
    val componentId: String,
    val triggerType: String,
    val visibleText: String,
    val viewType: String
)
```

### 2.2 UiMapModel
```kotlin
data class UiMapModel(
    val page_index: Map<String, Int>,
    val action_index: Map<String, Int>,
    val action_metadata: Map<Int, ActionMeta>,
    val visible_text_index: Map<String, List<Int>>,
    val transition: Map<Int, Map<Int, List<Int>>>
)
```

### 2.3 UserGoal
```kotlin
data class UserGoal(
    val targetVisibleText: String,
    val startPage: String,
    val searchStrategy: SearchStrategy
)
```

### 2.4 SearchStrategy
```kotlin
enum class SearchStrategy {
    BFS,
    DFS
}
```

### 2.5 PlannerNode
```kotlin
data class PlannerNode(
    val pageIdx: Int,
    val path: List<Int> // actionId sequence
)
```

### 2.6 PlanResult
```kotlin
data class PlanResult(
    val success: Boolean,
    val actionPath: List<Int>,
    val reason: String?
)
```

## 3. 核心Planner类实现

### 3.1 类定义
```kotlin
class Planner(private val uiMap: UiMapModel) {
    // 实现搜索逻辑
}
```

### 3.2 核心搜索方法
```kotlin
fun plan(userGoal: UserGoal): PlanResult {
    // 阶段1：目标Action确定
    // 阶段2：初始状态构建
    // 阶段3：图搜索（BFS/DFS）
    // 阶段4：结果输出
}
```

### 3.3 阶段1：目标Action确定
- 使用`userGoal.targetVisibleText`在`uiMap.visible_text_index`中精确查找
- 处理规则：
  - 若不存在 → 返回失败（NO_TARGET_ACTION）
  - 若存在多个 → 全部作为“候选终点action”

### 3.4 阶段2：初始状态构建
- 使用`userGoal.startPage`查`uiMap.page_index`
- 得到`startPageIdx`
- 创建初始`PlannerNode`：`PlannerNode(startPageIdx, emptyList())`

### 3.5 阶段3：图搜索

#### 3.5.1 BFS实现
- 使用队列存储待访问节点
- 使用Set记录已访问状态（pageIdx + path）
- 遍历当前节点的所有actionId
- 若actionId在目标action集合中 → 返回结果
- 否则，生成新节点并加入队列

#### 3.5.2 DFS实现
- 使用栈存储待访问节点
- 使用Set记录已访问状态（pageIdx + path）
- 遍历当前节点的所有actionId
- 若actionId在目标action集合中 → 返回结果
- 否则，生成新节点并加入栈

### 3.6 阶段4：结果输出
- 成功时：返回`PlanResult(true, actionPath, null)`
- 失败时：返回`PlanResult(false, emptyList(), reason)`

## 4. 实现细节

### 4.1 状态记录
- 使用`Set<String>`记录已访问状态，格式：`"$pageIdx:${path.joinToString(",")}"`
- 防止死循环

### 4.2 JSON反序列化
- 使用`kotlinx.serialization.json.Json`进行JSON反序列化
- 确保所有数据类都标记为`@Serializable`

### 4.3 错误处理
- 定义明确的错误原因：
  - NO_TARGET_ACTION：未找到目标action
  - INVALID_START_PAGE：无效的起始页面
  - NO_PATH_FOUND：未找到可行路径

## 5. 测试用例设计

### 5.1 基本功能测试
- 测试BFS搜索
- 测试DFS搜索
- 测试不同目标文本
- 测试不同起始页面

### 5.2 边界情况测试
- 目标文本不存在
- 起始页面不存在
- 无可行路径
- 多个目标action

## 6. 与现有系统集成

### 6.1 输入输出
- 输入：UI地图张量（JSON）和结构化用户目标
- 输出：可执行的Action序列

### 6.2 依赖关系
- 纯Kotlin实现，不依赖Android UI
- 与现有`fsm_transition.json`格式兼容

## 7. 实现步骤

1. 创建`planner`文件夹和`build.gradle.kts`
2. 实现所有数据类
3. 实现`Planner`类的核心逻辑
4. 添加单元测试
5. 验证与现有UI地图张量的兼容性

通过以上实现，我们将创建一个严格的确定性图搜索器，符合用户的所有要求，能够在UI状态图中执行BFS或DFS搜索，输出可执行的Action序列。