package co.edu.unal.reto1

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var mGame: TicTacToeGame

    // Buttons making up the board
    private lateinit var mBoardButtons: Array<Button>

    // Various text displayed
    private lateinit var mInfoTextView: TextView

    //Score Text
    private lateinit var humanLabel: TextView
    private lateinit var tieLabel: TextView
    private lateinit var androidLabel: TextView

    //Number Game
    private var nGame: Int = 0

    private lateinit var selectedOption: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initializing the board buttons array
        mBoardButtons = arrayOf(
            findViewById(R.id.one),
            findViewById(R.id.two),
            findViewById(R.id.three),
            findViewById(R.id.four),
            findViewById(R.id.five),
            findViewById(R.id.six),
            findViewById(R.id.seven),
            findViewById(R.id.eight),
            findViewById(R.id.nine)
        )

        // Initializing the information text view
        mInfoTextView = findViewById(R.id.information)

        // Obtener las referencias a los TextViews
        humanLabel = findViewById(R.id.humanLabel)
        tieLabel = findViewById(R.id.tieLabel)
        androidLabel = findViewById(R.id.androidLabel)

        mGame = TicTacToeGame()

        supportActionBar?.title = "Triki"

        startNewGame();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_game -> {
                startNewGame()
                return true
            }

            R.id.ai_difficulty -> {
                val options = arrayOf("Easy", "Harder", "Expert")
                var selectedOptionIndex = 0 // Variable para almacenar el índice seleccionado

                val builder = AlertDialog.Builder(this)
                builder
                    .setTitle("Choose a difficulty level:")
                    .setSingleChoiceItems(options, selectedOptionIndex) { _, which ->
                        selectedOptionIndex = which // Actualiza el índice seleccionado
                    }
                    .setPositiveButton("OK") { dialog, _ ->

                        selectedOption = options[selectedOptionIndex]
                        Toast.makeText(this, "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
                        mGame.setDifficultyLevel(selectedOption)
                        startNewGame()
                        dialog.dismiss()

                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                val dialog = builder.create()
                dialog.show()

                return true
            }

            R.id.quit -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setMessage("Are you sure you want to quit?")
                    .setTitle("Quit")
                    .setPositiveButton("Yes") { dialog, which ->
                        this.onDestroy()
                    }
                    .setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
                return true
            }
        }
        return false
    }

    // Set up the game board.
    fun startNewGame() {

        nGame += 1
        mGame.clearBoard();

        // Reset all buttons
        for ((index, boton) in mBoardButtons.withIndex()) {
            boton.text = ""
            boton.isEnabled = true
            boton.setOnClickListener(ButtonClickListener(index,mBoardButtons,mGame,mInfoTextView))  // Pasamos el índice correcto
        }

        humanLabel.text = "Human: ${mGame.getScoreHuman()}"
        tieLabel.text = "Tie: ${mGame.getScoreTie()}"
        androidLabel.text = "Android: ${mGame.getScoreAndroid()}"

        if(nGame%2==0){
            mInfoTextView.setText("Android goes first")
            setMoveFirstComputer()
        }else{
            mInfoTextView.setText("You go first")
        }

    }

    private fun setMoveFirstComputer() {
        val move = mGame.getComputerMove()
        mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
        mBoardButtons[move].isEnabled = false
        mBoardButtons[move].text = TicTacToeGame.COMPUTER_PLAYER.toString()
        if (TicTacToeGame.COMPUTER_PLAYER == TicTacToeGame.HUMAN_PLAYER) mBoardButtons[move].setTextColor(
            Color.rgb(
                0,
                200,
                0
            )
        )
        else mBoardButtons[move].setTextColor(Color.rgb(200, 0, 0))


        val winner = mGame.checkForWinner()

        when (winner) {
            0 -> mInfoTextView.text = "It's your turn."
            1 -> {
                for (boton in mBoardButtons) {
                    boton.isEnabled = false
                }
                mInfoTextView.text = "It's a tie!"
            }
            2 -> {
                for (boton in mBoardButtons) {
                    boton.isEnabled = false
                }
                mInfoTextView.text = "You won!"}
            3 -> {
                for (boton in mBoardButtons) {
                    boton.isEnabled = false
                }
                mInfoTextView.text = "Android won!"}
        }
    }
}

