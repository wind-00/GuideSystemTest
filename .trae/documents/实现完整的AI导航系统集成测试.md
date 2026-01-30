# 实现完整的AI导航系统集成

## 问题分析

通过分析项目结构，我发现：

1. 项目已包含所有必要模块：orchestrator（协调器）、executor（执行器）、map-recognizer（地图识别器）、planner（规划器）
2. app模块已正确依赖这些模块
3. 但MainActivity只是一个普通的测试界面，没有启动orchestrator服务的逻辑
4. 用户希望像正常软件一样运行，启动后就能使用悬浮窗输入目的

## 实现计划

### 1. 修改MainActivity
- 在MainActivity启动时初始化并启动orchestrator服务
- 添加权限检查和请求逻辑（SYSTEM_ALERT_WINDOW）
- 保持原有的测试界面功能，同时集成orchestrator服务

### 2. 实现Orchestrator初始化逻辑
- 在MainActivity中创建Orchestrator的完整依赖链
- 初始化RuntimeStateProvider、PlannerClient、ExecutorClient
- 启动OverlayService悬浮窗服务

### 3. 添加权限处理
- 检查并请求SYSTEM_ALERT_WINDOW权限
- 在权限授予后启动悬浮窗

### 4. 集成测试验证
- 确保应用启动后悬浮窗能够正常显示
- 验证用户输入处理流程
- 测试planner规划和executor执行的完整流程
- 确保原有的测试界面功能不受影响

### 5. 界面优化
- 添加启动状态指示器
- 确保悬浮窗正常显示在所有界面上
- 提供清晰的状态反馈

## 技术要点

- 在MainActivity的onCreate方法中初始化orchestrator服务
- 使用Service启动悬浮窗
- 实现完整的依赖注入
- 处理Android权限请求
- 确保后台线程执行规划和执行操作
- 提供清晰的状态反馈

## 预期结果

完成后，用户可以：
1. 运行应用（像正常软件一样）
2. 授予悬浮窗权限（如果需要）
3. 看到悬浮窗显示在界面上
4. 在悬浮窗中输入目标
5. 观察系统自动规划路径并执行操作
6. 最终到达目标界面

这样就实现了完整的AI导航系统集成，用户可以像使用正常软件一样使用它。