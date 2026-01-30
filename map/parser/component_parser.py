#!/usr/bin/env python3
"""
组件解析器，用于从Kotlin文件中提取组件信息
"""

import re

def parse_components(file_content):
    """
    从文件内容中解析所有组件信息
    
    Args:
        file_content (str): 文件内容
        
    Returns:
        list: 组件列表，每个组件包含componentId、viewType、supportedTriggers等字段
    """
    components = []
    
    # 提取所有binding.xxx.监听器调用
    # 监听器类型映射
    listener_triggers = {
        'setOnClickListener': 'CLICK',
        'setOnLongClickListener': 'LONG_CLICK',
        'setOnCheckedChangeListener': 'CHECKED_CHANGE',
        'setOnSeekBarChangeListener': 'PROGRESS_CHANGE',
        'setOnTouchListener': 'TOUCH'
    }
    
    # 用于存储组件ID和对应的触发类型
    component_triggers = {}
    
    # 遍历所有监听器类型
    for listener_method, trigger_type in listener_triggers.items():
        # 匹配binding.xxx.listener_method
        pattern = re.compile(r'binding\.(\w+)\.' + listener_method)
        matches = pattern.finditer(file_content)
        
        for match in matches:
            component_id = match.group(1)
            
            if component_id not in component_triggers:
                component_triggers[component_id] = []
            
            if trigger_type not in component_triggers[component_id]:
                component_triggers[component_id].append(trigger_type)
    
    # 为每个组件确定viewType和semanticRole
    for component_id, trigger_types in component_triggers.items():
        view_type = determine_view_type(component_id)
        
        # 确定semanticRole
        semantic_role = determine_semantic_role(component_id, view_type)
        
        # 提取intentTags
        intent_tags = extract_intent_tags(component_id)
        
        # 构建triggers数组
        triggers = []
        for trigger_type in trigger_types:
            triggers.append({
                'triggerType': trigger_type,
                'effect': None  # effect将由effect_extractor.py填充
            })
        
        components.append({
            'componentId': component_id,
            'viewType': view_type,
            'semanticRole': semantic_role,
            'intentTags': intent_tags,
            'triggers': triggers
        })
    
    return components

def determine_view_type(component_id):
    """
    根据组件ID确定组件类型
    
    Args:
        component_id (str): 组件ID
        
    Returns:
        str: 组件类型
    """
    if component_id.startswith('btn'):
        if 'icon' in component_id.lower():
            return 'ICON_BUTTON'
        return 'BUTTON'
    elif component_id.startswith('switch'):
        return 'SWITCH'
    elif component_id.startswith('checkbox'):
        return 'CHECKBOX'
    elif component_id.startswith('radio'):
        return 'RADIO_BUTTON'
    elif component_id.startswith('seekBar') or component_id.startswith('slider'):
        return 'SEEKBAR'
    elif component_id.startswith('txt'):
        return 'TEXT_VIEW'
    elif component_id.startswith('img'):
        return 'IMAGE_VIEW'
    elif component_id.endswith('Group'):
        return 'VIEW_GROUP'
    else:
        return 'VIEW'

def determine_semantic_role(component_id, view_type):
    """
    根据组件ID和viewType确定semanticRole
    
    Args:
        component_id (str): 组件ID
        view_type (str): 组件类型
        
    Returns:
        str: 组件的semanticRole
    """
    # INPUT 仅用于：EditText/TextField, Slider/SeekBar, Switch/Checkbox
    input_view_types = ['SWITCH', 'CHECKBOX', 'RADIO_BUTTON', 'SEEKBAR']
    if 'edit' in component_id.lower() or 'input' in component_id.lower():
        return 'INPUT'
    elif view_type in input_view_types:
        return 'INPUT'
    
    # 根据组件ID关键词确定semanticRole
    if 'btn' in component_id:
        if 'filter' in component_id.lower():
            return 'FILTER'
        elif 'nav' in component_id.lower() or 'back' in component_id.lower():
            return 'NAVIGATE'
        else:
            return 'ACTION'
    elif 'filter' in component_id.lower():
        return 'FILTER'
    elif 'switch' in component_id.lower() or 'toggle' in component_id.lower():
        return 'TOGGLE'
    elif view_type in ['BUTTON', 'ICON_BUTTON']:
        return 'ACTION'
    elif view_type in ['TEXT_VIEW'] and 'filter' in component_id.lower():
        return 'FILTER'
    else:
        return 'ACTION'

def extract_intent_tags(component_id):
    """
    从组件ID中提取intentTags
    
    Args:
        component_id (str): 组件ID
        
    Returns:
        list: intentTags列表
    """
    tags = []
    
    # 拆分组件ID，提取关键词
    # 例如：taskFilterWork -> ['task', 'filter', 'work']
    import re
    # 使用正则表达式将驼峰命名转换为下划线命名，然后拆分
    snake_case = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', component_id).lower()
    parts = snake_case.split('_')
    
    # 添加常见的关键词
    for part in parts:
        if part and part not in ['btn', 'txt', 'img', 'view', 'layout', 'container', 'll', 'rl', 'fl']:
            tags.append(part)
    
    # 去重
    return list(set(tags))
