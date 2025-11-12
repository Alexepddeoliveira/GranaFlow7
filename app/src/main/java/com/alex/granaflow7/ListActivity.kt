package com.alex.granaflow7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import androidx.core.view.GestureDetectorCompat
import android.view.MotionEvent
import android.content.Intent

class ListActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Carrega o fragmento de transações
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TransactionFragment())
            .commit()

        // List --> (Add, Summary)
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_DISTANCE = 120
            private val SWIPE_VELOCITY = 200

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val startX = e1?.x ?: e2.x
                val startY = e1?.y ?: e2.y
                val diffX = e2.x - startX
                val diffY = e2.y - startY

                val isHorizontal = kotlin.math.abs(diffX) > kotlin.math.abs(diffY)
                val passedDistance = kotlin.math.abs(diffX) > SWIPE_DISTANCE
                val passedVelocity = kotlin.math.abs(velocityX) > SWIPE_VELOCITY

                if (isHorizontal && passedDistance && passedVelocity) {
                    if (diffX < 0) {
                        //proxima
                        goToNextTab()
                    } else {
                        //Anterior
                        goToPreviousTab()
                    }
                    return true
                }
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Atualiza o fragmento
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment is TransactionFragment) {
            fragment.onResume() // recarrega o banco
        }
    }

    // <--- Navegação do carrossel --->
    private fun goToNextTab() {
        // Lista → Resumo
        startActivity(Intent(this, SummaryActivity::class.java))
        finish() // opcional: não empilhar telas
    }

    private fun goToPreviousTab() {
        // Lista ← Add
        startActivity(Intent(this, AddTransactionActivity::class.java))
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (::gestureDetector.isInitialized) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }
}
