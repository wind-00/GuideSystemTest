package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivitySecond2Binding

class SecondActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivitySecond2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecond2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 点击事件处理
        setupClickListeners()
        
        // 初始化任务统计信息
        updateTaskStats()
    }

    private fun setupClickListeners() {
        // 任务统计卡片 - 查看今日任务
        binding.taskCardStats.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("taskType", "today")
            intent.putExtra("title", "今日任务")
            startActivity(intent)
        }
        
        // 查看已完成任务
        binding.taskBtnCompleted.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("taskType", "completed")
            intent.putExtra("title", "已完成任务")
            startActivity(intent)
        }
        
        // 查看待办任务
        binding.taskBtnPending.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("taskType", "pending")
            intent.putExtra("title", "待办任务")
            startActivity(intent)
        }
        
        // 创建新任务
        binding.taskBtnCreate.setOnClickListener {
            val intent = Intent(this, ThirdActivity3::class.java)
            intent.putExtra("action", "create")
            startActivity(intent)
        }
        
        // 编辑任务
        binding.taskBtnEdit.setOnClickListener {
            val intent = Intent(this, ThirdActivity3::class.java)
            intent.putExtra("action", "edit")
            intent.putExtra("taskId", "123")
            startActivity(intent)
        }
        
        // 删除任务
        binding.taskBtnDelete.setOnClickListener {
            showToast("删除任务功能")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.taskBtnDelete.text = "删除中..."
            binding.taskBtnDelete.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            // 延迟恢复按钮状态
            binding.taskBtnDelete.postDelayed({
                binding.taskBtnDelete.text = "删除任务"
                binding.taskBtnDelete.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            }, 1000)
        }
        
        // 搜索任务
        binding.taskBtnSearch.setOnClickListener {
            showToast("搜索任务功能")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.taskBtnSearch.text = "搜索中..."
            binding.taskBtnSearch.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            // 延迟恢复按钮状态
            binding.taskBtnSearch.postDelayed({
                binding.taskBtnSearch.text = "搜索任务"
                binding.taskBtnSearch.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            }, 1000)
        }
        
        // 筛选条件 - 全部任务
        binding.taskFilterAll.setOnClickListener {
            showToast("查看全部任务")
            updateTaskStats("all")
            // 添加UI状态变化：改变按钮背景色表示选中状态
            updateFilterButtonStates(binding.taskFilterAll)
        }
        
        // 筛选条件 - 工作任务
        binding.taskFilterWork.setOnClickListener {
            showToast("查看工作任务")
            updateTaskStats("work")
            // 添加UI状态变化：改变按钮背景色表示选中状态
            updateFilterButtonStates(binding.taskFilterWork)
        }
        
        // 筛选条件 - 个人任务
        binding.taskFilterPersonal.setOnClickListener {
            showToast("查看个人任务")
            updateTaskStats("personal")
            // 添加UI状态变化：改变按钮背景色表示选中状态
            updateFilterButtonStates(binding.taskFilterPersonal)
        }
        
        // 筛选条件 - 紧急任务
        binding.taskFilterUrgent.setOnClickListener {
            showToast("查看紧急任务")
            updateTaskStats("urgent")
            // 添加UI状态变化：改变按钮背景色表示选中状态
            updateFilterButtonStates(binding.taskFilterUrgent)
        }

        // 返回第一层级
        binding.taskBtnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTaskStats(filter: String = "all") {
        // 根据筛选条件更新任务统计信息
        when (filter) {
            "all" -> {
                binding.taskTxtTodayValue.text = "今日任务: 5"
                binding.taskTxtPendingValue.text = "待办任务: 12"
            }
            "work" -> {
                binding.taskTxtTodayValue.text = "今日任务: 3"
                binding.taskTxtPendingValue.text = "待办任务: 8"
            }
            "personal" -> {
                binding.taskTxtTodayValue.text = "今日任务: 2"
                binding.taskTxtPendingValue.text = "待办任务: 4"
            }
            "urgent" -> {
                binding.taskTxtTodayValue.text = "今日任务: 1"
                binding.taskTxtPendingValue.text = "待办任务: 3"
            }
            else -> {
                binding.taskTxtTodayValue.text = "今日任务: 5"
                binding.taskTxtPendingValue.text = "待办任务: 12"
            }
        }
    }
    
    /**
     * 更新筛选按钮的状态，高亮显示当前选中的筛选条件
     */
    private fun updateFilterButtonStates(selectedButton: View) {
        // 重置所有筛选按钮的背景色
        val defaultColor = resources.getColor(android.R.color.holo_blue_dark)
        val selectedColor = resources.getColor(android.R.color.holo_green_light)
        
        binding.taskFilterAll.setBackgroundColor(defaultColor)
        binding.taskFilterWork.setBackgroundColor(defaultColor)
        binding.taskFilterPersonal.setBackgroundColor(defaultColor)
        binding.taskFilterUrgent.setBackgroundColor(defaultColor)
        
        // 高亮当前选中的按钮
        selectedButton.setBackgroundColor(selectedColor)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}