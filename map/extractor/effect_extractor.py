#!/usr/bin/env python3
"""
效果提取器，用于从Kotlin文件中提取效果信息并添加到对应的组件中
"""

from map.utils.file_utils import read_file, get_file_name
from map.parser.activity_parser import parse_activity_class
from map.parser.effect_parser import parse_effects

def extract_effects_to_components(kotlin_files, pages):
    """
    从Kotlin文件列表中提取效果信息并添加到对应的组件中
    
    Args:
        kotlin_files (list): Kotlin文件路径列表
        pages (list): 页面列表
        
    Returns:
        list: 更新后的页面列表，包含效果信息
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
            
            # 如果页面存在，提取效果并添加到组件中
            if page_id in page_map:
                page = page_map[page_id]
                effects = parse_effects(content)
                
                # 为每个组件添加效果
                for component in page['components']:
                    component_id = component['componentId']
                    
                    if component_id in effects:
                        component_effects = effects[component_id]
                        
                        # 为每个trigger设置对应的effect
                        for i, trigger in enumerate(component['triggers']):
                            if i < len(component_effects):
                                trigger['effect'] = component_effects[i]
                    
                    # 确保所有trigger都有effect
                    for trigger in component['triggers']:
                        if trigger['effect'] is None:
                            component_id = component['componentId']
                            is_back_component = 'back' in component_id.lower() or component['semanticRole'] == 'NAVIGATE'
                            
                            # 根据semanticRole生成合适的effect
                            if is_back_component:
                                # 返回组件使用NAVIGATION效果，navigationRole为BACK
                                trigger['effect'] = {
                                    'effectType': 'NAVIGATION',
                                    'navigationRole': 'BACK'
                                    # possibleTargetPageIds将由map_validator.py填充
                                }
                            elif component['semanticRole'] == 'FILTER':
                                # 筛选组件使用STATE_CHANGE效果，语义明确的stateKey和stateValue
                                trigger['effect'] = {
                                    'effectType': 'STATE_CHANGE',
                                    'stateScope': 'PAGE_LOCAL',
                                    'stateKey': f'{component_id}_filter',
                                    'stateValue': component_id.split('filter')[-1].lower()
                                }
                            elif component['semanticRole'] == 'TOGGLE':
                                # 切换组件使用STATE_CHANGE效果，语义明确的stateKey和stateValue
                                trigger['effect'] = {
                                    'effectType': 'STATE_CHANGE',
                                    'stateScope': 'PAGE_LOCAL',
                                    'stateKey': f'{component_id}_checked',
                                    'stateValue': 'true'
                                }
                            elif component['semanticRole'] == 'INPUT':
                                # 输入组件使用STATE_CHANGE效果，语义明确的stateKey和stateValue
                                trigger['effect'] = {
                                    'effectType': 'STATE_CHANGE',
                                    'stateScope': 'PAGE_LOCAL',
                                    'stateKey': f'{component_id}_value',
                                    'stateValue': 'updated'
                                }
                            else:
                                # 默认使用UI_INTERACTION
                                trigger['effect'] = {
                                    'effectType': 'UI_INTERACTION',
                                    'interactionRole': 'CONFIRM'
                                }
    
    return pages
