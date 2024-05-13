package com.example.penguinpatrol

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class HomePage : AppCompatActivity() {

    private lateinit var highScore: Button
    private lateinit var playBtn: Button
    private lateinit var instructions: Button
    private lateinit var highScoreTextView: TextView
    private var highestScore: Int = 0
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        highScore = findViewById(R.id.highScoreButton)

        playBtn = findViewById(R.id.startButton)
        instructions = findViewById(R.id.instructionsButton)
        sharedPreferences = getSharedPreferences("PenguinPatrol", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)

        highScore.setOnClickListener {

            val builder = layoutInflater.inflate(R.layout.highestscore, null)
            val dialog = Dialog(this)
            dialog.apply {
                setContentView(builder)
                val width = resources.displayMetrics.widthPixels * 0.6
                val height = resources.displayMetrics.heightPixels * 0.4
                window?.setLayout(width.toInt(), height.toInt())


                highScoreTextView = builder.findViewById(R.id.highscore)
                highScoreTextView.text = getString(R.string.highscore) + highestScore


                val okBtn: Button = builder.findViewById(R.id.okBtn)
                okBtn.setOnClickListener {
                    dismiss()
                }
            }.show()
        }
            playBtn.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            instructions.setOnClickListener {
                val intent = Intent(this, Instructions::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
