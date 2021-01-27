package com.minMax.tictactoe

class Board (val state : List<List<Field>> = listOf(
    listOf(Field(), Field(), Field()),
    listOf(Field(), Field(), Field()),
    listOf(Field(), Field(), Field()),
)) {

    var turn = Turn.X

    var outcome = Outcome.RUN

    fun changeTurn() {
        turn = if (turn == Turn.O) {
            Turn.X
        } else {
            Turn.O
        }
    }

    fun setOutcome(){
        outcome = checkOutcome(state)
    }

    fun reset() {
        outcome = Outcome.RUN
        turn = Turn.O
        state.forEach { list ->
            list.forEach {
                it.value = Value.EMPTY
            }
        }
    }

    fun cloneState() : List<List<Field>> {
        val cloneState : List<List<Field>> = listOf(
            listOf(Field(), Field(), Field()),
            listOf(Field(), Field(), Field()),
            listOf(Field(), Field(), Field()),
        )
        state.forEachIndexed{i, list ->
            list.forEachIndexed { j, field ->
                cloneState[i][j].value = field.value
            }
        }
        return cloneState
    }

    fun cpuMove(state: List<List<Field>>): Pair<Int, Int> {
        val stateScore = mutableMapOf<Pair<Int, Int>, Int>()
        state.forEachIndexed { i, list ->
            list.forEachIndexed { j, field ->
                if (field.value == Value.EMPTY) {
                    field.value = if (turn == Turn.X) {
                        Value.X
                    } else {
                        Value.O
                    }
                    stateScore[Pair(i, j)] =
                        scoreState(
                            state, if (turn == Turn.O) {
                                Turn.O
                            } else {
                                Turn.X
                            }
                        )
                    field.value = Value.EMPTY
                }
            }
        }
        return stateScore.filter { it.value == stateScore.maxByOrNull { it.value }!!.value }.keys.random()
    }

    private fun checkOutcome(state: List<List<Field>>): Outcome {
        for (i in 0..2) {
            //horizontal
            if (state[i][0].value == state[i][1].value && state[i][1].value == state[i][2].value) {
                if (state[i][0].value == Value.X) {
                    return Outcome.X_WIN
                } else if (state[i][0].value == Value.O) {
                    return Outcome.O_WIN
                }
            }
        }
        //vertical
        for (i in 0..2) {
            if (state[0][i].value == state[1][i].value && state[1][i].value == state[2][i].value) {
                if (state[0][i].value == Value.X) {
                    return Outcome.X_WIN
                } else if (state[0][i].value == Value.O) {
                    return Outcome.O_WIN
                }
            }
        }
        //diagonal
        if (state[0][0].value == state[1][1].value && state[1][1].value == state[2][2].value ||
            state[0][2].value == state[1][1].value && state[1][1].value == state[2][0].value
        ) {
            if (state[1][1].value == Value.X) {
                return Outcome.X_WIN
            } else if (state[1][1].value == Value.O) {
                return Outcome.O_WIN
            }
        }

        state.forEach { list ->
            if (list.any {
                    it.value == Value.EMPTY
                }) {
                return Outcome.RUN
            }
        }
        
        return Outcome.DRAW
    }

    private fun scoreState(state: List<List<Field>>, forSide: Turn): Int {
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
                state.forEach {
                    it.forEach {
                        if (it.value == Value.EMPTY) {
                            it.value = if (forSide == Turn.X) {
                                Value.O
                            } else {
                                Value.X
                            }
                            scores.add(
                                scoreState(
                                    state, if (forSide == Turn.X) {
                                        Turn.O
                                    } else {
                                        Turn.X
                                    }
                                )
                            )
                            it.value = Value.EMPTY
                        }
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