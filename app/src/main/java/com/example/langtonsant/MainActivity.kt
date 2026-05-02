package com.example.langtonsant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.langtonsant.ui.theme.LangtonsAntTheme
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.text.iterator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LangtonsAntTheme {
                LangtonsAntGame()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LangtonsAntGame() {
    var playing by remember { mutableStateOf(false) }
    val height = 1050
    val width = 550
    var limit by remember { mutableStateOf(true) } // Speeds up when false
    var fullUnlimit by remember { mutableStateOf(false) } // When true -> stops limit reactivating

    val game = remember { LangtonsAnt(height, width) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.displayCutout)
            .windowInsetsPadding(WindowInsets.navigationBars),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                        limit = true
                        fullUnlimit = false
                        playing = !playing
                    }
                },
            ) {
                Icon(
                    painterResource(
                        if (!playing) {
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
            if (playing) {
                IconButton(
                    onClick = {
                        limit = !limit
                    }
                ) {
                    Icon(
                        painterResource(
                            if (limit) {
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

            if (!limit) {
                IconButton(
                    onClick = {
                        fullUnlimit = !fullUnlimit
                    }
                ) {
                    Icon(
                        painterResource(
                            if (fullUnlimit) {
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

        if (!playing) {
            GameMenu(game)
        } else {
            GameCanvas(height, width, game)
        }

        // Ant looper
        LaunchedEffect(playing) {
            if (!playing) return@LaunchedEffect
            var unlimitedLoops = 0
            thread {
                while (playing) {
                    game.moveAnt()
                    if (limit && game.iterations > 1500000) {
                        sleep(0, 200)
                    } else if (limit) {
                        sleep(0, 100)
                    } else if (game.iterations > 200000 && unlimitedLoops > 100000 && !fullUnlimit) {
                        limit = true
                        unlimitedLoops = 0
                    } else {
                        unlimitedLoops++
                    }
                }
            }
        }
    }
}

@Composable
fun GameCanvas(height: Int, width: Int, game: LangtonsAnt) {
    val pxSize = 2f
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Needed for game updates
        val currentTick = game.iterations

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (game.gameBoard[y][x] != Color(0xFF000000)) {
                    drawRect(
                        color = game.gameBoard[y][x],
                        topLeft = Offset(x * pxSize, y * pxSize),
                        size = Size(pxSize, pxSize)
                    )
                }
            }
        }
    }
}

@Composable
fun GameMenu(game: LangtonsAnt) {
    val selectedColors = remember { game.rulesColors }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        PresetDropdownMenu(game)
        InputRules(game)
        SelectedColors(game, selectedColors)
        Spacer(Modifier.height(10.dp))
        SelectColors(game, selectedColors)
    }
}


@Composable
fun PresetDropdownMenu(game: LangtonsAnt) {
    val presets = getPresets()
    var currentPreset by remember { mutableStateOf(presets[0]) }
    var showingDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        GameText("Preset: ")

        Box {
            GameText(
                currentPreset.name,
                modifier = Modifier.clickable { showingDropdown = true }
            )

            DropdownMenu(
                expanded = showingDropdown,
                onDismissRequest = { showingDropdown = false }
            ) {
                presets.forEach { preset ->
                    DropdownMenuItem(
                        text = { GameText(preset.name) },
                        onClick = {
                            currentPreset = preset

                            // Clear
                            game.rules.clear()
                            game.rulesColors.clear()

                            // Add the rules
                            for (j in preset.rules.indices) {
                                game.rules.add(preset.rules[j].toString())
                            }

                            game.rulesColors.addAll(preset.colors)

                            showingDropdown = false
                        }
                    )
                }
            }
        }
    }
}

fun getPresets(): Array<Preset>{
    return arrayOf(
        Preset("None", "", listOf()),
        Preset("Box", "LRRL", listOf(Color.Black, Color(0xFF1A235E), Color(0xFF3F51B5), Color.White)),
        Preset(
            "Crystalline squares",
            "LRRRRRLRRRRLLLLLL",
            listOf(
                Color.Black,
                Color(0xFF918c8c),
                Color(0xFFFF903B),
                Color(0xFF992e2e),
                Color(0xFF730C22),
                Color(0xFFC70734),
                Color(0xFF9C27B0),
                Color(0xFF6C077E),
                Color(0xFF673AB7),
                Color(0xFFB39DDB),
                Color(0xFF1A235E),
                Color(0xFF3F51B5),
                Color(0xFF03A9F4),
                Color(0xFF8CD3FA),
                Color(0xFF024F3F),
                Color(0xFF1FFFA6),
                Color.White
            )
        ),
        Preset(
            "Triangles",
            "LLLLLRLLLRRLLLLL",
            listOf(
                Color.Black,
                Color(0xFF992e2e),
                Color(0xFF918c8c),
                Color(0xFFFF903B),
                Color(0xFF6C077E),
                Color(0xFF9C27B0),
                Color(0xFF730C22),
                Color(0xFF1A235E),
                Color(0xFF3F51B5),
                Color(0xFFC70734),
                Color(0xFF673AB7),
                Color(0xFF03A9F4),
                Color.White,
                Color(0xFFB39DDB),
                Color(0xFF024F3F),
                Color(0xFF8CD3FA),
            )
        ),
        Preset(
            "Irregular triangles",
            "LRRRRLRRLLRRRRRL",
            listOf(
                Color.Black,
                Color(0xFF992e2e),
                Color(0xFF918c8c),
                Color(0xFFFF903B),
                Color(0xFF6C077E),
                Color(0xFF9C27B0),
                Color(0xFF730C22),
                Color(0xFF1A235E),
                Color(0xFF1FFFA6),
                Color(0xFF3F51B5),
                Color(0xFFC70734),
                Color(0xFF673AB7),
                Color(0xFF03A9F4),
                Color.White,
                Color(0xFF024F3F),
                Color(0xFF8CD3FA),
            )
        )
    )
}

@Composable
fun InputRules(game: LangtonsAnt) {
    var inputTextState by remember {
        mutableStateOf(
            game.rules.toList().toString()
                .removePrefix("[")
                .removeSuffix("]")
        )
    }

    key(game.rules) {
        inputTextState = game.rules
            .toList()
            .toString()
            .removePrefix("[")
            .removeSuffix("]")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        GameText("Rules")
        Spacer(Modifier.width(10.dp))
        TextField(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(55.dp),
            value = inputTextState,
            onValueChange = { input ->
                inputTextState = input
                game.rules.clear()
                for (char in input) {
                    if (char == 'R' || char == 'L') {
                        game.rules.add(char.toString())
                    }
                }
            }
        )
    }
}

@Composable
fun SelectedColors(game: LangtonsAnt, selectedColors: SnapshotStateList<Color>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        GameText("Colors")
        Spacer(Modifier.width(5.dp))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            for (i in 0 until selectedColors.size) {
                item {
                    Canvas(
                        Modifier
                            .size(50.dp)
                            .padding(10.dp)
                            .border(0.dp, Color.White)
                            .clickable {
                                game.rulesColors.removeAt(i)
//                                selectedColors.removeAt(i)
                            }
                    ) {
                        try {
                            drawRect(color = game.rulesColors[i])
                        } catch (e: Exception) {  }
                    }
                }
            }
        }

        RandomButton(game)
    }
}

@Composable
fun RandomButton(game: LangtonsAnt) {
    val selectableColors = arrayOf(
        Color.White,
        Color(0xFF918c8c),
        Color(0xFFFF903B),
        Color(0xFF992e2e),
        Color(0xFF730C22),
        Color(0xFFC70734),
        Color(0xFF9C27B0),
        Color(0xFF6C077E),
        Color(0xFF673AB7),
        Color(0xFFB39DDB),
        Color(0xFF1A235E),
        Color(0xFF3F51B5),
        Color(0xFF03A9F4),
        Color(0xFF8CD3FA),
        Color(0xFF024F3F),
        Color(0xFF1FFFA6),
        Color(0xFF04A84E),
    )

    IconButton(
        modifier = Modifier
            .size(60.dp),
        onClick = {
            selectableColors.shuffle()
            game.rulesColors.clear()
            game.rulesColors.add(Color.Black)
            for (i in 0 until game.rules.size - 1) {
                game.rulesColors.add(selectableColors[i])
            }
        }
    ) {
        Icon(
            painterResource(R.drawable.random),
            null,
            tint = Color.White
        )
    }
}

@Composable
fun SelectColors(game: LangtonsAnt, selectedColors: SnapshotStateList<Color>) {
    val selectableColors = arrayOf(
        Color.Black,
        Color.White,
        Color(0xFF918c8c),
        Color(0xFFFF903B),
        Color(0xFF992e2e),
        Color(0xFF730C22),
        Color(0xFFC70734),
        Color(0xFF9C27B0),
        Color(0xFF6C077E),
        Color(0xFF673AB7),
        Color(0xFFB39DDB),
        Color(0xFF1A235E),
        Color(0xFF3F51B5),
        Color(0xFF03A9F4),
        Color(0xFF8CD3FA),
        Color(0xFF024F3F),
        Color(0xFF1FFFA6),
        Color(0xFF04A84E),
    )

    GameText("Select colors")

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Top
    ) {
        for (color in selectableColors) {
            item {
                Canvas(
                    Modifier
                        .size(50.dp)
                        .padding(10.dp)
                        .border(0.dp, Color.White)
                        .clickable {
                            game.rulesColors.add(color)
                        }
                ) {
                    drawRect(color = color)
                }
            }
        }
    }
}

@Composable
fun GameText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        color = Color.White,
        fontSize = 15.sp
    )
}

data class Preset(
    val name: String,
    val rules: String,
    val colors: List<Color>
)
