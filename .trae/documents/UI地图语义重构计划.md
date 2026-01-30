# UI地图语义重构计划

## 一、当前结构问题分析

1. **Intent语义模糊**
   - 使用`expectedNextStateIds`预测性字段，违反确定性要求
   - 没有明确的状态转移定义（fromStateId → toStateId）
   - 缺乏明确的后置条件验证跳转成功

2. **Component职责越界**
   - 隐式表达行为后果
   - 没有明确区分静态属性和行为触发

3. **State与Intent语义重叠**
   - State可能包含推测性信息
   - 缺少明确的状态识别规则

## 二、重构目标

让生成的地图能够明确回答：在某一个明确的页面状态下，用户对某一个明确的组件执行某一种触发行为，系统将确定性地进入哪一个状态，并且可以通过哪些条件验证该跳转是否成功。

## 三、重构方案

### 1. 重构Intent模型

将Intent从"事件描述"重构为"状态转移定义"，包含以下核心字段：

| 字段名 | 类型 | 描述 | 必要性 |
| --- | --- | --- | --- |
| `intentId` | 字符串 | 意图唯一标识 | 必须 |
| `description` | 字符串 | 意图描述 | 必须 |
| `fromStateId` | 字符串 | 起始状态ID | 必须 |
| `trigger` | 对象 | 触发条件（componentId + triggerType） | 必须 |
| `toStateId` | 字符串 | 目标状态ID | 必须 |
| `postConditions` | 数组 | 跳转成功验证条件 | 必须 |
| `failureHandling` | 对象 | 失败处理策略 | 可选 |

### 2. 删除预测性字段

- 删除所有`expectedNextStateIds`字段
- 所有状态转移必须通过`fromStateId` → `toStateId`明确表达

### 3. 明确State职责

- 只允许描述"我是什么页面状态"
- 只允许定义"如何通过signals识别我"
- 只允许作为Intent的起点或终点被引用

### 4. 收敛Component职责

- 只允许描述UI类型、静态属性、位置尺寸、支持的触发类型
- 不允许决定行为后果或跳转目标

## 四、重构后的JSON结构示例

```json
{
  "appMeta": {
    "appName": "GuideSystemTest",
    "packageName": "com.example.guidesystemtest",
    "versionName": "1.0.0",
    "versionCode": 1,
    "uiFramework": "VIEW"
  },
  "uiModel": {
    "pages": [/* 保持不变，只描述UI结构 */]
  },
  "stateModel": {
    "states": [/* 保持不变，只描述状态识别规则 */]
  },
  "intentModel": {
    "intents": [
      {
        "intentId": "btnToSecond1_click",
        "description": "在Main状态下点击跳转到第二层级1按钮，进入Second状态",
        "fromStateId": "Main",
        "trigger": {
          "componentId": "btnToSecond1",
          "type": "CLICK"
        },
        "toStateId": "Second",
        "postConditions": [
          {
            "type": "COMPONENT_VISIBLE",
            "target": "txtTitle",
            "expectedValue": true,
            "matcher": "EQUALS"
          },
          {
            "type": "TEXT_VISIBLE",
            "target": "第二层级",
            "expectedValue": true,
            "matcher": "EQUALS"
          }
        ],
        "failureHandling": {
          "retryable": false,
          "fallbackStateId": "Main"
        }
      }
    ]
  }
}
```

## 五、具体修复步骤

1. **分析现有Intent**：遍历所有intent，确定每个intent的fromStateId和toStateId
2. **构建postConditions**：为每个导航Intent添加合适的后置条件
3. **重构Intent结构**：将现有intent转换为新的结构
4. **删除预测性字段**：删除所有expectedNextStateIds
5. **验证结构完整性**：确保所有intent都有明确的状态转移定义
6. **生成修复后的JSON**：输出最终修复后的地图JSON

## 六、可执行路径示例

**路径**：Main → Second → Third → Back → Second → Back → Main

1. **Main → Second**
   - Intent: `btnToSecond1_click`
   - fromStateId: `Main`
   - toStateId: `Second`
   - postConditions: 验证Second页面组件可见

2. **Second → Third**
   - Intent: `btnToThird1_click`
   - fromStateId: `Second`
   - toStateId: `Third`
   - postConditions: 验证Third页面组件可见

3. **Third → Second**
   - Intent: `btnBack_click`（Third页面）
   - fromStateId: `Third`
   - toStateId: `Second`
   - postConditions: 验证Second页面组件可见

4. **Second → Main**
   - Intent: `btnBack_click`（Second页面）
   - fromStateId: `Second`
   - toStateId: `Main`
   - postConditions: 验证Main页面组件可见

## 七、预期成果

修复后的UI地图将具有以下特点：

1. **确定性**：每个Intent明确表达状态转移
2. **可验证**：每个跳转都有明确的后置条件
3. **可复用**：结构清晰，便于自动化执行
4. **语义完整**：Intent、State、Component职责边界明确
5. **无预测性**：所有行为后果都明确定义

这个重构计划将确保生成的UI地图能够作为"可执行UI行为地图"使用，满足用户提出的严格语义要求。