package co.edu.unal.reto1

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class GameActivity : AppCompatActivity() {

    private lateinit var selectedGameRef: DatabaseReference
    private val gameId = "game1" // Reemplaza con tu lógica para identificar el juego

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = FirebaseDatabase.getInstance()
        selectedGameRef = database.getReference("games").child(gameId)

        // Escuchar actualizaciones del estado del juego
        listenGameUpdates()

        // Llamar para realizar tu primer movimiento (ejemplo)
        makeMove(4, "X") // El jugador marca la posición 4
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
        selectedGameRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val gameState = currentData.getValue(Game::class.java)

                if (gameState != null && gameState.currentTurn == "player1") {
                    val updatedBoard = gameState.boardState.toMutableList()
                    updatedBoard[position] = playerSymbol

                    gameState.boardState = updatedBoard
                    gameState.currentTurn = "player2"

                    currentData.value = gameState
                    return Transaction.success(currentData)
                }

                return Transaction.abort()
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
}
