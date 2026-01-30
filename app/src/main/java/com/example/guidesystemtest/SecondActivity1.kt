package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivitySecondBinding

class SecondActivity1 : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 点击事件处理
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 预约挂号
        binding.btnAppointment.setOnClickListener {
            val intent = Intent(this, AppointmentActivity::class.java)
            startActivity(intent)
        }

        // 查看挂号
        binding.btnViewAppointment.setOnClickListener {
            val intent = Intent(this, ViewAppointmentActivity::class.java)
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