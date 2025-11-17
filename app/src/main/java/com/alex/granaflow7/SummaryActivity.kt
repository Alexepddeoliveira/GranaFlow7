package com.alex.granaflow7

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import java.text.SimpleDateFormat
import java.util.*

class SummaryActivity : AppCompatActivity() {

    private lateinit var dao: LaunchDao

    private lateinit var tvFaltaPagar: TextView
    private lateinit var tvTotalPagar: TextView
    private lateinit var tvTotalReceber: TextView
    private lateinit var tvTotalMes: TextView
    private lateinit var tvTotalConta: TextView
    private lateinit var tvRestoMes: TextView
    private lateinit var tvSobraMesAnterior: TextView

    private lateinit var spMonth: Spinner
    private lateinit var spYear: Spinner
    private lateinit var btnShare: Button
    private lateinit var btnVerGrafico: Button
    private lateinit var btnQuizFinanceiro: Button

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        dao = AppDatabase.getDatabase(this).launchDao()

        // TextViews
        tvFaltaPagar       = findViewById(R.id.tvFaltaPagar)
        tvTotalPagar       = findViewById(R.id.tvTotalPagar)
        tvTotalReceber     = findViewById(R.id.tvTotalReceber)
        tvTotalMes         = findViewById(R.id.tvTotalMes)
        tvTotalConta       = findViewById(R.id.tvTotalConta)
        tvRestoMes         = findViewById(R.id.tvRestoMes)
        tvSobraMesAnterior = findViewById(R.id.tvSobraMesAnterior)

        // Spinners
        spMonth = findViewById(R.id.spMonthSummary)
        spYear  = findViewById(R.id.spYearSummary)

        // Botões
        btnShare          = findViewById(R.id.btnShare)
        btnVerGrafico     = findViewById(R.id.btnVerGrafico)
        btnQuizFinanceiro = findViewById(R.id.btnQuizFinanceiro)

        setupSpinners()

        btnShare.setOnClickListener {
            shareSummary()
        }

        // Abre a tela do gráfico com o mês/ano escolhidos
        btnVerGrafico.setOnClickListener {
            val month = spMonth.selectedItemPosition + 1
            val year  = spYear.selectedItem.toString().toInt()
            val intent = Intent(this, ChartActivity::class.java)
            intent.putExtra("month", month)
            intent.putExtra("year", year)
            startActivity(intent)
        }

        // Abre o quiz financeiro
        btnQuizFinanceiro.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }

        // Gestos de swipe (Summary ↔ List/Main)
        gestureDetector = GestureDetectorCompat(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_DISTANCE = 120
                private val SWIPE_VELOCITY = 200

                override fun onDown(e: MotionEvent): Boolean = true

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

                    val isHorizontal   = kotlin.math.abs(diffX) > kotlin.math.abs(diffY)
                    val passedDistance = kotlin.math.abs(diffX) > SWIPE_DISTANCE
                    val passedVelocity = kotlin.math.abs(velocityX) > SWIPE_VELOCITY

                    if (isHorizontal && passedDistance && passedVelocity) {
                        if (diffX < 0) {
                            goToNextTab()
                        } else {
                            goToPreviousTab()
                        }
                        return true
                    }
                    return false
                }
            }
        )
    }

    private fun setupSpinners() {
        val months = listOf(
            "Janeiro","Fevereiro","Março","Abril",
            "Maio","Junho","Julho","Agosto",
            "Setembro","Outubro","Novembro","Dezembro"
        )

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) // 0–11
        val currentYear  = cal.get(Calendar.YEAR)

        spMonth.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            months
        )
        spMonth.setSelection(currentMonth)

        val years = (currentYear - 3..currentYear + 1).map { it.toString() }
        spYear.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            years
        )
        spYear.setSelection(years.indexOf(currentYear.toString()))

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateSummary()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spMonth.onItemSelectedListener = listener
        spYear.onItemSelectedListener  = listener

        updateSummary()
    }

    private fun updateSummary() {
        val allLaunches = dao.getAll()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val selectedMonth = spMonth.selectedItemPosition + 1
        val selectedYear  = spYear.selectedItem.toString().toInt()

        // Lançamentos do mês selecionado
        val filtered = allLaunches.filter { launch ->
            try {
                val d = sdf.parse(launch.date)
                val c = Calendar.getInstance()
                c.time = d!!
                c.get(Calendar.MONTH) + 1 == selectedMonth &&
                        c.get(Calendar.YEAR) == selectedYear
            } catch (e: Exception) {
                false
            }
        }

        // Sobra do mês anterior (saldo = receitas pagas - despesas pagas)
        val sobraMesAnterior = calculatePreviousMonthCarry(
            allLaunches,
            selectedMonth,
            selectedYear
        )

        // Falta pagar = despesas não pagas
        val faltaPagar = filtered
            .filter { !it.isIncome && !it.isPaid }
            .sumOf { it.amount }

        // Gastos totais = todas as despesas (pagas + não pagas)
        val totalPagar = filtered
            .filter { !it.isIncome }
            .sumOf { it.amount }

        // Total a ser recebido = receitas não pagas
        val totalReceber = filtered
            .filter { it.isIncome && !it.isPaid }
            .sumOf { it.amount }

        // Total recebido base = receitas pagas do mês
        val totalRecebidoBase = filtered
            .filter { it.isIncome && it.isPaid }
            .sumOf { it.amount }

        // Sobra positiva entra como reforço nas receitas do mês seguinte
        val extraSobra = if (sobraMesAnterior > 0.0) sobraMesAnterior else 0.0
        val totalRecebido = totalRecebidoBase + extraSobra

        // Despesas pagas (para o "Total em conta")
        val despesasPagas = filtered
            .filter { !it.isIncome && it.isPaid }
            .sumOf { it.amount }

        // Total em conta = (receitas pagas + sobra anterior positiva) - despesas pagas
        val totalConta = totalRecebido - despesasPagas

        // RESTO DO MÊS (saldo do período) = TOTAL RECEBIDO - TOTAL GASTO
        val restoMes = totalRecebido - totalPagar

        // Exibir valores formatados
        setColoredValue(tvFaltaPagar,   faltaPagar)
        setColoredValue(tvTotalPagar,   totalPagar)
        setColoredValue(tvTotalReceber, totalReceber)
        setColoredValue(tvTotalMes,     totalRecebido)
        setColoredValue(tvTotalConta,   totalConta)
        setColoredValue(tvRestoMes,     restoMes)
        setColoredValue(tvSobraMesAnterior, sobraMesAnterior)
    }

    /**
     * Calcula a sobra (saldo) do mês anterior ao mês/ano selecionados:
     * receitas pagas - despesas pagas daquele mês.
     */
    private fun calculatePreviousMonthCarry(
        allLaunches: List<LaunchEntity>,
        month: Int,
        year: Int
    ): Double {
        if (allLaunches.isEmpty()) return 0.0

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()

        var prevMonth = month - 1
        var prevYear  = year
        if (prevMonth < 1) {
            prevMonth = 12
            prevYear -= 1
        }

        val previousLaunches = allLaunches.filter { launch ->
            try {
                val d = sdf.parse(launch.date)
                if (d != null) {
                    cal.time = d
                    (cal.get(Calendar.MONTH) + 1 == prevMonth) &&
                            (cal.get(Calendar.YEAR) == prevYear)
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }

        val prevReceitasPagas = previousLaunches
            .filter { it.isIncome && it.isPaid }
            .sumOf { it.amount }

        val prevDespesasPagas = previousLaunches
            .filter { !it.isIncome && it.isPaid }
            .sumOf { it.amount }

        return prevReceitasPagas - prevDespesasPagas
    }

    private fun setColoredValue(textView: TextView, value: Double) {
        textView.text = "R$ %.2f".format(value)
        val color = if (value < 0) {
            Color.parseColor("#DC2626") // vermelho
        } else {
            Color.parseColor("#059669") // verde
        }
        textView.setTextColor(color)
    }

    private fun shareSummary() {
        val text = """
            Resumo financeiro - ${spMonth.selectedItem} de ${spYear.selectedItem}
            
            Falta pagar: ${tvFaltaPagar.text}
            Gastos totais: ${tvTotalPagar.text}
            Total a ser recebido: ${tvTotalReceber.text}
            Total recebido (incluindo sobra do mês anterior): ${tvTotalMes.text}
            Total em conta: ${tvTotalConta.text}
            Resto do mês (total recebido - total gasto): ${tvRestoMes.text}
            Sobra do mês anterior: ${tvSobraMesAnterior.text}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Compartilhar resumo"))
    }

    // Navegação do carrossel
    private fun goToNextTab() {
        // Summary → Main
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun goToPreviousTab() {
        // Summary ← List
        startActivity(Intent(this, ListActivity::class.java))
        finish()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (::gestureDetector.isInitialized) {
            gestureDetector.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }
}
