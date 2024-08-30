package com.example.kalkulatorbenar

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    private lateinit var displayInput: TextView
    private lateinit var displayOutput: TextView
    private var expression: String = ""
    private var lastNumeric: Boolean = false
    private var stateError: Boolean = false
    private var lastDot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayInput = findViewById(R.id.tvInput)
        displayOutput = findViewById(R.id.tvOutput)
    }


    fun onDigit(view: View) {
        if (stateError) {
            displayInput.text = (view as Button).text
            stateError = false
        } else {
            displayInput.append((view as Button).text)
        }
        lastNumeric = true
        lastDot = false
    }

    fun onOperator(view: View) {
        val buttonText = (view as Button).text.toString()

        if (buttonText == "(" || buttonText == ")") {
            Toast.makeText(this, "Mohon maaf tombol belum berfungsi", Toast.LENGTH_SHORT).show()
        } else if (lastNumeric && !stateError) {
            displayInput.append(buttonText)
            lastNumeric = false
            lastDot = false
        }
    }


    fun onClear(view: View) {
        displayInput.text = ""
        displayOutput.text = ""
        lastNumeric = false
        stateError = false
        lastDot = false
    }

    fun onCalculate(view: View) {
        if (lastNumeric && !stateError) {
            val txt = displayInput.text.toString()
            try {
                val result = evaluate(txt)
                displayOutput.text = if (result == result.toLong().toDouble()) {
                    result.toLong().toString() // Menampilkan tanpa desimal jika hasilnya bilangan bulat
                } else {
                    result.toString() // Menampilkan dengan desimal jika hasilnya bukan bilangan bulat
                }
                lastDot = true
            } catch (ex: Exception) {
                displayOutput.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }


    private fun evaluate(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: ${expression[pos]}")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm() // addition
                        eat('-'.code) -> x -= parseTerm() // subtraction
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor() // multiplication
                        eat('/'.code) -> x /= parseFactor() // division
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = pos
                when {
                    eat('('.code) -> { // parentheses
                        x = parseExpression()
                        eat(')'.code)
                    }
                    ch in '0'.code..'9'.code || ch == '.'.code -> { // numbers
                        while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                        x = expression.substring(startPos, pos).toDouble()
                    }
                    else -> throw RuntimeException("Unexpected: ${ch.toChar()}")
                }

                return x
            }
        }.parse()
    }
}