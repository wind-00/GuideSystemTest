package com.example.guidesystemtest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityThirdBinding

class ThirdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThirdBinding
    private var taskId: String? = null
    private var taskType: String? = null
    private var taskTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的参数
        taskId = intent.getStringExtra("taskId")
        taskType = intent.getStringExtra("taskType")
        taskTitle = intent.getStringExtra("title")
        
        // 初始化任务详情
        initTaskDetails()
        
        // 点击事件处理
        setupClickListeners()
        
        // 任务操作处理
        setupTaskActions()
    }

    private fun initTaskDetails() {
        // 根据不同的任务类型设置标题和内容
        val title = when (taskType) {
            "today" -> "今日任务列表"
            "completed" -> "已完成任务"
            "pending" -> "待办任务"
            else -> taskTitle ?: "任务详情 - $taskId"
        }
        
        binding.txtTitle.text = title
        
        // 根据任务类型初始化不同的操作按钮
        when (taskType) {
            "today", "completed", "pending" -> {
                // 列表模式下的操作按钮
                binding.btnOption1.text = "创建新任务"
                binding.btnOption2.text = "导出任务列表"
                binding.btnOption3.text = "刷新任务"
                binding.btnOption4.text = "筛选任务"
            }
            else -> {
                // 详情模式下的操作按钮
                binding.btnOption1.text = "查看任务描述"
                binding.btnOption2.text = "查看任务附件"
                binding.btnOption3.text = "查看任务历史"
                binding.btnOption4.text = "查看任务评论"
            }
        }
        
        // 设置开关和滑块的初始状态
        binding.switch1.text = "标记为完成"
        binding.switch2.text = "设为重要任务"
        binding.switch3.text = "开启任务提醒"
    }

    private fun setupClickListeners() {
        // 根据任务类型处理不同的按钮点击事件
        when (taskType) {
            "today", "completed", "pending" -> {
                // 列表模式下的操作
                binding.btnOption1.setOnClickListener {
                    showToast("创建新任务功能")
                    // 添加UI状态变化：改变按钮文本和背景色
                    binding.btnOption1.text = "创建中..."
                    binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                    // 延迟恢复按钮状态
                    binding.btnOption1.postDelayed({
                        binding.btnOption1.text = "创建新任务"
                        binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
                    }, 1000)
                }
                binding.btnOption2.setOnClickListener {
                    showToast("导出任务列表功能")
                    // 添加UI状态变化：改变按钮文本和背景色
                    binding.btnOption2.text = "导出中..."
                    binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                    // 延迟恢复按钮状态
                    binding.btnOption2.postDelayed({
                        binding.btnOption2.text = "导出任务列表"
                        binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
                    }, 1000)
                }
                binding.btnOption3.setOnClickListener {
                    showToast("刷新任务列表")
                    // 添加UI状态变化：改变按钮文本和背景色
                    binding.btnOption3.text = "刷新中..."
                    binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light))
                    // 延迟恢复按钮状态
                    binding.btnOption3.postDelayed({
                        binding.btnOption3.text = "刷新任务"
                        binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark))
                    }, 1000)
                }
                binding.btnOption4.setOnClickListener {
                    showToast("筛选任务功能")
                    // 添加UI状态变化：改变按钮文本和背景色
                    binding.btnOption4.text = "筛选中..."
                    binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                    // 延迟恢复按钮状态
                    binding.btnOption4.postDelayed({
                        binding.btnOption4.text = "筛选任务"
                        binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
                    }, 1000)
                }
            }
            else -> {
                // 详情模式下的操作
                binding.btnOption1.setOnClickListener {
                    showToast("任务描述: 完成地图识别器测试用例开发，包含多种触发方式和层级结构")
                    // 添加UI状态变化：改变按钮文本
                    binding.btnOption1.text = "已查看描述"
                    binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                    // 延迟恢复按钮状态
                    binding.btnOption1.postDelayed({
                        binding.btnOption1.text = "查看任务描述"
                        binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
                    }, 1000)
                }
                binding.btnOption2.setOnClickListener {
                    showToast("任务附件: 设计文档.pdf, 测试用例.xlsx")
                    // 添加UI状态变化：改变按钮文本
                    binding.btnOption2.text = "已查看附件"
                    binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                    // 延迟恢复按钮状态
                    binding.btnOption2.postDelayed({
                        binding.btnOption2.text = "查看任务附件"
                        binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
                    }, 1000)
                }
                binding.btnOption3.setOnClickListener {
                    showToast("任务历史: 2026-01-18 创建任务, 2026-01-19 更新描述")
                    // 添加UI状态变化：改变按钮文本
                    binding.btnOption3.text = "已查看历史"
                    binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_orange_light))
                    // 延迟恢复按钮状态
                    binding.btnOption3.postDelayed({
                        binding.btnOption3.text = "查看任务历史"
                        binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark))
                    }, 1000)
                }
                binding.btnOption4.setOnClickListener {
                    showToast("任务评论: 无评论")
                    // 添加UI状态变化：改变按钮文本
                    binding.btnOption4.text = "已查看评论"
                    binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                    // 延迟恢复按钮状态
                    binding.btnOption4.postDelayed({
                        binding.btnOption4.text = "查看任务评论"
                        binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
                    }, 1000)
                }
            }
        }

        // 返回第二层级
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupTaskActions() {
        // 任务状态切换
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            showToast("任务${if (isChecked) "已完成" else "重新设为待办"}")
        }
        // 重要性切换
        binding.switch2.setOnCheckedChangeListener { _, isChecked ->
            showToast("任务${if (isChecked) "已设为重要" else "已取消重要标记"}")
        }
        // 提醒设置
        binding.switch3.setOnCheckedChangeListener { _, isChecked ->
            showToast("任务提醒${if (isChecked) "已开启" else "已关闭"}")
        }
        
        // 进度滑块
        binding.slider.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtSliderValue.text = "任务进度: $progress%"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                showToast("任务进度已更新为 ${seekBar?.progress}%")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}