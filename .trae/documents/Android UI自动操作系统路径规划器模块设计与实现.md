# 路径规划器（Path Planner）模块设计与实现

## 1. 模块概述

路径规划器是一个纯逻辑组件，不直接执行UI操作，也不依赖Android运行环境，可以作为独立库存在和测试。它负责将用户的自然语言指令转换为结构化的执行计划。

## 2. 核心数据结构设计

### 2.1 输入模型
- **UserIntent**: 字符串类型，用户的自然语言指令
- **UIMap**: 结构化的UI地图，包含状态、组件和意图
- **CurrentStateId**: 字符串类型，当前页面状态ID（默认硬编码为"Main"）

### 2.2 中间模型
- **TargetSpec**: 结构化的目标规范，由LLM生成
- **Path**: 意图序列，从起始状态到目标状态
- **ExecutorStep**: 执行器可消费的步骤

### 2.3 输出模型
```kotlin
data class PlannerOutput(
    val target: TargetSpec,
    val plannedPath: List<ExecutorStep>,
    val assumptions: Assumptions
)
```

## 3. 核心组件设计

### 3.1 PathPlanner（主类）
- 协调整个规划流程
- 接收输入，返回规划结果

### 3.2 TargetResolver
- 接口：定义从用户意图到TargetSpec的转换
- 实现：使用AI API生成结构化TargetSpec

### 3.3 TargetLocator
- 在UIMap中查找匹配TargetSpec的目标节点
- 支持状态目标和组件目标

### 3.4 PathSearcher
- 实现BFS算法，搜索从起始状态到目标状态的路径
- 支持多条路径的生成

### 3.5 PathSelector
- 选择最优路径（默认最短路径）
- 预留接口支持自定义策略（风险评估、稳定性权重）

### 3.6 ExecutionFlowGenerator
- 将意图路径转换为执行器步骤序列
- 明确每一步的意图、UI绑定和期望状态

## 4. 实现步骤

### 4.1 定义数据模型
- 实现UIMap、State、Component、Intent等核心数据类
- 实现TargetSpec、Path、ExecutorStep等中间和输出数据类

### 4.2 实现核心组件
- 实现TargetResolver接口及AI实现
- 实现TargetLocator的目标查找逻辑
- 实现PathSearcher的BFS路径搜索
- 实现PathSelector的路径选择策略
- 实现ExecutionFlowGenerator的执行流程生成

### 4.3 实现主规划器
- 整合所有组件，实现完整的规划流程
- 添加错误处理和日志记录

### 4.4 编写测试用例
- 测试简单路径规划
- 测试复杂路径规划
- 测试错误处理

## 5. 技术栈

- **语言**: Kotlin
- **JSON处理**: Jackson
- **测试框架**: JUnit 5
- **构建工具**: Gradle

## 6. 目录结构

```
path-planner/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/pathplanner/
│   │   │       ├── PathPlanner.kt          # 主类
│   │   │       ├── models/                # 数据模型
│   │   │       ├── resolver/              # 目标解析
│   │   │       ├── locator/               # 目标定位
│   │   │       ├── search/                # 路径搜索
│   │   │       ├── selector/              # 路径选择
│   │   │       └── generator/             # 执行流程生成
│   │   └── resources/
│   └── test/
│       └── kotlin/
│           └── com/example/pathplanner/   # 测试用例
├── build.gradle.kts
└── README.md
```

## 7. 核心算法

### 7.1 目标解析
- 使用AI API将自然语言转换为结构化TargetSpec
- 支持状态目标和组件目标

### 7.2 路径搜索
- 实现广度优先搜索（BFS）算法
- 生成所有可能的路径

### 7.3 路径选择
- 默认策略：最短路径
- 预留接口支持自定义策略

## 8. 扩展性设计

- 支持自定义路径选择策略
- 支持不同的AI API提供商
- 支持复杂意图和参数
- 支持路径风险评估和稳定性权重

## 9. 测试策略

- 单元测试：测试各个组件的独立功能
- 集成测试：测试完整的规划流程
- 边界测试：测试异常情况和极限条件

## 10. 预期输出

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
      "uiBinding": {
        "componentId": "button1",
        "trigger": "CLICK"
      },
      "expectedState": "Settings"
    }
  ],
  "assumptions": {
    "startState": "Main",
    "confidence": 0.9
  }
}
```

## 11. 实现进度

1. ✅ 数据结构设计
2. ✅ 核心组件设计
3. ✅ 算法设计
4. ⏳ 代码实现
5. ⏳ 测试编写
6. ⏳ 文档完善

该设计确保了模块的纯逻辑性、可测试性和可扩展性，能够满足Android UI自动操作系统的路径规划需求。