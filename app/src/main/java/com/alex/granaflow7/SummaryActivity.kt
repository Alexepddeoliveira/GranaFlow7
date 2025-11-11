package com.alex.granaflow7

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class SummaryActivity : AppCompatActivity() {

    private lateinit var dao: LaunchDao

    private lateinit var tvFaltaPagar: TextView
    private lateinit var tvTotalPagar: TextView
    private lateinit var tvTotalReceber: TextView
    private lateinit var tvTotalMes: TextView
    private lateinit var tvTotalConta: TextView
    private lateinit var spMonth: Spinner
    private lateinit var spYear: Spinner
    private lateinit var btnShare: Button
    private lateinit var btnVerGrafico: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        dao = AppDatabase.getDatabase(this).launchDao()

        tvFaltaPagar = findViewById(R.id.tvFaltaPagar)
        tvTotalPagar = findViewById(R.id.tvTotalPagar)
        tvTotalReceber = findViewById(R.id.tvTotalReceber)
        tvTotalMes = findViewById(R.id.tvTotalMes)
        tvTotalConta = findViewById(R.id.tvTotalConta)
        spMonth = findViewById(R.id.spMonthSummary)
        spYear = findViewById(R.id.spYearSummary)
        btnShare = findViewById(R.id.btnShare)
        btnVerGrafico = findViewById(R.id.btnVerGrafico)

        setupSpinners()

        btnShare.setOnClickListener {
            shareSummary()
        }

        // abre a tela do gráfico com o mês/ano escolhidos
        btnVerGrafico.setOnClickListener {
            val month = spMonth.selectedItemPosition + 1
            val year = spYear.selectedItem.toString().toInt()
            val intent = Intent(this, ChartActivity::class.java)
            intent.putExtra("month", month)
            intent.putExtra("year", year)
            startActivity(intent)
        }
    }

    private fun setupSpinners() {
        val months = listOf(
            "Janeiro","Fevereiro","Março","Abril",
            "Maio","Junho","Julho","Agosto",
            "Setembro","Outubro","Novembro","Dezembro"
        )

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        spMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        spMonth.setSelection(currentMonth)

        val years = (currentYear - 3..currentYear + 1).map { it.toString() }
        spYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
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
        spYear.onItemSelectedListener = listener

        updateSummary()
    }

    private fun updateSummary() {
        val allLaunches = dao.getAll()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val selectedMonth = spMonth.selectedItemPosition + 1
        val selectedYear = spYear.selectedItem.toString().toInt()

        val filtered = allLaunches.filter { launch ->
            try {
                val d = sdf.parse(launch.date)
                val c = Calendar.getInstance()
                c.time = d
                c.get(Calendar.MONTH) + 1 == selectedMonth &&
                        c.get(Calendar.YEAR) == selectedYear
            } catch (e: Exception) {
                false
            }
        }

        // Falta pagar = despesas não pagas
        val faltaPagar = filtered
            .filter { !it.isIncome && !it.isPaid }
            .sumOf { it.amount }

        // Gastos totais = todas as despesas
        val totalPagar = filtered
            .filter { !it.isIncome }
            .sumOf { it.amount }

        // Total a ser recebido = receitas não pagas
        val totalReceber = filtered
            .filter { it.isIncome && !it.isPaid }
            .sumOf { it.amount }

        // Total recebido = receitas pagas
        val totalRecebido = filtered
            .filter { it.isIncome && it.isPaid }
            .sumOf { it.amount }

        // Total em conta = receitas pagas - despesas pagas
        val despesasPagas = filtered
            .filter { !it.isIncome && it.isPaid }
            .sumOf { it.amount }
        val totalConta = totalRecebido - despesasPagas

        // Exibir valores formatados
        setColoredValue(tvFaltaPagar, faltaPagar)
        setColoredValue(tvTotalPagar, totalPagar)
        setColoredValue(tvTotalReceber, totalReceber)
        setColoredValue(tvTotalMes, totalRecebido)
        setColoredValue(tvTotalConta, totalConta)
    }

    private fun setColoredValue(textView: TextView, value: Double) {
        textView.text = "R$ %.2f".format(value)
        val color = if (value < 0) Color.parseColor("#DC2626") // vermelho
        else Color.parseColor("#059669") // verde
        textView.setTextColor(color)
    }

    private fun shareSummary() {
        val text = """
            Resumo financeiro - ${spMonth.selectedItem} de ${spYear.selectedItem}
            
            Falta pagar: ${tvFaltaPagar.text}
            Gastos totais: ${tvTotalPagar.text}
            Total a ser recebido: ${tvTotalReceber.text}
            Total recebido: ${tvTotalMes.text}
            Total em conta: ${tvTotalConta.text}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        startActivity(Intent.createChooser(sendIntent, "Compartilhar resumo"))
    }
}
