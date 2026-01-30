#!/usr/bin/env python3
"""
JSON生成器，用于生成UI地图JSON文件
"""

import json

def generate_ui_map(pages, output_file='ui_map.json'):
    """
    生成UI地图JSON文件
    
    Args:
        pages (list): 页面列表
        output_file (str): 输出文件路径
        
    Returns:
        dict: 生成的UI地图数据
    """
    # 只包含允许的顶层实体：pages
    ui_map = {
        "pages": pages
    }
    
    # 写入JSON文件
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(ui_map, f, indent=2, ensure_ascii=False)
    
    return ui_map

def generate_ui_map_string(pages):
    """
    生成UI地图JSON字符串
    
    Args:
        pages (list): 页面列表
        
    Returns:
        str: 生成的UI地图JSON字符串
    """
    # 只包含允许的顶层实体：pages
    ui_map = {
        "pages": pages
    }
    
    return json.dumps(ui_map, indent=2, ensure_ascii=False)
