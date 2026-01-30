import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateMap {
    public static void main(String[] args) {
        System.out.println("=== 开始生成应用自动化地图 ===");
        
        try {
            // 1. 模拟应用元信息
            Map<String, Object> appMeta = new HashMap<>();
            appMeta.put("appName", "GuideSystemTest");
            appMeta.put("packageName", "com.example.guidesystemtest");
            appMeta.put("versionName", "1.0.0");
            appMeta.put("versionCode", 1);
            appMeta.put("uiFramework", "VIEW");
            
            // 2. 模拟UI模型
            Map<String, Object> uiModel = new HashMap<>();
            List<Map<String, Object>> pages = new ArrayList<>();
            
            // 主页面
            Map<String, Object> mainPage = new HashMap<>();
            mainPage.put("pageId", "Main");
            mainPage.put("pageName", "Main");
            mainPage.put("route", "/main");
            mainPage.put("layoutType", "VIEW");
            
            // 主页面组件
            List<Map<String, Object>> mainComponents = new ArrayList<>();
            
            // 普通按钮
            Map<String, Object> normalButton = new HashMap<>();
            normalButton.put("componentId", "btnNormal");
            normalButton.put("viewType", "BUTTON");
            normalButton.put("text", "普通按钮");
            normalButton.put("contentDescription", null);
            normalButton.put("position", Map.of("x", 0, "y", 0));
            normalButton.put("size", Map.of("width", 0, "height", 0));
            normalButton.put("enabled", true);
            normalButton.put("supportedTriggers", List.of("CLICK"));
            mainComponents.add(normalButton);
            
            // 图标按钮
            Map<String, Object> iconButton = new HashMap<>();
            iconButton.put("componentId", "btnIcon");
            iconButton.put("viewType", "ICON_BUTTON");
            iconButton.put("text", null);
            iconButton.put("contentDescription", "图标按钮");
            iconButton.put("position", Map.of("x", 0, "y", 0));
            iconButton.put("size", Map.of("width", 0, "height", 0));
            iconButton.put("enabled", true);
            iconButton.put("supportedTriggers", List.of("CLICK"));
            mainComponents.add(iconButton);
            
            // 进入第二层级1按钮
            Map<String, Object> toSecond1Button = new HashMap<>();
            toSecond1Button.put("componentId", "btnToSecond1");
            toSecond1Button.put("viewType", "BUTTON");
            toSecond1Button.put("text", "进入第二层级1");
            toSecond1Button.put("contentDescription", null);
            toSecond1Button.put("position", Map.of("x", 0, "y", 0));
            toSecond1Button.put("size", Map.of("width", 0, "height", 0));
            toSecond1Button.put("enabled", true);
            toSecond1Button.put("supportedTriggers", List.of("CLICK"));
            mainComponents.add(toSecond1Button);
            
            // 进入第二层级2按钮
            Map<String, Object> toSecond2Button = new HashMap<>();
            toSecond2Button.put("componentId", "btnToSecond2");
            toSecond2Button.put("viewType", "BUTTON");
            toSecond2Button.put("text", "进入第二层级2");
            toSecond2Button.put("contentDescription", null);
            toSecond2Button.put("position", Map.of("x", 0, "y", 0));
            toSecond2Button.put("size", Map.of("width", 0, "height", 0));
            toSecond2Button.put("enabled", true);
            toSecond2Button.put("supportedTriggers", List.of("CLICK"));
            mainComponents.add(toSecond2Button);
            
            mainPage.put("components", mainComponents);
            pages.add(mainPage);
            
            // 第二层级1页面
            Map<String, Object> secondPage = new HashMap<>();
            secondPage.put("pageId", "Second");
            secondPage.put("pageName", "Second");
            secondPage.put("route", "/second");
            secondPage.put("layoutType", "VIEW");
            secondPage.put("components", new ArrayList<>());
            pages.add(secondPage);
            
            // 第二层级2页面
            Map<String, Object> second2Page = new HashMap<>();
            second2Page.put("pageId", "Second2");
            second2Page.put("pageName", "Second2");
            second2Page.put("route", "/second2");
            second2Page.put("layoutType", "VIEW");
            second2Page.put("components", new ArrayList<>());
            pages.add(second2Page);
            
            uiModel.put("pages", pages);
            
            // 3. 模拟状态模型
            Map<String, Object> stateModel = new HashMap<>();
            List<Map<String, Object>> states = new ArrayList<>();
            
            // 主页面状态
            Map<String, Object> mainState = new HashMap<>();
            mainState.put("stateId", "Main");
            mainState.put("name", "Main");
            mainState.put("description", "主页面");
            
            List<Map<String, Object>> mainSignals = new ArrayList<>();
            Map<String, Object> mainSignal = new HashMap<>();
            mainSignal.put("type", "PAGE_ACTIVE");
            mainSignal.put("target", "Main");
            mainSignal.put("expectedValue", null);
            mainSignal.put("matcher", "EQUALS");
            mainSignals.add(mainSignal);
            
            mainState.put("signals", mainSignals);
            mainState.put("relatedPageIds", List.of("Main"));
            states.add(mainState);
            
            // 第二层级1页面状态
            Map<String, Object> secondState = new HashMap<>();
            secondState.put("stateId", "Second");
            secondState.put("name", "Second");
            secondState.put("description", "第二层级1页面");
            
            List<Map<String, Object>> secondSignals = new ArrayList<>();
            Map<String, Object> secondSignal = new HashMap<>();
            secondSignal.put("type", "PAGE_ACTIVE");
            secondSignal.put("target", "Second");
            secondSignal.put("expectedValue", null);
            secondSignal.put("matcher", "EQUALS");
            secondSignals.add(secondSignal);
            
            secondState.put("signals", secondSignals);
            secondState.put("relatedPageIds", List.of("Second"));
            states.add(secondState);
            
            // 第二层级2页面状态
            Map<String, Object> second2State = new HashMap<>();
            second2State.put("stateId", "Second2");
            second2State.put("name", "Second2");
            second2State.put("description", "第二层级2页面");
            
            List<Map<String, Object>> second2Signals = new ArrayList<>();
            Map<String, Object> second2Signal = new HashMap<>();
            second2Signal.put("type", "PAGE_ACTIVE");
            second2Signal.put("target", "Second2");
            second2Signal.put("expectedValue", null);
            second2Signal.put("matcher", "EQUALS");
            second2Signals.add(second2Signal);
            
            second2State.put("signals", second2Signals);
            second2State.put("relatedPageIds", List.of("Second2"));
            states.add(second2State);
            
            stateModel.put("states", states);
            stateModel.put("initialStateId", "Main");
            
            // 4. 模拟意图模型
            Map<String, Object> intentModel = new HashMap<>();
            List<Map<String, Object>> intents = new ArrayList<>();
            
            // 普通按钮点击意图
            Map<String, Object> normalClickIntent = new HashMap<>();
            normalClickIntent.put("intentId", "btnNormal_click");
            normalClickIntent.put("type", "CLICK");
            normalClickIntent.put("description", "普通按钮的点击事件");
            
            List<Map<String, Object>> normalBindings = new ArrayList<>();
            Map<String, Object> normalBinding = new HashMap<>();
            normalBinding.put("componentId", "btnNormal");
            normalBinding.put("trigger", "CLICK");
            normalBinding.put("parameters", new HashMap<>());
            normalBindings.add(normalBinding);
            
            normalClickIntent.put("uiBindings", normalBindings);
            normalClickIntent.put("expectedNextStateIds", new ArrayList<>());
            intents.add(normalClickIntent);
            
            // 图标按钮点击意图
            Map<String, Object> iconClickIntent = new HashMap<>();
            iconClickIntent.put("intentId", "btnIcon_click");
            iconClickIntent.put("type", "CLICK");
            iconClickIntent.put("description", "图标按钮的点击事件");
            
            List<Map<String, Object>> iconBindings = new ArrayList<>();
            Map<String, Object> iconBinding = new HashMap<>();
            iconBinding.put("componentId", "btnIcon");
            iconBinding.put("trigger", "CLICK");
            iconBinding.put("parameters", new HashMap<>());
            iconBindings.add(iconBinding);
            
            iconClickIntent.put("uiBindings", iconBindings);
            iconClickIntent.put("expectedNextStateIds", new ArrayList<>());
            intents.add(iconClickIntent);
            
            // 进入第二层级1意图
            Map<String, Object> toSecond1Intent = new HashMap<>();
            toSecond1Intent.put("intentId", "btnToSecond1_click");
            toSecond1Intent.put("type", "NAVIGATE");
            toSecond1Intent.put("description", "进入第二层级1按钮的点击事件");
            
            List<Map<String, Object>> toSecond1Bindings = new ArrayList<>();
            Map<String, Object> toSecond1Binding = new HashMap<>();
            toSecond1Binding.put("componentId", "btnToSecond1");
            toSecond1Binding.put("trigger", "CLICK");
            toSecond1Binding.put("parameters", new HashMap<>());
            toSecond1Bindings.add(toSecond1Binding);
            
            toSecond1Intent.put("uiBindings", toSecond1Bindings);
            toSecond1Intent.put("expectedNextStateIds", List.of("Second"));
            intents.add(toSecond1Intent);
            
            // 进入第二层级2意图
            Map<String, Object> toSecond2Intent = new HashMap<>();
            toSecond2Intent.put("intentId", "btnToSecond2_click");
            toSecond2Intent.put("type", "NAVIGATE");
            toSecond2Intent.put("description", "进入第二层级2按钮的点击事件");
            
            List<Map<String, Object>> toSecond2Bindings = new ArrayList<>();
            Map<String, Object> toSecond2Binding = new HashMap<>();
            toSecond2Binding.put("componentId", "btnToSecond2");
            toSecond2Binding.put("trigger", "CLICK");
            toSecond2Binding.put("parameters", new HashMap<>());
            toSecond2Bindings.add(toSecond2Binding);
            
            toSecond2Intent.put("uiBindings", toSecond2Bindings);
            toSecond2Intent.put("expectedNextStateIds", List.of("Second2"));
            intents.add(toSecond2Intent);
            
            intentModel.put("intents", intents);
            
            // 5. 构建完整地图
            Map<String, Object> appAutomationMap = new HashMap<>();
            appAutomationMap.put("appMeta", appMeta);
            appAutomationMap.put("uiModel", uiModel);
            appAutomationMap.put("stateModel", stateModel);
            appAutomationMap.put("intentModel", intentModel);
            
            // 6. 序列化地图
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(appAutomationMap);
            
            // 7. 保存到文件
            File outputFile = new File("app_automation_map.json");
            if (outputFile.exists()) {
                outputFile.delete();
                System.out.println("已删除旧的地图文件");
            }
            Files.writeString(outputFile.toPath(), json);
            
            System.out.println("\n✅ 地图已成功生成并保存！");
            System.out.println("📄 输出文件: " + outputFile.getAbsolutePath());
            System.out.println("📊 地图统计:");
            System.out.println("   页面数量: " + pages.size());
            System.out.println("   状态数量: " + states.size());
            System.out.println("   意图数量: " + intents.size());
            System.out.println("📄 文件大小: " + outputFile.length() + " 字节");
            
        } catch (IOException e) {
            System.err.println("\n❌ 生成地图失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("\n=== 地图生成完成 ===");
    }
}