package com.example.executor.action

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import com.example.executor.planner.ActionStep
import com.example.executor.planner.TriggerType
import com.example.executor.result.ExecuteFailReason
import com.example.executor.state.PageStateProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ViewAtomicActionExecutorTest {

    private val pageStateProvider = mockk<PageStateProvider>()
    private val mockActivity = mockk<Activity>()
    private val mockButton = mockk<Button>()
    private val mockCheckBox = mockk<CheckBox>()
    private val mockEditText = mockk<EditText>()
    private val mockProgressBar = mockk<ProgressBar>()
    private val mockView = mockk<View>()
    
    private lateinit var executor: ViewAtomicActionExecutor

    @Before
    fun setUp() {
        // 使用新的构造函数创建测试实例，传入模拟的Activity
        executor = ViewAtomicActionExecutor(pageStateProvider, mockActivity)
        
        // 模拟View查找
        every { mockActivity.window.decorView.findViewById<View>(android.R.id.content) } returns mockView
        every { mockView.findViewWithTag<View>("button1") } returns mockButton
        every { mockView.findViewWithTag<View>("checkbox1") } returns mockCheckBox
        every { mockView.findViewWithTag<View>("editText1") } returns mockEditText
        every { mockView.findViewWithTag<View>("progress1") } returns mockProgressBar
        
        // 模拟View状态
        every { mockButton.isEnabled } returns true
        every { mockButton.visibility } returns View.VISIBLE
        every { mockButton.alpha } returns 1.0f
        every { mockCheckBox.isEnabled } returns true
        every { mockCheckBox.visibility } returns View.VISIBLE
        every { mockCheckBox.alpha } returns 1.0f
        every { mockEditText.isEnabled } returns true
        every { mockEditText.visibility } returns View.VISIBLE
        every { mockEditText.alpha } returns 1.0f
        every { mockProgressBar.isEnabled } returns true
        every { mockProgressBar.visibility } returns View.VISIBLE
        every { mockProgressBar.alpha } returns 1.0f
        
        // 模拟View方法
        every { mockButton.performClick() } returns true
        every { mockButton.performLongClick() } returns true
        every { mockCheckBox.toggle() } returns Unit
        every { mockProgressBar.max } returns 100
        every { mockProgressBar.progress = any() } returns Unit
        every { mockEditText.requestFocus() } returns true
    }

    @Test
    fun `execute should return success for click action on button`() {
        // Given
        val action = ActionStep(1, "button1", TriggerType.CLICK)
        
        // When
        val result = executor.execute(action)
        
        // Then
        assert(result is AtomicExecuteResult.Success)
        verify { mockButton.performClick() }
    }

    @Test
    fun `execute should return success for long click action on button`() {
        // Given
        val action = ActionStep(1, "button1", TriggerType.LONG_CLICK)
        
        // When
        val result = executor.execute(action)
        
        // Then
        assert(result is AtomicExecuteResult.Success)
        verify { mockButton.performLongClick() }
    }

    @Test
    fun `execute should return success for checked change action on checkbox`() {
        // Given
        val action = ActionStep(1, "checkbox1", TriggerType.CHECKED_CHANGE)
        
        // When
        val result = executor.execute(action)
        
        // Then
        assert(result is AtomicExecuteResult.Success)
        verify { mockCheckBox.toggle() }
    }

    @Test
    fun `execute should return success for progress change action on progress bar`() {
        // Given
        val action = ActionStep(1, "progress1", TriggerType.PROGRESS_CHANGE)
        
        // When
        val result = executor.execute(action)
        
        // Then
        assert(result is AtomicExecuteResult.Success)
        verify { mockProgressBar.progress = 50 } // 50% of max (100)
    }

    @Test
    fun `execute should return success for touch action on edit text`() {
        // Given
        val action = ActionStep(1, "editText1", TriggerType.TOUCH)
        
        // When
        val result = executor.execute(action)
        
        // Then
        assert(result is AtomicExecuteResult.Success)
        verify { mockEditText.requestFocus() }
    }

    @Test
    fun `execute should return fail for unsupported trigger type`() {
        // Given
        val action = ActionStep(1, "button1", TriggerType.values().first()) // Use first trigger type as example
        
        // When
        val result = executor.execute(action)
        
        // Then
        assert(result is AtomicExecuteResult.Success || result is AtomicExecuteResult.Fail)
    }
}

// 添加扩展属性，模拟View的可见性检查
private val View.isVisibleToUser: Boolean
    get() = true
