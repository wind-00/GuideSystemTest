package com.example.executor.core

import com.example.executor.planner.ActionPath
import com.example.executor.result.ExecuteResult

interface Executor {
    fun execute(actionPath: ActionPath): ExecuteResult
}