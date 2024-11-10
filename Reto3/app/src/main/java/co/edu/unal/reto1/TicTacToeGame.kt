package co.edu.unal.reto1

import java.util.Random

class TicTacToeGame {

    // Characters used to represent the human, computer, and open spots
    companion object {
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
        const val BOARD_SIZE = 9
    }

    private val mBoard: CharArray = CharArray(BOARD_SIZE) { OPEN_SPOT } // Representación del tablero
    private val mRand: Random = Random()
    private var human: Int = 0
    private var tie: Int = 0
    private var android: Int = 0

    init {
        // Este es el constructor que podría inicializar el juego si fuera necesario.
    }

    /** Limpia el tablero de todos los X's y O's, configurando todas las casillas a OPEN_SPOT. */
    fun clearBoard() {
        for (i in mBoard.indices) {
            mBoard[i] = OPEN_SPOT
        }
    }

    /** Establece el movimiento del jugador en la ubicación dada en el tablero.
     *  La ubicación debe estar disponible, o el tablero no se modificará.
     *
     * @param player - El jugador HUMANO o COMPUTADORA
     * @param location - La ubicación (0-8) para realizar el movimiento
     */
    fun setMove(player: Char, location: Int) {
        if (location in 0 until BOARD_SIZE && mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player
        }
    }

    /** Devuelve el mejor movimiento para la computadora.
     *  Debes llamar a setMove() para realizar el movimiento de la computadora en la ubicación.
     * @return El mejor movimiento para la computadora (0-8).
     */
    fun getComputerMove(): Int {
        var move: Int
        do {
            move = mRand.nextInt(BOARD_SIZE)
        } while (mBoard[move] != OPEN_SPOT) // Repite hasta que encuentre un espacio libre
        return move
    }

    /** Verifica si hay un ganador y devuelve un valor de estado indicando quién ha ganado.
     * @return 0 si no hay ganador o empate todavía, 1 si es un empate, 2 si ganó X, o 3 si ganó O.
     */
    fun checkForWinner(): Int {
        // Verificar las filas, columnas y diagonales
        val winningPositions = arrayOf(
            // Filas
            arrayOf(0, 1, 2), arrayOf(3, 4, 5), arrayOf(6, 7, 8),
            // Columnas
            arrayOf(0, 3, 6), arrayOf(1, 4, 7), arrayOf(2, 5, 8),
            // Diagonales
            arrayOf(0, 4, 8), arrayOf(2, 4, 6)
        )

        for (positions in winningPositions) {
            if (mBoard[positions[0]] != OPEN_SPOT &&
                mBoard[positions[0]] == mBoard[positions[1]] &&
                mBoard[positions[0]] == mBoard[positions[2]]
            ) {
                if (mBoard[positions[0]] == HUMAN_PLAYER){
                    human += 1
                    return 2
                }else{
                    android += 1
                    return 3
                }
            }
        }

        // Verificar si hay un empate
        if (mBoard.all { it != OPEN_SPOT }){
            tie += 1
            return 1
        } else {
            return 0
        }
    }

    fun printBoard() {
        for (i in 0 until BOARD_SIZE step 3) {
            println("${mBoard[i]} | ${mBoard[i + 1]} | ${mBoard[i + 2]}")
            if (i < 6) println("---------")
        }
    }

    fun getScoreHuman(): Int{
        return human
    }

    fun getScoreTie(): Int{
        return tie
    }

    fun getScoreAndroid(): Int{
        return android
    }

}
