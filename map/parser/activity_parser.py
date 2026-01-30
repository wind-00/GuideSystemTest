#!/usr/bin/env python3
"""
Activity解析器，用于从Kotlin文件中提取Activity类信息
"""

import re

def parse_activity_class(file_content, file_name):
    """
    从文件内容中解析Activity类信息
    
    Args:
        file_content (str): 文件内容
        file_name (str): 文件名
        
    Returns:
        dict: Activity类信息，包含pageId、pageName、is_activity等字段；如果不是Activity类，返回None
    """
    # 匹配继承自AppCompatActivity或Fragment的类
    class_pattern = re.compile(r'class\s+(\w+)\s*:\s*(AppCompatActivity|Fragment)')
    match = class_pattern.search(file_content)
    
    if match:
        class_name = match.group(1)
        base_class = match.group(2)
        
        # 确定页面角色
        if 'Main' in class_name:
            page_role = 'ENTRY'
        elif 'List' in class_name:
            page_role = 'LIST'
        elif 'Detail' in class_name:
            page_role = 'DETAIL'
        elif 'Edit' in class_name or 'Form' in class_name:
            page_role = 'FORM'
        elif 'Dialog' in class_name:
            page_role = 'DIALOG'
        else:
            # 默认角色
            page_role = 'DETAIL'
        
        return {
            'pageId': class_name,
            'pageRole': page_role,
            'is_activity': base_class == 'AppCompatActivity',
            'is_fragment': base_class == 'Fragment'
        }
    
    return None
