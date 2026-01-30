#!/usr/bin/env python3
"""
UI Map Builder - 从Android Kotlin代码中静态生成UI地图JSON
"""

import argparse
import os
from map.utils.file_utils import find_kotlin_files, find_xml_files, parse_all_xml_layouts
from map.extractor.page_extractor import extract_pages
from map.extractor.component_extractor import extract_components_to_pages
from map.extractor.effect_extractor import extract_effects_to_components
from map.extractor.map_validator import validate_and_enhance_map
from map.generator.json_generator import generate_ui_map

def main():
    """
    主函数，处理命令行参数并生成UI地图
    """
    # 解析命令行参数
    parser = argparse.ArgumentParser(description='UI Map Builder - 从Android Kotlin代码中静态生成UI地图JSON')
    parser.add_argument('--dir', '-d', required=True, help='Kotlin代码目录路径')
    parser.add_argument('--output', '-o', default='ui_map.json', help='输出JSON文件路径，默认ui_map.json')
    
    args = parser.parse_args()
    
    # 验证目录是否存在
    if not os.path.exists(args.dir):
        print(f"错误：目录 {args.dir} 不存在")
        return 1
    
    # 1. 查找所有Kotlin文件
    print(f"正在查找 {args.dir} 目录下的Kotlin文件...")
    kotlin_files = find_kotlin_files(args.dir)
    print(f"找到 {len(kotlin_files)} 个Kotlin文件")
    
    # 2. 查找所有XML布局文件
    print(f"正在查找 {args.dir} 目录下的XML布局文件...")
    xml_files = find_xml_files(args.dir)
    print(f"找到 {len(xml_files)} 个XML布局文件")
    
    # 3. 解析所有XML布局文件，获取组件可见文本映射
    print("正在解析XML布局文件，提取组件可见文本...")
    component_visible_text_map = parse_all_xml_layouts(xml_files)
    print(f"提取到 {len(component_visible_text_map)} 个组件的可见文本")
    
    # 4. 提取页面信息
    print("正在提取页面信息...")
    pages = extract_pages(kotlin_files)
    print(f"提取到 {len(pages)} 个页面")
    
    # 5. 提取组件信息并添加到页面中
    print("正在提取组件信息...")
    pages = extract_components_to_pages(kotlin_files, pages)
    
    # 6. 为组件添加visibleText字段
    print("正在为组件添加visibleText字段...")
    total_components = 0
    components_with_visible_text = 0
    
    for page in pages:
        for component in page['components']:
            total_components += 1
            component_id = component['componentId']
            # 检查组件是否有对应的visibleText
            if component_id in component_visible_text_map:
                component['visibleText'] = component_visible_text_map[component_id]
                components_with_visible_text += 1
    
    print(f"已为 {components_with_visible_text}/{total_components} 个组件添加visibleText字段")
    
    # 7. 提取效果信息并添加到组件中
    print("正在提取效果信息...")
    pages = extract_effects_to_components(kotlin_files, pages)
    
    # 8. 验证并增强UI地图
    print("正在验证并增强UI地图...")
    is_valid, validated_pages, errors = validate_and_enhance_map(pages)
    
    if errors:
        print("\n警告：发现以下问题：")
        for error in errors:
            print(f"- {error}")
        
        # 检查是否有致命错误（如PAGE_TRANSITION targetPageId为空）
        fatal_errors = [error for error in errors if "PAGE_TRANSITION targetPageId is empty" in error]
        if fatal_errors:
            print("\n错误：存在致命问题，无法生成有效地图")
            return 1
        
        print("\n正在修复可自动修复的问题...")
    
    # 9. 生成UI地图JSON文件
    print(f"正在生成UI地图JSON文件 {args.output}...")
    generate_ui_map(validated_pages, args.output)
    
    print("UI地图生成完成！")
    print(f"输出文件：{args.output}")
    
    return 0

if __name__ == "__main__":
    exit(main())
