package com.example.orchestrator.control

import com.example.orchestrator.model.OrchestratorStatus

interface Orchestrator {
    fun startExecution(inputText: String)
    fun stopExecution()
    fun getCurrentStatus(): OrchestratorStatus
}
