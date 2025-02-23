package com.example.ia4_q2

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ia4_q2.ui.theme.Ia4_q2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Ia4_q2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Hangman(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

val words = listOf("APPLE", "ELEPHANT", "PYRAMID", "OCEAN", "BASKETBALL")

val wordsWithHints = mapOf(
    "APPLE" to "FOOD",
    "ELEPHANT" to "ANIMAL",
    "PYRAMID" to "ANCIENT STRUCTURE",
    "OCEAN" to "LARGE BODY OF WATER",
    "BASKETBALL" to "SPORT",
)

@Composable
fun Hangman(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var selectedWord by rememberSaveable { mutableStateOf(words.random()) }
    var guessedLetters by rememberSaveable { mutableStateOf(setOf<Char>()) }
    var wrongGuesses by rememberSaveable { mutableIntStateOf(0) }
    var hintClicks by rememberSaveable { mutableIntStateOf(0) }
    var hintText by rememberSaveable { mutableStateOf("") }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val maxWrongGuesses = 6
    val context = LocalContext.current

    //reset all saved game data
    fun resetGame() {
        selectedWord = words.random()
        guessedLetters = setOf()
        wrongGuesses = 0
        hintClicks = 0
        hintText = ""
        showDialog = false
    }

    //show dialog when game is won or lost
    LaunchedEffect(wrongGuesses, guessedLetters) {
        if (wrongGuesses >= maxWrongGuesses || selectedWord.all { it in guessedLetters }) {
            showDialog = true
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(if (wrongGuesses >= maxWrongGuesses) "Game Over" else "You Win!") },
            text = { Text("The word was: $selectedWord") },
            confirmButton = {
                Button(onClick = { resetGame() }) {
                    Text("New Game")
                }
            }
        )
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LetterPanel(
                        modifier = Modifier,
                        selectedWord, guessedLetters, wrongGuesses,
                        onLetterClick = { letter ->
                            guessedLetters = guessedLetters + letter
                            if (letter !in selectedWord) wrongGuesses++
                        }, 5, isLandscape
                    )

                    HintPanel(modifier = Modifier.weight(1f), hintClicks, onHintClick = {
                        //implement all hint rules
                        if (wrongGuesses >= maxWrongGuesses || (hintClicks > 0 && maxWrongGuesses - wrongGuesses == 1)) {
                            Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
                        } else {
                        if (hintClicks == 0) {
                            //search for hint by hash index and display text hint
                            val hint = wordsWithHints[selectedWord]
                            hintText = "Hint: $hint"
                        } else if (hintClicks == 1) {
                            //disable half letters that arent in the word
                            val remainingLetters = ('A'..'Z').filter { it !in guessedLetters && it !in selectedWord }
                            val halfToDisable = remainingLetters.shuffled().take(remainingLetters.size / 2)
                            guessedLetters = guessedLetters + halfToDisable
                            wrongGuesses++
                        } else if (hintClicks == 2) {
                            //show all vowels
                            val vowels = listOf('A', 'E', 'I', 'O', 'U')
                            guessedLetters = guessedLetters + vowels
                            wrongGuesses++
                        }
                            hintClicks++
                        }
                    }, hintText)
                }

                GamePanel(
                    modifier = Modifier.weight(1f),
                    selectedWord,
                    guessedLetters, wrongGuesses, maxWrongGuesses,
                    onReset = { resetGame() }, isLandscape
                )
            }
        }
        else {
            Column(modifier = Modifier.fillMaxSize()) {
                GamePanel(modifier.fillMaxWidth(), selectedWord, guessedLetters, wrongGuesses, maxWrongGuesses, onReset = { resetGame() }, isLandscape)
                LetterPanel(modifier, selectedWord, guessedLetters, wrongGuesses, onLetterClick = {
                    guessedLetters = guessedLetters + it
                    if (it !in selectedWord) wrongGuesses++
                }, 7, isLandscape)
            }
        }
    }
}

@Composable
fun LetterPanel(modifier: Modifier, word: String, guessedLetters: Set<Char>, wrongGuesses: Int, onLetterClick: (Char) -> Unit, chunk: Int, landscape: Boolean) {
    val letters = ('A'..'Z').toList()
    var mod = modifier
    if (!landscape) {
        mod = mod.fillMaxSize()
    }
    Column(modifier = mod
        .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
        {
        Text("Choose a letter:", fontSize = 20.sp)
        Row (modifier = Modifier, horizontalArrangement = Arrangement.Center) {
            letters.chunked(chunk).forEach { row ->
                Column {
                    row.forEach { letter ->
                        Button(
                            onClick = { onLetterClick(letter) },
                            enabled = letter !in guessedLetters,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(letter.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HintPanel(modifier: Modifier, hintClicks: Int, onHintClick: () -> Unit, hintText: String) {
    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        horizontalArrangement = Arrangement.Center){
        Button (onClick = onHintClick, enabled = hintClicks < 3) { Text("Hint") }
        Text(hintText, modifier = Modifier.padding(10.dp))
    }
}

@Composable
fun GamePanel(modifier: Modifier, word: String, guessedLetters: Set<Char>, wrongGuesses: Int, maxWrong: Int, onReset: () -> Unit, landscape: Boolean) {
    var mod = modifier
    if (!landscape) {
        mod = modifier.fillMaxWidth()
    } else {
        mod = modifier.fillMaxHeight()
    }
    Column(modifier = mod.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Button(onClick = onReset) { Text("New Game") }
        //cycle through hangman images depending on num of wrong guesses
        val image = when (wrongGuesses) {
            0 -> R.drawable.h0
            1 -> R.drawable.h1
            2 -> R.drawable.h2
            3 -> R.drawable.h3
            4 -> R.drawable.h4
            5 -> R.drawable.h5
            6 -> R.drawable.h6
            else -> R.drawable.h0
        }
        Image(modifier = Modifier.padding(8.dp) ,painter = painterResource(image), contentDescription = null)
        Text("Word: ${word.map { if (it in guessedLetters) it else '_' }.joinToString(" ")}", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        Text("Wrong guesses: $wrongGuesses/$maxWrong", fontSize = 18.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Ia4_q2Theme {
        Hangman()
    }
}