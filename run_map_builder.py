#!/usr/bin/env python3
"""
UI Map Builder 入口脚本
"""

import sys
import os

# 添加当前目录到Python路径，确保能导入map包
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from map import __main__

if __name__ == "__main__":
    sys.exit(__main__.main())
