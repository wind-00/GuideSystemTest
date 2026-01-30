package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityConfirmBinding

class ConfirmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmBinding
    private var appointmentType: String? = null
    private var department: String? = null
    private var doctor: String? = null
    private var doctorTitle: String? = null
    private var selectedDate: String? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的参数
        appointmentType = intent.getStringExtra("appointmentType")
        department = intent.getStringExtra("department")
        doctor = intent.getStringExtra("doctor")
        doctorTitle = intent.getStringExtra("doctorTitle")

        // 点击事件处理
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 选择日期
        binding.btnSelectDate.setOnClickListener {
            selectedDate = "2026-02-01"
            binding.btnSelectDate.text = "已选择: $selectedDate"
            showToast("选择日期: $selectedDate")
        }

        // 选择时间段 - 上午
        binding.btnTimeMorning.setOnClickListener {
            selectedTime = "上午"
            updateTimeButtonStates(binding.btnTimeMorning)
            showToast("选择时间段: 上午")
        }

        // 选择时间段 - 下午
        binding.btnTimeAfternoon.setOnClickListener {
            selectedTime = "下午"
            updateTimeButtonStates(binding.btnTimeAfternoon)
            showToast("选择时间段: 下午")
        }

        // 确认预约
        binding.btnConfirmAppointment.setOnClickListener {
            if (selectedDate.isNullOrEmpty() || selectedTime.isNullOrEmpty()) {
                showToast("请选择日期和时间段")
                return@setOnClickListener
            }
            
            // 保存预约信息到SharedPreferences
            saveAppointmentInfo()
            
            showToast("预约成功！")
            
            // 跳转到查看挂号界面
            val intent = Intent(this, ViewAppointmentActivity::class.java)
            startActivity(intent)
        }

        // 取消操作
        binding.btnCancelOperation.setOnClickListener {
            showToast("取消预约")
            finish()
        }

        // 返回上一级
        binding.buttonNavigateBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTimeButtonStates(selectedButton: android.view.View) {
        // 重置所有时间段按钮的背景色
        val defaultColor = resources.getColor(android.R.color.holo_blue_dark)
        val selectedColor = resources.getColor(android.R.color.holo_green_light)
        
        binding.btnTimeMorning.setBackgroundColor(defaultColor)
        binding.btnTimeAfternoon.setBackgroundColor(defaultColor)
        
        // 高亮当前选中的按钮
        selectedButton.setBackgroundColor(selectedColor)
    }

    private fun saveAppointmentInfo() {
        val sharedPreferences = getSharedPreferences("appointment", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        
        val appointmentId = System.currentTimeMillis().toString()
        editor.putString("$appointmentId.type", appointmentType)
        editor.putString("$appointmentId.department", department)
        editor.putString("$appointmentId.doctor", doctor)
        editor.putString("$appointmentId.doctorTitle", doctorTitle)
        editor.putString("$appointmentId.date", selectedDate)
        editor.putString("$appointmentId.time", selectedTime)
        editor.putString("$appointmentId.status", "confirmed")
        editor.putLong("$appointmentId.timestamp", System.currentTimeMillis())
        
        // 保存预约ID列表
        val appointmentIds = sharedPreferences.getStringSet("appointmentIds", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        appointmentIds.add(appointmentId)
        editor.putStringSet("appointmentIds", appointmentIds)
        
        editor.apply()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}