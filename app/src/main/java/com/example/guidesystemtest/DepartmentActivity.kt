package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityDepartmentBinding

class DepartmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepartmentBinding
    private var appointmentType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取传递的参数
        appointmentType = intent.getStringExtra("appointmentType")

        // 点击事件处理
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // 内科
        binding.btnInternalMedicine.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "internal")
            startActivity(intent)
        }

        // 外科
        binding.btnSurgery.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "surgery")
            startActivity(intent)
        }

        // 儿科
        binding.btnPediatrics.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "pediatric")
            startActivity(intent)
        }

        // 妇产科
        binding.btnObstetricsAndGynecology.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "obstetrics")
            startActivity(intent)
        }

        // 眼科
        binding.btnOphthalmology.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "ophthalmology")
            startActivity(intent)
        }

        // 耳鼻喉科
        binding.btnOtolaryngology.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "ent")
            startActivity(intent)
        }

        // 口腔科
        binding.btnDentistry.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "dental")
            startActivity(intent)
        }

        // 皮肤科
        binding.btnDermatology.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "dermatology")
            startActivity(intent)
        }

        // 神经内科
        binding.btnNeurology.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "neurology")
            startActivity(intent)
        }

        // 心血管内科
        binding.btnCardiology.setOnClickListener {
            val intent = Intent(this, DoctorActivity::class.java)
            intent.putExtra("appointmentType", appointmentType)
            intent.putExtra("department", "cardiology")
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