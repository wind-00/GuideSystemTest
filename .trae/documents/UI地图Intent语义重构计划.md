## UI地图Intent语义重构计划

### 问题分析

当前UI地图的JSON结构在UI描述层已经完整，但行为语义层存在以下问题：
1. `NO_OP`类型被过度使用，无法区分不同类型的行为
2. 同名组件在不同State下的Intent没有拆分，导致行为结果不明确
3. Intent的type类型需要更细粒度的划分

### 重构目标

将系统重构为符合以下逻辑的结构：
**Intent = 在某个 State 下，对某个 Component 的某个 Trigger，必然导致一个确定的 State 转移**

### 重构内容

1. **Intent类型细化**
   - 将Intent类型拆分为三类：
     - `NAVIGATION`：State发生变化的行为
     - `STATE_INTERNAL`：State不变，但UI内部状态改变的行为
     - `NO_STATE_CHANGE`：纯UI行为，不影响状态
   - 优化现有`NAVIGATE_BACK`类型，确保其语义清晰

2. **同名组件Intent拆分**
   - 为每个同名组件在不同State下生成独立的Intent
   - 示例：将`btnBack_click`拆分为`btnBack_click_fromSecond`和`btnBack_click_fromThird`
   - 确保每个Intent的intentId唯一且具有描述性

3. **完善Intent语义字段**
   - 确保每个Intent都包含：
     - `fromStateId`：明确的起始状态
     - `toStateId`：唯一的目标状态
     - `postConditions`：用于验证状态转移的条件
     - `trigger`：明确的触发组件和类型

4. **优化postConditions**
   - 确保postConditions与目标State的signals保持一致
   - 使用简洁有效的验证条件

### 重构步骤

1. **修改IntentModel.kt**
   - 更新IntentType枚举，添加新的类型
   - 确保Intent数据类结构正确

2. **修改MapGenerator.kt**
   - 更新`buildIntentModelFromScreenInfos`方法，实现新的Intent构建逻辑
   - 添加Intent类型判断逻辑
   - 实现同名组件Intent拆分

3. **修改run-map-generator.kts**
   - 更新脚本中的Intent构建逻辑，确保生成符合新结构的JSON

4. **运行地图生成器**
   - 生成修复后的JSON文件
   - 验证Intent结构是否符合要求

5. **验证状态转移链路**
   - 确保Main → Second → Third → Back的完整链路可用
   - 标注每一步的Intent驱动

### 预期结果

- 生成符合新语义结构的完整JSON文件
- Intent数量不减少，但语义清晰度大幅提升
- 每个Intent都能明确回答五个核心问题
- 支持自动执行引擎的理解和执行

### 最终输出

1. 完整修复后的intentModel
2. 一个明确的状态转移链路示例
3. 详细的结构说明

这个重构计划将确保UI地图成为一个确定性、可验证、可复用的行为地图，能够被自动执行引擎理解和执行。