package com.example.maprecognizer.serializer

import com.example.maprecognizer.data.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type
import java.util.*

/**
 * JSON序列化器，负责将AppAutomationMap转换为JSON格式
 */
class JsonSerializer {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create()
    
    /**
     * 将AppAutomationMap转换为JSON字符串
     * @param map 应用自动化地图
     * @return JSON字符串
     */
    fun toJson(map: AppAutomationMap): String {
        return gson.toJson(map)
    }
    
    /**
     * 将JSON字符串转换为AppAutomationMap
     * @param json JSON字符串
     * @return 应用自动化地图
     */
    fun fromJson(json: String): AppAutomationMap {
        return gson.fromJson(json, AppAutomationMap::class.java)
    }
    
    /**
     * 将AppAutomationMap转换为缩进的JSON字符串，方便阅读
     * @param map 应用自动化地图
     * @return 缩进的JSON字符串
     */
    fun toPrettyJson(map: AppAutomationMap): String {
        return gson.toJson(map)
    }
}
