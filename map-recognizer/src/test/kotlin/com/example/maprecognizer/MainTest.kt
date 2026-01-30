package com.example.maprecognizer

import org.junit.Test

class MainTest {
    
    @Test
    fun testMain() {
        // 设置系统属性
        System.setProperty("map.output.file", "app_automation_map_from_test.json")
        System.setProperty("project.root.dir", ".")
        
        // 调用 main 函数
        main(emptyArray())
    }
}