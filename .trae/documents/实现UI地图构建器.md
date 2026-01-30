# UI地图构建器实现计划

## 项目概述
实现一个独立的Python模块，用于从Android Kotlin代码中静态生成UI地图JSON，供路径规划器使用，完全不依赖运行时环境或动态状态。

## 实现方案

### 1. 技术选型
- 使用Python语言开发
- 依赖正则表达式进行代码解析
- 无需额外安装依赖库
- 模块化设计，便于扩展和维护

### 2. 项目结构

```
├── ui_map_builder.py          # 主脚本，命令行入口
├── parser/                    # 代码解析模块
│   ├── __init__.py
│   ├── activity_parser.py     # Activity解析器
│   ├── component_parser.py    # 组件解析器
│   └── effect_parser.py       # 效果解析器
├── extractor/                 # 组件及效果提取模块
│   ├── __init__.py
│   ├── page_extractor.py      # 页面提取器
│   ├── component_extractor.py # 组件提取器
│   └── effect_extractor.py    # 效果提取器
├── generator/                 # JSON 生成模块
│   ├── __init__.py
│   └── json_generator.py      # JSON生成器
└── utils/                     # 工具函数
    ├── __init__.py
    └── file_utils.py          # 文件处理工具
```

### 3. 核心功能

#### 3.1 页面提取
- 遍历指定目录下的所有Kotlin文件
- 识别继承自AppCompatActivity或Fragment的类作为页面
- 生成pageId和pageName

#### 3.2 组件提取
- 从页面文件中提取所有UI组件
- 提取组件信息：
  - componentId：组件唯一ID
  - viewType：组件类型（BUTTON、SWITCH、SEEKBAR等）
  - text或contentDescription（如有）
  - supportedTriggers：组件支持的触发类型

#### 3.3 效果提取
- 对组件的触发回调逻辑进行静态分析
- 识别效果类型：
  - NAVIGATION：页面跳转，需标明targetPageId
  - NAVIGATION_BACK：返回上一级页面
  - STATE_CHANGE：仅改变当前页面内部状态
  - NO_OP：无效果或无法识别

#### 3.4 JSON生成
- 按照指定格式生成JSON输出
- 输出文件名为ui_map.json
- 包含版本信息、页面列表、组件列表和效果列表

### 4. 实现步骤

1. **创建项目结构**：建立各模块目录和文件
2. **实现工具函数**：file_utils.py，用于文件遍历和读取
3. **实现页面解析**：activity_parser.py和page_extractor.py
4. **实现组件解析**：component_parser.py和component_extractor.py
5. **实现效果解析**：effect_parser.py和effect_extractor.py
6. **实现JSON生成**：json_generator.py
7. **实现主脚本**：ui_map_builder.py，提供命令行接口
8. **测试脚本运行**

### 5. 命令行接口

```bash
python ui_map_builder.py --dir <path_to_kotlin_files> --output <output_file_path>
```

### 6. 输出格式

严格按照用户指定的格式生成JSON，包含：
- version：版本号
- pages：页面列表
  - pageId：页面唯一ID
  - pageName：页面名称
  - components：组件列表
    - componentId：组件唯一ID
    - viewType：组件类型
    - text：组件文本（如有）
    - supportedTriggers：支持的触发类型
    - effects：效果列表
      - effectType：效果类型
      - targetPageId：目标页面ID（仅NAVIGATION效果）

### 7. 关键实现细节

#### 7.1 静态分析方法
- 使用正则表达式匹配Kotlin语法模式
- 识别组件声明和监听器设置
- 分析监听器回调函数的逻辑
- 提取页面跳转和返回语句

#### 7.2 组件类型识别
- 基于组件ID前缀识别：
  - btn：BUTTON
  - switch：SWITCH
  - checkbox：CHECKBOX
  - radio：RADIO_BUTTON
  - seekBar或slider：SEEKBAR
  - txt：TEXT_VIEW
  - img：IMAGE_VIEW
  - icon：ICON_BUTTON

#### 7.3 触发类型识别
- 基于监听器类型识别：
  - setOnClickListener：CLICK
  - setOnLongClickListener：LONG_CLICK
  - setOnCheckedChangeListener：CHECKED_CHANGE
  - setOnSeekBarChangeListener：PROGRESS_CHANGE
  - setOnTouchListener：TOUCH

#### 7.4 效果类型识别
- 基于回调函数内容识别：
  - startActivity调用：NAVIGATION
  - finish或onBackPressed调用：NAVIGATION_BACK
  - 其他状态改变操作：STATE_CHANGE
  - 无法识别的操作：NO_OP

### 8. 测试计划

1. **单元测试**：测试各模块的核心功能
2. **集成测试**：测试完整的生成流程
3. **实际项目测试**：使用用户提供的项目进行测试
4. **输出验证**：验证生成的JSON格式是否符合要求

### 9. 后续优化方向

- 支持更多类型的页面（Dialog、BottomSheet）
- 支持更多类型的监听器和效果
- 提高代码解析的准确性和健壮性
- 添加更多命令行参数选项
- 支持配置文件

## 预期结果

- 成功生成符合要求的UI地图JSON文件
- 包含应用中所有的页面、组件和效果
- 效果类型准确识别，支持页面跳转、返回、状态变化和无操作
- 生成的JSON文件可直接用于路径规划
- 代码结构清晰，易于扩展和维护