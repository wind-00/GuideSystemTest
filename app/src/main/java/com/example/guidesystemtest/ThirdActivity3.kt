package com.example.guidesystemtest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityThird3Binding

class ThirdActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityThird3Binding
    private var action: String? = null
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThird3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的参数
        action = intent.getStringExtra("action")
        taskId = intent.getStringExtra("taskId")
        
        // 初始化页面
        initTaskPage()
        
        // 点击事件处理
        setupClickListeners()
        
        // 任务属性设置
        setupTaskProperties()
    }

    private fun initTaskPage() {
        // 根据操作类型设置页面标题和功能
        val isCreateMode = action == "create"
        val title = if (isCreateMode) "创建新任务" else "编辑任务 - $taskId"
        
        binding.txtTitle.text = title
        
        // 设置按钮文本
        binding.btnOption1.text = "选择任务类型"
        binding.btnOption2.text = "设置截止日期"
        binding.btnOption3.text = "添加任务成员"
        binding.btnOption4.text = "添加任务标签"
        
        // 设置开关和滑块的初始状态
        binding.switch1.text = "开启任务提醒"
        binding.switch2.text = "设为私密任务"
        binding.switch3.text = "允许子任务"
    }

    private fun setupClickListeners() {
        // 任务操作
        binding.btnOption1.setOnClickListener {
            showToast("任务类型: 开发任务")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption1.text = "已选择类型"
            binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            // 延迟恢复按钮状态
            binding.btnOption1.postDelayed({
                binding.btnOption1.text = "选择任务类型"
                binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            }, 1000)
        }
        binding.btnOption2.setOnClickListener {
            showToast("截止日期: 2026-01-31")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption2.text = "已设置日期"
            binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            // 延迟恢复按钮状态
            binding.btnOption2.postDelayed({
                binding.btnOption2.text = "设置截止日期"
                binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            }, 1000)
        }
        binding.btnOption3.setOnClickListener {
            showToast("任务成员: 张三, 李四")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption3.text = "已添加成员"
            binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            // 延迟恢复按钮状态
            binding.btnOption3.postDelayed({
                binding.btnOption3.text = "添加任务成员"
                binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            }, 1000)
        }
        binding.btnOption4.setOnClickListener {
            showToast("任务标签: 重要, 紧急")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption4.text = "已添加标签"
            binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            // 延迟恢复按钮状态
            binding.btnOption4.postDelayed({
                binding.btnOption4.text = "添加任务标签"
                binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            }, 1000)
        }

        // 保存或更新任务
        val saveAction = if (action == "create") "创建" else "更新"
        binding.btnBack.setOnClickListener {
            showToast("任务${saveAction}成功")
            finish()
        }
    }

    private fun setupTaskProperties() {
        // 任务优先级设置
        binding.slider.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val priority = when {
                    progress < 33 -> "低"
                    progress < 66 -> "中"
                    else -> "高"
                }
                binding.txtSliderValue.text = "任务优先级: $priority ($progress%)"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                showToast("任务优先级已设置")
            }
        })
        
        // 任务属性开关
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            showToast("任务提醒${if (isChecked) "已开启" else "已关闭"}")
        }
        binding.switch2.setOnCheckedChangeListener { _, isChecked ->
            showToast("任务已${if (isChecked) "设为私密" else "设为公开"}")
        }
        binding.switch3.setOnCheckedChangeListener { _, isChecked ->
            showToast("子任务功能${if (isChecked) "已开启" else "已关闭"}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}