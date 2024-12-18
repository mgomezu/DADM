package co.edu.unal.reto1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View


class BoardView: View {
    val GRID_WIDTH = 6;

    private lateinit var mHumanBitmap: Bitmap
    private lateinit var mComputerBitmap: Bitmap

    private var mPaint: Paint? = null

    private var mGame: TicTacToeGame? = null
    fun setGame(game: TicTacToeGame?) {
        mGame = game
    }

    fun initialize() {
        mHumanBitmap = BitmapFactory.decodeResource(resources, R.drawable.o_img)
        mComputerBitmap = BitmapFactory.decodeResource(resources, R.drawable.x_img)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint?.color = Color.BLACK

    }

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initialize()
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Determine the width and height of the View
        val boardWidth = width
        val boardHeight = height

        // Make thick, light gray lines
        mPaint?.color = Color.LTGRAY
        mPaint?.strokeWidth = GRID_WIDTH.toFloat()

        // Calculate the dimensions of each cell
        val cellWidth = boardWidth / 3
        val cellHeight = boardHeight / 3

        // Draw the two vertical board lines
        canvas.drawLine(
            cellWidth.toFloat(),
            0f,
            cellWidth.toFloat(),
            boardHeight.toFloat(),
            mPaint!!
        )
        canvas.drawLine(
            (cellWidth * 2).toFloat(),
            0f,
            (cellWidth * 2).toFloat(),
            boardHeight.toFloat(),
            mPaint!!
        )

        // Draw the two horizontal board lines
        canvas.drawLine(
            0f,
            cellHeight.toFloat(),
            boardWidth.toFloat(),
            cellHeight.toFloat(),
            mPaint!!
        )
        canvas.drawLine(
            0f,
            (cellHeight * 2).toFloat(),
            boardWidth.toFloat(),
            (cellHeight * 2).toFloat(),
            mPaint!!
        )

        // Dibuja todas las imágenes X y O
        for (i in 0 until TicTacToeGame.BOARD_SIZE) {
            val col = i % 3
            val row = i / 3

            // Define los límites de un rectángulo de destino para la imagen
            val left = (col * cellWidth).toInt()  // Suponiendo que tienes el valor de cellWidth
            val top = (row * cellHeight).toInt()  // Suponiendo que tienes el valor de cellHeight
            val right = left + cellWidth.toInt()
            val bottom = top + cellHeight.toInt()

            // Verifica si mGame no es null y dibuja la imagen correspondiente
            if (mGame != null) {
                when (mGame!!.getBoardOccupant(i)) {
                    TicTacToeGame.HUMAN_PLAYER -> {
                        // Dibuja la imagen del jugador humano
                        canvas.drawBitmap(mHumanBitmap,null, Rect(left, top, right, bottom),null)
                    }
                    TicTacToeGame.COMPUTER_PLAYER-> {
                        // Dibuja la imagen del jugador computadora
                        canvas.drawBitmap(mComputerBitmap, null, Rect(left, top, right, bottom), null)
                    }
                }
            }
        }
    }

    fun getBoardCellWidth(): Int {
        return width / 3
    }

    fun getBoardCellHeight(): Int {
        return height / 3
    }

}
