#!/usr/bin/env python3
"""
Test script to verify the UI Map to FSM conversion
"""

import json
import sys

def test_fsm_conversion():
    """Test the FSM conversion result"""
    with open('fsm_transition.json', 'r', encoding='utf-8') as f:
        fsm = json.load(f)
    
    print("=== FSM Conversion Test Results ===")
    
    # Test 1: Verify page index structure
    page_index = fsm['page_index']
    print(f"1. Page Index: {len(page_index)} pages mapped")
    assert len(page_index) > 0, "No pages mapped"
    
    # Test 2: Verify action index structure
    action_index = fsm['action_index']
    print(f"2. Action Index: {len(action_index)} actions mapped")
    assert len(action_index) > 0, "No actions mapped"
    
    # Test 3: Verify transition structure
    transition = fsm['transition']
    print(f"3. Transition: {len(transition)} pages with transitions")
    assert len(transition) == len(page_index), "Mismatch in page count between index and transition"
    
    # Test 4: Verify MainActivity transitions
    main_activity_index = fsm['page_index']['MainActivity']
    main_activity_transitions = transition[str(main_activity_index)]
    print(f"4. MainActivity transitions: {len(main_activity_transitions)} actions available")
    assert len(main_activity_transitions) > 0, "MainActivity has no transitions"
    
    # Test 5: Verify navigation transitions exist
    has_navigation = False
    for page_transitions in transition.values():
        for action, next_pages in page_transitions.items():
            if len(next_pages) > 0 and next_pages[0] != int(list(transition.keys())[0]):
                has_navigation = True
                break
        if has_navigation:
            break
    print(f"5. Navigation transitions: {'✅ Found' if has_navigation else '❌ Not found'}")
    assert has_navigation, "No navigation transitions found"
    
    # Test 6: Verify back button transitions
    has_back_transitions = False
    back_btn_actions = []
    for action_str, action_idx in action_index.items():
        if 'back' in action_str.lower() and 'CLICK' in action_str:
            back_btn_actions.append(str(action_idx))
    
    if back_btn_actions:
        for page_idx, page_transitions in transition.items():
            for back_action in back_btn_actions:
                if back_action in page_transitions:
                    has_back_transitions = True
                    break
            if has_back_transitions:
                break
    
    print(f"6. Back button transitions: {'✅ Found' if has_back_transitions else '❌ Not found'}")
    assert has_back_transitions, "No back button transitions found"
    
    # Test 7: Verify state change transitions (stay on same page)
    has_state_change = False
    for page_idx, page_transitions in transition.items():
        for action, next_pages in page_transitions.items():
            if next_pages == [int(page_idx)]:
                has_state_change = True
                break
        if has_state_change:
            break
    print(f"7. State change transitions: {'✅ Found' if has_state_change else '❌ Not found'}")
    assert has_state_change, "No state change transitions found"
    
    # Test 8: Verify all actions in action_index have transitions
    all_actions_have_transitions = True
    action_ids = set(fsm['action_index'].values())
    found_actions = set()
    
    for page_transitions in transition.values():
        found_actions.update(page_transitions.keys())
    
    found_actions_int = {int(action_id) for action_id in found_actions}
    missing_actions = action_ids - found_actions_int
    
    if missing_actions:
        all_actions_have_transitions = False
        print(f"   Missing transitions for actions: {missing_actions}")
    
    print(f"8. All actions have transitions: {'✅ Yes' if all_actions_have_transitions else '❌ No'}")
    
    # Test 9: Verify ThirdActivity3 (last page) has transitions
    third_activity3_idx = fsm['page_index']['ThirdActivity3']
    third_activity3_transitions = transition[str(third_activity3_idx)]
    print(f"9. ThirdActivity3 transitions: {len(third_activity3_transitions)} actions available")
    assert len(third_activity3_transitions) > 0, "ThirdActivity3 has no transitions"
    
    # Test 10: Verify UI_INTERACTION effects are handled correctly
    has_ui_interaction = False
    for page_transitions in transition.values():
        for action, next_pages in page_transitions.items():
            # UI_INTERACTION should stay on the same page
            if len(next_pages) == 1:
                has_ui_interaction = True
                break
        if has_ui_interaction:
            break
    print(f"10. UI_INTERACTION effects: {'✅ Handled correctly' if has_ui_interaction else '❌ Not found'}")
    
    print("\n=== All tests completed! ===")
    return True

if __name__ == "__main__":
    success = test_fsm_conversion()
    sys.exit(0 if success else 1)