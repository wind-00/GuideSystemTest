package com.example.guidesystemtest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityMainBinding
import com.example.orchestrator.control.Orchestrator
import com.example.orchestrator.control.OrchestratorImpl
import com.example.orchestrator.executor.ExecutorClient
import com.example.orchestrator.executor.ExecutorClientImpl
import com.example.orchestrator.overlay.OverlayManager
import com.example.orchestrator.planner.PlannerClient
import com.example.orchestrator.planner.PlannerClientImpl
import com.example.orchestrator.state.RuntimeStateProvider
import com.example.orchestrator.state.RuntimeStateProviderImpl
import com.example.executor.core.Executor
import com.example.executor.core.ExecutionEngine
import com.example.executor.action.AtomicActionExecutor
import com.example.executor.action.ViewAtomicActionExecutor
import com.example.executor.state.PageStateProvider
import com.example.executor.state.ActivityPageStateProvider
import com.example.executor.util.UiStabilizer
import com.example.executor.util.LooperUiStabilizer
import com.example.planner.Planner
import com.example.planner.createPlannerFromJson
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var orchestrator: Orchestrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 检查并请求悬浮窗权限
        checkOverlayPermission()
        
        // 初始化orchestrator服务
        initOrchestrator()

        // 点击事件处理
        setupClickListeners()
        
        // 滑动事件处理
        setupSeekBarListeners()
        
        // 长按事件处理
        setupLongClickListeners()
    }

    private fun initOrchestrator() {
        // 初始化各组件
        val pageStateProvider: ActivityPageStateProvider = ActivityPageStateProvider(application)
        // 手动设置当前Activity，确保能立即获取到页面ID
        pageStateProvider.setCurrentActivity(this)
        val runtimeStateProvider: RuntimeStateProvider = RuntimeStateProviderImpl(pageStateProvider)
        
        // 从fsm_transition.json文件读取UI地图配置
        val uiMapJson = try {
            Log.d("MainActivity", "=== Reading fsm_transition.json file ===")
            
            // 优先从assets目录读取fsm_transition.json文件
            try {
                Log.d("MainActivity", "Trying to read from assets directory")
                val inputStream = assets.open("fsm_transition.json")
                val content = inputStream.bufferedReader().use { it.readText() }
                Log.d("MainActivity", "✓ Successfully read fsm_transition.json from assets, size: ${content.length} characters")
                inputStream.close()
                content
            } catch (assetError: Exception) {
                Log.w("MainActivity", "✗ Failed to read from assets: ${assetError.message}")
                
                // 尝试使用绝对路径直接读取fsm_transition.json文件
                val filePath = "c:\\Users\\13210\\AndroidStudioProjects\\GuideSystemTest\\fsm_transition.json"
                val file = File(filePath)
                Log.d("MainActivity", "Trying to read from absolute path: ${file.absolutePath}")
                if (file.exists()) {
                    Log.d("MainActivity", "✓ Found fsm_transition.json file: ${file.absolutePath}")
                    val content = file.readText()
                    Log.d("MainActivity", "✓ Successfully read fsm_transition.json file, size: ${content.length} characters")
                    content
                } else {
                    Log.e("MainActivity", "✗ fsm_transition.json file not found at absolute path: ${file.absolutePath}")
                    // 尝试从项目根目录读取fsm_transition.json文件
                    val rootFile = File("fsm_transition.json")
                    Log.d("MainActivity", "Trying to read from project root: ${rootFile.absolutePath}")
                    if (rootFile.exists()) {
                        Log.d("MainActivity", "✓ Found fsm_transition.json file: ${rootFile.absolutePath}")
                        val content = rootFile.readText()
                        Log.d("MainActivity", "✓ Successfully read fsm_transition.json file, size: ${content.length} characters")
                        content
                    } else {
                        Log.w("MainActivity", "✗ fsm_transition.json file not found at project root")
                        // 使用默认配置作为fallback
                        Log.w("MainActivity", "⚠ Using fallback configuration")
                        "{\"page_index\":{\"ExecutorTestActivity\":0,\"ExecutorTestFragment\":1,\"MainActivity\":2,\"SecondActivity\":3,\"SecondActivity2\":4,\"ThirdActivity\":5,\"ThirdActivity2\":6,\"ThirdActivity3\":7},\"action_index\":{\"auto_back_btn\":0,\"btnBack\":1,\"btnIcon\":2,\"btnLongClick\":3,\"btnNormal\":4,\"btnOption1\":5,\"btnOption2\":6,\"btnOption3\":7,\"btnOption4\":8,\"btnToSecond1\":9,\"btnToSecond2\":10,\"checkbox1\":11,\"checkbox2\":12,\"longClickArea\":13,\"radioGroup\":14,\"seekBarHorizontal\":15,\"seekBarVertical\":16,\"slider\":17,\"switch1\":18,\"switch2\":19,\"switch3\":20,\"switchButton\":21,\"taskBtnBack\":22,\"taskBtnCompleted\":23,\"taskBtnCreate\":24,\"taskBtnDelete\":25,\"taskBtnEdit\":26,\"taskBtnPending\":27,\"taskBtnSearch\":28,\"taskCardStats\":29,\"taskFilterAll\":30,\"taskFilterPersonal\":31,\"taskFilterUrgent\":32,\"taskFilterWork\":33},\"action_metadata\":{0:{\"componentId\":\"auto_back_btn\",\"triggerType\":\"CLICK\",\"visibleText\":\"\",\"viewType\":\"BUTTON\",\"page\":\"SecondActivity\"},1:{\"componentId\":\"btnBack\",\"triggerType\":\"CLICK\",\"visibleText\":\"返回第二层级\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},2:{\"componentId\":\"btnIcon\",\"triggerType\":\"CLICK\",\"visibleText\":\"图标按钮\",\"viewType\":\"ICON_BUTTON\",\"page\":\"MainActivity\"},3:{\"componentId\":\"btnLongClick\",\"triggerType\":\"LONG_CLICK\",\"visibleText\":\"长按按钮\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},4:{\"componentId\":\"btnNormal\",\"triggerType\":\"CLICK\",\"visibleText\":\"普通按钮\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},5:{\"componentId\":\"btnOption1\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项1\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},6:{\"componentId\":\"btnOption2\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项2\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},7:{\"componentId\":\"btnOption3\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项3\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},8:{\"componentId\":\"btnOption4\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项4\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},9:{\"componentId\":\"btnToSecond1\",\"triggerType\":\"CLICK\",\"visibleText\":\"医院就医\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},10:{\"componentId\":\"btnToSecond2\",\"triggerType\":\"CLICK\",\"visibleText\":\"跳转到第二层级2\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},11:{\"componentId\":\"checkbox1\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"复选1\",\"viewType\":\"CHECKBOX\",\"page\":\"MainActivity\"},12:{\"componentId\":\"checkbox2\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"复选2\",\"viewType\":\"CHECKBOX\",\"page\":\"MainActivity\"},13:{\"componentId\":\"longClickArea\",\"triggerType\":\"LONG_CLICK\",\"visibleText\":\"长按区域\",\"viewType\":\"VIEW\",\"page\":\"MainActivity\"},14:{\"componentId\":\"radioGroup\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"\",\"viewType\":\"RADIO_GROUP\",\"page\":\"MainActivity\"},15:{\"componentId\":\"seekBarHorizontal\",\"triggerType\":\"PROGRESS_CHANGE\",\"visibleText\":\"\",\"viewType\":\"SEEKBAR\",\"page\":\"MainActivity\"},16:{\"componentId\":\"seekBarVertical\",\"triggerType\":\"PROGRESS_CHANGE\",\"visibleText\":\"\",\"viewType\":\"SEEKBAR\",\"page\":\"MainActivity\"},17:{\"componentId\":\"slider\",\"triggerType\":\"PROGRESS_CHANGE\",\"visibleText\":\"\",\"viewType\":\"SEEKBAR\",\"page\":\"ThirdActivity3\"},18:{\"componentId\":\"switch1\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"第三层级3开关1\",\"viewType\":\"SWITCH\",\"page\":\"ThirdActivity3\"},19:{\"componentId\":\"switch2\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"第三层级3开关2\",\"viewType\":\"SWITCH\",\"page\":\"ThirdActivity3\"},20:{\"componentId\":\"switch3\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"第三层级3开关3\",\"viewType\":\"SWITCH\",\"page\":\"ThirdActivity3\"},21:{\"componentId\":\"switchButton\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"开关按钮\",\"viewType\":\"SWITCH\",\"page\":\"MainActivity\"},22:{\"componentId\":\"taskBtnBack\",\"triggerType\":\"CLICK\",\"visibleText\":\"返回第一层级\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},23:{\"componentId\":\"taskBtnCompleted\",\"triggerType\":\"CLICK\",\"visibleText\":\"查看已完成任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},24:{\"componentId\":\"taskBtnCreate\",\"triggerType\":\"CLICK\",\"visibleText\":\"创建新任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},25:{\"componentId\":\"taskBtnDelete\",\"triggerType\":\"CLICK\",\"visibleText\":\"删除任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},26:{\"componentId\":\"taskBtnEdit\",\"triggerType\":\"CLICK\",\"visibleText\":\"编辑任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},27:{\"componentId\":\"taskBtnPending\",\"triggerType\":\"CLICK\",\"visibleText\":\"查看待办任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},28:{\"componentId\":\"taskBtnSearch\",\"triggerType\":\"CLICK\",\"visibleText\":\"搜索任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},29:{\"componentId\":\"taskCardStats\",\"triggerType\":\"CLICK\",\"visibleText\":\"\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},30:{\"componentId\":\"taskFilterAll\",\"triggerType\":\"CLICK\",\"visibleText\":\"全部\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},31:{\"componentId\":\"taskFilterPersonal\",\"triggerType\":\"CLICK\",\"visibleText\":\"个人\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},32:{\"componentId\":\"taskFilterUrgent\",\"triggerType\":\"CLICK\",\"visibleText\":\"紧急\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},33:{\"componentId\":\"taskFilterWork\",\"triggerType\":\"CLICK\",\"visibleText\":\"工作\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"}},\"transition\":{\"2\":{\"9\":[6],\"10\":[7],\"2\":[2],\"3\":[2],\"4\":[2],\"11\":[2],\"12\":[2],\"13\":[2],\"14\":[2],\"15\":[2],\"16\":[2],\"17\":[2],\"18\":[2],\"19\":[2],\"21\":[2]},\"6\":{\"5\":[10],\"23\":[10],\"24\":[10],\"25\":[10],\"26\":[10],\"18\":[10],\"19\":[10],\"20\":[10],\"17\":[10]},\"7\":{\"22\":[2],\"24\":[13],\"23\":[10],\"26\":[13],\"25\":[13],\"29\":[10],\"30\":[7],\"31\":[7],\"32\":[7],\"33\":[7]},\"10\":{\"1\":[7],\"5\":[10],\"23\":[10],\"24\":[10],\"25\":[10],\"26\":[10],\"18\":[10],\"19\":[10],\"20\":[10],\"17\":[10]},\"13\":{\"1\":[7],\"5\":[13],\"23\":[13],\"24\":[13],\"25\":[13],\"26\":[13],\"18\":[13],\"19\":[13],\"20\":[13],\"17\":[13]}},\"visible_text_index\":{\"返回第二层级\":[1],\"图标按钮\":[2],\"长按按钮\":[3],\"普通按钮\":[4],\"第三层级3选项1\":[5],\"第三层级3选项2\":[6],\"第三层级3选项3\":[7],\"第三层级3选项4\":[8],\"医院就医\":[9],\"跳转到第二层级2\":[10],\"复选1\":[11],\"复选2\":[12],\"长按区域\":[13],\"第三层级3开关1\":[18],\"第三层级3开关2\":[19],\"第三层级3开关3\":[20],\"开关按钮\":[21],\"返回第一层级\":[22],\"查看已完成任务\":[23],\"创建新任务\":[24],\"删除任务\":[25],\"编辑任务\":[26],\"查看待办任务\":[27],\"搜索任务\":[28],\"全部\":[30],\"个人\":[31],\"紧急\":[32],\"工作\":[33]}}"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "✗ Error reading fsm_transition.json: ${e.message}", e)
            // 使用默认配置作为fallback
            Log.w("MainActivity", "⚠ Using fallback configuration due to error")
            "{\"page_index\":{\"ExecutorTestActivity\":0,\"ExecutorTestFragment\":1,\"MainActivity\":2,\"SecondActivity\":3,\"SecondActivity2\":4,\"ThirdActivity\":5,\"ThirdActivity2\":6,\"ThirdActivity3\":7},\"action_index\":{\"auto_back_btn\":0,\"btnBack\":1,\"btnIcon\":2,\"btnLongClick\":3,\"btnNormal\":4,\"btnOption1\":5,\"btnOption2\":6,\"btnOption3\":7,\"btnOption4\":8,\"btnToSecond1\":9,\"btnToSecond2\":10,\"checkbox1\":11,\"checkbox2\":12,\"longClickArea\":13,\"radioGroup\":14,\"seekBarHorizontal\":15,\"seekBarVertical\":16,\"slider\":17,\"switch1\":18,\"switch2\":19,\"switch3\":20,\"switchButton\":21,\"taskBtnBack\":22,\"taskBtnCompleted\":23,\"taskBtnCreate\":24,\"taskBtnDelete\":25,\"taskBtnEdit\":26,\"taskBtnPending\":27,\"taskBtnSearch\":28,\"taskCardStats\":29,\"taskFilterAll\":30,\"taskFilterPersonal\":31,\"taskFilterUrgent\":32,\"taskFilterWork\":33},\"action_metadata\":{0:{\"componentId\":\"auto_back_btn\",\"triggerType\":\"CLICK\",\"visibleText\":\"\",\"viewType\":\"BUTTON\",\"page\":\"SecondActivity\"},1:{\"componentId\":\"btnBack\",\"triggerType\":\"CLICK\",\"visibleText\":\"返回第二层级\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},2:{\"componentId\":\"btnIcon\",\"triggerType\":\"CLICK\",\"visibleText\":\"图标按钮\",\"viewType\":\"ICON_BUTTON\",\"page\":\"MainActivity\"},3:{\"componentId\":\"btnLongClick\",\"triggerType\":\"LONG_CLICK\",\"visibleText\":\"长按按钮\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},4:{\"componentId\":\"btnNormal\",\"triggerType\":\"CLICK\",\"visibleText\":\"普通按钮\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},5:{\"componentId\":\"btnOption1\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项1\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},6:{\"componentId\":\"btnOption2\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项2\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},7:{\"componentId\":\"btnOption3\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项3\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},8:{\"componentId\":\"btnOption4\",\"triggerType\":\"CLICK\",\"visibleText\":\"第三层级3选项4\",\"viewType\":\"BUTTON\",\"page\":\"ThirdActivity3\"},9:{\"componentId\":\"btnToSecond1\",\"triggerType\":\"CLICK\",\"visibleText\":\"医院就医\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},10:{\"componentId\":\"btnToSecond2\",\"triggerType\":\"CLICK\",\"visibleText\":\"跳转到第二层级2\",\"viewType\":\"BUTTON\",\"page\":\"MainActivity\"},11:{\"componentId\":\"checkbox1\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"复选1\",\"viewType\":\"CHECKBOX\",\"page\":\"MainActivity\"},12:{\"componentId\":\"checkbox2\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"复选2\",\"viewType\":\"CHECKBOX\",\"page\":\"MainActivity\"},13:{\"componentId\":\"longClickArea\",\"triggerType\":\"LONG_CLICK\",\"visibleText\":\"长按区域\",\"viewType\":\"VIEW\",\"page\":\"MainActivity\"},14:{\"componentId\":\"radioGroup\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"\",\"viewType\":\"RADIO_GROUP\",\"page\":\"MainActivity\"},15:{\"componentId\":\"seekBarHorizontal\",\"triggerType\":\"PROGRESS_CHANGE\",\"visibleText\":\"\",\"viewType\":\"SEEKBAR\",\"page\":\"MainActivity\"},16:{\"componentId\":\"seekBarVertical\",\"triggerType\":\"PROGRESS_CHANGE\",\"visibleText\":\"\",\"viewType\":\"SEEKBAR\",\"page\":\"MainActivity\"},17:{\"componentId\":\"slider\",\"triggerType\":\"PROGRESS_CHANGE\",\"visibleText\":\"\",\"viewType\":\"SEEKBAR\",\"page\":\"ThirdActivity3\"},18:{\"componentId\":\"switch1\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"第三层级3开关1\",\"viewType\":\"SWITCH\",\"page\":\"ThirdActivity3\"},19:{\"componentId\":\"switch2\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"第三层级3开关2\",\"viewType\":\"SWITCH\",\"page\":\"ThirdActivity3\"},20:{\"componentId\":\"switch3\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"第三层级3开关3\",\"viewType\":\"SWITCH\",\"page\":\"ThirdActivity3\"},21:{\"componentId\":\"switchButton\",\"triggerType\":\"CHECKED_CHANGE\",\"visibleText\":\"开关按钮\",\"viewType\":\"SWITCH\",\"page\":\"MainActivity\"},22:{\"componentId\":\"taskBtnBack\",\"triggerType\":\"CLICK\",\"visibleText\":\"返回第一层级\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},23:{\"componentId\":\"taskBtnCompleted\",\"triggerType\":\"CLICK\",\"visibleText\":\"查看已完成任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},24:{\"componentId\":\"taskBtnCreate\",\"triggerType\":\"CLICK\",\"visibleText\":\"创建新任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},25:{\"componentId\":\"taskBtnDelete\",\"triggerType\":\"CLICK\",\"visibleText\":\"删除任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},26:{\"componentId\":\"taskBtnEdit\",\"triggerType\":\"CLICK\",\"visibleText\":\"编辑任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},27:{\"componentId\":\"taskBtnPending\",\"triggerType\":\"CLICK\",\"visibleText\":\"查看待办任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},28:{\"componentId\":\"taskBtnSearch\",\"triggerType\":\"CLICK\",\"visibleText\":\"搜索任务\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},29:{\"componentId\":\"taskCardStats\",\"triggerType\":\"CLICK\",\"visibleText\":\"\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},30:{\"componentId\":\"taskFilterAll\",\"triggerType\":\"CLICK\",\"visibleText\":\"全部\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},31:{\"componentId\":\"taskFilterPersonal\",\"triggerType\":\"CLICK\",\"visibleText\":\"个人\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},32:{\"componentId\":\"taskFilterUrgent\",\"triggerType\":\"CLICK\",\"visibleText\":\"紧急\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"},33:{\"componentId\":\"taskFilterWork\",\"triggerType\":\"CLICK\",\"visibleText\":\"工作\",\"viewType\":\"VIEW\",\"page\":\"SecondActivity2\"}},\"transition\":{\"2\":{\"9\":[6],\"10\":[7],\"2\":[2],\"3\":[2],\"4\":[2],\"11\":[2],\"12\":[2],\"13\":[2],\"14\":[2],\"15\":[2],\"16\":[2],\"17\":[2],\"18\":[2],\"19\":[2],\"21\":[2]},\"6\":{\"5\":[10],\"23\":[10],\"24\":[10],\"25\":[10],\"26\":[10],\"18\":[10],\"19\":[10],\"20\":[10],\"17\":[10]},\"7\":{\"22\":[2],\"24\":[13],\"23\":[10],\"26\":[13],\"25\":[13],\"29\":[10],\"30\":[7],\"31\":[7],\"32\":[7],\"33\":[7]},\"10\":{\"1\":[7],\"5\":[10],\"23\":[10],\"24\":[10],\"25\":[10],\"26\":[10],\"18\":[10],\"19\":[10],\"20\":[10],\"17\":[10]},\"13\":{\"1\":[7],\"5\":[13],\"23\":[13],\"24\":[13],\"25\":[13],\"26\":[13],\"18\":[13],\"19\":[13],\"20\":[13],\"17\":[13]}},\"visible_text_index\":{\"返回第二层级\":[1],\"图标按钮\":[2],\"长按按钮\":[3],\"普通按钮\":[4],\"第三层级3选项1\":[5],\"第三层级3选项2\":[6],\"第三层级3选项3\":[7],\"第三层级3选项4\":[8],\"医院就医\":[9],\"跳转到第二层级2\":[10],\"复选1\":[11],\"复选2\":[12],\"长按区域\":[13],\"第三层级3开关1\":[18],\"第三层级3开关2\":[19],\"第三层级3开关3\":[20],\"开关按钮\":[21],\"返回第一层级\":[22],\"查看已完成任务\":[23],\"创建新任务\":[24],\"删除任务\":[25],\"编辑任务\":[26],\"查看待办任务\":[27],\"搜索任务\":[28],\"全部\":[30],\"个人\":[31],\"紧急\":[32],\"工作\":[33]}}"
        }
        
        val planner: Planner = createPlannerFromJson(uiMapJson)
        val plannerClient: PlannerClient = PlannerClientImpl(planner)
        // 设置上下文，用于从assets目录读取fsm_transition.json文件
        plannerClient.setContext(this)
        
        // 初始化Executor
        val atomicActionExecutor: AtomicActionExecutor = ViewAtomicActionExecutor(pageStateProvider, this)
        val uiStabilizer: UiStabilizer = LooperUiStabilizer()
        val executor: Executor = ExecutionEngine(pageStateProvider, atomicActionExecutor, uiStabilizer)
        val executorClient: ExecutorClient = ExecutorClientImpl(executor)
        
        // 初始化悬浮窗管理器
        val overlayManager = OverlayManager(this)
        overlayManager.setRuntimeStateProvider(runtimeStateProvider)
        // 设置ActivityPageStateProvider并注册监听器
        overlayManager.setActivityPageStateProvider(pageStateProvider)
        // 设置执行器，用于特殊对话结束后执行路径
        overlayManager.setExecutor(executor)
        
        // 初始化orchestrator
        orchestrator = OrchestratorImpl(
            this,
            runtimeStateProvider,
            plannerClient,
            executorClient,
            overlayManager
        )
        
        // 显示悬浮窗
        try {
            val overlaySuccess = overlayManager.showOverlay()
            if (!overlaySuccess) {
                showToast("悬浮窗初始化失败，请检查权限设置")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("悬浮窗初始化失败: ${e.message}")
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, 1001)
            } else {
                // 权限已授予，悬浮窗会在服务启动时自动显示
                showToast("悬浮窗权限已授予")
            }
        } else {
            // Android 6.0以下不需要权限
            showToast("悬浮窗权限已授予")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // 权限已授予，悬浮窗会在服务启动时自动显示
                    showToast("悬浮窗权限已授予")
                } else {
                    showToast("需要悬浮窗权限才能使用AI导航功能")
                }
            }
        }
    }

    private fun setupClickListeners() {
        // 普通按钮
        binding.btnNormal.setOnClickListener {
            showToast("普通按钮点击")
            // 添加UI状态变化：改变按钮文本
            binding.btnNormal.text = "已点击普通按钮"
            // 延迟恢复按钮文本，展示可观察效果
            binding.btnNormal.postDelayed({
                binding.btnNormal.text = "普通按钮"
            }, 1000)
        }

        // 图标按钮
        binding.btnIcon.setOnClickListener {
            showToast("图标按钮点击")
            // 添加UI状态变化：改变按钮文本
            binding.btnIcon.text = "已点击图标按钮"
            // 延迟恢复按钮文本，展示可观察效果
            binding.btnIcon.postDelayed({
                binding.btnIcon.text = "图标按钮"
            }, 1000)
        }

        // 开关按钮
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            showToast("开关状态: $isChecked")
        }

        // 单选按钮
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val text = when (checkedId) {
                binding.radio1.id -> "单选1选中"
                binding.radio2.id -> "单选2选中"
                binding.radio3.id -> "单选3选中"
                else -> "未知选中"
            }
            showToast(text)
        }

        // 复选按钮
        binding.checkbox1.setOnCheckedChangeListener { _, isChecked ->
            showToast("复选1状态: $isChecked")
        }
        binding.checkbox2.setOnCheckedChangeListener { _, isChecked ->
            showToast("复选2状态: $isChecked")
        }

        // 进入第二层级1 - 医院就医
        binding.btnToSecond1.setOnClickListener {
            val intent = Intent(this, SecondActivity1::class.java)
            startActivity(intent)
        }
        
        // 进入第二层级2
        binding.btnToSecond2.setOnClickListener {
            val intent = Intent(this, SecondActivity2::class.java)
            startActivity(intent)
        }
    }

    private fun setupSeekBarListeners() {
        // 水平滑块
        binding.seekBarHorizontal.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtSeekBarValue.text = "水平滑块值: $progress"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        // 垂直滑块
        binding.seekBarVertical.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtVerticalSeekBarValue.text = "垂直滑块值: $progress"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun setupLongClickListeners() {
        // 长按按钮
        binding.btnLongClick.setOnLongClickListener {
            showToast("长按按钮触发")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnLongClick.text = "长按已触发"
            binding.btnLongClick.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            // 延迟恢复按钮状态
            binding.btnLongClick.postDelayed({
                binding.btnLongClick.text = "长按按钮"
                binding.btnLongClick.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            }, 1000)
            true
        }

        // 长按区域
        binding.longClickArea.setOnLongClickListener {
            showToast("长按区域触发")
            // 添加UI状态变化：改变长按区域背景色
            binding.longClickArea.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            // 延迟恢复区域背景色
            binding.longClickArea.postDelayed({
                binding.longClickArea.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            }, 1000)
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}