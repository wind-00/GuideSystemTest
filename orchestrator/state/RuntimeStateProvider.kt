package com.example.orchestrator.state

interface RuntimeStateProvider {
    fun getCurrentPageId(): String?
}
