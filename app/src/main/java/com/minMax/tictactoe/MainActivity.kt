package com.minMax.tictactoe

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.minMax.tictactoe.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val board = Board()

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + parentJob)

    private var cpuMoving = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.board = board
        setContentView(binding.root)

        resetBoard(Turn.X)

        setupClickListener(binding.A0, board.state[0][0])
        setupClickListener(binding.A1, board.state[0][1])
        setupClickListener(binding.A2, board.state[0][2])
        setupClickListener(binding.B0, board.state[1][0])
        setupClickListener(binding.B1, board.state[1][1])
        setupClickListener(binding.B2, board.state[1][2])
        setupClickListener(binding.C0, board.state[2][0])
        setupClickListener(binding.C1, board.state[2][1])
        setupClickListener(binding.C2, board.state[2][2])

        binding.xButton.setOnClickListener {
            resetBoard(Turn.X)
        }

        binding.oButton.setOnClickListener {
            resetBoard(Turn.O)
        }
    }

    private fun resetBoard(humanTurn: Turn){
        board.reset()
        board.turn = Turn.X
        if (board.turn != humanTurn) {
            cpuMove()
        }
        binding.invalidateAll()
    }

    private fun setupClickListener(textView: TextView, field: Field) {
        textView.setOnClickListener {
            if ( !cpuMoving && field.value == Value.EMPTY && board.outcome == Outcome.RUN) {
                if (board.turn == Turn.O) {
                    field.value = Value.O
                } else {
                    field.value = Value.X
                }
                binding.invalidateAll()
                checkResult(true)
            }
        }
    }

    private fun checkResult(human: Boolean) {
        board.setOutcome()
        runOnUiThread {
            when (board.outcome) {
                Outcome.O_WIN,
                Outcome.X_WIN,
                Outcome.DRAW -> showOutcome(board.outcome)
                Outcome.RUN -> {
                    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(10)
                    board.changeTurn()
                    if (human) {
                        cpuMove()
                    }
                }
            }
        }
    }

    private fun cpuMove() {
        binding.progressBar.isVisible = true
        cpuMoving = true
        coroutineScope.launch(Dispatchers.IO) {
            val move = board.cpuMove(board.cloneState())
            board.state[move.first][move.second].value = if (board.turn == Turn.O) {
                Value.O
            } else {
                Value.X
            }
            binding.invalidateAll()
            checkResult(false)
            hideProgress()
            cpuMoving = false
        }
    }

    private fun hideProgress(){
        runOnUiThread {
            binding.progressBar.isVisible = false
        }
    }

    private fun showOutcome(outcome: Outcome) {
        runOnUiThread {
            when (outcome) {
                Outcome.O_WIN -> {
                    Toast.makeText(applicationContext, "O is the winner!", Toast.LENGTH_LONG).show()
                    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(100)
                }
                Outcome.X_WIN -> {
                    Toast.makeText(applicationContext, "X is the winner!", Toast.LENGTH_LONG).show()
                    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(100)
                }
                Outcome.DRAW -> {
                    Toast.makeText(applicationContext, "It's a draw!", Toast.LENGTH_LONG).show()
                    (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(100)
                }
            }
        }
    }
}