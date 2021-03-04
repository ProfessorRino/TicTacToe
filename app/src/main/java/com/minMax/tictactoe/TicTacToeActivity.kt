package com.minMax.tictactoe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.minMax.tictactoe.ViewModel.Companion.BOARD_SIZE

private lateinit var model: ViewModel

class TicTacToeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProvider(this).get(ViewModel::class.java)

        setContent {
            Column {
                ShowOutcome()
                GameBoard()
                Controls()
                LoadingScreen()
            }
        }

        model.reset(Turn.X)

        model.showOutcomeLiveData.observe(this, {
            when (it) {
                Outcome.O_WIN,
                Outcome.X_WIN,
                Outcome.DRAW -> vibrate(100)
                Outcome.RUN -> vibrate(35)
            }
        })
    }

    private fun vibrate(ms: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(ms)
        }
    }

    @Composable
    private fun ShowOutcome() {
        val outcome by model.showOutcomeLiveData.observeAsState(false)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(25.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = when (outcome) {
                    Outcome.X_WIN -> "X wins!"
                    Outcome.O_WIN -> "O wins!"
                    Outcome.DRAW -> "Draw!"
                    else -> ""
                },
                style = MaterialTheme.typography.h2,
                textAlign = TextAlign.Center,
                color = Color.Red
            )
        }
    }

    @Composable
    fun GameBoard() {
        val state: List<Boolean?> by model.boardState.observeAsState(List(BOARD_SIZE) { null })
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(0.5f)
        ) {
            for (row in 0..6 step (3)) {
                Row(
                    verticalAlignment = Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (col in 0..2) {
                        val index = row + col
                        TextButton(
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, Color.Black),
                            onClick = { model.onFieldClick(index) }
                        ) {
                            Text(
                                text = if (state[index] == true) {
                                    "X"
                                } else if (state[index] == false) {
                                    "O"
                                } else " ",
                                style = MaterialTheme.typography.h3
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Controls() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
        ) {
            Button(onClick = { model.reset(Turn.X) }) {
                Text(
                    text = "Play X",
                    style = MaterialTheme.typography.button
                )
            }
            Button(onClick = { model.reset(Turn.O) }) {
                Text(
                    text = "Play O",
                    style = MaterialTheme.typography.button
                )
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
        val loading: Boolean by model.cpuMoving.observeAsState(false)
        if (loading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}