# 整合Python地图生成文件到map目录

## 目标
将所有与地图生成相关的Python文件整合到一个名为`map`的文件夹中，保持良好的模块化结构。

## 计划步骤

1. **创建目录结构**
   - 创建`map/`主目录
   - 在`map/`下创建子目录：`extractor/`、`generator/`、`parser/`、`utils/`
   - 创建`map/__init__.py`文件，将map目录作为Python包

2. **移动文件**
   - 将`extractor/`目录下的所有文件移动到`map/extractor/`
   - 将`generator/`目录下的所有文件移动到`map/generator/`
   - 将`parser/`目录下的所有文件移动到`map/parser/`
   - 将`utils/`目录下的所有文件移动到`map/utils/`
   - 将`ui_map_builder.py`移动到`map/`目录下，并重命名为`__main__.py`，以便通过`python -m map`运行

3. **修改导入语句**
   - 修改`map/__main__.py`中的导入语句
   - 修改`map/extractor/`、`map/generator/`、`map/parser/`目录下所有文件的导入语句
   - 确保所有模块间的引用路径正确

4. **创建入口脚本**
   - 在项目根目录创建一个简单的入口脚本`run_map_builder.py`，用于调用map包中的功能

5. **测试运行**
   - 运行整合后的地图生成器，确保功能正常
   - 验证生成的UI地图JSON文件与之前的一致

## 模块化保持
- 保持原有的模块化结构不变
- 每个子目录仍负责原来的功能
- 模块间的依赖关系保持不变
- 只是将文件物理位置移动到了`map/`目录下

## 预期效果
- 所有与地图生成相关的Python文件都整合到了`map/`目录中
- 保持了良好的模块化结构
- 可以通过`python -m map`或根目录的入口脚本运行
- 功能与之前完全一致

## 文件结构变化
```
# 整合前
.
├── extractor/
├── generator/
├── parser/
├── utils/
└── ui_map_builder.py

# 整合后
.
├── map/
│   ├── extractor/
│   ├── generator/
│   ├── parser/
│   ├── utils/
│   ├── __init__.py
│   └── __main__.py  # 原ui_map_builder.py
└── run_map_builder.py  # 新入口脚本
```