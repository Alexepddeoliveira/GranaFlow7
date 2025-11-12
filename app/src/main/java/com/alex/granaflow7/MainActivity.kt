package com.alex.granaflow7

import android.view.GestureDetector
import androidx.core.view.GestureDetectorCompat
import android.view.MotionEvent
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvHomeBalance: TextView

    private lateinit var tvHomeFaltaPagar: TextView
    private lateinit var tvHomeTotalPagar: TextView
    private lateinit var tvHomeTotalReceber: TextView
    private lateinit var tvHomeTotalMes: TextView
    private lateinit var tvHomeTotalConta: TextView
    private lateinit var tvResumoTitulo: TextView
    private lateinit var dao: LaunchDao

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dao = AppDatabase.getDatabase(this).launchDao()

        val shortcutAdd = findViewById<LinearLayout>(R.id.shortcutAdd)
        val shortcutList = findViewById<LinearLayout>(R.id.shortcutList)
        val shortcutSummary = findViewById<LinearLayout>(R.id.shortcutSummary)
        val shortcutAbout = findViewById<LinearLayout>(R.id.shortcutAbout)
        val btnGoSummary = findViewById<Button>(R.id.btnGoSummary)
        val btnGoCharts = findViewById<Button>(R.id.btnGoCharts)

        tvHomeBalance = findViewById(R.id.tvHomeBalance)
        tvHomeFaltaPagar = findViewById(R.id.tvHomeFaltaPagar)
        tvHomeTotalPagar = findViewById(R.id.tvHomeTotalPagar)
        tvHomeTotalReceber = findViewById(R.id.tvHomeTotalReceber)
        tvHomeTotalMes = findViewById(R.id.tvHomeTotalMes)
        tvHomeTotalConta = findViewById(R.id.tvHomeTotalConta)
        tvResumoTitulo = findViewById(R.id.tvResumoTitulo)

        // navegação
        gestureDetector = GestureDetectorCompat(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_DISTANCE = 120
            private val SWIPE_VELOCITY = 200
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val startX = e1?.x ?: e2.x
                val diffX = e2.x - startX
                val diffY = e2.y - (e1?.y ?: e2.y)

                if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY) &&
                    kotlin.math.abs(diffX) > SWIPE_DISTANCE &&
                    kotlin.math.abs(velocityX) > SWIPE_VELOCITY
                ) {
                    if (diffX < 0) goToNextTab() else goToPreviousTab()
                    return true
                }
                return false
            }
        })

        shortcutAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
        shortcutList.setOnClickListener {
            startActivity(Intent(this, ListActivity::class.java))
        }
        shortcutSummary.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
        shortcutAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
        btnGoSummary.setOnClickListener {
            startActivity(Intent(this, SummaryActivity::class.java))
        }
        btnGoCharts.setOnClickListener {
            // manda mês/ano atual pra tela de gráfico
            val cal = Calendar.getInstance()
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)

            val intent = Intent(this, ChartActivity::class.java).apply {
                putExtra("month", month)
                putExtra("year", year)
            }
            startActivity(intent)
        }

        updateDashboard()
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val allLaunches = dao.getAll()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        val nomeMes = SimpleDateFormat("MMMM", Locale("pt", "BR")).format(Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        tvResumoTitulo.text = "Resumo financeiro — $nomeMes $currentYear"

        val periodLaunches = allLaunches.filter { launch ->
            try {
                val d = sdf.parse(launch.date)
                val c = Calendar.getInstance()
                c.time = d
                c.get(Calendar.MONTH) + 1 == currentMonth && c.get(Calendar.YEAR) == currentYear
            } catch (e: Exception) {
                false
            }
        }

        val income = periodLaunches.filter { it.isIncome }.sumOf { it.amount }
        val expense = periodLaunches.filter { !it.isIncome }.sumOf { it.amount }
        val balance = income - expense

        tvHomeBalance.text = "R$ %.2f".format(balance)

        val faltaPagar = periodLaunches.filter { !it.isIncome && !it.isPaid }.sumOf { it.amount }
        val totalPagar = periodLaunches.filter { !it.isIncome }.sumOf { it.amount }
        val totalReceber = periodLaunches.filter { it.isIncome && !it.isPaid }.sumOf { it.amount }
        val totalRecebido = periodLaunches.filter { it.isIncome && it.isPaid }.sumOf { it.amount }
        val despesasPagas = periodLaunches.filter { !it.isIncome && it.isPaid }.sumOf { it.amount }
        val totalConta = totalRecebido - despesasPagas

        tvHomeFaltaPagar.text = "R$ %.2f".format(faltaPagar)
        tvHomeTotalPagar.text = "R$ %.2f".format(totalPagar)
        tvHomeTotalReceber.text = "R$ %.2f".format(totalReceber)
        tvHomeTotalMes.text = "R$ %.2f".format(totalRecebido)
        tvHomeTotalConta.text = "R$ %.2f".format(totalConta)
    }

    private fun goToNextTab() {
        // Home → AddTransaction
        startActivity(Intent(this, AddTransactionActivity::class.java))
        finish()
    }
    private fun goToPreviousTab() {
        // Home ← Resumo
        startActivity(Intent(this, SummaryActivity::class.java))
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (::gestureDetector.isInitialized) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

}
