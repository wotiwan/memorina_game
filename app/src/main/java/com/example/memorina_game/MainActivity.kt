package com.example.memorina_game


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    var openCardsCount = 0
    var matchedPairs = 0
    lateinit var firstCard: ImageView
    lateinit var messageView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        val layout = LinearLayout(applicationContext)
        layout.orientation = LinearLayout.VERTICAL

        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.weight = 1f // единичный вес

        val catViews = ArrayList<ImageView>()
        for (i in 1..8) {
            catViews.add( // вызываем конструктор для создания нового ImageView
                ImageView(applicationContext).apply {
                    val card1Id = resources.getIdentifier("card_$i", "drawable", packageName)
                    setImageResource(R.drawable.backside)
                    layoutParams = params
                    tag = Pair("card_$i", false)
                    setOnClickListener(cardClickListener)
                })
            catViews.add(
                ImageView(applicationContext).apply {
                    var isOpened = false
                    val card2Id = resources.getIdentifier("card_${i}", "drawable", packageName)
                    setImageResource(R.drawable.backside)
                    layoutParams = params
                    tag = Pair("card_$i", false)
                    setOnClickListener(cardClickListener)
                })
            messageView = TextView(this).apply {
                textSize = 24f
                setTextColor(Color.WHITE)
                text = ""
                visibility = View.GONE
            }
            layout.addView(messageView)
        }
        catViews.shuffle()

        val rows = Array(4) {
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            }
        }

        var count = 0
        for (view in catViews) {
            val row: Int = count / 4
            rows[row].addView(view)
            count ++
        }
        for (row in rows) {
            layout.addView(row)
        }
        setContentView(layout)
    }

    suspend fun setBackgroundWithDelay(v: ImageView, id: String) {
        v.setBackgroundColor(Color.YELLOW)
        delay(500)
        val imageResId = resources.getIdentifier("backside", "drawable", packageName)
        v.setImageResource(imageResId)
        v.tag = Pair(id, false)
        v.setBackgroundColor(Color.TRANSPARENT)
    }

    suspend fun openCards(v: ImageView, id: String) {
        delay(500)
        val imageResId = resources.getIdentifier(id, "drawable", packageName)
        v.setImageResource(imageResId)
        v.tag = Pair(id, true)
    }

    suspend fun checkMatch(v1: ImageView, v2: ImageView) {
        val (id1, isOpened1) = v1.tag as Pair<String, Boolean>
        val (id2, isOpened2) = v2.tag as Pair<String, Boolean>
        if (id1 == id2) {
            delay(1000)
            v1.visibility = View.INVISIBLE
            v1.isClickable = false
            v2.visibility = View.INVISIBLE
            v2.isClickable = false
            openCardsCount = 0
            matchedPairs++
        }
        if (matchedPairs == 8) {
            runOnUiThread {
                messageView.text = "Вы выиграли!"
                messageView.visibility = View.VISIBLE
            }
        }
    }

    // обработчик нажатия на кнопку
    private val cardClickListener = View.OnClickListener { view ->
        val card = view as ImageView
        val (cardId, isOpened) = card.tag as Pair<String, Boolean>
        when(openCardsCount) {
            0 -> {
                if (!isOpened) {
                    GlobalScope.launch (Dispatchers.Main)
                    { openCards(card, cardId) }
                    openCardsCount++
                } else {
                    GlobalScope.launch(Dispatchers.Main)
                    { setBackgroundWithDelay(card, cardId) }
                    openCardsCount--
                }
                firstCard = card
            }
            1 -> {
                if (!isOpened) {
                    GlobalScope.launch (Dispatchers.Main)
                    { openCards(card, cardId) }
                    GlobalScope.launch (Dispatchers.Main)
                    { checkMatch(card, firstCard) }
                    openCardsCount++

                } else {
                    GlobalScope.launch (Dispatchers.Main)
                    { setBackgroundWithDelay(card, cardId) }
                    openCardsCount--
                }

            }
            2 -> {
                if (isOpened) {
                    GlobalScope.launch (Dispatchers.Main)
                    { setBackgroundWithDelay(card, cardId) }
                    openCardsCount--
                }
            }
        }
    }
}
