package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.recordit.pam.lab06.R
import java.util.*

class Lab03Activity : AppCompatActivity() {
    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView
    private lateinit var completionPlayer: MediaPlayer
    private lateinit var negativePlayer: MediaPlayer
    private var isSound: Boolean = true

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
                        mBoard.isEnabled = false
                        if (isSound) completionPlayer.start()

                        e.tiles.forEach { tile ->
                            tile.revealed = true
                            animatePairedButton(tile.button) {
                                tile.removeOnClickListener()
                                mBoard.isEnabled = true
                            }
                        }
                    }
                    GameStates.NoMatch -> {
                        mBoard.isEnabled = false
                        if (isSound) negativePlayer.start()

                        e.tiles.forEach { it.revealed = true }
                        var finishedCount = 0
                        e.tiles.forEach { tile ->
                            animateNoMatch(tile.button) {
                                finishedCount++
                                if (finishedCount == e.tiles.size) {
                                    e.tiles.forEach { it.revealed = false }
                                    mBoard.isEnabled = true
                                }
                            }
                        }
                    }
                    GameStates.Finished -> {
                        if (isSound) completionPlayer.start()
                        e.tiles.forEach { tile ->
                            tile.revealed = true
                            animatePairedButton(tile.button) {
                                Toast.makeText(this, "Gratulacje! Koniec gry", Toast.LENGTH_LONG).show()
                                mBoard.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("game_state", mBoardModel.getState())
    }

    private fun animatePairedButton(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val random = Random()
        button.pivotX = random.nextFloat() * 200f
        button.pivotY = random.nextFloat() * 200f

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 0f, 1080f)
        val scalingX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 4f)
        val scalingY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 4f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

        set.duration = 1000
        set.interpolator = android.view.animation.DecelerateInterpolator()
        set.playTogether(rotation, scalingX, scalingY, fade)
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animator: Animator) {
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0.0f
                action.run()
            }
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        set.start()
    }

    private fun animateNoMatch(button: ImageButton, action: Runnable) {
        val set = AnimatorSet()
        val moveLeft = ObjectAnimator.ofFloat(button, "rotation", 0f, -15f)
        val moveRight = ObjectAnimator.ofFloat(button, "rotation", -15f, 15f)
        val moveBack = ObjectAnimator.ofFloat(button, "rotation", 15f, 0f)

        set.duration = 150
        set.playSequentially(moveLeft, moveRight, moveBack)
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animator: Animator) { action.run() }
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        set.start()
    }
    override fun onResume() {
        super.onResume()
        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)
    }

    override fun onPause() {
        super.onPause()
        if (::completionPlayer.isInitialized) completionPlayer.release()
        if (::negativePlayer.isInitialized) negativePlayer.release()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.board_activity_sound -> {
                if (isSound) {
                    item.setIcon(R.drawable.outline_android_cell_4_bar_off_24)
                    isSound = false
                    Toast.makeText(this, "Sound turned off", Toast.LENGTH_SHORT).show()
                } else {
                    item.setIcon(R.drawable.outline_android_cell_4_bar_24)
                    isSound = true
                    Toast.makeText(this, "Sound turned on", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}