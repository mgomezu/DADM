package co.edu.unal.reto1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class GameListActivity : AppCompatActivity() {

    // Referencia a Firebase
    private lateinit var gamesRef: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var gamesList: MutableList<String>
    private lateinit var gameKeysList: MutableList<String> // Guarda las claves de los juegos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Layout para la lista de juegos

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
        }
    }

    private fun loadAvailableGames() {
        gamesRef.orderByChild("status").equalTo("waiting")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    gamesList.clear()
                    gameKeysList.clear()

                    if (snapshot.exists()) {
                        println("Si")
                        // Si hay datos, actualizamos la lista de juegos
                        for (game in snapshot.children) {
                            val gameId = game.key
                            val creator = game.child("creator").getValue(String::class.java)
                            val displayText = "Juego creado por: $creator"

                            gamesList.add(displayText)
                            gameKeysList.add(gameId!!)
                        }

                        val adapter = ArrayAdapter(
                            this@GameListActivity,
                            android.R.layout.simple_list_item_1,
                            gamesList
                        )
                        listView.adapter = adapter

                        findViewById<TextView>(R.id.noGamesText).visibility = View.GONE
                    } else {
                        println("No")
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
        val selectedGameRef = gamesRef.child(gameId)

        selectedGameRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val game = currentData.getValue(Game::class.java)

                if (game?.opponent == null) {
                    game?.opponent = "player2" // Aquí puedes asignar un ID dinámico del jugador
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
                    val intent = Intent(this@GameListActivity, GameActivity::class.java)
                    intent.putExtra("gameId", gameId)
                    startActivity(intent)
                } else {
                    Log.e("Firebase", "No se pudo unir al juego. Ya tiene un oponente.")
                }
            }
        })
    }
}
