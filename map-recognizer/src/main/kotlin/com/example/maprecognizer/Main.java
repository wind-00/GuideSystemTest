package com.example.maprecognizer;

import com.example.maprecognizer.analyzer.NavigationAnalyzer;
import com.example.maprecognizer.analyzer.UIComponentAnalyzer;
import com.example.maprecognizer.data.AppAutomationMap;
import com.example.maprecognizer.generator.MapGenerator;
import com.example.maprecognizer.serializer.JsonSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 地图生成器主类，用于从命令行生成应用自动化地图
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== 开始生成应用自动化地图 ===");
        
        try {
            // 获取输出路径
            String outputPath = System.getProperty("map.output.file", "app_automation_map_from_module.json");
            String rootDir = System.getProperty("project.root.dir", ".");
            
            System.out.println("输出路径: " + outputPath);
            System.out.println("项目根目录: " + rootDir);
            
            // 创建分析器
            NavigationAnalyzer navigationAnalyzer = new NavigationAnalyzer();
            UIComponentAnalyzer uiComponentAnalyzer = new UIComponentAnalyzer();
            
            // 分析导航信息
            System.out.println("\n1. 分析导航信息...");
            List<com.example.maprecognizer.analyzer.NavigationInfo> navigationInfo = navigationAnalyzer.analyzeNavigationFiles(rootDir);
            System.out.println("   已分析到 " + navigationInfo.size() + " 个页面的导航信息");
            
            // 分析UI组件信息
            System.out.println("\n2. 分析UI组件信息...");
            List<com.example.maprecognizer.analyzer.UIComponentInfo> uiComponents = uiComponentAnalyzer.analyzeUIComponentFiles(rootDir);
            System.out.println("   已分析到 " + uiComponents.size() + " 个UI组件");
            
            // 生成地图
            System.out.println("\n3. 生成应用自动化地图...");
            MapGenerator mapGenerator = new MapGenerator();
            AppAutomationMap appAutomationMap = mapGenerator.generateAppAutomationMap(navigationInfo, uiComponents);
            
            // 使用序列化器保存到文件
            System.out.println("\n4. 序列化地图数据...");
            JsonSerializer serializer = new JsonSerializer();
            String json = serializer.toJson(appAutomationMap);
            
            // 保存到文件
            System.out.println("\n5. 保存地图到文件...");
            File file = new File(outputPath);
            
            // 确保目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
            
            System.out.println("\n✅ 地图已成功生成并保存！");
            System.out.println("📄 输出文件: " + file.getAbsolutePath());
            System.out.println("📊 地图统计:");
            System.out.println("   页面数量: " + appAutomationMap.getUiModel().getPages().size());
            System.out.println("   状态数量: " + appAutomationMap.getStateModel().getStates().size());
            System.out.println("   转换数量: " + appAutomationMap.getStateModel().getTransitions().size());
            System.out.println("   意图数量: " + appAutomationMap.getIntentModel().getIntents().size());
            System.out.println("📄 文件大小: " + file.length() + " 字节");
            
        } catch (IOException e) {
            System.err.println("\n❌ 生成地图失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("\n❌ 发生意外错误: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("\n=== 地图生成完成 ===");
    }
}
