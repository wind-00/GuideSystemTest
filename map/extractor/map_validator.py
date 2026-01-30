#!/usr/bin/env python3
"""
地图验证器，用于验证和增强UI地图，确保符合静态图要求
"""

def validate_and_enhance_map(pages):
    """
    验证并增强UI地图，确保符合静态图要求
    
    核心原则：
    1. pageId 是 UI 状态节点的唯一标识，同一个 pageId 在最终地图中只能出现一次
    2. 所有具有相同 pageId 的 page 实体必须被合并
    3. 合并规则：components 按 componentId 去重后合并
    4. interactionDensity 等派生统计字段不应混入核心地图
    
    Args:
        pages (list): 页面列表
        
    Returns:
        tuple: (is_valid, validated_pages, errors)
    """
    errors = []
    
    # 第一步：合并同 pageId 的所有页面
    page_map = {}
    for page in pages:
        page_id = page['pageId']
        if page_id not in page_map:
            # 初始化页面结构
            page_map[page_id] = {
                'pageId': page_id,
                'components': [],
                'component_ids': set()  # 用于去重
            }
        
        # 合并组件，按 componentId 去重
        for component in page['components']:
            component_id = component['componentId']
            if component_id not in page_map[page_id]['component_ids']:
                page_map[page_id]['components'].append(component)
                page_map[page_id]['component_ids'].add(component_id)
    
    # 获取合并后的页面列表
    merged_pages = list(page_map.values())
    
    # 第二步：收集所有pageId，用于验证页面存在性
    page_ids = [page['pageId'] for page in merged_pages]
    
    # 第三步：分析页面入度，确定entryPoint
    page_in_degree = {page_id: 0 for page_id in page_ids}
    
    # 第四步：分析页面跳转关系，建立页面导航图
    page_navigation_graph = {page_id: [] for page_id in page_ids}
    page_back_graph = {page_id: [] for page_id in page_ids}
    
    # 计算页面入度和导航图
    for page in merged_pages:
        page_id = page['pageId']
        for component in page['components']:
            for trigger in component['triggers']:
                effect = trigger['effect']
                if effect and effect['effectType'] == 'NAVIGATION':
                    if 'targetPageId' in effect:
                        target_page_id = effect['targetPageId']
                        if target_page_id in page_ids:
                            page_in_degree[target_page_id] += 1
                            page_navigation_graph[page_id].append(target_page_id)
                            page_back_graph[target_page_id].append(page_id)
    
    # 确定entryPoint：入度为0且是MainActivity或页面列表中的第一个页面
    entry_point = None
    for page in merged_pages:
        page_id = page['pageId']
        if page_in_degree[page_id] == 0:
            if page_id == 'MainActivity' or entry_point is None:
                entry_point = page_id
    
    # 第五步：为每个合并后的页面增强和修复
    validated_pages = []
    for page in merged_pages:
        validated_page = page.copy()
        page_id = validated_page['pageId']
        
        # 5.1 设置entryPoint
        validated_page['entryPoint'] = page_id == entry_point
        
        # 5.2 移除 component_ids 辅助字段（仅用于合并）
        if 'component_ids' in validated_page:
            del validated_page['component_ids']
        
        # 5.3 验证并增强组件
        validated_components = []
        for component in validated_page['components']:
            validated_component = component.copy()
            is_back_component = 'back' in validated_component['componentId'].lower() or validated_component['semanticRole'] == 'NAVIGATE'
            
            # 5.3.1 增强和修复每个trigger和effect
            for trigger in validated_component['triggers']:
                trigger_type = trigger['triggerType']
                effect = trigger['effect']
                
                # 5.3.1.1 确保effect存在
                if not effect:
                    errors.append(f"Component {validated_component['componentId']} in page {page_id}: Trigger {trigger_type} has no effect")
                    # 自动修复：根据semanticRole生成合适的effect
                    if is_back_component:
                        trigger['effect'] = {
                            'effectType': 'NAVIGATION',
                            'navigationRole': 'BACK'
                        }
                    elif validated_component['semanticRole'] in ['FILTER', 'TOGGLE', 'INPUT']:
                        trigger['effect'] = {
                            'effectType': 'STATE_CHANGE',
                            'stateScope': 'PAGE_LOCAL',
                            'stateKey': f'{validated_component["componentId"]}_state',
                            'stateDelta': 'CHANGED'
                        }
                    else:
                        interaction_role = 'ACTIVATE'
                        if validated_component['semanticRole'] == 'ACTION':
                            interaction_role = 'ACTIVATE'
                        elif validated_component['viewType'] in ['SEEKBAR']:
                            interaction_role = 'ADJUST'
                        else:
                            interaction_role = 'SELECT'
                        trigger['effect'] = {
                            'effectType': 'UI_INTERACTION',
                            'interactionRole': interaction_role
                        }
                    continue
                
                # 5.3.1.2 修复NAVIGATION效果
                if effect['effectType'] == 'NAVIGATION':
                    if 'navigationRole' not in effect:
                        effect['navigationRole'] = 'ENTER_FLOW'
                    
                    # 修复语义为"返回"的组件
                    if is_back_component or effect.get('navigationRole') == 'BACK':
                        effect['effectType'] = 'NAVIGATION'
                        effect['navigationRole'] = 'BACK'
                        
                        # 移除所有关于实际返回页面的假设字段
                        if 'returnPolicy' in effect:
                            del effect['returnPolicy']
                        if 'fallbackTargetPageId' in effect:
                            del effect['fallbackTargetPageId']
                        if 'possibleTargetPageIds' in effect:
                            del effect['possibleTargetPageIds']
                    
                    # 确保NAVIGATION有目标（仅前进操作需要）
                    if effect['navigationRole'] != 'BACK':
                        has_target = 'targetPageId' in effect and effect['targetPageId']
                        has_possible_targets = 'possibleTargetPageIds' in effect and effect['possibleTargetPageIds']
                        
                        if not has_target and not has_possible_targets:
                            errors.append(f"Component {validated_component['componentId']} in page {page_id}: NAVIGATION effect has no target")
                
                # 5.3.1.3 确保所有back组件都使用正确的NAVIGATION效果
                elif is_back_component and effect['effectType'] != 'NAVIGATION':
                    effect['effectType'] = 'NAVIGATION'
                    effect['navigationRole'] = 'BACK'
                    
                    if 'returnPolicy' in effect:
                        del effect['returnPolicy']
                    if 'fallbackTargetPageId' in effect:
                        del effect['fallbackTargetPageId']
                    if 'possibleTargetPageIds' in effect:
                        del effect['possibleTargetPageIds']
                    if 'interactionRole' in effect:
                        del effect['interactionRole']
                    
                # 5.3.1.4 修复STATE_CHANGE效果
                elif effect['effectType'] == 'STATE_CHANGE':
                    # 修复stateKey，使其语义明确
                    if 'stateKey' not in effect or effect['stateKey'] in ['unknown', 'toggle', 'input', 'filter']:
                        state_key = f'{validated_component["componentId"]}_state'
                        if validated_component['semanticRole'] == 'FILTER':
                            state_key = f'{validated_component["componentId"]}_filter'
                        elif validated_component['semanticRole'] == 'TOGGLE' or validated_component['viewType'] in ['SWITCH', 'CHECKBOX']:
                            state_key = f'{validated_component["componentId"]}_checked'
                        elif validated_component['semanticRole'] == 'INPUT' or validated_component['viewType'] in ['SEEKBAR', 'SLIDER']:
                            state_key = f'{validated_component["componentId"]}_value'
                        effect['stateKey'] = state_key
                    
                    # 添加状态域声明，增强静态语义信息
                    if validated_component['viewType'] in ['SWITCH', 'CHECKBOX', 'RADIO_GROUP', 'RADIO_BUTTON']:
                        effect['stateType'] = 'BOOLEAN'
                    elif validated_component['viewType'] in ['SEEKBAR', 'SLIDER']:
                        effect['stateType'] = 'SCALAR'
                    elif validated_component['semanticRole'] == 'FILTER':
                        effect['stateType'] = 'ENUM'
                    else:
                        effect['stateType'] = 'UNKNOWN'
                    
                    effect['isReversible'] = True
                    effect['stateImpact'] = 'LOCAL'
                    
                    # 移除具体的状态取值推断
                    if 'stateValue' in effect:
                        del effect['stateValue']
                    
                    effect['stateDelta'] = 'CHANGED'
                
                # 确保BACK按钮使用正确的效果类型
                if is_back_component and effect['effectType'] != 'NAVIGATION':
                    effect['effectType'] = 'NAVIGATION'
                    if 'interactionRole' in effect:
                        del effect['interactionRole']
                
                # 5.3.1.5 细化UI_INTERACTION效果
                elif effect['effectType'] == 'UI_INTERACTION':
                    if 'interactionRole' not in effect or effect['interactionRole'] == 'CONFIRM':
                        if validated_component['semanticRole'] == 'ACTION':
                            if 'submit' in validated_component['componentId'].lower() or 'login' in validated_component['componentId'].lower() or 'register' in validated_component['componentId'].lower():
                                effect['interactionRole'] = 'SUBMIT'
                            else:
                                effect['interactionRole'] = 'ACTIVATE'
                        elif validated_component['semanticRole'] == 'FILTER':
                            effect['interactionRole'] = 'SELECT'
                        elif validated_component['semanticRole'] == 'TOGGLE':
                            effect['interactionRole'] = 'ACTIVATE'
                        elif validated_component['viewType'] in ['SEEKBAR', 'SLIDER']:
                            effect['interactionRole'] = 'ADJUST'
                        elif validated_component['semanticRole'] in ['INPUT', 'TEXT']:
                            effect['interactionRole'] = 'SELECT'
                        elif validated_component['semanticRole'] == 'NAVIGATE':
                            effect['interactionRole'] = 'ACTIVATE'
                        else:
                            effect['interactionRole'] = 'SELECT'
            
            # 5.3.2 规范修正：semanticRole 统一修正规则
            input_components = ['EDITTEXT', 'TEXT_FIELD', 'SWITCH', 'CHECKBOX', 'SEEKBAR', 'SLIDER', 'RADIO_GROUP', 'RADIO_BUTTON']
            has_click_trigger_to_nav_or_ui = False
            has_state_change_trigger = False
            
            for trigger in validated_component['triggers']:
                trigger_type = trigger['triggerType']
                effect = trigger['effect']
                if trigger_type in ['CLICK', 'LONG_CLICK', 'TOUCH'] and effect['effectType'] in ['NAVIGATION', 'UI_INTERACTION']:
                    has_click_trigger_to_nav_or_ui = True
                if trigger_type in ['CHECKED_CHANGE', 'PROGRESS_CHANGE'] or effect['effectType'] == 'STATE_CHANGE':
                    has_state_change_trigger = True
            
            # 修正 RadioGroup 组件的 semanticRole
            if validated_component['viewType'] == 'RADIO_GROUP':
                has_state_change = any(trig['triggerType'] in ['CHECKED_CHANGE'] for trig in validated_component['triggers'])
                if has_state_change:
                    validated_component['semanticRole'] = 'INPUT'
            
            # 修正 FILTER 组件的语义处理
            if validated_component['semanticRole'] == 'FILTER':
                for trigger in validated_component['triggers']:
                    effect = trigger['effect']
                    if effect['effectType'] == 'UI_INTERACTION':
                        effect['interactionRole'] = 'SELECT'
            
            # 基础语义角色修正规则
            if has_state_change_trigger and validated_component['semanticRole'] in ['ACTION', 'NAVIGATE']:
                if validated_component['viewType'] in ['SWITCH', 'CHECKBOX', 'SEEKBAR', 'SLIDER', 'RADIO_GROUP', 'RADIO_BUTTON']:
                    validated_component['semanticRole'] = 'INPUT'
            
            elif has_click_trigger_to_nav_or_ui and validated_component['semanticRole'] == 'INPUT':
                if validated_component['viewType'] not in input_components and validated_component['semanticRole'] != 'FILTER':
                    if is_back_component:
                        validated_component['semanticRole'] = 'NAVIGATE'
                    else:
                        validated_component['semanticRole'] = 'ACTION'
            
            # 5.3.3 规范修正：Radio 组件 viewType 规范增强
            component_id = validated_component['componentId'].lower()
            if 'radio' in component_id:
                if 'group' in component_id or validated_component['viewType'] == 'RADIO_BUTTON' and 'group' in component_id:
                    validated_component['viewType'] = 'RADIO_GROUP'
                    if validated_component['semanticRole'] not in ['INPUT', 'SELECTION']:
                        validated_component['semanticRole'] = 'INPUT'
                elif validated_component['viewType'] not in ['RADIO_BUTTON', 'RADIO_GROUP']:
                    validated_component['viewType'] = 'RADIO_BUTTON'
                    if validated_component['semanticRole'] not in ['INPUT', 'SELECTION']:
                        validated_component['semanticRole'] = 'INPUT'
            
            # 5.3.4 为组件生成 canonicalIntent - 确保所有组件都执行
            canonical_intent = None
            
            # 直接基于组件属性生成canonicalIntent
            if 'back' in validated_component['componentId'].lower() or validated_component['semanticRole'] == 'NAVIGATE':
                canonical_intent = 'back'
            elif 'filter' in validated_component['semanticRole'].lower():
                canonical_intent = 'filter'
            elif 'input' in validated_component['semanticRole'].lower() or validated_component['viewType'] in ['SWITCH', 'CHECKBOX', 'SEEKBAR', 'SLIDER', 'RADIO_GROUP', 'RADIO_BUTTON']:
                canonical_intent = 'toggle' if validated_component['viewType'] in ['SWITCH', 'CHECKBOX', 'RADIO_GROUP', 'RADIO_BUTTON'] else 'input'
            elif 'action' in validated_component['semanticRole'].lower():
                canonical_intent = 'action'
            
            # 确保所有组件都有canonicalIntent
            if not canonical_intent:
                # 兜底：使用componentId的前缀或默认值
                component_id = validated_component['componentId'].lower()
                if '_' in component_id:
                    canonical_intent = component_id.split('_')[0]
                else:
                    canonical_intent = component_id
            
            # 强制添加canonicalIntent到组件
            validated_component['canonicalIntent'] = canonical_intent
            
            # 5.3.5 验证component是否满足硬约束
            component_meets_constraint = False
            for trigger in validated_component['triggers']:
                effect = trigger['effect']
                if effect:
                    effect_type = effect['effectType']
                    
                    if effect_type == 'NAVIGATION':
                        component_meets_constraint = True
                        break
                    
                    elif effect_type == 'STATE_CHANGE':
                        component_meets_constraint = True
                        break
                    
                    elif effect_type == 'UI_INTERACTION' and 'interactionRole' in effect:
                        component_meets_constraint = True
                        break
            
            if not component_meets_constraint:
                errors.append(f"Component {validated_component['componentId']} in page {page_id}: Does not meet constraint")
                # 自动修复
                for trigger in validated_component['triggers']:
                    if validated_component['semanticRole'] == 'ACTION':
                        interaction_role = 'SUBMIT' if 'submit' in validated_component['componentId'].lower() else 'ACTIVATE'
                    elif validated_component['semanticRole'] == 'FILTER':
                        interaction_role = 'SELECT'
                    elif validated_component['semanticRole'] == 'TOGGLE':
                        interaction_role = 'ACTIVATE'
                    elif validated_component['viewType'] in ['SEEKBAR']:
                        interaction_role = 'ADJUST'
                    else:
                        interaction_role = 'SELECT'
                    
                    trigger['effect'] = {
                        'effectType': 'UI_INTERACTION',
                        'interactionRole': interaction_role
                    }
            
            validated_components.append(validated_component)
        
        validated_page['components'] = validated_components
        validated_pages.append(validated_page)
    
    # 5.5 移除虚拟组件：只忠实记录实际构造，禁止添加虚拟组件
    # 不再为页面添加虚拟的auto_back_btn组件，只使用实际存在的组件
    # 这样可以确保fsm_transition.json中的映射与实际页面组件完全匹配
    
    is_valid = len(errors) == 0
    return is_valid, validated_pages, errors
