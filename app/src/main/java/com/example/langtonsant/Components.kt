package com.example.langtonsant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ControlButtonRow(playing: MutableState<Boolean>, limit: MutableState<Boolean>, fullUnlimit: MutableState<Boolean>, game: LangtonsAnt) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Play pause button
        IconButton(
            onClick = {
                if (game.rules.isNotEmpty() && game.rulesColors.size == game.rules.size) {
                    game.clearGameBoard()
                    limit.value = true
                    fullUnlimit.value = false
                    playing.value = !playing.value
                }
            },
        ) {
            Icon(
                painterResource(
                    if (!playing.value) {
                        R.drawable.play
                    } else {
                        R.drawable.pause
                    }
                ),
                null,
                tint = Color.White
            )
        }

        // Speed limit button
        if (playing.value) {
            IconButton(
                onClick = {
                    limit.value = !limit.value
                }
            ) {
                Icon(
                    painterResource(
                        if (limit.value) {
                            R.drawable.single_arrow_down
                        } else {
                            R.drawable.single_arrow_up
                        }
                    ),
                    null,
                    tint = Color.White
                )
            }
        }

        if (!limit.value) {
            IconButton(
                onClick = {
                    fullUnlimit.value = !fullUnlimit.value
                }
            ) {
                Icon(
                    painterResource(
                        if (fullUnlimit.value) {
                            R.drawable.speed
                        } else {
                            R.drawable.slow
                        }
                    ),
                    null,
                    tint = Color.White
                )
            }
        }
    }
}