# Executor模块测试指南

## 1. 模块介绍

Executor模块是一个UI执行器，负责根据Planner提供的原子动作路径顺序执行UI操作，并返回结构化的执行结果。

### 核心功能

- **页面状态感知**：获取当前页面ID
- **原子动作执行**：支持多种触发类型（CLICK、LONG_CLICK、CHECKED_CHANGE、PROGRESS_CHANGE、TOUCH）
- **UI同步控制**：等待UI线程空闲
- **执行超时检测**：处理执行超时的情况
- **结构化执行结果**：返回详细的执行结果和失败原因

## 2. 测试环境搭建

### 2.1 前提条件

- Android Studio Arctic Fox或更高版本
- Android SDK 34或更高版本
- Kotlin 1.8.10或更高版本
- 模拟器或真实Android设备（API 24+）

### 2.2 项目配置

1. **添加Executor模块依赖**

   在`app/build.gradle.kts`文件中添加以下依赖：

   ```kotlin
   implementation(project(":executor"))
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
   ```

2. **在settings.gradle.kts中包含Executor模块**

   ```kotlin
   include(":app")
   include(":executor")
   // 其他模块...
   ```

## 3. 测试方法

### 3.1 使用ExecutorTestActivity进行手动测试

1. **运行应用**

   启动应用后，ExecutorTestActivity会作为主Activity启动。

2. **测试各种动作**

   - **Test Click**：测试点击动作
   - **Test Long Click**：测试长按动作
   - **Test EditText**：测试EditText触摸动作（会弹出键盘）
   - **Test CheckBox**：测试CheckBox状态改变动作
   - **Test ProgressBar**：测试ProgressBar进度改变动作
   - **Navigate to Second Activity**：测试页面导航动作

3. **查看执行结果**

   执行结果会显示在屏幕下方的文本框中，包括成功或失败的详细信息。

### 3.2 运行集成测试

1. **准备测试设备**

   连接真实设备或启动模拟器。

2. **运行测试**

   在Android Studio中，右键点击`ExecutorIntegrationTest`类，选择"Run 'ExecutorIntegrationTest'"。

3. **查看测试结果**

   测试结果会显示在"Run"窗口中，包括每个测试用例的执行状态。

### 3.3 编写自定义测试

1. **创建测试用的ActionPath**

   ```kotlin
   val actionPath = ActionPath(
       startPageId = "YourActivity",
       steps = listOf(
           ActionStep(
               actionId = 1,
               componentId = "your_button_id",
               triggerType = TriggerType.CLICK
           )
       )
   )
   ```

2. **执行ActionPath**

   ```kotlin
   val executor = ExecutionEngine(
       pageStateProvider,
       atomicActionExecutor,
       uiStabilizer
   )

   val result = executor.execute(actionPath)

   when (result) {
       is ExecuteResult.Success -> {
           // 执行成功
       }
       is ExecuteResult.Failed -> {
           // 执行失败，处理失败原因
           val errorMessage = "Failed at step ${result.stepIndex}: ${result.reason}"
       }
   }
   ```

## 4. 测试辅助工具

### 4.1 ExecutorTestUtils

`ExecutorTestUtils`提供了以下辅助方法：

- `createTestExecutor()`：创建测试用的执行引擎
- `createClickActionPath()`：创建点击动作的ActionPath
- `createLongClickActionPath()`：创建长按动作的ActionPath
- `createTouchActionPath()`：创建触摸动作的ActionPath
- `createCheckedChangeActionPath()`：创建复选框状态改变动作的ActionPath
- `createProgressChangeActionPath()`：创建进度条改变动作的ActionPath
- `getCurrentActivity()`：获取当前活动的Activity
- `generateActionId()`：生成唯一的ActionId

### 4.2 使用示例

```kotlin
// 创建测试执行引擎
val executor = ExecutorTestUtils.createTestExecutor()

// 创建点击动作的ActionPath
val actionPath = ExecutorTestUtils.createClickActionPath(
    startPageId = "YourActivity",
    componentId = "your_button_id"
)

// 执行ActionPath
val result = executor.execute(actionPath)
```

## 5. 常见问题及解决方案

### 5.1 组件未找到

**问题**：执行结果显示`COMPONENT_NOT_FOUND`

**解决方案**：
- 确保`componentId`与布局文件中的View ID或tag匹配
- 检查View是否在当前页面中存在
- 验证View的可见性和可交互性

### 5.2 页面不匹配

**问题**：执行结果显示`PAGE_MISMATCH`

**解决方案**：
- 确保`startPageId`与当前Activity的类名一致
- 检查Activity的生命周期状态

### 5.3 执行超时

**问题**：执行结果显示`TIMEOUT`

**解决方案**：
- 检查设备性能和响应速度
- 增加执行超时时间
- 验证UI操作是否会导致长时间阻塞

### 5.4 组件不可交互

**问题**：执行结果显示`COMPONENT_NOT_INTERACTABLE`

**解决方案**：
- 确保View是可见的（`visibility = View.VISIBLE`）
- 确保View是启用的（`enabled = true`）
- 检查View是否被其他View遮挡

## 6. 测试场景示例

### 6.1 登录流程测试

```kotlin
val loginActionPath = ActionPath(
    startPageId = "LoginActivity",
    steps = listOf(
        ActionStep(1, "edit_text_username", TriggerType.TOUCH),
        ActionStep(2, "edit_text_password", TriggerType.TOUCH),
        ActionStep(3, "button_login", TriggerType.CLICK)
    )
)

val result = executor.execute(loginActionPath)
```

### 6.2 表单提交测试

```kotlin
val formSubmitActionPath = ActionPath(
    startPageId = "FormActivity",
    steps = listOf(
        ActionStep(1, "edit_text_name", TriggerType.TOUCH),
        ActionStep(2, "edit_text_email", TriggerType.TOUCH),
        ActionStep(3, "check_box_terms", TriggerType.CHECKED_CHANGE),
        ActionStep(4, "button_submit", TriggerType.CLICK)
    )
)

val result = executor.execute(formSubmitActionPath)
```

### 6.3 页面导航测试

```kotlin
val navigationActionPath = ActionPath(
    startPageId = "MainActivity",
    steps = listOf(
        ActionStep(1, "button_navigate_to_settings", TriggerType.CLICK),
        ActionStep(2, "button_navigate_to_profile", TriggerType.CLICK),
        ActionStep(3, "button_navigate_back", TriggerType.CLICK)
    )
)

val result = executor.execute(navigationActionPath)
```

## 7. 模块扩展

### 7.1 自定义页面状态提供者

实现`PageStateProvider`接口，提供自定义的页面状态感知逻辑：

```kotlin
class CustomPageStateProvider : PageStateProvider {
    override fun getCurrentPageId(): String? {
        // 自定义页面状态感知逻辑
    }
}
```

### 7.2 自定义原子动作执行器

实现`AtomicActionExecutor`接口，提供自定义的原子动作执行逻辑：

```kotlin
class CustomAtomicActionExecutor : AtomicActionExecutor {
    override fun execute(action: ActionStep): AtomicExecuteResult {
        // 自定义原子动作执行逻辑
    }
}
```

### 7.3 自定义UI稳定器

实现`UiStabilizer`接口，提供自定义的UI同步控制逻辑：

```kotlin
class CustomUiStabilizer : UiStabilizer {
    override fun waitForIdle(timeoutMs: Long): Boolean {
        // 自定义UI同步控制逻辑
    }
}
```

## 8. 总结

Executor模块提供了一种可靠的方式来执行UI操作，并返回详细的执行结果。通过本测试指南，您可以了解如何搭建测试环境、运行测试、编写自定义测试，以及解决常见的测试问题。

如果您在测试过程中遇到任何问题，请参考本指南的"常见问题及解决方案"部分，或联系开发团队获取帮助。

---

**版本**: 1.0.0
**更新日期**: 2026-01-28
