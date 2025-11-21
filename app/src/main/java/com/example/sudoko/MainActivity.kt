package com.example.sudoko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoko.data.SudokuGame
import com.example.sudoko.data.SudokuGenerator
import com.example.sudoko.data.SudokuRepository
import com.example.sudoko.ui.theme.SudokoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SudokoTheme {
                SudokuApp()
            }
        }
    }
}

// Data class to hold the state of a single cell
data class CellData(
    val value: String = "",
    val isInitial: Boolean = false,
    val isWrong: Boolean = false,
    val notes: Set<String> = emptySet()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuApp() {
    var showMenu by remember { mutableStateOf(false) }
    var showNewGameDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var gameDifficulty by remember { mutableStateOf(SudokuGenerator.Difficulty.Easy) }
    var game by remember { mutableStateOf(SudokuRepository.getGame(gameDifficulty)) }
    var resetTrigger by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("Sudoku")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2025 by Alex",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Game (Easy)") },
                            onClick = {
                                showMenu = false
                                gameDifficulty = SudokuGenerator.Difficulty.Easy
                                showNewGameDialog = true
                            }
                        )
                         DropdownMenuItem(
                            text = { Text("New Game (Intermediate)") },
                            onClick = {
                                showMenu = false
                                gameDifficulty = SudokuGenerator.Difficulty.Intermediate
                                showNewGameDialog = true
                            }
                        )
                         DropdownMenuItem(
                            text = { Text("New Game (Advanced)") },
                            onClick = {
                                showMenu = false
                                gameDifficulty = SudokuGenerator.Difficulty.Advanced
                                showNewGameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset Board") },
                            onClick = {
                                showMenu = false
                                resetTrigger++
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About") },
                            onClick = {
                                showMenu = false
                                showAboutDialog = true
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        SudokuGameScreen(
            modifier = Modifier.padding(innerPadding),
            game = game,
            resetKey = resetTrigger,
            onNewGame = {
                game = SudokuRepository.getGame(gameDifficulty) // Keep same difficulty
                resetTrigger++
            }
        )

        if (showNewGameDialog) {
            AlertDialog(
                onDismissRequest = { showNewGameDialog = false },
                title = { Text("New Game") },
                text = { Text("Start a new ${gameDifficulty.name} game? Current progress will be lost.") },
                confirmButton = {
                    TextButton(onClick = {
                        game = SudokuRepository.getGame(gameDifficulty)
                        resetTrigger++
                        showNewGameDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewGameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text("About") },
                text = { 
                    Column {
                        Text("Sudoku App")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Developer: Alex 2025")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun SudokuGameScreen(
    modifier: Modifier = Modifier,
    game: SudokuGame,
    resetKey: Int,
    onNewGame: () -> Unit
) {
    // Use 'key' composable to reset state when resetKey changes
    key(game, resetKey) {
        var selectedRow by remember { mutableStateOf<Int?>(null) }
        var selectedCol by remember { mutableStateOf<Int?>(null) }
        var isNoteMode by remember { mutableStateOf(false) }
        var selectedNumber by remember { mutableStateOf<String?>(null) } // New state for selected number
        
        // Initialize board state
        var boardState by remember {
            mutableStateOf(
                List(81) { index ->
                    val char = game.initialState[index]
                    CellData(
                        value = if (char == '0') "" else char.toString(),
                        isInitial = char != '0'
                    )
                }
            )
        }
        
        var mistakes by remember { mutableStateOf(0) }
        val maxMistakes = 3
        var isGameOver by remember { mutableStateOf(false) }
        var isGameWon by remember { mutableStateOf(false) }
        var hintsRemaining by remember { mutableStateOf(3) }

        // Calculate completed numbers (numbers present 9 times on the board)
        val completedNumbers = remember(boardState) {
            (1..9).map { it.toString() }.filter { num ->
                boardState.count { it.value == num && !it.isWrong } == 9
            }.toSet()
        }

        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Difficulty: ${game.difficulty}")
                    Text("Mistakes: $mistakes/$maxMistakes")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            SudokuBoard(
                boardState = boardState,
                selectedRow = selectedRow,
                selectedCol = selectedCol,
                selectedNumber = selectedNumber, // Pass selected number to board
                onCellClick = { row, col ->
                    if (!isGameOver && !isGameWon) {
                        if (selectedNumber != null) {
                            // If a number is selected from pad, try to place it in the clicked cell
                            val index = row * 9 + col
                            val cell = boardState[index]
                             if (!cell.isInitial) {
                                val number = selectedNumber!!
                                val newBoard = boardState.toMutableList()
                                if (isNoteMode) {
                                    // Toggle note
                                    val currentNotes = cell.notes.toMutableSet()
                                    if (currentNotes.contains(number)) {
                                        currentNotes.remove(number)
                                    } else {
                                        currentNotes.add(number)
                                    }
                                    newBoard[index] = cell.copy(notes = currentNotes)
                                } else {
                                    // Enter value
                                    val correctValue = game.solution[index].toString()
                                    val isWrong = number != correctValue
                                    
                                    if (isWrong) {
                                        mistakes++
                                        if (mistakes >= maxMistakes) isGameOver = true
                                    }
                                    
                                    newBoard[index] = cell.copy(
                                        value = number,
                                        isWrong = isWrong,
                                        notes = emptySet()
                                    )
                                }
                                boardState = newBoard
                                
                                // Check win
                                if (boardState.none { it.value.isEmpty() || it.isWrong }) {
                                    isGameWon = true
                                }
                             }
                        } else {
                            // Normal selection logic
                            selectedRow = row
                            selectedCol = col
                        }
                    }
                }
            )

            // Controls and Number Pad
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Tools: Hint and Note Toggle
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (selectedRow != null && selectedCol != null && hintsRemaining > 0 && !isGameOver && !isGameWon) {
                                val index = selectedRow!! * 9 + selectedCol!!
                                if (!boardState[index].isInitial) {
                                    val correctVal = game.solution[index].toString()
                                    // Update cell to correct value and clear notes
                                    val newBoard = boardState.toMutableList()
                                    newBoard[index] = CellData(value = correctVal, isInitial = false, notes = emptySet())
                                    boardState = newBoard
                                    hintsRemaining--
                                    
                                    // Check win
                                    if (boardState.none { it.value.isEmpty() || it.isWrong }) {
                                        isGameWon = true
                                    }
                                }
                            }
                        },
                        enabled = hintsRemaining > 0 && selectedRow != null && selectedCol != null && !boardState[selectedRow!! * 9 + selectedCol!!].isInitial
                    ) {
                        Text("Hint ($hintsRemaining)")
                    }

                    IconToggleButton(
                        checked = isNoteMode,
                        onCheckedChange = { isNoteMode = it }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Edit, 
                                contentDescription = "Notes",
                                tint = if (isNoteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isNoteMode) "On" else "Off",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isNoteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                NumberPad(
                    onNumberClick = { number ->
                        if (!isGameOver && !isGameWon && !completedNumbers.contains(number)) {
                            // Logic:
                            // If selectedNumber is null, we are selecting a new number.
                            // We should also apply it to the board if a cell is selected (Cell-First).
                            //
                            // If selectedNumber is NOT null, we are changing the paint tool.
                            // We should NOT apply it to the board immediately, even if a cell is selected (Digit-First tool switch).

                            val isChangingTool = selectedNumber != null

                            if (selectedNumber == number) {
                                selectedNumber = null // Deselect if clicked again
                            } else {
                                selectedNumber = number // Select number for "paint" mode
                            }
                            
                            // Only apply to currently selected cell if we were NOT switching tools
                            if (!isChangingTool && selectedRow != null && selectedCol != null) {
                                val index = selectedRow!! * 9 + selectedCol!!
                                val cell = boardState[index]
                                
                                if (!cell.isInitial) {
                                    val newBoard = boardState.toMutableList()
                                    if (isNoteMode) {
                                        // Toggle note
                                        val currentNotes = cell.notes.toMutableSet()
                                        if (currentNotes.contains(number)) {
                                            currentNotes.remove(number)
                                        } else {
                                            currentNotes.add(number)
                                        }
                                        newBoard[index] = cell.copy(notes = currentNotes)
                                    } else {
                                        // Enter value
                                        val correctValue = game.solution[index].toString()
                                        val isWrong = number != correctValue
                                        
                                        if (isWrong) {
                                            mistakes++
                                            if (mistakes >= maxMistakes) isGameOver = true
                                        }
                                        
                                        newBoard[index] = cell.copy(
                                            value = number,
                                            isWrong = isWrong,
                                            notes = emptySet()
                                        )
                                    }
                                    boardState = newBoard
                                    
                                    if (boardState.none { it.value.isEmpty() || it.isWrong }) {
                                        isGameWon = true
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                         selectedNumber = null // Clear selected number on delete
                        if (selectedRow != null && selectedCol != null && !isGameOver && !isGameWon) {
                            val index = selectedRow!! * 9 + selectedCol!!
                            val cell = boardState[index]
                            if (!cell.isInitial) {
                                val newBoard = boardState.toMutableList()
                                newBoard[index] = cell.copy(value = "", isWrong = false, notes = cell.notes) 
                                if (cell.value.isEmpty()) {
                                     newBoard[index] = cell.copy(notes = emptySet())
                                }
                                boardState = newBoard
                            }
                        }
                    },
                    selectedNumber = selectedNumber,
                    completedNumbers = completedNumbers
                )
            }
        }

        if (isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Game Over") },
                text = { Text("You have made too many mistakes.") },
                confirmButton = {
                    TextButton(onClick = onNewGame) {
                        Text("New Game")
                    }
                }
            )
        }

        if (isGameWon) {
             AlertDialog(
                onDismissRequest = { },
                title = { Text("Congratulations!") },
                text = { Text("You solved the puzzle!") },
                confirmButton = {
                    TextButton(onClick = onNewGame) {
                        Text("New Game")
                    }
                }
            )
        }
    }
}

@Composable
fun SudokuBoard(
    boardState: List<CellData>,
    selectedRow: Int?,
    selectedCol: Int?,
    selectedNumber: String?,
    onCellClick: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .aspectRatio(1f)
            .background(Color.White) // Force white background for the entire board
            .border(2.dp, Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until 9) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (col in 0 until 9) {
                        val index = row * 9 + col
                        val cellData = boardState[index]
                        
                        // Logic for highlighting
                        val isSelected = row == selectedRow && col == selectedCol
                        
                        // Determine the value to check for "same number" highlighting
                        // Either the value in the selected cell, OR the selected number from the pad
                        val valueToCheck = selectedNumber ?: if (selectedRow != null && selectedCol != null) boardState[selectedRow * 9 + selectedCol].value else ""
                        
                        val isSameNumber = valueToCheck.isNotEmpty() && cellData.value == valueToCheck
                        
                        // Highlight row and col of the selected cell (or if we want to highlight row/col of matching numbers? usually just the selected cell's row/col)
                        // User asked: "select a box with a number... highlight all the boxes and rows containing that number"
                        // This usually means highlighting all instances of that number.
                        // AND "highlight... rows containing that number" -> this implies seeing the 'influence' of that number?
                        // Standard Sudoku apps:
                        // 1. Highlight the selected cell.
                        // 2. Highlight the Row, Column, and Block of the selected cell (lightly).
                        // 3. Highlight all cells containing the same number (darker/distinct).
                        
                        val isInSameRowOrCol = (selectedRow != null && row == selectedRow) || (selectedCol != null && col == selectedCol)
                        // Block logic: (row / 3) == (selectedRow / 3) && (col / 3) == (selectedCol / 3)
                        val isInSameBlock = selectedRow != null && selectedCol != null && (row / 3 == selectedRow / 3) && (col / 3 == selectedCol / 3)

                        SudokuCell(
                            row = row,
                            col = col,
                            data = cellData,
                            isSelected = isSelected,
                            isSameNumber = isSameNumber,
                            isRelated = isInSameRowOrCol || isInSameBlock,
                            modifier = Modifier.weight(1f),
                            onClick = { onCellClick(row, col) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    row: Int,
    col: Int,
    data: CellData,
    isSelected: Boolean,
    isSameNumber: Boolean,
    isRelated: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color.Cyan.copy(alpha = 0.5f)
        isSameNumber -> Color.Blue.copy(alpha = 0.3f) // Darker highlight for same numbers
        isRelated -> Color.LightGray.copy(alpha = 0.3f) // Lighter highlight for row/col/block
        else -> Color.White // Default white background
    }
    
    val textColor = when {
        data.isInitial -> Color.Black
        data.isWrong -> Color.Red
        else -> Color.Blue
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val thickStrokeWidth = 4.dp.toPx()

                val rightWidth = if ((col + 1) % 3 == 0 && col != 8) thickStrokeWidth else strokeWidth
                val bottomWidth = if ((row + 1) % 3 == 0 && row != 8) thickStrokeWidth else strokeWidth

                // Draw right border
                if (col < 8) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = rightWidth
                    )
                }

                // Draw bottom border
                if (row < 8) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = bottomWidth
                    )
                }
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (data.value.isNotEmpty()) {
            Text(
                text = data.value,
                fontSize = 20.sp,
                fontWeight = if (data.isInitial) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        } else if (data.notes.isNotEmpty()) {
            // Draw notes grid
            Column(
                modifier = Modifier.fillMaxSize().padding(2.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (r in 0..2) {
                    Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0..2) {
                            val num = r * 3 + c + 1
                            val numStr = num.toString()
                            Text(
                                text = if (data.notes.contains(numStr)) numStr else "",
                                fontSize = 8.sp,
                                lineHeight = 8.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    selectedNumber: String?,
    completedNumbers: Set<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 1..5) {
                val numStr = i.toString()
                NumberButton(
                    number = numStr, 
                    onClick = { onNumberClick(numStr) },
                    isSelected = selectedNumber == numStr,
                    isDisabled = completedNumbers.contains(numStr)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 6..9) {
                val numStr = i.toString()
                NumberButton(
                    number = numStr, 
                    onClick = { onNumberClick(numStr) },
                    isSelected = selectedNumber == numStr,
                    isDisabled = completedNumbers.contains(numStr)
                )
            }
             NumberButton(number = "X", onClick = onDeleteClick, isDelete = true, isSelected = false)
        }
    }
}

@Composable
fun NumberButton(
    number: String,
    onClick: () -> Unit,
    isDelete: Boolean = false,
    isSelected: Boolean,
    isDisabled: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        enabled = !isDisabled,
        color = if (isDisabled) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Grayed out
        } else if (isDelete) {
            MaterialTheme.colorScheme.errorContainer 
        } else if (isSelected) {
            MaterialTheme.colorScheme.primary // Darker/Selected color
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDisabled) {
                     Color.Gray
                } else if (isDelete) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else if (isSelected) {
                     MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SudokuAppPreview() {
    SudokoTheme {
        SudokuApp()
    }
}