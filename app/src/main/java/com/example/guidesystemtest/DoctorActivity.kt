package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityDoctorBinding

class DoctorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorBinding
    private var appointmentType: String? = null
    private var department: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的参数
        appointmentType = intent.getStringExtra("appointmentType")
        department = intent.getStringExtra("department")

        // 点击事件处理
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 医生A（主任医师）
        binding.btnDoctorA.setOnClickListener {
            val intent = Intent(this, ConfirmActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", department)
            intent.putExtra("doctor", "DoctorA")
            intent.putExtra("doctorTitle", "主任医师")
            startActivity(intent)
        }

        // 医生B（副主任医师）
        binding.btnDoctorB.setOnClickListener {
            val intent = Intent(this, ConfirmActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", department)
            intent.putExtra("doctor", "DoctorB")
            intent.putExtra("doctorTitle", "副主任医师")
            startActivity(intent)
        }

        // 医生C（主治医师）
        binding.btnDoctorC.setOnClickListener {
            val intent = Intent(this, ConfirmActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", department)
            intent.putExtra("doctor", "DoctorC")
            intent.putExtra("doctorTitle", "主治医师")
            startActivity(intent)
        }

        // 医生D（住院医师）
        binding.btnDoctorD.setOnClickListener {
            val intent = Intent(this, ConfirmActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", department)
            intent.putExtra("doctor", "DoctorD")
            intent.putExtra("doctorTitle", "住院医师")
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