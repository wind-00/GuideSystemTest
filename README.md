# 医院导诊系统修复说明

## 问题描述
1. **地图文件读取问题**：应用无法直接导入 `fsm_transition.json` 作为地图，导致每次地图不会正常更新
2. **Planner 找不到目标动作**：Planner 无法找到 "查看挂号" 动作
3. **执行器找不到组件**：执行器无法找到组件，导致执行失败

## 解决方案

### 1. 创建 assets 目录并复制地图文件
- 在 `app/src/main` 目录下创建 `assets` 目录
- 将 `fsm_transition.json` 文件复制到 `assets` 目录中
- 确保应用在安装后能直接访问该文件

### 2. 修改 MainActivity.kt 文件
- 添加从 assets 目录读取 `fsm_transition.json` 文件的功能
- 实现多级后备方案：优先从 assets 目录读取，失败后尝试从绝对路径和项目根目录读取
- 添加详细的日志，追踪文件读取过程
- 添加 `plannerClient.setContext(this)` 调用，将 context 传递给 PlannerClientImpl

### 3. 修改 PlannerClient.kt 接口
- 添加 `setContext(context: Context)` 方法，允许从 MainActivity 传递 context

### 4. 修改 PlannerClientImpl.kt 文件
- 添加 `context` 字段，用于从 assets 目录读取文件
- 实现 `setContext` 方法，接收并存储 context
- 改进文件加载机制：优先使用 context 从 assets 目录读取 `fsm_transition.json` 文件，失败后尝试从其他路径读取
- 在 `plan` 方法中添加详细的日志，追踪 Planner 的操作过程
- 修复 actionId=35 的映射问题，确保执行器能找到对应的组件
- 将所有 println 语句替换为 Log.d 语句，确保在 Android 设备上能看到日志
- 更新默认映射，包含 `fsm_transition.json` 中的所有 actionId 到 componentId 的映射
- 确保 Planner 能正确识别和使用动作

### 5. 修改 Planner.kt 文件
- 改进所有搜索方法（enhancedBfs、enhancedDfs、bfs、dfs、bfsToPage、dfsToPage），让它们能处理返回按钮的情况
- 当 `toPages` 为空数组时，将其视为一个可以执行的动作，让执行器尝试执行这个返回操作
- 确保 Planner 能找到从 ViewAppointmentActivity 到 SecondActivity1 的路径

### 6. 修改 Planner.kt 文件（添加返回按钮策略）
- 添加 `tryReturnPath` 方法，当无法找到直接路径时，尝试使用返回按钮策略寻找路径
- 当无法找到从当前页面到目标页面的直接路径时，尝试执行返回按钮动作，然后从其他页面寻找路径
- 确保 Planner 能找到从 SecondActivity1 到 SecondActivity2 的路径

### 7. 验证修复效果
- 确保应用能直接导入 `fsm_transition.json` 作为地图
- 验证 Planner 能找到 "查看挂号" 动作
- 确保执行器能找到对应的组件并执行动作
- 验证 Planner 能找到从 ViewAppointmentActivity 到 SecondActivity1 的路径，支持返回按钮操作
- 验证 Planner 能找到从 SecondActivity1 到 SecondActivity2 的路径，支持返回按钮策略

## 技术原理

### 文件读取机制
- **assets 目录**：应用安装后，assets 目录中的文件会被打包到 APK 中，应用可以直接访问
- **多级后备**：当从 assets 目录读取失败时，会尝试从绝对路径和项目根目录读取，确保在不同环境下都能正常工作

### 动作映射机制
- **actionId 到 componentId**：`fsm_transition.json` 文件中定义了 actionId 到 (componentId, triggerType) 的映射
- **默认映射**：当无法读取 `fsm_transition.json` 文件时，使用硬编码的默认映射作为后备
- **详细日志**：添加详细的日志，追踪文件读取和 Planner 操作过程，便于排查问题

### 执行器组件查找
- **组件查找**：执行器通过 componentId 查找对应的 View 组件
- **多种查找方式**：尝试通过 tag、id 和递归查找等多种方式查找组件
- **特殊处理**：对返回按钮等特殊组件进行特殊处理，提高执行成功率

### 返回按钮处理
- **Planner 支持**：Planner 现在能处理返回按钮的情况，当 `toPages` 为空数组时，将其视为一个可以执行的动作
- **路径规划**：Planner 能找到从 ViewAppointmentActivity 到 SecondActivity1 的路径，支持返回按钮操作
- **返回按钮策略**：当无法找到直接路径时，Planner 会尝试执行返回按钮动作，然后从其他页面寻找路径
- **执行器支持**：执行器能执行返回按钮操作，返回到上一个页面

## 修复效果
- ✅ 应用能直接导入 `fsm_transition.json` 作为地图
- ✅ Planner 能找到 "查看挂号" 动作
- ✅ 执行器能找到对应的组件并执行动作
- ✅ 应用在卸载/重装后能正常工作
- ✅ 详细的日志便于排查问题
- ✅ Planner 能找到从 ViewAppointmentActivity 到 SecondActivity1 的路径，支持返回按钮操作
- ✅ Planner 能找到从 SecondActivity1 到 SecondActivity2 的路径，支持返回按钮策略

## 使用说明
1. 确保 `fsm_transition.json` 文件已复制到 `assets` 目录中
2. 运行应用，测试导诊功能
3. 查看日志，确认文件读取和 Planner 操作过程正常

## 注意事项
- 当修改 `fsm_transition.json` 文件后，需要重新编译应用，确保新的地图文件被打包到 APK 中
- 如果在开发环境中运行，应用会优先从 assets 目录读取地图文件，失败后尝试从绝对路径和项目根目录读取
- 如果在生产环境中运行，应用会从 assets 目录读取地图文件