#!/usr/bin/env python3
"""
效果解析器，用于从Kotlin文件中提取组件效果信息
"""

import re

def parse_effects(file_content):
    """
    从文件内容中解析所有组件的效果信息
    
    Args:
        file_content (str): 文件内容
        
    Returns:
        dict: 组件效果映射，键为componentId，值为效果列表
    """
    effects = {}
    
    # 解析点击事件效果
    effects.update(parse_click_effects(file_content))
    
    # 解析其他事件效果
    # 目前只实现点击事件，其他事件类型可以根据需要扩展
    
    return effects

def parse_click_effects(file_content):
    """
    解析点击事件的效果信息
    
    Args:
        file_content (str): 文件内容
        
    Returns:
        dict: 组件效果映射，键为componentId，值为效果列表
    """
    effects = {}
    
    # 匹配binding.xxx.setOnClickListener {
    #     // 回调逻辑
    # }
    # 使用更复杂的正则表达式来处理嵌套的大括号
    click_pattern = re.compile(r'binding\.(\w+)\.setOnClickListener\s*\{((?:[^{}]|\{[^{}]*\})*)\}')
    matches = click_pattern.finditer(file_content, re.DOTALL)
    
    for match in matches:
        component_id = match.group(1)
        callback_content = match.group(2)
        
        component_effects = []
        
        # 检查是否有finish()或onBackPressed()调用（返回）
        if 'finish()' in callback_content or 'onBackPressed()' in callback_content:
            # 返回操作使用NAVIGATION效果，navigationRole为BACK
            component_effects.append({
                'effectType': 'NAVIGATION',
                'navigationRole': 'BACK'
                # possibleTargetPageIds将由map_validator.py填充
            })
        else:
            # 检查是否有startActivity调用（页面跳转）
            startActivity_pattern = re.compile(r'Intent\(this,\s*(\w+)::class\.java\)')
            startActivity_matches = startActivity_pattern.finditer(callback_content)
            
            has_navigation = False
            
            for startActivity_match in startActivity_matches:
                target_page_id = startActivity_match.group(1)
                
                # 确定navigationRole
                if 'Detail' in target_page_id:
                    navigation_role = 'VIEW_DETAIL'
                elif 'Edit' in target_page_id or 'Form' in target_page_id:
                    navigation_role = 'EDIT_ENTITY'
                else:
                    navigation_role = 'ENTER_FLOW'
                
                effect = {
                    'effectType': 'NAVIGATION',
                    'targetPageId': target_page_id,
                    'navigationRole': navigation_role
                }
                
                # 检查是否有存储操作（SharedPreferences）
                # 或者是否是与预约相关的选择按键（会影响最终存储内容）
                is_appointment_selection = component_id in ['btnSelectDate', 'btnTimeMorning', 'btnTimeAfternoon']
                is_appointment_type_selection = component_id in ['btnNormalClinic', 'btnExpertClinic', 'btnEmergency', 'btnPhysicalExam', 'btnChronicDisease']
                is_department_selection = component_id in ['btnInternalMedicine', 'btnSurgery', 'btnPediatrics', 'btnObstetricsAndGynecology', 'btnOphthalmology', 'btnOtolaryngology', 'btnDentistry', 'btnDermatology', 'btnNeurology', 'btnCardiology']
                if 'getSharedPreferences' in callback_content or 'save' in callback_content.lower() or 'saveAppointmentInfo' in callback_content or is_appointment_selection or is_appointment_type_selection or is_department_selection:
                    effect['sideEffects'] = [{
                        'type': 'STORAGE',
                        'description': '存储信息'
                    }]
                
                component_effects.append(effect)
                has_navigation = True
            
            # 如果没有页面跳转，检查是否有状态改变操作
            if not has_navigation:
                # 检查是否有binding.xxx赋值操作（状态改变）
                if 'binding.' in callback_content and ('=' in callback_content or 'setText' in callback_content):
                    # 尝试提取状态信息
                    # 匹配binding.xxx.xxx = value
                    state_pattern = re.compile(r'binding\.(\w+)\.(\w+)\s*=\s*(\w+)')
                    state_match = state_pattern.search(callback_content)
                    
                    # 状态变化效果，只表达"发生了变化"，不推断具体值
                    if state_match:
                        # 提取组件ID、状态键
                        component_id = state_match.group(1)
                        state_key = state_match.group(2)
                        state_key_full = f'{component_id}_{state_key}'
                    else:
                        # 尝试从组件ID生成语义明确的状态信息
                        state_key_full = f'{component_id}_state'
                    
                    effect = {
                        'effectType': 'STATE_CHANGE',
                        'stateScope': 'PAGE_LOCAL',
                        'stateKey': state_key_full,
                        'stateDelta': 'CHANGED'
                    }
                    
                    # 检查是否有存储操作（SharedPreferences）
                    # 或者是否是与预约相关的选择按键（会影响最终存储内容）
                    is_appointment_selection = component_id in ['btnSelectDate', 'btnTimeMorning', 'btnTimeAfternoon']
                    is_appointment_type_selection = component_id in ['btnNormalClinic', 'btnExpertClinic', 'btnEmergency', 'btnPhysicalExam', 'btnChronicDisease']
                    is_department_selection = component_id in ['btnInternalMedicine', 'btnSurgery', 'btnPediatrics', 'btnObstetricsAndGynecology', 'btnOphthalmology', 'btnOtolaryngology', 'btnDentistry', 'btnDermatology', 'btnNeurology', 'btnCardiology']
                    if 'getSharedPreferences' in callback_content or 'save' in callback_content.lower() or 'saveAppointmentInfo' in callback_content or is_appointment_selection or is_appointment_type_selection or is_department_selection:
                        effect['sideEffects'] = [{
                            'type': 'STORAGE',
                            'description': '存储信息'
                        }]
                    
                    component_effects.append(effect)
                else:
                    # 纯交互操作
                    effect = {
                        'effectType': 'UI_INTERACTION',
                        'interactionRole': 'CONFIRM'
                    }
                    
                    # 检查是否有存储操作（SharedPreferences）
                    # 或者是否是与预约相关的选择按键（会影响最终存储内容）
                    is_appointment_selection = component_id in ['btnSelectDate', 'btnTimeMorning', 'btnTimeAfternoon']
                    is_appointment_type_selection = component_id in ['btnNormalClinic', 'btnExpertClinic', 'btnEmergency', 'btnPhysicalExam', 'btnChronicDisease']
                    is_department_selection = component_id in ['btnInternalMedicine', 'btnSurgery', 'btnPediatrics', 'btnObstetricsAndGynecology', 'btnOphthalmology', 'btnOtolaryngology', 'btnDentistry', 'btnDermatology', 'btnNeurology', 'btnCardiology']
                    if 'getSharedPreferences' in callback_content or 'save' in callback_content.lower() or 'saveAppointmentInfo' in callback_content or is_appointment_selection or is_appointment_type_selection or is_department_selection:
                        effect['sideEffects'] = [{
                            'type': 'STORAGE',
                            'description': '存储信息'
                        }]
                    
                    component_effects.append(effect)
        
        effects[component_id] = component_effects
    
    return effects

def parse_checked_change_effects(file_content):
    """
    解析CheckedChange事件的效果信息
    
    Args:
        file_content (str): 文件内容
        
    Returns:
        dict: 组件效果映射，键为componentId，值为效果列表
    """
    effects = {}
    
    # 匹配binding.xxx.setOnCheckedChangeListener {
    #     // 回调逻辑
    # }
    checked_pattern = re.compile(r'binding\.(\w+)\.setOnCheckedChangeListener\s*\{[^}]*\}')
    matches = checked_pattern.finditer(file_content, re.DOTALL)
    
    for match in matches:
        component_id = match.group(1)
        
        # CheckedChange事件改变内部状态，只表达"发生了变化"
        effects[component_id] = [{
            'effectType': 'STATE_CHANGE',
            'stateScope': 'PAGE_LOCAL',
            'stateKey': f'{component_id}_checked',
            'stateDelta': 'CHANGED'
        }]
    
    return effects

def parse_seekbar_change_effects(file_content):
    """
    解析SeekBarChange事件的效果信息
    
    Args:
        file_content (str): 文件内容
        
    Returns:
        dict: 组件效果映射，键为componentId，值为效果列表
    """
    effects = {}
    
    # 匹配binding.xxx.setOnSeekBarChangeListener {
    #     // 回调逻辑
    # }
    seekbar_pattern = re.compile(r'binding\.(\w+)\.setOnSeekBarChangeListener\s*\{[^}]*\}')
    matches = seekbar_pattern.finditer(file_content, re.DOTALL)
    
    for match in matches:
        component_id = match.group(1)
        
        # SeekBarChange事件改变内部状态，只表达"发生了变化"
        effects[component_id] = [{
            'effectType': 'STATE_CHANGE',
            'stateScope': 'PAGE_LOCAL',
            'stateKey': f'{component_id}_progress',
            'stateDelta': 'CHANGED'
        }]
    
    return effects
