#!/usr/bin/env python3
"""
整合脚本，用于执行完整的FSM构建流程：
1. 生成UI地图（ui_map.json）
2. 生成FSM转换图（fsm_transition.json）
3. 增强FSM转换图（添加action_metadata和visible_text_index）
"""

import os
import sys
import subprocess

# 定义硬编码参数
PROJECT_DIR = "c:/Users/13210/AndroidStudioProjects/GuideSystemTest"
ANDROID_SRC_DIR = "c:/Users/13210/AndroidStudioProjects/GuideSystemTest/app/src/main"
UI_MAP_OUTPUT = "c:/Users/13210/AndroidStudioProjects/GuideSystemTest/ui_map.json"
FSM_TRANSITION_OUTPUT = "c:/Users/13210/AndroidStudioProjects/GuideSystemTest/fsm_transition.json"

def run_command(command, cwd=None):
    """运行命令并返回结果"""
    print(f"执行命令: {' '.join(command)}")
    try:
        result = subprocess.run(
            command,
            cwd=cwd,
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        print(result.stdout)
        if result.stderr:
            print(f"错误输出: {result.stderr}")
        return True
    except subprocess.CalledProcessError as e:
        print(f"命令执行失败: {e}")
        print(f"错误输出: {e.stderr}")
        return False

def main():
    """主函数，执行完整的FSM构建流程"""
    print("=== 开始完整的FSM构建流程 ===")
    
    # 步骤1: 生成UI地图（ui_map.json）
    print("\n=== 步骤1: 生成UI地图 ===")
    map_command = [
        sys.executable,
        "-m", "map",
        "--dir", ANDROID_SRC_DIR,
        "--output", UI_MAP_OUTPUT
    ]
    if not run_command(map_command, cwd=PROJECT_DIR):
        print("步骤1执行失败，终止流程")
        return 1
    
    # 步骤2: 生成FSM转换图（fsm_transition.json）
    print("\n=== 步骤2: 生成FSM转换图 ===")
    fsm_convert_command = [
        sys.executable,
        "-m", "map.fsm.ui_map_to_fsm"
    ]
    if not run_command(fsm_convert_command, cwd=PROJECT_DIR):
        print("步骤2执行失败，终止流程")
        return 1
    
    # 步骤3: 增强FSM转换图
    print("\n=== 步骤3: 增强FSM转换图 ===")
    enhance_command = [
        sys.executable,
        "-m", "map.fsm.enhance_fsm_transition"
    ]
    if not run_command(enhance_command, cwd=PROJECT_DIR):
        print("步骤3执行失败，终止流程")
        return 1
    
    print("\n=== 完整的FSM构建流程执行完成 ===")
    print(f"生成的文件:")
    print(f"- UI地图: {UI_MAP_OUTPUT}")
    print(f"- FSM转换图: {FSM_TRANSITION_OUTPUT}")
    return 0

if __name__ == "__main__":
    exit(main())
