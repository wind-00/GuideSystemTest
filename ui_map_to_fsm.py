#!/usr/bin/env python3
"""
UI Map to FSM Converter

Converts the UI map JSON into a state transition tensor structure:
- page_index: maps pageId to integer index
- action_index: maps (componentId, triggerType) to integer index
- transition: transition[page_idx][action_idx] → Set<page_index>
"""

import json

class UIMapToFSM:
    def __init__(self, ui_map_json_path):
        self.ui_map_path = ui_map_json_path
        self.page_index = {}
        self.action_index = {}
        self.transition = {}
        self._load_ui_map()
    
    def _load_ui_map(self):
        """Load the UI map from JSON file"""
        with open(self.ui_map_path, 'r', encoding='utf-8') as f:
            self.ui_map = json.load(f)
    
    def build_page_index(self):
        """Build page index mapping: pageId → integer index"""
        page_ids = [page['pageId'] for page in self.ui_map['pages']]
        # Sort pageIds alphabetically for consistency
        page_ids.sort()
        self.page_index = {page_id: idx for idx, page_id in enumerate(page_ids)}
    
    def build_action_index(self):
        """Build action index mapping: (componentId, triggerType) → integer index"""
        actions = set()
        
        for page in self.ui_map['pages']:
            for component in page['components']:
                component_id = component['componentId']
                for trigger in component['triggers']:
                    trigger_type = trigger['triggerType']
                    actions.add((component_id, trigger_type))
        
        # Sort actions for consistency
        actions = sorted(actions)
        self.action_index = {action: idx for idx, action in enumerate(actions)}
    
    def build_transition(self):
        """Build the state transition dictionary"""
        # Initialize transition dictionary
        self.transition = {page_idx: {} for page_idx in self.page_index.values()}
        
        for page in self.ui_map['pages']:
            page_id = page['pageId']
            p = self.page_index[page_id]
            
            for component in page['components']:
                component_id = component['componentId']
                
                for trigger in component['triggers']:
                    trigger_type = trigger['triggerType']
                    action = (component_id, trigger_type)
                    a = self.action_index[action]
                    
                    effect = trigger['effect']
                    next_pages = set()
                    
                    if effect['effectType'] == 'NAVIGATION':
                        navigation_role = effect.get('navigationRole', '')
                        
                        if navigation_role == 'BACK':
                            # BACK navigation - use possibleTargetPageIds
                            possible_targets = effect.get('possibleTargetPageIds', [])
                            for target in possible_targets:
                                if target in self.page_index:
                                    next_pages.add(self.page_index[target])
                        else:
                            # Forward navigation - use targetPageId
                            target_page_id = effect.get('targetPageId')
                            if target_page_id and target_page_id in self.page_index:
                                next_pages.add(self.page_index[target_page_id])
                    elif effect['effectType'] == 'STATE_CHANGE':
                        # STATE_CHANGE - stays on the same page
                        next_pages.add(p)
                    elif effect['effectType'] == 'UI_INTERACTION':
                        # UI_INTERACTION - stays on the same page
                        next_pages.add(p)
                    
                    # Add to transition dictionary
                    if a not in self.transition[p]:
                        self.transition[p][a] = set()
                    self.transition[p][a].update(next_pages)
    
    def convert(self):
        """Run the full conversion process"""
        self.build_page_index()
        self.build_action_index()
        self.build_transition()
        return {
            'page_index': self.page_index,
            'action_index': {
                f"({component_id}, {trigger_type})": idx 
                for (component_id, trigger_type), idx in self.action_index.items()
            },
            'transition': {
                str(p): {
                    str(a): list(next_pages)
                    for a, next_pages in page_transitions.items()
                }
                for p, page_transitions in self.transition.items()
            }
        }
    
    def save(self, output_path='fsm_transition.json'):
        """Save the conversion result to a JSON file"""
        result = self.convert()
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(result, f, indent=2, ensure_ascii=False)
        print(f"FSM transition saved to {output_path}")

if __name__ == "__main__":
    # Example usage
    converter = UIMapToFSM('ui_map.json')
    converter.save()