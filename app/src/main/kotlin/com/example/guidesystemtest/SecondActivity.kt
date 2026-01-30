package com.example.guidesystemtest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // 设置返回按钮点击事件
        findViewById<android.widget.Button>(R.id.button_navigate_back).setOnClickListener {
            finish()
        }
    }

}
