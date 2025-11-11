package com.alex.granaflow7

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class ChartActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var dao: LaunchDao

    private lateinit var spChartKind: Spinner
    private lateinit var spPeriodMode: Spinner
    private lateinit var spChartMonth: Spinner
    private lateinit var spChartYear: Spinner
    private lateinit var containerMonthYear: LinearLayout

    private lateinit var tvTituloGrafico: TextView
    private lateinit var tvSubTituloGrafico: TextView
    private lateinit var btnToggleChart: Button
    private lateinit var btnShareChart: Button

    private var lastCategoryLabels: List<String> = emptyList()
    private var lastCategoryTotals: List<Float> = emptyList()
    private var isPie = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        dao = AppDatabase.getDatabase(this).launchDao()

        pieChart = findViewById(R.id.pieChartResumo)
        barChart = findViewById(R.id.barChartResumo)
        spChartKind = findViewById(R.id.spChartKind)
        spPeriodMode = findViewById(R.id.spPeriodMode)
        spChartMonth = findViewById(R.id.spChartMonth)
        spChartYear = findViewById(R.id.spChartYear)
        containerMonthYear = findViewById(R.id.containerMonthYear)
        tvTituloGrafico = findViewById(R.id.tvTituloGrafico)
        tvSubTituloGrafico = findViewById(R.id.tvSubTituloGrafico)
        btnToggleChart = findViewById(R.id.btnToggleChart)
        btnShareChart = findViewById(R.id.btnShareChart)

        setupSpinners()
        setupButtons()
        updateChart()
    }

    private fun setupButtons() {
        btnToggleChart.setOnClickListener {
            isPie = !isPie
            applyChartVisibility()
            renderCurrentChart()
        }

        btnShareChart.setOnClickListener {
            shareChartAsImage()
        }
    }

    private fun setupSpinners() {
        val tipos = listOf("Receitas", "Despesas")
        spChartKind.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)

        val periodos = listOf("Mês", "Ano", "Tudo")
        spPeriodMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, periodos)

        val meses = listOf(
            "Janeiro","Fevereiro","Março","Abril",
            "Maio","Junho","Julho","Agosto",
            "Setembro","Outubro","Novembro","Dezembro"
        )
        spChartMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, meses)

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val anos = (currentYear - 3..currentYear + 1).map { it.toString() }
        spChartYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, anos)

        val monthFromIntent = intent.getIntExtra("month", -1)
        val yearFromIntent = intent.getIntExtra("year", -1)
        if (monthFromIntent in 1..12) {
            spChartMonth.setSelection(monthFromIntent - 1)
        } else {
            spChartMonth.setSelection(cal.get(Calendar.MONTH))
        }
        if (yearFromIntent != -1) {
            val idx = anos.indexOf(yearFromIntent.toString())
            if (idx >= 0) spChartYear.setSelection(idx) else spChartYear.setSelection(anos.indexOf(currentYear.toString()))
        } else {
            spChartYear.setSelection(anos.indexOf(currentYear.toString()))
        }

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                handlePeriodVisibility()
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spChartKind.onItemSelectedListener = listener
        spPeriodMode.onItemSelectedListener = listener
        spChartMonth.onItemSelectedListener = listener
        spChartYear.onItemSelectedListener = listener

        handlePeriodVisibility()
    }

    private fun handlePeriodVisibility() {
        val mode = spPeriodMode.selectedItem?.toString() ?: "Mês"
        containerMonthYear.visibility = if (mode == "Mês") View.VISIBLE else View.GONE
    }

    private fun applyChartVisibility() {
        if (isPie) {
            pieChart.visibility = View.VISIBLE
            barChart.visibility = View.GONE
            btnToggleChart.text = "Ver em barras"
        } else {
            pieChart.visibility = View.GONE
            barChart.visibility = View.VISIBLE
            btnToggleChart.text = "Ver em pizza"
        }
    }

    private fun updateChart() {
        val tipo = spChartKind.selectedItem?.toString() ?: "Receitas"
        val periodo = spPeriodMode.selectedItem?.toString() ?: "Mês"

        val allLaunches = dao.getAll()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val selectedMonth = spChartMonth.selectedItemPosition + 1
        val selectedYear = spChartYear.selectedItem.toString().toInt()

        val filteredByPeriod = allLaunches.filter { launch ->
            try {
                val d = sdf.parse(launch.date)
                val c = Calendar.getInstance()
                c.time = d
                val m = c.get(Calendar.MONTH) + 1
                val y = c.get(Calendar.YEAR)
                when (periodo) {
                    "Mês" -> (m == selectedMonth && y == selectedYear)
                    "Ano" -> (y == selectedYear)
                    else -> true
                }
            } catch (e: Exception) {
                false
            }
        }

        val isIncome = (tipo == "Receitas")
        val filtered = filteredByPeriod.filter { it.isIncome == isIncome }

        val byCategory = filtered.groupBy { it.category.ifBlank { "Sem categoria" } }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }
            .toList()
            .sortedByDescending { it.second }

        lastCategoryLabels = byCategory.map { it.first }
        lastCategoryTotals = byCategory.map { it.second }

        tvTituloGrafico.text = if (isIncome) "Receitas por categoria" else "Despesas por categoria"
        tvSubTituloGrafico.text = when (periodo) {
            "Mês" -> "Mês: ${spChartMonth.selectedItem} de ${spChartYear.selectedItem}"
            "Ano" -> "Ano: ${spChartYear.selectedItem}"
            else -> "Todos os lançamentos"
        }

        renderCurrentChart()
    }

    private fun renderCurrentChart() {
        applyChartVisibility()

        if (lastCategoryLabels.isEmpty()) {
            pieChart.clear()
            barChart.clear()
            pieChart.setNoDataText("Sem dados para esse filtro")
            barChart.setNoDataText("Sem dados para esse filtro")
            return
        }

        if (isPie) renderPie() else renderBar()
    }

    private fun renderPie() {
        val entries = lastCategoryLabels.mapIndexed { i, label ->
            PieEntry(lastCategoryTotals[i], label)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 3f
        dataSet.colors = buildColors(entries.size)

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(android.graphics.Color.WHITE)

        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = true
        pieChart.setDrawEntryLabels(false)
        pieChart.invalidate()
    }

    private fun renderBar() {
        val entries = lastCategoryTotals.mapIndexed { i, total -> BarEntry(i.toFloat(), total) }

        val dataSet = BarDataSet(entries, "Categorias")
        dataSet.colors = buildColors(entries.size)

        val data = BarData(dataSet)
        data.barWidth = 0.45f
        data.setValueTextSize(10f)
        data.setValueTextColor(android.graphics.Color.BLACK)

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(lastCategoryLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = lastCategoryLabels.size
        xAxis.isGranularityEnabled = true

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f
        barChart.setFitBars(true)
        barChart.invalidate()
    }

    private fun buildColors(size: Int): List<Int> {
        val base = listOf(
            ContextCompat.getColor(this, R.color.gf_green),
            ContextCompat.getColor(this, R.color.gf_purple),
            android.graphics.Color.parseColor("#F97316"),
            android.graphics.Color.parseColor("#3B82F6"),
            android.graphics.Color.parseColor("#EF4444"),
            android.graphics.Color.parseColor("#14B8A6"),
            android.graphics.Color.parseColor("#A855F7")
        )
        return List(size) { base[it % base.size] }
    }

    private fun shareChartAsImage() {
        // gráfico atual
        val chartView = if (isPie) pieChart else barChart
        val bitmap: Bitmap = chartView.chartBitmap ?: return

        try {
            // esse método joga no MediaStore e já devolve o caminho com content://
            val path = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "grafico_granaflow",
                "Gráfico gerado pelo app"
            )

            if (path == null) {
                Toast.makeText(this, "Não foi possível salvar o gráfico", Toast.LENGTH_SHORT).show()
                return
            }

            val imageUri = android.net.Uri.parse(path)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
            }

            startActivity(Intent.createChooser(shareIntent, "Compartilhar gráfico"))

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao compartilhar gráfico", Toast.LENGTH_SHORT).show()
        }
    }
}
