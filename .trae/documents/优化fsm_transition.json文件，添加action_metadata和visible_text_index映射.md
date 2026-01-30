1. 读取fsm_transition.json文件，获取现有的page_index、action_index和transition数据
2. 读取ui_map.json文件，构建组件信息映射（componentId → viewType, visibleText, pageId）
3. 遍历action_index，为每个actionId构建action_metadata条目：
   - 解析action key，获取componentId和triggerType
   - 从组件映射中获取viewType、visibleText和pageId
   - 构建包含componentId、triggerType、visibleText、viewType和page的metadata
4. 构建visible_text_index映射：
   - 遍历action_metadata，将visibleText不为空的actionId添加到对应的visibleText条目下
5. 将新构建的action_metadata和visible_text_index添加到fsm_transition.json中
6. 保存修改后的fsm_transition.json文件

所有数据均来源于现有文件，无推断、无置信度、无合并和归一化，完全保持原始数据。