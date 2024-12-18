package co.edu.unal.reto1

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener


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
    private var isUpdating = false


    private lateinit var selectedGameRef: DatabaseReference
    private var gameId = "game1" // Reemplaza con tu lógica para identificar el juego
    // Referencia a Firebase
    private lateinit var gamesRef: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var gamesList: MutableList<String>
    private lateinit var gameKeysList: MutableList<String> // Guarda las claves de los juegos

    private var player = "player1"
    private var currentplayer = "player1"

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

        // Inicializa Firebase
        gamesRef = FirebaseDatabase.getInstance().getReference("games")

        // Configura la lista de juegos
        listView = findViewById(R.id.listViewGames)
        gamesList = mutableListOf()
        gameKeysList = mutableListOf()

        // Cargar lista de juegos disponibles
        loadAvailableGames()

        // Manejar la selección de un juego
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedGameKey = gameKeysList[position]
            joinGame(selectedGameKey)
            gameId = selectedGameKey
        }

        val database = FirebaseDatabase.getInstance()
        selectedGameRef = database.getReference("games").child(gameId)

        // Escuchar actualizaciones del estado del juego
        listenGameUpdates()

        mGame.setScoreHuman(mHumanWins)
        mGame.setScoreTie(mTies)
        mGame.setScoreAndroid(mComputerWins)

        mBoardView = findViewById(R.id.board)
        mBoardView.setGame(mGame)
        mBoardView.setOnTouchListener(mTouchListener)

        supportActionBar?.title = "Triki"

        if (savedInstanceState == null) {
            //startNewGame();
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

        //mInfoTextView.visibility = View.VISIBLE
        findViewById<ListView>(R.id.listViewGames).visibility = View.GONE
        player = "player1"
        currentplayer = "player1"
        val creatorId = "player1" // Aquí puedes obtener el ID dinámico del jugador (opcional)
        mGame.clearBoard() // Método que limpia el estado interno del tablero
        mBoardView.invalidate() // Refresca la vista del tablero
        mGameOver = false
        findViewById<TextView>(R.id.noGamesText).visibility = View.GONE
        // Crea el nuevo juego
        val newGame = Game()
        newGame.status = "waiting"
        newGame.currentTurn = currentplayer
        // Crea una referencia en Firebase para el nuevo juego
        val newGameKey = gamesRef.push().key ?: return
        gameId = newGameKey
            gamesRef.child(newGameKey).setValue(newGame)
            .addOnSuccessListener {
                Log.d("Firebase", "Nuevo juego creado con ID: $newGameKey")
                mBoardView.visibility = View.VISIBLE

            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al crear el juego", e)
                Toast.makeText(this, "No se pudo crear el juego.", Toast.LENGTH_SHORT).show()
            }

        actualizar()
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
        Log.d("player", player)
        Log.d("currentplayer", currentplayer)
        val col = (event.x / mBoardView.getBoardCellWidth()).toInt()
        val row = (event.y / mBoardView.getBoardCellHeight()).toInt()
        val pos = row * 3 + col
        if(currentplayer  == player){
            val result = if (player == "player1") TicTacToeGame.HUMAN_PLAYER else TicTacToeGame.COMPUTER_PLAYER
            if (!mGameOver && setMove(result, pos)) {
                //disableBoardTouch()
                //enableBoardTouch()
                makeMove(pos,result+"")
                var winner = mGame.checkForWinner()
                when (winner) {

                    1 -> {
                        mGameOver = true
                        mInfoTextView.text = "It's a tie!"
                        //mInfoTextView.visibility  = View.VISIBLE
                        mTies+=1
                        tieSound.start()
                    }
                    2 -> {
                        mGameOver = true
                        if(player == "player1") mInfoTextView.text = "You won!" else mInfoTextView.text = "You lose!"
                        //mInfoTextView.visibility  = View.VISIBLE
                        mHumanWins+=1
                        winSound.start()
                    }
                    3 -> {
                        mGameOver = true
                        if(player == "player1") mInfoTextView.text = "You lose!" else mInfoTextView.text = "You won!"
                        //mInfoTextView.visibility  = View.VISIBLE
                        mComputerWins+=1
                        loseSound.start()
                    }
                }

            }
        }
        true
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

    private fun loadAvailableGames() {
        gamesRef.orderByChild("status").equalTo("waiting")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    gamesList.clear()
                    gameKeysList.clear()

                    if (snapshot.exists()) {
                        // Si hay datos, actualizamos la lista de juegos
                        for (game in snapshot.children) {
                            val gameId = game.key
                            val creator = game.child("creator").getValue(String::class.java)
                            val displayText = "Nuevo Juego creado"

                            gamesList.add(displayText)
                            gameKeysList.add(gameId!!)
                        }

                        val adapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_list_item_1,
                            gamesList
                        )
                        listView.adapter = adapter

                        findViewById<TextView>(R.id.noGamesText).visibility = View.GONE
                    } else {
                        // Si no hay datos, mostramos el mensaje
                        findViewById<TextView>(R.id.noGamesText).visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error al obtener juegos disponibles", error.toException())
                }
            })
    }


    private fun joinGame(gameId: String) {
       // mInfoTextView.visibility = View.VISIBLE
        player = "player2"

        findViewById<ListView>(R.id.listViewGames).visibility = View.GONE
        mGame.clearBoard()
        mBoardView.invalidate()
        mGameOver = false
        findViewById<TextView>(R.id.noGamesText).visibility = View.GONE
        mBoardView.visibility = View.VISIBLE

        val selectedGameRef = gamesRef.child(gameId)

        selectedGameRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val game = currentData.getValue(Game::class.java)

                if (game?.opponent == null) {
                    game?.opponent = "player2"
                    game?.status = "inProgress"
                    currentData.value = game
                    return Transaction.success(currentData)
                } else {
                    return Transaction.abort()
                }
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (committed) {
                    Log.d("Firebase", "Te uniste al juego")
                    actualizar()  // Llamar a actualizar después de unirte al juego
                } else {
                    Log.e("Firebase", "No se pudo unir al juego")
                }
            }
        })
    }


    private fun listenGameUpdates() {
        selectedGameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gameState = snapshot.getValue(Game::class.java)
                if (gameState != null) {
                    Log.d("Firebase", "Tablero: ${gameState.boardState}")
                    Log.d("Firebase", "Turno actual: ${gameState.currentTurn}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al obtener el estado del juego", error.toException())
            }
        })
    }

    private fun makeMove(position: Int, playerSymbol: String) {

        val selectedGameRef = gamesRef.child(gameId)
        selectedGameRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val game = currentData.getValue(Game::class.java)
                if (game != null) {
                    if(currentplayer  == player) {
                        if (currentplayer == "player2") {
                            game?.currentTurn = "player1"
                            currentplayer = "player1"
                            Log.d("Firebase", "cambiado a 1")
                        } else {
                            game?.currentTurn = "player2"
                            currentplayer = "player2"
                            Log.d("Firebase", "cambiado a 2")
                        }
                    }
                }

                val updatedBoard = game?.boardState?.toMutableList()
                updatedBoard?.set(position, playerSymbol)
                if (updatedBoard != null) {
                    game.boardState = updatedBoard
                }else{
                    Log.d("Firebase", "Movimiento no exitoso")
                }


                currentData.value = game
                return Transaction.success(currentData)

            }


            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (committed) Log.d("Firebase", "Movimiento exitoso")
            }
        })
    }

    private fun actualizar() {
        Log.d("Firebase", "Ejecutando actualizar()")

        val gamesRef = FirebaseDatabase.getInstance().getReference("games").child(gameId)

        gamesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gameState = snapshot.getValue(Game::class.java)

                if (gameState != null) {
                    // Actualizar el estado del tablero en tu clase mGame
                    mGame.updateBoardFromDatabase(gameState.boardState)
                    mBoardView.invalidate()  // Actualizar la vista del tablero

                    // Actualizar el turno actual del juego
                    currentplayer = gameState.currentTurn.toString()
                    Log.d("Firebase", "Turno actual: $currentplayer")
                    mInfoTextView.text = if (currentplayer == "player1") "Your Turn" else "Opponent's Turn"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al leer datos: ${error.message}")
                isUpdating = false
            }
        })
    }



    private fun printBoard() {
        val board = mGame.getBoardState()
        val boardFormatted = """
        ${board[0]} | ${board[1]} | ${board[2]}
        ---------
        ${board[3]} | ${board[4]} | ${board[5]}
        ---------
        ${board[6]} | ${board[7]} | ${board[8]}
    """.trimIndent()
        Log.d("GameBoard", "\n$boardFormatted")
    }


}

