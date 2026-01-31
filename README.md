# 可复用自动操控助手项目指南

## 1. 项目概述

本项目是一个可复用的自动操控助手工具，可附加在正常的Android应用上，将其转化为具有自动操控能力的应用。系统采用模块化设计，包含四个核心模块：`map`、`planner`、`executor`和`orchestrator`，各模块职责明确，通过标准化接口进行交互。

## 2. 系统架构与交互关系

### 2.1 模块之间和用户、App配合关系

```mermaid
graph TD
    %% 定义样式
    classDef userStyle fill:#E3F2FD,stroke:#2196F3,stroke-width:2px,color:#0D47A1
    classDef coordStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20
    classDef coreStyle fill:#FFF3E0,stroke:#FF9800,stroke-width:2px,color:#E65100
    classDef dataStyle fill:#F3E5F5,stroke:#9C27B0,stroke-width:2px,color:#4A148C
    classDef appStyle fill:#EEEEEE,stroke:#616161,stroke-width:2px,color:#212121
    
    %% 节点定义
    subgraph 用户层
        User[用户] -->|输入请求| Overlay[悬浮覆盖视图]
        class User,Overlay userStyle
    end

    subgraph 协调层
        Overlay -->|传递请求| Orchestrator[协调器]
        Orchestrator -->|状态反馈| Overlay
        class Orchestrator coordStyle
    end

    subgraph 核心层
        Orchestrator -->|路径规划请求| Planner[路径规划器]
        Planner -->|规划结果| Orchestrator
        Orchestrator -->|执行请求| Executor[执行引擎]
        Executor -->|执行结果| Orchestrator
        class Planner,Executor coreStyle
    end

    subgraph 数据层
        Map[UI地图生成器] -->|UI地图| Planner
        class Map dataStyle
    end

    subgraph 应用层
        Executor -->|操作| App[目标应用]
        App -->|状态| Executor
        class App appStyle
    end
```

### 2.1.1 简洁版模块连接图

```mermaid
graph TD
    %% 定义样式
    classDef userStyle fill:#E3F2FD,stroke:#2196F3,stroke-width:2px,color:#0D47A1
    classDef coordStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20
    classDef coreStyle fill:#FFF3E0,stroke:#FF9800,stroke-width:2px,color:#E65100
    classDef dataStyle fill:#F3E5F5,stroke:#9C27B0,stroke-width:2px,color:#4A148C
    classDef appStyle fill:#EEEEEE,stroke:#616161,stroke-width:2px,color:#212121
    
    %% 节点定义
    User[用户] -->|输入请求| Overlay[悬浮覆盖视图]
    Overlay -->|传递请求| Orchestrator[协调器]
    Orchestrator -->|路径规划请求| Planner[路径规划器]
    Map[UI地图生成器] -->|UI地图| Planner
    Planner -->|规划结果| Orchestrator
    Orchestrator -->|执行请求| Executor[执行引擎]
    Executor -->|操作| App[目标应用]
    App -->|状态| Executor
    Executor -->|执行结果| Orchestrator
    Orchestrator -->|状态反馈| Overlay
    
    %% 应用样式
    class User,Overlay userStyle
    class Orchestrator coordStyle
    class Planner,Executor coreStyle
    class Map dataStyle
    class App appStyle
```

### 2.2 各模块运作流程

#### 2.2.1 Map模块运作流程

```mermaid
graph TD
    %% 定义样式
    classDef startStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20,shape:circle
    classDef processStyle fill:#E3F2FD,stroke:#2196F3,stroke-width:2px,color:#0D47A1
    classDef endStyle fill:#FFEBEE,stroke:#F44336,stroke-width:2px,color:#B71C1C,shape:circle
    
    %% 节点定义
    Start[开始] -->|输入代码目录| FindFiles[查找Kotlin和XML文件]
    FindFiles --> ParseXML[解析XML布局文件]
    ParseXML --> ExtractPages[提取页面信息]
    ExtractPages --> ExtractComponents[提取组件信息]
    ExtractComponents --> ExtractEffects[提取效果信息]
    ExtractEffects --> ValidateMap[验证并增强UI地图]
    ValidateMap --> GenerateJSON[生成UI地图JSON文件]
    GenerateJSON --> End[结束]
    
    %% 应用样式
    class Start startStyle
    class FindFiles,ParseXML,ExtractPages,ExtractComponents,ExtractEffects,ValidateMap,GenerateJSON processStyle
    class End endStyle
```

#### 2.2.2 Planner模块运作流程

```mermaid
graph TD
    %% 定义样式
    classDef startStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20,shape:circle
    classDef processStyle fill:#E3F2FD,stroke:#2196F3,stroke-width:2px,color:#0D47A1
    classDef decisionStyle fill:#FFF3E0,stroke:#FF9800,stroke-width:2px,color:#E65100,shape:diamond
    classDef endStyle fill:#FFEBEE,stroke:#F44336,stroke-width:2px,color:#B71C1C,shape:circle
    
    %% 节点定义
    Start[开始] -->|输入起始页面和目标| LoadMap[加载UI地图]
    LoadMap --> FindTarget[确定目标Action和页面]
    FindTarget --> BuildInitialState[构建初始状态]
    BuildInitialState --> SelectStrategy[选择搜索策略]
    SelectStrategy -->|BFS| BFS[执行BFS搜索]
    SelectStrategy -->|DFS| DFS[执行DFS搜索]
    BFS --> GeneratePath[生成操作路径]
    DFS --> GeneratePath
    GeneratePath --> ReturnResult[返回路径规划结果]
    ReturnResult --> End[结束]
    
    %% 应用样式
    class Start startStyle
    class LoadMap,FindTarget,BuildInitialState,BFS,DFS,GeneratePath,ReturnResult processStyle
    class SelectStrategy decisionStyle
    class End endStyle
```

#### 2.2.3 Executor模块运作流程

```mermaid
graph TD
    %% 定义样式
    classDef startStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20,shape:circle
    classDef processStyle fill:#E3F2FD,stroke:#2196F3,stroke-width:2px,color:#0D47A1
    classDef decisionStyle fill:#FFF3E0,stroke:#FF9800,stroke-width:2px,color:#E65100,shape:diamond
    classDef successStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20
    classDef failureStyle fill:#FFEBEE,stroke:#F44336,stroke-width:2px,color:#B71C1C
    classDef endStyle fill:#FFEBEE,stroke:#F44336,stroke-width:2px,color:#B71C1C,shape:circle
    
    %% 节点定义
    Start[开始] -->|输入操作路径| ValidateStartPage[校验起始页面]
    ValidateStartPage --> ExecuteSteps[顺序执行动作步骤]
    ExecuteSteps --> ExecuteAtomicAction[执行原子动作]
    ExecuteAtomicAction --> CheckTimeout[检查执行超时]
    CheckTimeout -->|超时| ReturnFailure[返回执行失败]
    CheckTimeout -->|成功| WaitUIStable[等待UI稳定]
    WaitUIStable --> CheckUITimeout[检查UI稳定超时]
    CheckUITimeout -->|超时| ReturnFailure
    CheckUITimeout -->|成功| SensePageState[重新感知页面状态]
    SensePageState -->|还有步骤| ExecuteSteps
    SensePageState -->|无步骤| ReturnSuccess[返回执行成功]
    ReturnFailure --> End[结束]
    ReturnSuccess --> End
    
    %% 应用样式
    class Start startStyle
    class ValidateStartPage,ExecuteSteps,ExecuteAtomicAction,WaitUIStable,SensePageState processStyle
    class CheckTimeout,CheckUITimeout,SensePageState decisionStyle
    class ReturnSuccess successStyle
    class ReturnFailure failureStyle
    class End endStyle
```

#### 2.2.4 Orchestrator模块运作流程

```mermaid
graph TD
    %% 定义样式
    classDef startStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20,shape:circle
    classDef processStyle fill:#E3F2FD,stroke:#2196F3,stroke-width:2px,color:#0D47A1
    classDef decisionStyle fill:#FFF3E0,stroke:#FF9800,stroke-width:2px,color:#E65100,shape:diamond
    classDef successStyle fill:#E8F5E8,stroke:#4CAF50,stroke-width:2px,color:#1B5E20
    classDef failureStyle fill:#FFEBEE,stroke:#F44336,stroke-width:2px,color:#B71C1C
    
    %% 节点定义
    Start[开始] -->|输入用户请求| ParseRequest[解析用户请求]
    ParseRequest --> CallPlanner[调用Planner进行路径规划]
    CallPlanner --> GetPlanResult[获取规划结果]
    GetPlanResult -->|规划失败| ReturnPlanError[返回规划错误]
    GetPlanResult -->|规划成功| CallExecutor[调用Executor执行路径]
    CallExecutor --> GetExecuteResult[获取执行结果]
    GetExecuteResult -->|执行失败| ReturnExecuteError[返回执行错误]
    GetExecuteResult -->|执行成功| ReturnSuccess[返回执行成功]
    ReturnPlanError --> UpdateStatus[更新系统状态]
    ReturnExecuteError --> UpdateStatus
    ReturnSuccess --> UpdateStatus
    UpdateStatus --> WaitRequest[等待新的请求]
    WaitRequest --> ParseRequest
    
    %% 应用样式
    class Start startStyle
    class ParseRequest,CallPlanner,CallExecutor,UpdateStatus,WaitRequest processStyle
    class GetPlanResult,GetExecuteResult decisionStyle
    class ReturnSuccess successStyle
    class ReturnPlanError,ReturnExecuteError failureStyle
```

## 3. 核心模块功能与职责

### 3.1 Map模块

**功能**：从Android Kotlin代码中静态生成UI地图JSON文件，为路径规划提供基础数据。

**核心职责**：
- 查找和解析Kotlin代码和XML布局文件
- 提取页面、组件和效果信息
- 验证并增强UI地图
- 生成标准化的UI地图JSON文件

### 3.2 Planner模块

**功能**：基于UI地图执行路径规划，生成从起始页面到目标页面的最优操作路径。

**核心职责**：
- 加载和解析UI地图数据
- 确定目标Action和页面
- 构建初始状态
- 执行图搜索（BFS或DFS）
- 生成最优操作路径

### 3.3 Executor模块

**功能**：执行由planner生成的操作路径，实现自动化操作。

**核心职责**：
- 校验起始页面
- 顺序执行每个动作步骤
- 执行原子动作（带超时检测）
- 等待UI稳定（带超时检测）
- 重新感知页面状态
- 返回执行结果

### 3.4 Orchestrator模块

**功能**：协调其他模块的工作，接收用户请求，调用planner进行路径规划，然后调用executor执行规划的路径。

**核心职责**：
- 接收和解析用户请求
- 调用planner进行路径规划
- 调用executor执行规划的路径
- 监控执行状态
- 反馈执行结果
- 管理系统状态

## 4. 技术特点与优势

### 4.1 可复用性
- 模块化设计，可作为独立库集成到任何Android应用
- 标准化接口，便于与不同应用集成
- 无需修改原有应用代码，只需附加本工具

### 4.2 智能路径规划
- 支持BFS和DFS两种搜索策略
- 基于UI地图的静态分析，提高路径规划准确性
- 考虑页面跳转和组件交互，生成最优操作路径

### 4.3 实时执行控制
- 带超时检测的执行机制，提高系统稳定性
- 实时UI稳定检测，确保操作执行成功
- 实时页面状态感知，适应动态UI变化

### 4.4 用户友好的交互
- 悬浮覆盖视图，方便用户随时输入请求
- 实时执行状态反馈，让用户了解操作进展
- 简洁的错误提示，帮助用户理解失败原因

## 5. 应用场景

- **自动化测试**：自动执行应用中的操作流程，提高测试效率
- **用户辅助**：为用户提供自动化操作助手，简化复杂操作
- **无障碍功能**：为有障碍用户提供操作辅助，提高应用可访问性
- **演示和教程**：自动执行应用操作，用于产品演示和教程制作

## 6. 总结

本项目实现了一个可复用的自动操控助手工具，通过模块化设计和标准化接口，可轻松集成到任何Android应用中，为应用添加自动化操作能力。系统采用静态分析生成UI地图，结合智能路径规划和实时执行控制，实现了高效、准确的自动化操作。该工具具有广泛的应用场景，可提高应用的易用性、可测试性和可访问性。

---

**版本**: 1.0.0
**更新日期**: 2026-01-30