package com.example.pathplanner.resolver

import com.example.pathplanner.models.TargetSpec
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * GLM目标解析器测试
 */
class GLMTargetResolverTest {
    
    private lateinit var glmTargetResolver: GLMTargetResolver
    
    @Before
    fun setup() {
        // 从用户提供的API密钥创建GLM目标解析器
        val apiKey = "17fb3e69b36f471ab107c7605797f8bf.wMPUTygxdEPkBQi3"
        glmTargetResolver = GLMTargetResolverFactory.create(apiKey)
    }
    
    /**
     * 测试GLM API目标解析
     * 注意：这个测试会实际调用GLM API，需要网络连接
     */
    @Test
    fun testGLMResolve() {
        runBlocking {
            // 测试"打开设置"意图
            val settingsTarget: TargetSpec = glmTargetResolver.resolve("打开设置")
            println("设置目标：$settingsTarget")
            
            // 测试"搜索"意图
            val searchTarget: TargetSpec = glmTargetResolver.resolve("搜索")
            println("搜索目标：$searchTarget")
            
            // 测试"返回主页"意图
            val homeTarget: TargetSpec = glmTargetResolver.resolve("返回主页")
            println("主页目标：$homeTarget")
            
            // 所有测试都应该成功返回TargetSpec，不会抛出异常
        }
    }
}