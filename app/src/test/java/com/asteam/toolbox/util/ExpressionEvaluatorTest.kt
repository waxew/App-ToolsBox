package com.asteam.toolbox.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionEvaluatorTest {
    @Test fun basicArithmeticHonorsPrecedence() {
        assertEquals(14.0, ExpressionEvaluator.evaluate("2+3*4"), 0.000001)
    }
    @Test fun functionsAndConstantsWork() {
        assertEquals(1.0, ExpressionEvaluator.evaluate("sin(90)"), 0.000001)
        assertEquals(4.0, ExpressionEvaluator.evaluate("sqrt(16)"), 0.000001)
    }
    @Test fun exponentIsRightAssociative() {
        assertEquals(512.0, ExpressionEvaluator.evaluate("2^3^2"), 0.000001)
    }
}
