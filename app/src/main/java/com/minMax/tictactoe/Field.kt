package com.minMax.tictactoe

enum class Value(val string : String) {
    EMPTY(""),
    X("X"),
    O("O")
}


data class Field(var value: Value = Value.EMPTY) {
    fun getString():String {
        return value.string
    }
}