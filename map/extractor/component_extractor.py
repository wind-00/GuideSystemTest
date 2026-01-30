#!/usr/bin/env python3
"""
组件提取器，用于从Kotlin文件中提取组件信息并添加到页面中
"""

from map.utils.file_utils import read_file, get_file_name
from map.parser.activity_parser import parse_activity_class
from map.parser.component_parser import parse_components

def extract_components_to_pages(kotlin_files, pages):
    """
    从Kotlin文件列表中提取组件信息并添加到对应的页面中
    
    Args:
        kotlin_files (list): Kotlin文件路径列表
        pages (list): 页面列表
        
    Returns:
        list: 更新后的页面列表，包含组件信息
    """
    # 创建pageId到页面对象的映射，便于快速查找
    page_map = {page['pageId']: page for page in pages}
    
    for file_path in kotlin_files:
        content = read_file(file_path)
        file_name = get_file_name(file_path)
        
        # 解析Activity类信息
        activity_info = parse_activity_class(content, file_name)
        if activity_info:
            page_id = activity_info['pageId']
            
            # 如果页面存在，提取组件并添加到页面中
            if page_id in page_map:
                components = parse_components(content)
                page_map[page_id]['components'] = components
    
    return pages
