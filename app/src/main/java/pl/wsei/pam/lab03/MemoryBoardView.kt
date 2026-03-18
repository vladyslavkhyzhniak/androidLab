package pl.wsei.pam.lab03

import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import pl.wsei.pam.lab01.R
import java.util.*

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()
    private val icons: List<Int> = listOf(
        R.drawable.baseline_rocket_24,
        R.drawable.baseline_audiotrack_24,
        R.drawable.baseline_3d_rotation_24,
        R.drawable.outline_4g_mobiledata_badge_24,
        R.drawable.outline_30fps_select_24,
        R.drawable.outline_ads_click_24,
    )
    private val deckResource: Int = R.drawable.deck
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = {}
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    init {
        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            val numPairs = (cols * rows) / 2
            val iconsToUse = icons.take(numPairs)
            it.addAll(iconsToUse)
            it.addAll(iconsToUse)
            it.shuffle()
        }

        gridLayout.columnCount = cols
        gridLayout.rowCount = rows

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"
                    val layoutParams = GridLayout.LayoutParams()
                    layoutParams.width = 0
                    layoutParams.height = 0
                    layoutParams.setGravity(Gravity.CENTER)
                    layoutParams.columnSpec = GridLayout.spec(col, 1, 1f)
                    layoutParams.rowSpec = GridLayout.spec(row, 1, 1f)
                    it.layoutParams = layoutParams
                    gridLayout.addView(it)
                }
                addTile(btn, shuffledIcons.removeAt(0))
            }
        }
    }

    private fun onClickTile(v: View) {
        val tile = tiles[v.tag] ?: return
        if (tile.revealed) return

        matchedPair.push(tile)
        val matchResult = logic.process { tile.tileResource }
        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))
        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    fun getState(): IntArray {
        val state = IntArray(rows * cols)
        var i = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles["${row}x${col}"]
                state[i] = if (tile?.revealed == true) tile.tileResource else -1
                i++
            }
        }
        return state
    }

    fun setState(state: IntArray) {
        var i = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles["${row}x${col}"]
                if (state[i] != -1) {
                    tile?.revealed = true
                }
                i++
            }
        }
    }
}