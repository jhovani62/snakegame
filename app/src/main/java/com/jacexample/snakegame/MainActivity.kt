package com.jacexample.snakegame

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jacexample.snakegame.ui.theme.SnakegameTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
lateinit var mediaPlayer: MediaPlayer
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakegameTheme {
                GameScreen(onExit = { finish() })
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }
    private fun releaseMediaPlayer() {
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }
}
@Composable
fun GameScreen(onExit: () -> Unit) {
    val context = LocalContext.current
    var showStartScreen by remember { mutableStateOf(true) }
    if (showStartScreen) {
        StartScreen(
            onStartGame = {
                initializeMediaPlayer(context)
                showStartScreen = false
            },
            onExit = {
                releaseMediaPlayer()
                onExit()
            }
        )
    } else {
        SnakeGame(
            onRestart = {
                initializeMediaPlayer(context)
                showStartScreen = false
            },
            onExitToStartScreen = {
                releaseMediaPlayer()
                showStartScreen = true
            }
        )
    }
}
fun releaseMediaPlayer() {
}
@Composable
fun StartScreen(onStartGame: () -> Unit, onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB4D49A))
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Snake Game", color = Color.White, modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onStartGame()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Nuevo Juego")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    releaseMediaPlayer()
                    onExit()
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Salir")
            }
        }
    }
}
@Composable
fun SnakeGame(onRestart: () -> Unit, onExitToStartScreen: () -> Unit) {
    val boardSize by remember { mutableStateOf(18) }
    var snake by remember { mutableStateOf(listOf(Point(5, 5))) }
    var food by remember { mutableStateOf(generateFood(boardSize)) }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var isGameOver by remember { mutableStateOf(false) }
    var justAte by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var snakeSpeed by remember { mutableStateOf(340L) }
    var timeElapsed by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    LaunchedEffect(isGameOver, isPaused) {
        if (!isGameOver && !isPaused) {
            while (!isGameOver && !isPaused) {
                delay(1000L)
                timeElapsed++
            }
        }
    }
    LaunchedEffect(isGameOver, isPaused) {
        if (!isGameOver && !isPaused) {
            while (!isGameOver && !isPaused) {
                delay(snakeSpeed)
                val newSnake = moveSnake(snake, direction)
                if (newSnake.first() == food) {
                    snake = listOf(food) + newSnake
                    food = generateFood(boardSize)
                    justAte = true
                    score += 5
                    snakeSpeed = (snakeSpeed * 0.9).toLong()
                } else {
                    snake = newSnake
                    justAte = false
                }
                if (!justAte && checkCollision(snake, boardSize)) {
                    isGameOver = true
                    mediaPlayer.pause()
                }
            }
        }
    }
    if (isPaused) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    } else {
        if (!mediaPlayer.isPlaying && !isGameOver) {
            mediaPlayer.start()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Puntaje: $score", color = Color.Black)
            Text(text = "Tiempo: ${timeElapsed}s", color = Color.Black)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isGameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡Juego Terminado! Puntaje Final: $score",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Red
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    isGameOver = false
                    isPaused = false
                    timeElapsed = 0
                    score = 0
                    snake = listOf(Point(boardSize / 2, boardSize / 2))
                    food = generateFood(boardSize)
                    direction = Direction.RIGHT
                    snakeSpeed = 335L

                    onRestart()
                }) {
                    Text("Reiniciar")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onExitToStartScreen) {
                    Text("Salir a Inicio")
                }
            }
        } else {
            GameBoard(snake, food, boardSize)
            Spacer(modifier = Modifier.height(16.dp))
            ControlButtons(
                onUpClick = { if (direction != Direction.DOWN && !isPaused) direction = Direction.UP },
                onDownClick = { if (direction != Direction.UP && !isPaused) direction = Direction.DOWN },
                onLeftClick = { if (direction != Direction.RIGHT && !isPaused) direction = Direction.LEFT },
                onRightClick = { if (direction != Direction.LEFT && !isPaused) direction = Direction.RIGHT }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { isPaused = !isPaused }) {
                Text(if (isPaused) "Reanudar" else "Pausar")
            }
        }
    }
}
@Composable
fun GameBoard(snake: List<Point>, food: Point, boardSize: Int) {
    Column {
        for (y in 0 until boardSize) {
            Row {
                for (x in 0 until boardSize) {
                    val point = Point(x, y)
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(
                                when {
                                    snake.contains(point) -> Color.Green
                                    food == point -> Color.Red
                                    else -> Color(0xFFB4D49A)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun ControlButtons(
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(16.dp)
            .size(200.dp)
    ) {
        Button(onClick = onUpClick, modifier = Modifier.align(Alignment.TopCenter).padding(4.dp)) {
            Text("▲")
        }
        Button(onClick = onLeftClick, modifier = Modifier.align(Alignment.CenterStart).padding(4.dp)) {
            Text("◄")
        }
        Button(onClick = onRightClick, modifier = Modifier.align(Alignment.CenterEnd).padding(4.dp)) {
            Text("►")
        }
        Button(onClick = onDownClick, modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp)) {
            Text("▼")
        }
    }
}
private fun initializeMediaPlayer(context: Context) {
    if (!::mediaPlayer.isInitialized) {
        mediaPlayer = MediaPlayer.create(context, R.raw.snake).apply {
            isLooping = true
        }
    }
    if (!mediaPlayer.isPlaying) {
        mediaPlayer.start()
    }
}
fun generateFood(boardSize: Int): Point {
    return Point(Random.nextInt(boardSize), Random.nextInt(boardSize))
}
// Movimiento de la serpiente
fun moveSnake(snake: List<Point>, direction: Direction): List<Point> {
    val newHead = when (direction) {
        Direction.UP -> Point(snake.first().x, snake.first().y - 1)
        Direction.DOWN -> Point(snake.first().x, snake.first().y + 1)
        Direction.LEFT -> Point(snake.first().x - 1, snake.first().y)
        Direction.RIGHT -> Point(snake.first().x + 1, snake.first().y)
    }
    return listOf(newHead) + snake.dropLast(1)
}
// Verifica colisiones.
fun checkCollision(snake: List<Point>, boardSize: Int): Boolean {
    val head = snake.first()
    return head.x < 0 || head.x >= boardSize || head.y < 0 || head.y >= boardSize || snake.drop(1).contains(head)
}
enum class Direction { UP, DOWN, LEFT, RIGHT }
data class Point(val x: Int, val y: Int)
