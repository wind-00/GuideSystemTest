# UI Map Builder重构计划

## 一、重构目标
根据用户提出的新建模原则，重构UI地图生成器，生成符合要求的静态UI状态地图，用于路径规划。

## 二、主要修改点

### 1. 顶层结构修改
- 更新版本号从1.0到1.1

### 2. Page结构修改
- 添加`pageRole`字段，可选值：DETAIL | LIST | FORM | DIALOG | ENTRY
- 保留`entryPoint`字段
- 移除`pageName`字段（禁止自然语言描述）

### 3. Component结构修改
- 添加`semanticRole`字段，可选值：FILTER | ACTION | NAVIGATE | TOGGLE | INPUT
- 添加`intentTags`数组
- 将`effects`字段改为`triggers`数组，每个trigger包含`triggerType`和`effect`字段
- 移除`text`字段（禁止自然语言描述）

### 4. Effect设计修改
- 页面跳转：`effectType: NAVIGATION`，包含`targetPageId`和`navigationRole`
- 页面内状态变化：`effectType: STATE_CHANGE`，包含`stateScope`、`stateKey`和`stateValue`
- 纯交互：`effectType: UI_INTERACTION`，包含`interactionRole`
- 禁止使用NO_OP

### 5. 硬约束实现
- 确保所有component满足：改变page、改变显式state或具有明确interactionRole
- 确保所有effect可被Planner判定是否值得作为路径终点或只是中间步骤

## 三、文件修改计划

### 1. `extractor/page_extractor.py`
- 修改`extract_pages`函数，添加`pageRole`字段
- 移除`pageName`字段

### 2. `parser/activity_parser.py`
- 修改`parse_activity_class`函数，移除`pageName`字段
- 添加页面角色判断逻辑

### 3. `parser/component_parser.py`
- 修改`parse_components`函数，添加`semanticRole`和`intentTags`字段
- 移除`text`字段
- 将`supportedTriggers`转换为`triggers`数组结构

### 4. `parser/effect_parser.py`
- 修改`parse_click_effects`和其他效果解析函数，使用新的effect结构
- 移除NO_OP效果，改为合适的effect类型

### 5. `extractor/effect_extractor.py`
- 修改`extract_effects_to_components`函数，将效果添加到`triggers`数组中
- 移除自动添加NO_OP效果的逻辑

### 6. `extractor/map_validator.py`
- 修改`validate_and_enhance_map`函数，适应新的结构
- 添加对新effect结构的验证
- 确保所有component满足硬约束

### 7. `generator/json_generator.py`
- 更新版本号从1.0到1.1

## 四、重构步骤

1. 首先修改数据结构定义，确保所有组件生成正确的结构
2. 然后修改解析逻辑，确保从Kotlin代码中提取正确的信息
3. 接着修改验证和增强逻辑，确保生成的地图符合硬约束
4. 最后修改JSON生成逻辑，更新版本号

## 五、预期输出

生成符合新建模原则的UI地图JSON文件，包含：
- 正确的版本号1.1
- 每个page包含pageId、pageRole和entryPoint字段
- 每个component包含componentId、viewType、semanticRole、intentTags和triggers字段
- 每个effect符合新的设计原则，没有NO_OP
- 所有component满足硬约束

## 六、验证方法

1. 运行重构后的脚本，生成UI地图JSON
2. 手动验证JSON结构是否符合要求
3. 确保所有component满足硬约束
4. 确保所有effect可被Planner判定