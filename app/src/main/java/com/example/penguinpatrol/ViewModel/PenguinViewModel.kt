package com.example.penguinpatrol.ViewModel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PenguinViewModel : ViewModel() {
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int> = _score

    init {
        // Initialize score value
        _score.value = 0
    }

    companion object {
        private const val SCORE_KEY = "score_key"
    }

    fun saveInstanceState(outState: Bundle) {
        // Save score to the bundle
        outState.putInt(SCORE_KEY, _score.value ?: 0)
    }

    fun restoreInstanceState(savedInstanceState: Bundle?) {
        // Restore score from the bundle
        savedInstanceState?.let {
            _score.value = it.getInt(SCORE_KEY, 0)
        }
    }

    fun incrementScore() {
        // Increment score by 1
        _score.value = (_score.value ?: 0) + 1
    }
}