package com.example.guidesystemtest

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityViewAppointmentBinding

class ViewAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 加载预约信息
        loadAppointmentInfo()

        // 点击事件处理
        setupClickListeners()
    }

    private fun loadAppointmentInfo() {
        val sharedPreferences = getSharedPreferences("appointment", MODE_PRIVATE)
        val appointmentIds = sharedPreferences.getStringSet("appointmentIds", mutableSetOf())
        
        if (appointmentIds.isNullOrEmpty()) {
            // 显示无预约信息
            binding.appointmentInfoContainer.visibility = View.GONE
            binding.textNoAppointment.visibility = View.VISIBLE
            binding.btnCancelAppointment.visibility = View.GONE
            return
        }

        // 显示预约信息
        binding.appointmentInfoContainer.visibility = View.VISIBLE
        binding.textNoAppointment.visibility = View.GONE
        binding.btnCancelAppointment.visibility = View.VISIBLE

        // 获取第一个预约信息显示
        val appointmentId = appointmentIds.first()
        val type = sharedPreferences.getString("$appointmentId.type", "")
        val department = sharedPreferences.getString("$appointmentId.department", "")
        val doctor = sharedPreferences.getString("$appointmentId.doctor", "")
        val doctorTitle = sharedPreferences.getString("$appointmentId.doctorTitle", "")
        val date = sharedPreferences.getString("$appointmentId.date", "")
        val time = sharedPreferences.getString("$appointmentId.time", "")

        // 设置预约信息
        binding.textClinicType.text = "挂号类型：${getAppointmentTypeText(type)}"
        binding.textDepartment.text = "科室：${getDepartmentText(department)}"
        binding.textDoctor.text = "医生：$doctor ($doctorTitle)"
        binding.textDate.text = "日期：$date"
        binding.textTime.text = "时间段：$time"
    }

    private fun getAppointmentTypeText(type: String?): String {
        return when (type) {
            "general" -> "普通门诊"
            "expert" -> "专家门诊"
            "emergency" -> "急诊"
            "physical" -> "体检预约"
            "chronic" -> "慢性病复诊"
            else -> "未知类型"
        }
    }

    private fun getDepartmentText(department: String?): String {
        return when (department) {
            "internal" -> "内科"
            "surgery" -> "外科"
            "pediatric" -> "儿科"
            "obstetrics" -> "妇产科"
            "ophthalmology" -> "眼科"
            "ent" -> "耳鼻喉科"
            "dental" -> "口腔科"
            "dermatology" -> "皮肤科"
            "neurology" -> "神经内科"
            "cardiology" -> "心血管内科"
            else -> "未知科室"
        }
    }

    private fun setupClickListeners() {
        // 取消挂号
        binding.btnCancelAppointment.setOnClickListener {
            val sharedPreferences = getSharedPreferences("appointment", MODE_PRIVATE)
            val appointmentIds = sharedPreferences.getStringSet("appointmentIds", mutableSetOf())
            
            if (!appointmentIds.isNullOrEmpty()) {
                val appointmentId = appointmentIds.first()
                val editor = sharedPreferences.edit()
                editor.remove("$appointmentId.type")
                editor.remove("$appointmentId.department")
                editor.remove("$appointmentId.doctor")
                editor.remove("$appointmentId.doctorTitle")
                editor.remove("$appointmentId.date")
                editor.remove("$appointmentId.time")
                editor.remove("$appointmentId.status")
                editor.remove("$appointmentId.timestamp")
                
                // 更新预约ID列表
                val updatedIds = appointmentIds.toMutableSet()
                updatedIds.remove(appointmentId)
                editor.putStringSet("appointmentIds", updatedIds)
                editor.apply()
                
                showToast("已取消挂号")
                loadAppointmentInfo()
            }
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