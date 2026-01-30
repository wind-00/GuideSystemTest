# Kotlin地图绘制器模块自检与修复计划

## 一、修复目标
根据最高语义法则，将地图绘制器模块改造为纯"UI世界说明书"，确保：
- 模块职责单一，不包含混合逻辑
- 语义一致，不包含禁止内容
- 数据可物理获取，不包含不可验证字段
- 地图简洁，不包含冗余字段

## 二、具体修复内容

### 1. 数据模型修复
**IntentModel.kt**：
- 删除不可验证字段：`cost`和`riskLevel`

**Analyzer.kt**：
- 删除冗余字段：`NavigationInfo.arguments`
- 删除不可验证字段：`RuntimeComponentInfo`中的`position`、`size`等硬编码字段

### 2. 语义一致性修复
**MapGenerator.kt**：
- 删除硬编码的`appName`和`packageName`，改为从实际项目获取
- 将`Component.position`和`Component.size`从恒为0改为使用规则型描述或删除

**UIComponentAnalyzer.kt**：
- 删除`enabled`字段的硬编码值，改为从实际代码提取
- 确保`positionFormula`和`sizeFormula`基于真实可获取的来源

### 3. 职责划分优化
- 确保`Analyzer`只负责分析世界，不假设行为
- 确保`MapGenerator`只负责生成地图，不包含业务逻辑

### 4. 代码结构优化
- 添加必要的Kotlin文档注释，解释关键逻辑
- 确保命名规范，语义清晰

## 三、修复原则
- 严格遵循最高语义法则，不包含执行流程、策略和if/else语义判断
- 所有数据必须来自真实可获取的来源
- 删除不可验证或冗余字段，不保留"以后可能有用"的字段
- 确保Executor盲目执行地图时，世界行为仍然正确

## 四、预期结果
- 修复后的模块结构清晰，职责单一
- 语义一致，符合最高语义法则
- 数据可物理获取，不包含不可验证字段
- 地图简洁，不包含冗余字段
- 可编译、可运行