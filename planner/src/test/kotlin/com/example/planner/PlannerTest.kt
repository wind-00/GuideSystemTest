package com.example.planner

import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlannerTest {
    // 简单的测试地图JSON
    private val testMapJson = """
    {
        "page_index": {
            "MainActivity": 0,
            "SecondActivity": 1,
            "ThirdActivity": 2
        },
        "action_index": {
            "(btnToSecond, CLICK)": 0,
            "(btnToThird, CLICK)": 1,
            "(btnBack, CLICK)": 2,
            "(btnTarget, CLICK)": 3
        },
        "action_metadata": {
            "0": {
                "page": "MainActivity",
                "componentId": "btnToSecond",
                "triggerType": "CLICK",
                "visibleText": "Go to Second",
                "viewType": "BUTTON"
            },
            "1": {
                "page": "SecondActivity",
                "componentId": "btnToThird",
                "triggerType": "CLICK",
                "visibleText": "Go to Third",
                "viewType": "BUTTON"
            },
            "2": {
                "page": "SecondActivity",
                "componentId": "btnBack",
                "triggerType": "CLICK",
                "visibleText": "Back",
                "viewType": "BUTTON"
            },
            "3": {
                "page": "ThirdActivity",
                "componentId": "btnTarget",
                "triggerType": "CLICK",
                "visibleText": "Target Button",
                "viewType": "BUTTON"
            }
        },
        "visible_text_index": {
            "Go to Second": [0],
            "Go to Third": [1],
            "Back": [2],
            "Target Button": [3]
        },
        "transition": {
            "0": {
                "0": [1]
            },
            "1": {
                "1": [2],
                "2": [0]
            },
            "2": {
                "3": [2]
            }
        }
    }
    """

    private val uiMap = Json.decodeFromString<UiMapModel>(testMapJson)
    private val planner = Planner(uiMap)

    @Test
    fun `test BFS search finds path`() {
        val userGoal = UserGoal(
            targetVisibleText = "Target Button",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.BFS
        )

        val result = planner.plan(userGoal)

        assertTrue(result.success)
        assertEquals(listOf(0, 1, 3), result.actionPath)
    }

    @Test
    fun `test DFS search finds path`() {
        val userGoal = UserGoal(
            targetVisibleText = "Target Button",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.DFS
        )

        val result = planner.plan(userGoal)

        assertTrue(result.success)
        assertEquals(listOf(0, 1, 3), result.actionPath)
    }

    @Test
    fun `test invalid target visible text returns failure`() {
        val userGoal = UserGoal(
            targetVisibleText = "NonExistentText",
            startPage = "MainActivity",
            searchStrategy = SearchStrategy.BFS
        )

        val result = planner.plan(userGoal)

        assertFalse(result.success)
        assertEquals(emptyList(), result.actionPath)
        assertEquals(Planner.REASON_NO_TARGET_ACTION, result.reason)
    }

    @Test
    fun `test invalid start page returns failure`() {
        val userGoal = UserGoal(
            targetVisibleText = "Target Button",
            startPage = "NonExistentActivity",
            searchStrategy = SearchStrategy.BFS
        )

        val result = planner.plan(userGoal)

        assertFalse(result.success)
        assertEquals(emptyList(), result.actionPath)
        assertEquals(Planner.REASON_INVALID_START_PAGE, result.reason)
    }

    @Test
    fun `test no path found returns failure`() {
        // 创建一个无法到达目标的测试地图
        val noPathMapJson = """
        {
            "page_index": {
                "Page1": 0,
                "Page2": 1
            },
            "action_index": {
                "(btnNoop, CLICK)": 0,
                "(btnTarget, CLICK)": 1
            },
            "action_metadata": {
                "0": {
                    "page": "Page1",
                    "componentId": "btnNoop",
                    "triggerType": "CLICK",
                    "visibleText": "Noop",
                    "viewType": "BUTTON"
                },
                "1": {
                    "page": "Page2",
                    "componentId": "btnTarget",
                    "triggerType": "CLICK",
                    "visibleText": "Target",
                    "viewType": "BUTTON"
                }
            },
            "visible_text_index": {
                "Noop": [0],
                "Target": [1]
            },
            "transition": {
                "0": {
                    "0": [0]
                },
                "1": {
                    "1": [1]
                }
            }
        }
        """

        val noPathUiMap = Json.decodeFromString<UiMapModel>(noPathMapJson)
        val noPathPlanner = Planner(noPathUiMap)

        val userGoal = UserGoal(
            targetVisibleText = "Target",
            startPage = "Page1",
            searchStrategy = SearchStrategy.BFS
        )

        val result = noPathPlanner.plan(userGoal)

        assertFalse(result.success)
        assertEquals(emptyList(), result.actionPath)
        assertEquals(Planner.REASON_NO_PATH_FOUND, result.reason)
    }
}