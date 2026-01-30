package com.example.orchestrator.state

import com.example.executor.state.PageStateProvider

class RuntimeStateProviderImpl(private val pageStateProvider: PageStateProvider) : RuntimeStateProvider {
    override fun getCurrentPageId(): String? {
        return pageStateProvider.getCurrentPageId()
    }
}
