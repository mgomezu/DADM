package co.edu.unal.reto1

import android.view.View
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

    // The computer's difficulty levels
    enum class DifficultyLevel {
        Easy, Harder, Expert
    };

    // Current difficulty level
    private var mDifficultyLevel = DifficultyLevel.Expert

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
    fun setMove(player: Char, location: Int):Boolean {
        if (location in 0 until BOARD_SIZE && mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player
            return true
        }else{
            return false
        }
    }

    fun getRandomMove(): Int {
        var move: Int
        do {
            move = mRand.nextInt(BOARD_SIZE)
        } while (mBoard[move] != OPEN_SPOT) // Repite hasta que encuentre un espacio libre
        return move
    }

    fun getWinningMove(): Int {
        var move: Int
        move = -1
        val winningPositions = arrayOf(
            // Filas
            arrayOf(0, 1, 2), arrayOf(3, 4, 5), arrayOf(6, 7, 8),
            // Columnas
            arrayOf(0, 3, 6), arrayOf(1, 4, 7), arrayOf(2, 5, 8),
            // Diagonales
            arrayOf(0, 4, 8), arrayOf(2, 4, 6)
        )

        for (positions in winningPositions) {

            if(mBoard[positions[0]] == mBoard[positions[1]] && mBoard[positions[0]]==COMPUTER_PLAYER && mBoard[positions[2]] == OPEN_SPOT){
                return positions[2]
            }
            if(mBoard[positions[0]] == mBoard[positions[2]] && mBoard[positions[0]]==COMPUTER_PLAYER && mBoard[positions[1]] == OPEN_SPOT){
                return positions[1]
            }
            if(mBoard[positions[2]] == mBoard[positions[1]] && mBoard[positions[1]]==COMPUTER_PLAYER  && mBoard[positions[0]] == OPEN_SPOT){
                return positions[0]
            }
        }

        return move
    }

    fun getBlockingMove(): Int {
        var move: Int
        move = -1
        val winningPositions = arrayOf(
            // Filas
            arrayOf(0, 1, 2), arrayOf(3, 4, 5), arrayOf(6, 7, 8),
            // Columnas
            arrayOf(0, 3, 6), arrayOf(1, 4, 7), arrayOf(2, 5, 8),
            // Diagonales
            arrayOf(0, 4, 8), arrayOf(2, 4, 6)
        )

        for (positions in winningPositions) {

            if(mBoard[positions[0]] == mBoard[positions[1]] && mBoard[positions[0]]==HUMAN_PLAYER && mBoard[positions[2]] == OPEN_SPOT){
                return positions[2]
            }
            if(mBoard[positions[0]] == mBoard[positions[2]] && mBoard[positions[0]]==HUMAN_PLAYER && mBoard[positions[1]] == OPEN_SPOT){
                return positions[1]
            }
            if(mBoard[positions[2]] == mBoard[positions[1]] && mBoard[positions[1]]==HUMAN_PLAYER && mBoard[positions[0]] == OPEN_SPOT){
                return positions[0]
            }
        }

        return move
    }

    /** Devuelve el mejor movimiento para la computadora.
     *  Debes llamar a setMove() para realizar el movimiento de la computadora en la ubicación.
     * @return El mejor movimiento para la computadora (0-8).
     */
    fun getComputerMove(): Int {
        var move: Int
        move = -1

        if (mDifficultyLevel == DifficultyLevel.Easy) {
            // Movimiento aleatorio
            move = getRandomMove()
        } else if (mDifficultyLevel == DifficultyLevel.Harder) {
            // Movimiento ganador, si no se puede ganar, se elige uno aleatorio
            move = getWinningMove()
            if (move == -1) {
                move = getRandomMove()
            }
        } else if (mDifficultyLevel == DifficultyLevel.Expert) {
            // Primer intenta ganar, luego bloquear, y si no se puede, juega aleatoriamente
            move = getWinningMove()
            if (move == -1) {
                move = getBlockingMove()
            }
            if (move == -1) {
                move = getRandomMove()
            }
        }
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


    fun getScoreHuman(): Int{
        return human
    }

    fun getScoreTie(): Int{
        return tie
    }

    fun getScoreAndroid(): Int{
        return android
    }

    fun setScoreHuman(scoreHuman: Int){
        human = scoreHuman
    }

    fun setScoreTie(scoreTie: Int){
        tie = scoreTie
    }

    fun setScoreAndroid(scoreAndroid: Int){
        android = scoreAndroid
    }


    fun getDifficultyLevel(): DifficultyLevel {
        return mDifficultyLevel
    }

    fun setDifficultyLevel(selectedOption: String) {
        when(selectedOption){
            "Easy" -> {
                mDifficultyLevel =  DifficultyLevel.Easy
            }

            "Harder" -> {
                mDifficultyLevel =  DifficultyLevel.Harder
            }
            "Expert" -> {
                mDifficultyLevel =  DifficultyLevel.Expert
            }

        }
    }

    fun getBoardOccupant(index: Int): Char {
        // Verificar si el índice es válido
        if (index in 0..8) {
            return mBoard[index]
        }
        // Si el índice es inválido, retornar OPEN_SPOT o cualquier otro valor de error
        return OPEN_SPOT
    }

    fun getBoardState(): CharArray{
        return mBoard.copyOf()
    }

    fun setBoardState(newBoard: CharArray){
        if (newBoard.size == mBoard.size) {
            newBoard.copyInto(mBoard) // Copia los valores al tablero actual
        } else {
            throw IllegalArgumentException("El tamaño del nuevo tablero no coincide con el tamaño esperado.")
        }
    }

    // Método para actualizar el tablero desde Firebase
    fun updateBoardFromDatabase(boardState: List<String>) {
        for (i in 0 until BOARD_SIZE) {
            mBoard[i] = when (boardState[i]) {
                "X" -> HUMAN_PLAYER
                "O" -> COMPUTER_PLAYER
                else -> OPEN_SPOT
            }
        }
    }


}
