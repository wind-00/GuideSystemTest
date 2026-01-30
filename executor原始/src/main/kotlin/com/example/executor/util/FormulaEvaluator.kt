package com.example.executor.util

import android.content.res.Resources

/**
 * FormulaEvaluator is a utility class for evaluating mathematical formulas
 * used to calculate UI component positions and sizes
 */
class FormulaEvaluator(private val resources: Resources?) {
    
    /**
     * Evaluates a formula string and returns the result as an integer
     */
    fun evaluate(formula: String, context: Map<String, Any>? = null): Int {
        val processedFormula = processFormula(formula, context)
        return evaluateExpression(processedFormula)
    }
    
    /**
     * Processes the formula by replacing variables with their actual values
     */
    private fun processFormula(formula: String, context: Map<String, Any>?): String {
        var processed = formula
        
        // Add default context values with fallback to reasonable defaults for testing
        val defaultContext = mapOf(
            "screenWidth" to (resources?.displayMetrics?.widthPixels ?: 1080),
            "screenHeight" to (resources?.displayMetrics?.heightPixels ?: 2160)
        )
        
        // Merge with provided context
        val mergedContext = defaultContext + (context ?: emptyMap())
        
        // Replace variables in the formula
        mergedContext.forEach { (key, value) ->
            processed = processed.replace("$key", value.toString())
        }
        
        return processed
    }
    
    /**
     * Evaluates a mathematical expression
     * This is a simplified evaluator that handles basic arithmetic operations
     */
    private fun evaluateExpression(expression: String): Int {
        // Remove whitespace
        val cleanExpr = expression.replace("\\s+", "")
        
        // Simple expression evaluation (basic arithmetic)
        return try {
            // For now, use a simple approach for basic expressions
            // In a production environment, consider using a more robust parser
            val result = evalSimple(cleanExpr)
            result.toInt()
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid formula: $expression", e)
        }
    }
    
    /**
     * Evaluates simple arithmetic expressions
     */
    private fun evalSimple(expr: String): Double {
        // Handle parentheses first
        val exprWithParentheses = expr
        var result = exprWithParentheses
        
        // Find and evaluate expressions in parentheses
        val parenthesisRegex = "\\(([^()]+)\\)".toRegex()
        var match = parenthesisRegex.find(result)
        while (match != null) {
            val innerExpr = match.groupValues[1]
            val innerResult = evalSimple(innerExpr).toString()
            result = result.replaceRange(match.range, innerResult)
            match = parenthesisRegex.find(result)
        }
        
        // Evaluate multiplication and division first
        val mdRegex = "([\\d.]+)\\s*([*/])\\s*([\\d.]+)".toRegex()
        match = mdRegex.find(result)
        while (match != null) {
            val left = match.groupValues[1].toDouble()
            val op = match.groupValues[2]
            val right = match.groupValues[3].toDouble()
            
            val partialResult = when (op) {
                "*" -> left * right
                "/" -> left / right
                else -> throw IllegalArgumentException("Unknown operator: $op")
            }
            
            result = result.replaceRange(match.range, partialResult.toString())
            match = mdRegex.find(result)
        }
        
        // Evaluate addition and subtraction
        val asRegex = "([\\d.]+)\\s*([+-])\\s*([\\d.]+)".toRegex()
        match = asRegex.find(result)
        while (match != null) {
            val left = match.groupValues[1].toDouble()
            val op = match.groupValues[2]
            val right = match.groupValues[3].toDouble()
            
            val partialResult = when (op) {
                "+" -> left + right
                "-" -> left - right
                else -> throw IllegalArgumentException("Unknown operator: $op")
            }
            
            result = result.replaceRange(match.range, partialResult.toString())
            match = asRegex.find(result)
        }
        
        // Return the final result
        return result.toDouble()
    }
    
    /**
     * Evaluates a position formula map and returns the actual Position
     */
    fun evaluatePosition(formulaMap: Map<String, String>?): Position? {
        if (formulaMap == null) return null
        
        val xFormula = formulaMap["x"] ?: return null
        val yFormula = formulaMap["y"] ?: return null
        
        val x = evaluate(xFormula)
        val y = evaluate(yFormula)
        
        return Position(x, y)
    }
    
    /**
     * Evaluates a size formula map and returns the actual Size
     */
    fun evaluateSize(formulaMap: Map<String, String>?): Size? {
        if (formulaMap == null) return null
        
        val widthFormula = formulaMap["width"] ?: return null
        val heightFormula = formulaMap["height"] ?: return null
        
        val width = evaluate(widthFormula)
        val height = evaluate(heightFormula)
        
        return Size(width, height)
    }
    
    /**
     * Data class representing a position
     */
    data class Position(val x: Int, val y: Int)
    
    /**
     * Data class representing a size
     */
    data class Size(val width: Int, val height: Int)
}