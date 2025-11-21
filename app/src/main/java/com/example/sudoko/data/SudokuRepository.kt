package com.example.sudoko.data

object SudokuRepository {
    private val games = mutableListOf<SudokuGame>()

    init {
        // Generate games on init (or load from DB/Json in a real app)
        // We will generate on demand instead to save memory/startup, 
        // or we can pre-generate a few.
    }

    fun getGame(difficulty: SudokuGenerator.Difficulty): SudokuGame {
        return SudokuGenerator.generateGame(difficulty)
    }
    
    // Keep the old method signature for compatibility if needed, but redirect
    fun getRandomGame(): SudokuGame {
        return getGame(SudokuGenerator.Difficulty.Intermediate)
    }
}
