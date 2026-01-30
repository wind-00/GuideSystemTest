#!/usr/bin/env python3
"""
文件处理工具函数
"""

import os
import xml.etree.ElementTree as ET

def find_kotlin_files(directory):
    """
    遍历指定目录，查找所有Kotlin文件
    
    Args:
        directory (str): 要遍历的目录路径
        
    Returns:
        list: Kotlin文件路径列表
    """
    kotlin_files = []
    
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.kt'):
                file_path = os.path.join(root, file)
                kotlin_files.append(file_path)
    
    return kotlin_files

def find_xml_files(directory):
    """
    遍历指定目录，查找所有XML布局文件
    
    Args:
        directory (str): 要遍历的目录路径
        
    Returns:
        list: XML文件路径列表
    """
    xml_files = []
    
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.xml'):
                file_path = os.path.join(root, file)
                xml_files.append(file_path)
    
    return xml_files

def read_file(file_path):
    """
    读取文件内容
    
    Args:
        file_path (str): 文件路径
        
    Returns:
        str: 文件内容
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return f.read()
    except Exception as e:
        print(f"Error reading file {file_path}: {e}")
        return ""

def get_file_name(file_path):
    """
    获取文件名（不包含扩展名）
    
    Args:
        file_path (str): 文件路径
        
    Returns:
        str: 文件名
    """
    return os.path.splitext(os.path.basename(file_path))[0]

def parse_xml_layout(xml_file_path):
    """
    解析XML布局文件，提取组件的ID、text和contentDescription属性
    
    Args:
        xml_file_path (str): XML布局文件路径
        
    Returns:
        dict: 组件ID到visibleText的映射
    """
    component_visible_text = {}
    
    try:
        # 读取XML文件
        tree = ET.parse(xml_file_path)
        root = tree.getroot()
        
        # Android XML布局文件中的命名空间处理
        # 注意：ElementTree将命名空间属性转换为带前缀的格式，如{http://schemas.android.com/apk/res/android}id
        ANDROID_NS = '{http://schemas.android.com/apk/res/android}'
        
        # 遍历所有元素，查找带有android:id属性的组件
        for element in root.iter():
            # 检查元素是否有android:id属性
            android_id_key = f'{ANDROID_NS}id'
            if android_id_key in element.attrib:
                # 提取组件ID，格式为@+id/xxx，我们只需要xxx部分
                android_id = element.attrib[android_id_key]
                if android_id.startswith('@+id/'):
                    component_id = android_id[5:]  # 去掉@+id/前缀
                    
                    # 提取android:text属性
                    text_key = f'{ANDROID_NS}text'
                    visible_text = element.attrib.get(text_key, '')
                    
                    # 如果没有text属性，尝试提取android:contentDescription属性
                    if not visible_text:
                        content_desc_key = f'{ANDROID_NS}contentDescription'
                        visible_text = element.attrib.get(content_desc_key, '')
                    
                    # 如果有visible_text，添加到映射中
                    if visible_text:
                        component_visible_text[component_id] = visible_text
    
    except Exception as e:
        print(f"Error parsing XML file {xml_file_path}: {e}")
    
    return component_visible_text

def parse_all_xml_layouts(xml_files):
    """
    解析所有XML布局文件，合并组件的visibleText映射
    
    Args:
        xml_files (list): XML布局文件路径列表
        
    Returns:
        dict: 组件ID到visibleText的映射
    """
    all_component_visible_text = {}
    
    for xml_file in xml_files:
        component_visible_text = parse_xml_layout(xml_file)
        all_component_visible_text.update(component_visible_text)
    
    return all_component_visible_text
