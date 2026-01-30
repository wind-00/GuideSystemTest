package com.example.guidesystemtest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.guidesystemtest.databinding.ActivityThird2Binding

class ThirdActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityThird2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThird2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 点击事件处理
        setupClickListeners()
        
        // 滑动事件处理
        setupSlideListeners()
        
        // 开关事件处理
        setupSwitchListeners()
    }

    private fun setupClickListeners() {
        // 操作按钮
        binding.btnOption1.setOnClickListener {
            showToast("第三层级2选项1点击")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption1.text = "选项1已点击"
            binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            // 延迟恢复按钮状态
            binding.btnOption1.postDelayed({
                binding.btnOption1.text = "第三层级2选项1"
                binding.btnOption1.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            }, 1000)
        }
        binding.btnOption2.setOnClickListener {
            showToast("第三层级2选项2点击")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption2.text = "选项2已点击"
            binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            // 延迟恢复按钮状态
            binding.btnOption2.postDelayed({
                binding.btnOption2.text = "第三层级2选项2"
                binding.btnOption2.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            }, 1000)
        }
        binding.btnOption3.setOnClickListener {
            showToast("第三层级2选项3点击")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption3.text = "选项3已点击"
            binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            // 延迟恢复按钮状态
            binding.btnOption3.postDelayed({
                binding.btnOption3.text = "第三层级2选项3"
                binding.btnOption3.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            }, 1000)
        }
        binding.btnOption4.setOnClickListener {
            showToast("第三层级2选项4点击")
            // 添加UI状态变化：改变按钮文本和背景色
            binding.btnOption4.text = "选项4已点击"
            binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            // 延迟恢复按钮状态
            binding.btnOption4.postDelayed({
                binding.btnOption4.text = "第三层级2选项4"
                binding.btnOption4.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            }, 1000)
        }

        // 返回第二层级
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSlideListeners() {
        // 滑动条
        binding.slider.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.txtSliderValue.text = "滑动值: $progress"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun setupSwitchListeners() {
        // 开关
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            showToast("第三层级2开关1状态: $isChecked")
        }
        binding.switch2.setOnCheckedChangeListener { _, isChecked ->
            showToast("第三层级2开关2状态: $isChecked")
        }
        binding.switch3.setOnCheckedChangeListener { _, isChecked ->
            showToast("第三层级2开关3状态: $isChecked")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}