package com.minMax.tictactoe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class Outcome {
    RUN,
    DRAW,
    X_WIN,
    O_WIN
}

enum class Turn {
    X,
    O
}

class ViewModel (application: Application) : AndroidViewModel(application) {

    companion object {
        const val BOARD_SIZE = 9
    }

    private var turn = Turn.X
    private var outcome = Outcome.RUN

    val boardState : MutableLiveData<List<Boolean?>> by lazy { MutableLiveData<List<Boolean?>>() }
    val showOutcomeLiveData = MutableLiveData<Outcome>()
    val cpuMoving = MutableLiveData<Boolean>()

    init {
        boardState.value = List(BOARD_SIZE) { null }
        cpuMoving.value = false
    }

    fun reset(humanTurn: Turn) {
        outcome = Outcome.RUN
        boardState.value = List(BOARD_SIZE) { null }
        turn = Turn.X
        if (turn != humanTurn) {
            cpuMove(cloneState())
        }
        showOutcomeLiveData.postValue(Outcome.RUN)
    }

    fun onFieldClick(index: Int) {
        if (!cpuMoving.value!! && boardState.value!![index] == null && outcome == Outcome.RUN) {
            move(true, index)
        }
    }

    private fun changeTurn() {
        turn = if (turn == Turn.O) {
            Turn.X
        } else {
            Turn.O
        }
    }

    private fun cloneState() : MutableList<Boolean?> {
        val clonedState = MutableList<Boolean?>(BOARD_SIZE) { null }
        boardState.value!!.forEachIndexed { i, field ->
            clonedState[i] = field
        }
        return clonedState
    }

    private fun cpuMove(state: MutableList<Boolean?>) {
        cpuMoving.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val stateScores = mutableMapOf<Int, Int>()
            state.forEachIndexed { i, field ->
                if (field == null) {
                    state[i] = turn == Turn.X
                    stateScores[i] = scoreState(state, turn)
                    state[i] = null
                }
            }
            move(false, stateScores.filter {
                it.value == stateScores.maxByOrNull { it.value }!!.value
            }.keys.random())
            cpuMoving.postValue(false)
        }
    }

    private fun move(human: Boolean, field: Int) {
        val clone = cloneState()
        clone[field] = turn == Turn.X
        if (human) {
            boardState.value = clone
        } else {
            boardState.postValue(clone)
        }
        checkResult(human, clone)
    }

    private fun checkResult(human: Boolean, state: MutableList<Boolean?>) {
        outcome = checkOutcome(state)
        if (outcome == Outcome.RUN) {
            changeTurn()
            if (human) {
                cpuMove(cloneState())
            }
        } else {
            showOutcomeLiveData.postValue(outcome)
        }
    }

    private fun checkOutcome(state: List<Boolean?>): Outcome {
        //horizontal
        for (i in 0..6 step (3)) {
            if (state[i] == true && state[i + 1] == true && state[i + 2] == true) {
                return Outcome.X_WIN
            }
            if (state[i] == false && state[i + 1] == false && state[i + 2] == false) {
                return Outcome.O_WIN
            }
        }
        //vertical
        for (i in 0..2) {
            if (state[i] == true && state[i + 3] == true && state[i + 6] == true) {
                return Outcome.X_WIN
            }
            if (state[i] == false && state[i + 3] == false && state[i + 6] == false) {
                return Outcome.O_WIN
            }
        }
        //diagonal
        if (state[0] == true && state[4] == true && state[8] == true ||
            state[2] == true && state[4] == true && state[6] == true
        ) {
            return Outcome.X_WIN
        }
        if (state[0] == false && state[4] == false && state[8] == false ||
            state[2] == false && state[4] == false && state[6] == false
        ) {
            return Outcome.O_WIN
        }

        if (state.any { it == null }) {
            return Outcome.RUN
        }

        return Outcome.DRAW
    }

    private fun scoreState(state: MutableList<Boolean?>, forSide: Turn): Int {
        return when (checkOutcome(state)) {
            Outcome.X_WIN -> {
                if (turn == Turn.X) {
                    1
                } else {
                    -1
                }
            }
            Outcome.O_WIN -> {
                if (turn == Turn.O) {
                    1
                } else {
                    -1
                }
            }
            Outcome.DRAW -> 0
            Outcome.RUN -> {
                val scores = mutableListOf<Int>()
                state.forEachIndexed { index, field ->
                    if (field == null) {
                        state[index] = forSide == Turn.O
                        scores.add(
                            scoreState(state,
                                if (forSide == Turn.X) {
                                    Turn.O
                                } else {
                                    Turn.X
                                }
                            )
                        )
                        state[index] = null
                    }
                }
                return if (forSide != turn) {
                    scores.maxOrNull()!!
                } else {
                    scores.minOrNull()!!
                }
            }
        }
    }
}