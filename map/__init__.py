#!/usr/bin/env python3
"""
UI Map Builder - 从Android Kotlin代码中静态生成UI地图JSON
"""

__version__ = "1.0.0"

from map.extractor.page_extractor import extract_pages
from map.extractor.component_extractor import extract_components_to_pages
from map.extractor.effect_extractor import extract_effects_to_components
from map.extractor.map_validator import validate_and_enhance_map
from map.generator.json_generator import generate_ui_map
from map.utils.file_utils import find_kotlin_files

__all__ = [
    "extract_pages",
    "extract_components_to_pages",
    "extract_effects_to_components",
    "validate_and_enhance_map",
    "generate_ui_map",
    "find_kotlin_files",
]
