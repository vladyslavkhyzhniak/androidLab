package pl.wsei.pam.lab03

import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R
import java.util.*
import kotlin.concurrent.schedule

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        val columns = intent.getIntExtra("columns", 3)
        val rows = intent.getIntExtra("rows", 4)

        mBoard = findViewById(R.id.lab03_grid_layout)

        mBoardModel = MemoryBoardView(mBoard, columns, rows)
        if (savedInstanceState != null) {
            val savedState = savedInstanceState.getIntArray("game_state")
            if (savedState != null) {
                mBoardModel.setState(savedState)
            }
        }

        mBoardModel.setOnGameChangeListener { e ->
            runOnUiThread {
                when (e.state) {
                    GameStates.Matching -> {
                        e.tiles.forEach { it.revealed = true }
                    }
                    GameStates.Match -> {
                        e.tiles.forEach {
                            it.revealed = true
                            it.removeOnClickListener()
                        }
                    }
                    GameStates.NoMatch -> {
                        e.tiles.forEach { it.revealed = true }
                        Timer().schedule(1000) {
                            runOnUiThread {
                                e.tiles.forEach { it.revealed = false }
                            }
                        }
                    }
                    GameStates.Finished -> {
                        e.tiles.forEach { it.revealed = true }
                        Toast.makeText(this, "Gratulacje!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("game_state", mBoardModel.getState())
    }
}