#!/usr/bin/env python3
"""
页面提取器，用于从Kotlin文件中提取页面信息
"""

from map.utils.file_utils import read_file, get_file_name
from map.parser.activity_parser import parse_activity_class

def extract_pages(kotlin_files):
    """
    从Kotlin文件列表中提取所有页面
    
    Args:
        kotlin_files (list): Kotlin文件路径列表
        
    Returns:
        list: 页面列表，每个页面包含pageId、pageName等信息
    """
    pages = []
    
    for file_path in kotlin_files:
        content = read_file(file_path)
        file_name = get_file_name(file_path)
        
        activity_info = parse_activity_class(content, file_name)
        if activity_info:
            pages.append({
                'pageId': activity_info['pageId'],
                'pageRole': activity_info['pageRole'],
                'components': [],
                'entryPoint': False
            })
    
    return pages

def get_page_by_id(pages, page_id):
    """
    根据pageId获取页面
    
    Args:
        pages (list): 页面列表
        page_id (str): 页面ID
        
    Returns:
        dict: 页面信息，如果找不到返回None
    """
    for page in pages:
        if page['pageId'] == page_id:
            return page
    return None
