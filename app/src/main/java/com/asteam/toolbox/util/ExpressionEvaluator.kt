package com.asteam.toolbox.util

import kotlin.math.*

/**
 * Dependency-free parser for the scientific calculator.
 * Supports + - * / ^, parentheses, pi/e and common math functions.
 * Trigonometric input is interpreted in degrees.
 */
object ExpressionEvaluator {
    fun evaluate(expression: String): Double {
        val parser = Parser(
            expression.lowercase()
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "pi")
                .replace(" ", ""),
        )
        val value = parser.parseExpression()
        parser.requireEnd()
        return value
    }

    private class Parser(private val source: String) {
        private var index = 0

        fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                value = when {
                    consume('+') -> value + parseTerm()
                    consume('-') -> value - parseTerm()
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            var value = parsePower()
            while (true) {
                value = when {
                    consume('*') -> value * parsePower()
                    consume('/') -> value / parsePower()
                    else -> return value
                }
            }
        }

        private fun parsePower(): Double {
            var value = parseUnary()
            if (consume('^')) value = value.pow(parsePower())
            return value
        }

        private fun parseUnary(): Double = when {
            consume('+') -> parseUnary()
            consume('-') -> -parseUnary()
            else -> parsePrimary()
        }

        private fun parsePrimary(): Double {
            if (consume('(')) {
                val value = parseExpression()
                require(consume(')')) { "پرانتز بسته نشده است" }
                return value
            }
            if (peekLetter()) {
                val name = readIdentifier()
                return when (name) {
                    "pi" -> Math.PI
                    "e" -> Math.E
                    "sqrt" -> sqrt(readFunctionArgument())
                    "sin" -> sin(Math.toRadians(readFunctionArgument()))
                    "cos" -> cos(Math.toRadians(readFunctionArgument()))
                    "tan" -> tan(Math.toRadians(readFunctionArgument()))
                    "log" -> log10(readFunctionArgument())
                    "ln" -> ln(readFunctionArgument())
                    "abs" -> abs(readFunctionArgument())
                    else -> error("تابع یا ثابت ناشناخته: $name")
                }
            }
            return readNumber()
        }

        private fun readFunctionArgument(): Double {
            require(consume('(')) { "بعد از تابع باید ( قرار بگیرد" }
            val value = parseExpression()
            require(consume(')')) { "پرانتز تابع بسته نشده است" }
            return value
        }

        private fun readNumber(): Double {
            val start = index
            var dotSeen = false
            while (index < source.length) {
                val c = source[index]
                if (c.isDigit()) index++
                else if (c == '.' && !dotSeen) { dotSeen = true; index++ }
                else break
            }
            require(index > start) { "عدد معتبر پیدا نشد" }
            return source.substring(start, index).toDouble()
        }

        private fun readIdentifier(): String {
            val start = index
            while (index < source.length && source[index].isLetter()) index++
            return source.substring(start, index)
        }

        private fun peekLetter(): Boolean = index < source.length && source[index].isLetter()

        private fun consume(char: Char): Boolean {
            if (index < source.length && source[index] == char) { index++; return true }
            return false
        }

        fun requireEnd() {
            require(index == source.length) { "کاراکتر نامعتبر در موقعیت ${index + 1}" }
        }
    }
}
