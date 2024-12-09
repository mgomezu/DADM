package co.edu.unal.reto1

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var mGame: TicTacToeGame

    // Various text displayed
    private lateinit var mInfoTextView: TextView

    //Score Text
    private lateinit var humanLabel: TextView
    private lateinit var tieLabel: TextView
    private lateinit var androidLabel: TextView

    //Number Game
    private var nGame: Int = 0
    private var mHumanWins: Int = 0
    private var mComputerWins: Int = 0
    private var mTies: Int = 0

    private lateinit var selectedOption: String
    private var mGameOver: Boolean = false
    private lateinit var mBoardView: BoardView

    private lateinit var loseSound: MediaPlayer
    private lateinit var winSound: MediaPlayer
    private lateinit var tieSound: MediaPlayer
    private lateinit var mHumanMediaPlayer: MediaPlayer
    private lateinit var mComputerMediaPlayer: MediaPlayer

    private lateinit var mPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);

        // Restore the scores
        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        // Initializing the information text view
        mInfoTextView = findViewById(R.id.information)

        // Obtener las referencias a los TextViews
        humanLabel = findViewById(R.id.humanLabel)
        tieLabel = findViewById(R.id.tieLabel)
        androidLabel = findViewById(R.id.androidLabel)

        mGame = TicTacToeGame()

        mGame.setScoreHuman(mHumanWins)
        mGame.setScoreTie(mTies)
        mGame.setScoreAndroid(mComputerWins)

        mBoardView = findViewById(R.id.board)
        mBoardView.setGame(mGame)
        mBoardView.setOnTouchListener(mTouchListener)

        supportActionBar?.title = "Triki"

        if (savedInstanceState == null) {
            startNewGame();
        }
        else {
            // Restore the game's state
            mGame.setBoardState(savedInstanceState.getCharArray("board") ?: CharArray(9) { ' ' })
            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            mHumanWins = savedInstanceState.getInt("mHumanWins");
            mComputerWins = savedInstanceState.getInt("mComputerWins");
            mTies = savedInstanceState.getInt("mTies");
            nGame = savedInstanceState.getInt("nGame");
            mGame.setScoreHuman(mHumanWins)
            mGame.setScoreTie(mTies)
            mGame.setScoreAndroid(mComputerWins)
        }
        displayScores();

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

            R.id.reset_scores -> {
                resetScores()
                return true
            }

            R.id.quit -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder
                    .setMessage("Are you sure you want to quit?")
                    .setTitle("Quit")
                    .setPositiveButton("Yes") { dialog, which ->
                        finishAffinity()
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
        enableBoardTouch()
        nGame += 1
        mGame.clearBoard();
        mBoardView.invalidate();
        mGameOver = false
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


        val winner = mGame.checkForWinner()

        when (winner) {
            0 -> mInfoTextView.text = "It's your turn."
            1 -> {
                mInfoTextView.text = "It's a tie!"
                mTies+=1
                tieSound.start()
            }
            2 -> {
                mInfoTextView.text = "You won!"
                mHumanWins+=1
                winSound.start()
            }
            3 -> {
                mInfoTextView.text = "Android won!"
                mComputerWins+=1
                loseSound.start()
            }
        }
    }


    // Listen for touches on the board
    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = View.OnTouchListener { v, event ->
        // Determine which cell was touched
        val col = (event.x / mBoardView.getBoardCellWidth()).toInt()
        val row = (event.y / mBoardView.getBoardCellHeight()).toInt()
        val pos = row * 3 + col
        if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
            // If no winner yet, let the computer make a move
            disableBoardTouch()
            var winner = mGame.checkForWinner()
            when (winner) {
                0 -> {
                    mInfoTextView.text = "It's Android's turn."
                    val move = mGame.getComputerMove()

                    Handler(Looper.getMainLooper()).postDelayed({
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    winner = mGame.checkForWinner()
                    enableBoardTouch()
                    when (winner) {
                        0 -> mInfoTextView.text = "It's your turn."
                        1 -> {
                            mGameOver = true
                            mInfoTextView.text = "It's a tie!"
                            mTies+=1
                            tieSound.start()
                        }
                        2 -> {
                            mGameOver = true
                            mInfoTextView.text = "You won!"
                            mHumanWins+=1
                            winSound.start()
                        }
                        3 -> {
                            mGameOver = true
                            mInfoTextView.text = "Android won!"
                            mComputerWins+=1
                            loseSound.start()
                        }
                    }

                    }, 2000)
                }
                1 -> {
                    mGameOver = true
                    mInfoTextView.text = "It's a tie!"
                    mTies+=1
                    tieSound.start()
                }
                2 -> {
                    mGameOver = true
                    mInfoTextView.text = "You won!"
                    mHumanWins+=1
                    winSound.start()
                }
                3 -> {
                    mGameOver = true
                    mInfoTextView.text = "Android won!"
                    mComputerWins+=1
                    loseSound.start()
                }
            }

        }
        false
    }

    private fun disableBoardTouch() {
        mBoardView.setOnTouchListener { _, _ -> true }  // Deshabilitar las interacciones táctiles
    }

    private fun enableBoardTouch() {
        mBoardView.setOnTouchListener(mTouchListener)  // Restaurar el listener original
    }

    private fun setMove(player: Char, pos: Int): Boolean {
        if(mGame.setMove(player,pos)){
            if (player==TicTacToeGame.HUMAN_PLAYER){
                mHumanMediaPlayer.start();
            }else{
                mComputerMediaPlayer.start()
            }
            mBoardView.invalidate();
            return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        mHumanMediaPlayer = MediaPlayer.create(applicationContext, R.raw.human)
        mComputerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.computer)
        loseSound = MediaPlayer.create(applicationContext, R.raw.gameover)
        winSound = MediaPlayer.create(applicationContext, R.raw.winning)
        tieSound = MediaPlayer.create(applicationContext, R.raw.tie)
    }

    override fun onPause() {
        super.onPause()
        mHumanMediaPlayer.release()
        mComputerMediaPlayer.release()
        loseSound.release()
        winSound.release()
        tieSound.release()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharArray("board", mGame.getBoardState())
        outState.putBoolean("mGameOver", mGameOver)
        outState.putInt("mHumanWins", mGame.getScoreHuman())
        outState.putInt("mComputerWins", mGame.getScoreAndroid())
        outState.putInt("mTies", mGame.getScoreTie())
        outState.putCharSequence("info", mInfoTextView.text)
        outState.putInt("nGame", nGame)
    }

    private fun displayScores() {
        humanLabel.setText("Human: " + mHumanWins.toString())
        androidLabel.setText("Android: " + mComputerWins.toString())
        tieLabel.setText("Tie: " + mTies.toString())
        if(nGame%2==0){
            mInfoTextView.setText("Android goes first")
            setMoveFirstComputer()
        }else{
            mInfoTextView.setText("You go first")
        }
    }

    override fun onStop() {
        super.onStop()
        // Save the current scores
        val ed = mPrefs.edit()
        ed.putInt("mHumanWins", mHumanWins)
        ed.putInt("mComputerWins", mComputerWins)
        ed.putInt("mTies", mTies)
        ed.apply()
    }

    fun resetScores(){
        mHumanWins = 0
        mComputerWins = 0
        mTies =0

        mGame.setScoreHuman(mHumanWins)
        mGame.setScoreTie(mTies)
        mGame.setScoreAndroid(mComputerWins)

        humanLabel.text = "Human: ${mGame.getScoreHuman()}"
        tieLabel.text = "Tie: ${mGame.getScoreTie()}"
        androidLabel.text = "Android: ${mGame.getScoreAndroid()}"
    }
}

