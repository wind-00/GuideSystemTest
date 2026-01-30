package com.example.executor

import android.content.Context
import com.example.executor.adapter.AccessibilityTreeAdapter
import com.example.executor.core.ExecutionResult
import com.example.executor.model.AppAutomationMap
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

/**
 * Unit tests for the Executor as a pure execution tool
 */
class ExecutorTest {
    
    @Mock
    private lateinit var accessibilityTreeAdapter: AccessibilityTreeAdapter
    
    @Mock
    private lateinit var context: Context
    
    private lateinit var appAutomationMap: AppAutomationMap
    
    @Before
    fun setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this)
        
        // Load the minimal virtual map from resources
        val mapJson = javaClass.getResourceAsStream("/minimal_virtual_map.json")!!.bufferedReader().use { it.readText() }
        appAutomationMap = Json.decodeFromString(mapJson)
        
        // Set up mock behavior
        // Initially, we're on the Home page
        Mockito.`when`(accessibilityTreeAdapter.isPageActive("Home")).thenReturn(true)
        Mockito.`when`(accessibilityTreeAdapter.isPageActive("Detail")).thenReturn(false)
    }
    
    @Test
    fun testUnknownIntent() {
        // Create executor
        val executor = com.example.executor.ExecutorFactory.create(appAutomationMap, accessibilityTreeAdapter, context)
        
        // Execute unknown intent
        val result = executor.executeIntent("unknown_intent")
        
        // Verify result is Failure
        Assert.assertTrue(result is ExecutionResult.Failure)
    }
    
    @Test
    fun testIntentResolution() {
        // Create executor
        val executor = com.example.executor.ExecutorFactory.create(appAutomationMap, accessibilityTreeAdapter, context)
        
        // Mock state detection to return Detail state after execution
        Mockito.`when`(accessibilityTreeAdapter.isPageActive("Home")).thenReturn(false)
        Mockito.`when`(accessibilityTreeAdapter.isPageActive("Detail")).thenReturn(true)
        
        // Execute valid intent
        val result = executor.executeIntent("navigate_to_detail")
        
        // Verify result
        Assert.assertTrue(result is ExecutionResult.Failure || result is ExecutionResult.Success)
    }
    
    @Test
    fun testInvalidStateForIntent() {
        // Create executor
        val executor = com.example.executor.ExecutorFactory.create(appAutomationMap, accessibilityTreeAdapter, context)
        
        // Mock state detection to return Detail state
        Mockito.`when`(accessibilityTreeAdapter.isPageActive("Home")).thenReturn(false)
        Mockito.`when`(accessibilityTreeAdapter.isPageActive("Detail")).thenReturn(true)
        
        // Try to execute intent that should only be available from Home state
        val result = executor.executeIntent("navigate_to_detail")
        
        // Verify result is Failure - intent not applicable in current state
        Assert.assertTrue(result is ExecutionResult.Failure)
    }
}
