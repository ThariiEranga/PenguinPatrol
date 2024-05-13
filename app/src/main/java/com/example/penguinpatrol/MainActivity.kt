
package com.example.penguinpatrol

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.penguinpatrol.ViewModel.PenguinViewModel
import kotlin.random.Random
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {
    private var penguinCount = 0
    private var missCount = 0
    private var highestScore: Int = 0
    private var gameOver = false
    private lateinit var handler: Handler
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var penguinGenerator: Runnable
    private lateinit var gameContainer: FrameLayout
    private lateinit var scoreTextView: TextView
    private lateinit var pauseButton: Button
    private lateinit var backButton: Button
    private var  isGamePaused = false
    private lateinit var penguinViewModel: PenguinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("PenguinPatrol", MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)
        gameContainer = findViewById(R.id.gameContainer)
        scoreTextView = findViewById(R.id.scoreTextView)
        pauseButton = findViewById(R.id.pauseButton)
        backButton = findViewById(R.id.backButton)
        penguinViewModel = ViewModelProvider(this).get(PenguinViewModel::class.java)
        penguinViewModel.restoreInstanceState(savedInstanceState)

        penguinViewModel.score.observe(this, Observer { score ->
            scoreTextView.text = getString(R.string.Score) + score
        })

        pauseButton.setOnClickListener {
            if (!gameOver) {
                pauseGame()
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this,HomePage::class.java)
            startActivity(intent)
            finish()
        }

        startGame()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        penguinViewModel.saveInstanceState(outState)
    }

    private fun startGame() {

        scoreTextView.text = getString(R.string.Score) + penguinViewModel.score.value
        gameContainer.visibility = View.VISIBLE

        handler = Handler(Looper.getMainLooper())
        penguinGenerator = Runnable {
            if (!gameOver) {
                generatePenguin()
                handler.postDelayed(penguinGenerator, 1000)
            }
        }

        handler.post(penguinGenerator)
    }

    private fun generatePenguin() {
        if (!isGamePaused) {
            val penguin = ImageView(this)
            val penguinDrawable = ContextCompat.getDrawable(this, R.mipmap.pen_launcher_foreground)
            penguin.setImageDrawable(penguinDrawable)

            val penguinWidth = 200 // Set the desired width
            val penguinHeight = 200 // Set the desired height
            penguin.layoutParams = FrameLayout.LayoutParams(penguinWidth, penguinHeight)

            gameContainer.addView(penguin)

            val screenWidth = gameContainer.width
            val screenHeight = gameContainer.height

            val startX = screenWidth - penguinWidth // Start from the right edge of the screen
            val startY = Random.nextInt(screenHeight / 2, screenHeight - penguinHeight)

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.leftMargin = startX
            layoutParams.topMargin = startY

            penguin.layoutParams = layoutParams

            penguin.setOnClickListener {
                if (!gameOver) {
                    penguinViewModel.incrementScore()
                    }
                    scoreTextView.text = getString(R.string.Score) + penguinViewModel.score.value
                    gameContainer.removeView(penguin)
                }


            var duration = 5000L

            if (penguinCount > 0 && penguinCount % 5 == 0) {
                duration = 3000L
            }

            penguin.animate().translationX(-screenWidth.toFloat()).setDuration(duration).withEndAction {
                if (!gameOver && penguin.isShown) { // Check if the penguin is still on the screen (not clicked)
                    missCount++
                    Log.d("Missed count", "$missCount")
                    if (missCount >= 4) {
                        gameOver = true
                        var hs = sharedPreferences.getInt("highestScore", 0)
                        if(hs< penguinViewModel.score.value!!){
                            highestScore = penguinViewModel.score.value!!
                            saveHighestScore()
                        }
                        handleGameOver()
                    }
                }
            }
        }
    }

    private fun pauseGame() {
        isGamePaused = true
        handler.removeCallbacks(penguinGenerator)
        val childCount = gameContainer.childCount
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until childCount) {
            val view = gameContainer.getChildAt(i)
            if (view is ImageView && view.drawable != null && view.drawable.constantState == ContextCompat.getDrawable(this, R.mipmap.pen_launcher_foreground)?.constantState) {
                viewsToRemove.add(view)
            }
        }
        for (view in viewsToRemove) {
            gameContainer.removeView(view)
        }

        showPauseDialog()
    }

    private fun resumeGame() {
        isGamePaused = false
        handler.post(penguinGenerator)

        // Restore click listeners to penguins
        for (i in 0 until gameContainer.childCount) {
            val view = gameContainer.getChildAt(i)
            if (view is ImageView) {
                view.setOnClickListener {
                    if (!gameOver) {
                        penguinCount++
                        scoreTextView.text = getString(R.string.Score) + penguinViewModel.score.value
                        gameContainer.removeView(view)
                    }
                }
            }
        }

    }

    private fun showPauseDialog() {
        val builder = layoutInflater.inflate(R.layout.pausedialog, null)
        val dialog = Dialog(this)
        dialog.apply {
            setContentView(builder)
            val width = resources.displayMetrics.widthPixels * 0.6
            val height = resources.displayMetrics.heightPixels * 0.4
            window?.setLayout(width.toInt(), height.toInt())
            setCancelable(false)

            val resumeButton: Button = builder.findViewById(R.id.resumeBtn)
            resumeButton.setOnClickListener {
                resumeGame()
                dismiss()
            }
        }

        dialog.show()
    }

    private fun saveHighestScore() {
        sharedPreferences.edit().putInt("highestScore", highestScore).apply()
    }

    private fun handleGameOver() {
        gameContainer.removeAllViews()
        gameContainer.visibility = View.GONE

        handler.removeCallbacks(penguinGenerator)
        val builder = layoutInflater.inflate(R.layout.gameoverdialogbox, null)

        val dialog = Dialog(this)
        dialog.apply {
            setContentView(builder)
            val width = resources.displayMetrics.widthPixels * 0.6
            val height = resources.displayMetrics.heightPixels * 0.4
            window?.setLayout(width.toInt(), height.toInt())
            setCancelable(false)

            val tryagainButton: Button = builder.findViewById(R.id.tryagain)
            tryagainButton.setOnClickListener {
                val intent = Intent(this@MainActivity, HomePage::class.java)
                startActivity(intent)
                finish()
            }
        }
        dialog.show()
    }
}