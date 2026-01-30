package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityAppointmentBinding

class AppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 点击事件处理
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 普通门诊
        binding.btnNormalClinic.setOnClickListener {
            val intent = Intent(this, DepartmentActivity::class.java)
            intent.putExtra("appointmentType", "general")
            startActivity(intent)
        }

        // 专家门诊
        binding.btnExpertClinic.setOnClickListener {
            val intent = Intent(this, DepartmentActivity::class.java)
            intent.putExtra("appointmentType", "expert")
            startActivity(intent)
        }

        // 急诊
        binding.btnEmergency.setOnClickListener {
            val intent = Intent(this, DepartmentActivity::class.java)
            intent.putExtra("appointmentType", "emergency")
            startActivity(intent)
        }

        // 体检预约
        binding.btnPhysicalExam.setOnClickListener {
            val intent = Intent(this, DepartmentActivity::class.java)
            intent.putExtra("appointmentType", "physical")
            startActivity(intent)
        }

        // 慢性病复诊
        binding.btnChronicDisease.setOnClickListener {
            val intent = Intent(this, DepartmentActivity::class.java)
            intent.putExtra("appointmentType", "chronic")
            startActivity(intent)
        }

        // 返回上一级
        binding.buttonNavigateBack.setOnClickListener {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}