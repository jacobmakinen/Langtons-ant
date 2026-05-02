package com.example.langtonsant

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class LangtonsAnt(
    private val height: Int,
    private val width: Int
) {
    val rules = mutableStateListOf<String>()

    // Color in hex
    val rulesColors = mutableStateListOf<Color>()
    val gameBoard = Array(height) {
        Array(width) {
            Color(0xFF000000)
        }
    }
    private val ant = Ant(width / 2,height / 2, Direction.UP)
    var iterations by mutableLongStateOf(0L)

    fun moveAnt() {
        iterations ++
        if (iterations % 100000L == 0L) {
            Log.d("LangtonsAntLog", "Steps: $iterations")
        }
        val changedColor = getChangedSquareColor()
        rotate()
        gameBoard[ant.y][ant.x] = changedColor
        moveAntForward()
    }

    fun clearGameBoard() {
        for (i in 0 until gameBoard.size) {
            for (j in 0 until gameBoard[i].size) {
                gameBoard[i][j] = Color.Black
            }
        }
        ant.y = height / 2
        ant.x = width / 2
        ant.direction = Direction.UP
        iterations = 0
    }

    private fun moveAntForward() {
        // Increment and decrements are reversed for vertical changes due to top left being (0,0)
        when (ant.direction) {
            Direction.UP -> {
                if (ant.y != 0) {
                    ant.y --
                } else {
                    ant.y = height - 1
                }
            }
            Direction.LEFT -> {
                if (ant.x != 0) {
                    ant.x --
                } else {
                    ant.x = width - 1
                }
            }
            Direction.DOWN -> {
                if (ant.y + 1 != height) {
                    ant.y ++
                } else {
                    ant.y = 0
                }
            }
            Direction.RIGHT -> {
                if (ant.x + 1 != width) {
                    ant.x ++
                }  else {
                    ant.x = 0
                }
            }
        }
    }

    private fun getChangedSquareColor(): Color {
        val color = gameBoard[ant.y][ant.x]
        for (i in 0 until rulesColors.size) {
            if (rulesColors[i] == color) {
                return if (i + 1 != rulesColors.size) {
                    rulesColors[i + 1]
                } else {
                    rulesColors[0]
                }
            }
        }

        throw Exception("Could not find the color")
    }

    private fun rotate() {
        val color = gameBoard[ant.y][ant.x]

        for (i in 0 until rulesColors.size) {
            if (rulesColors[i] == color) {
                val rule = rules[i]
                if (rule.uppercase() == "L") {
                    when (ant.direction) {
                        Direction.UP -> ant.direction = Direction.LEFT
                        Direction.LEFT -> ant.direction = Direction.DOWN
                        Direction.DOWN -> ant.direction = Direction.RIGHT
                        Direction.RIGHT -> ant.direction = Direction.UP
                    }
                } else {
                    when (ant.direction) {
                        Direction.UP -> ant.direction = Direction.RIGHT
                        Direction.RIGHT -> ant.direction = Direction.DOWN
                        Direction.DOWN -> ant.direction = Direction.LEFT
                        Direction.LEFT -> ant.direction = Direction.UP
                    }
                }
            }
        }
    }
}

data class Ant(
    var x: Int,
    var y: Int,
    var direction: Direction
)

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}