根据新的要求，我将对地图生成器进行以下改进：

1. **增强StateModel信号强度**：
   - 将PAGE_ACTIVE信号作为补充，添加更强的COMPONENT_VISIBLE和TEXT_VISIBLE信号
   - 为Main状态添加基于组件ID和文本的强信号
   - 为Second和Second2状态添加可验证的信号

2. **优化信号生成策略**：
   - 优先使用组件可见性和文本可见性等强信号
   - 确保每个状态至少有一个可信信号
   - 避免纯语义描述，使用可运行期验证的具体信号

3. **保持IntentModel简洁性**：
   - 验证所有intent的componentId都来自uiModel
   - 确保trigger在supportedTriggers中
   - 保持expectedNextStateIds的合理性

4. **生成规则调整**：
   - 遵循信号优先级：COMPONENT_VISIBLE > VIEW_ID_EXISTS > TEXT_VISIBLE > CONTENT_DESC_VISIBLE > PAGE_ACTIVE
   - 确保地图信息密度足够高，有区分度，可运行期验证
   - 不凭空编造数据，基于现有UI组件信息生成

5. **输出完整的JSON地图**：
   - 直接修改app_automation_map_from_script.json文件
   - 确保符合所有语义规则
   - 不包含解释性文字

改进后的地图将更适合Executor在运行期进行盲目执行与验证，信息密度更高，可验证性更强，同时避免语义坍塌。