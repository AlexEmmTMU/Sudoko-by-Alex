package com.example.sudoko.data

data class SudokuGame(
    val id: Int,
    val difficulty: String,
    val initialState: String, // 81 characters, '0' for empty
    val solution: String
)
