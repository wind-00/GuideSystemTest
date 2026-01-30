#!/usr/bin/env python3
"""
增强fsm_transition.json文件，添加action_metadata和visible_text_index映射
"""

import json
import os

def main():
    # 定义文件路径
    project_dir = os.path.dirname(os.path.abspath(__file__))
    fsm_file_path = os.path.join(project_dir, 'fsm_transition.json')
    ui_map_file_path = os.path.join(project_dir, 'ui_map.json')
    
    # 1. 读取fsm_transition.json和ui_map.json文件
    print("正在读取fsm_transition.json文件...")
    with open(fsm_file_path, 'r', encoding='utf-8') as f:
        fsm_data = json.load(f)
    
    print("正在读取ui_map.json文件...")
    with open(ui_map_file_path, 'r', encoding='utf-8') as f:
        ui_map_data = json.load(f)
    
    # 2. 构建组件信息映射（componentId → viewType, visibleText, pageId）
    print("正在构建组件信息映射...")
    component_map = {}
    for page in ui_map_data['pages']:
        page_id = page['pageId']
        for component in page['components']:
            component_id = component['componentId']
            # 提取组件信息
            view_type = component['viewType']
            visible_text = component.get('visibleText', '')
            # 构建映射
            component_map[component_id] = {
                'viewType': view_type,
                'visibleText': visible_text,
                'pageId': page_id
            }
    
    # 3. 生成action_metadata映射
    print("正在生成action_metadata映射...")
    action_metadata = {}
    for action_key, action_id in fsm_data['action_index'].items():
        # 解析action_key，格式为"(componentId, triggerType)"
        # 去掉括号并分割为componentId和triggerType
        component_id, trigger_type = action_key.strip('()').split(', ')
        
        # 从组件映射中获取信息
        component_info = component_map.get(component_id, {})
        view_type = component_info.get('viewType', '')
        visible_text = component_info.get('visibleText', '')
        page = component_info.get('pageId', '')
        
        # 构建action_metadata条目
        action_metadata[str(action_id)] = {
            'componentId': component_id,
            'triggerType': trigger_type,
            'visibleText': visible_text,
            'viewType': view_type,
            'page': page
        }
    
    # 4. 生成visible_text_index映射
    print("正在生成visible_text_index映射...")
    visible_text_index = {}
    for action_id, metadata in action_metadata.items():
        visible_text = metadata['visibleText']
        # 只处理visibleText不为空的情况
        if visible_text:
            if visible_text not in visible_text_index:
                visible_text_index[visible_text] = []
            visible_text_index[visible_text].append(int(action_id))
    
    # 5. 将新字段添加到fsm_transition.json
    print("正在更新fsm_transition.json文件...")
    fsm_data['action_metadata'] = action_metadata
    fsm_data['visible_text_index'] = visible_text_index
    
    # 保存修改后的文件
    with open(fsm_file_path, 'w', encoding='utf-8') as f:
        json.dump(fsm_data, f, ensure_ascii=False, indent=2)
    
    print("增强fsm_transition.json文件完成！")
    print(f"添加了 {len(action_metadata)} 个action_metadata条目")
    print(f"添加了 {len(visible_text_index)} 个visible_text_index条目")

if __name__ == "__main__":
    main()