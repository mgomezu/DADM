package co.edu.unal.reto1

import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView


// Handles clicks on the game board buttons
class ButtonClickListener(private val location: Int, private val mBoardButtons: Array<Button>, private val mGame: TicTacToeGame, private val mInfoTextView: TextView) : View.OnClickListener {

    override fun onClick(view: View?) {
        if (mBoardButtons[location].isEnabled) {
            setMove(TicTacToeGame.HUMAN_PLAYER, location)

            // If no winner yet, let the computer make a move
            var winner = mGame.checkForWinner()
            if (winner == 0) {
                mInfoTextView.text = "It's Android's turn."
                val move = mGame.getComputerMove()
                setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                winner = mGame.checkForWinner()
            }

            when (winner) {
                0 -> mInfoTextView.text = "It's your turn."
                1 -> {
                    gameOver()
                    mInfoTextView.text = "It's a tie!"
                }
                2 -> {
                    gameOver()
                    mInfoTextView.text = "You won!"}
                3 -> {
                    gameOver()
                    mInfoTextView.text = "Android won!"}
            }
        }
    }

    private fun setMove(player: Char, location: Int) {
        mGame.setMove(player, location)
        mBoardButtons[location].isEnabled = false
        mBoardButtons[location].text = player.toString()
        if (player == TicTacToeGame.HUMAN_PLAYER) mBoardButtons[location].setTextColor(
            Color.rgb(
                0,
                200,
                0
            )
        )
        else mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0))
    }


    fun gameOver(){
        for (boton in mBoardButtons) {
            boton.isEnabled = false
        }
    }

}
