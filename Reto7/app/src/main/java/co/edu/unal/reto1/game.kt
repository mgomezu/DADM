package co.edu.unal.reto1

data class Game(
    var creator: String? = null,             // ID del jugador creador
    var opponent: String? = null,           // ID del oponente
    var boardState: List<String> = List(9) { " " },  // Estado del tablero 3x3
    var currentTurn: String? = null,        // Turno actual ("player1" o "player2")
    var status: String? = null,             // Estado del juego ("waiting", "inProgress", "finished")
    var winner: String? = null              // Ganador del juego ("player1", "player2" o null)
){
    override fun toString(): String {
        return "Game(, status='$status', currentTurn='$currentTurn')"
    }
}