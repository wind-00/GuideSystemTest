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
            showToast("暂无挂号信息")
            return
        }

        // 显示预约信息
        binding.appointmentInfoContainer.visibility = View.VISIBLE
        binding.textNoAppointment.visibility = View.GONE
        binding.btnCancelAppointment.visibility = View.VISIBLE

        // 获取最新的预约信息显示
        val appointmentId = appointmentIds.maxByOrNull { it.toLong() } ?: appointmentIds.first()
        val type = sharedPreferences.getString("$appointmentId.type", "")
        val department = sharedPreferences.getString("$appointmentId.department", "")
        val doctor = sharedPreferences.getString("$appointmentId.doctor", "")
        val doctorTitle = sharedPreferences.getString("$appointmentId.doctorTitle", "")
        val date = sharedPreferences.getString("$appointmentId.date", "")
        val time = sharedPreferences.getString("$appointmentId.time", "")

        // 调试信息
        showToast("加载挂号信息: $type, $department, $doctor, $date, $time")

        // 设置预约信息
        val clinicTypeText = "挂号类型：${getAppointmentTypeText(type)}"
        val departmentText = "科室：${getDepartmentText(department)}"
        val doctorText = "医生：$doctor ($doctorTitle)"
        val dateText = "日期：$date"
        val timeText = "时间段：$time"

        // 确保文本不为空
        binding.textClinicType.text = clinicTypeText
        binding.textDepartment.text = departmentText
        binding.textDoctor.text = doctorText
        binding.textDate.text = dateText
        binding.textTime.text = timeText

        // 强制设置文本颜色为黑色，确保在任何背景下都能看清
        binding.textClinicType.setTextColor(resources.getColor(android.R.color.black))
        binding.textDepartment.setTextColor(resources.getColor(android.R.color.black))
        binding.textDoctor.setTextColor(resources.getColor(android.R.color.black))
        binding.textDate.setTextColor(resources.getColor(android.R.color.black))
        binding.textTime.setTextColor(resources.getColor(android.R.color.black))
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