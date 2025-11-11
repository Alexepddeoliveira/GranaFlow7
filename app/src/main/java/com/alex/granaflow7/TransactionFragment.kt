package com.alex.granaflow7

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class TransactionFragment : Fragment() {

    private lateinit var dao: LaunchDao

    private lateinit var spMonth: Spinner
    private lateinit var spYear: Spinner

    private lateinit var btnFilterAll: Button
    private lateinit var btnFilterExpenses: Button
    private lateinit var btnFilterIncomes: Button

    private lateinit var tvTotal: TextView
    private lateinit var containerList: LinearLayout

    // 0 = todos, 1 = despesas, 2 = receitas
    private var currentCategoryFilter = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        dao = AppDatabase.getDatabase(requireContext()).launchDao()

        spMonth = view.findViewById(R.id.spMonthFilter)
        spYear = view.findViewById(R.id.spYearFilter)

        btnFilterAll = view.findViewById(R.id.btnFilterAll)
        btnFilterExpenses = view.findViewById(R.id.btnFilterExpenses)
        btnFilterIncomes = view.findViewById(R.id.btnFilterIncomes)

        tvTotal = view.findViewById(R.id.tvTotal)
        containerList = view.findViewById(R.id.containerList)

        setupMonthYearSpinners()
        setupCategoryButtons()

        updateList()

        return view
    }

    override fun onResume() {
        super.onResume()
        updateList()
    }

    private fun setupMonthYearSpinners() {
        val months = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        spMonth.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            months
        )
        spMonth.setSelection(currentMonth)

        val years = (currentYear - 3..currentYear + 1).map { it.toString() }
        spYear.adapter = ArrayAdapter(
            requireContext(),
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
                updateList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spMonth.onItemSelectedListener = listener
        spYear.onItemSelectedListener = listener
    }

    private fun setupCategoryButtons() {
        btnFilterAll.setOnClickListener {
            currentCategoryFilter = 0
            highlightCategoryButtons()
            updateList()
        }
        btnFilterExpenses.setOnClickListener {
            currentCategoryFilter = 1
            highlightCategoryButtons()
            updateList()
        }
        btnFilterIncomes.setOnClickListener {
            currentCategoryFilter = 2
            highlightCategoryButtons()
            updateList()
        }

        highlightCategoryButtons()
    }

    private fun highlightCategoryButtons() {
        val normal = resources.getColor(android.R.color.darker_gray, null)
        val selected = resources.getColor(android.R.color.holo_blue_light, null)

        btnFilterAll.setBackgroundColor(if (currentCategoryFilter == 0) selected else normal)
        btnFilterExpenses.setBackgroundColor(if (currentCategoryFilter == 1) selected else normal)
        btnFilterIncomes.setBackgroundColor(if (currentCategoryFilter == 2) selected else normal)
    }

    private fun updateList() {
        val allLaunches = dao.getAll()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val selectedMonth = spMonth.selectedItemPosition + 1
        val selectedYear = spYear.selectedItem.toString().toInt()

        // 1) filtra por mês/ano
        val filteredByDate = allLaunches.filter { launch ->
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

        // 2) filtra por categoria (despesa/receita/todos)
        val finalList = when (currentCategoryFilter) {
            1 -> filteredByDate.filter { !it.isIncome }  // despesas
            2 -> filteredByDate.filter { it.isIncome }   // receitas
            else -> filteredByDate
        }

        tvTotal.text = "Total de lançamentos: ${finalList.size}"
        containerList.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())

        for (launch in finalList) {
            val card = inflater.inflate(R.layout.item_launch, containerList, false) as MaterialCardView

            val tvTitle = card.findViewById<TextView>(R.id.tvItemTitle)
            val tvAmount = card.findViewById<TextView>(R.id.tvItemAmount)
            val tvDate = card.findViewById<TextView>(R.id.tvItemDate)

            // título com status pago / não pago
            val pagoPrefix = if (launch.isPaid) "✔ " else "❌ "
            tvTitle.text = pagoPrefix + launch.title

            tvAmount.text = "R$ %.2f".format(launch.amount)
            val color = if (launch.isIncome) "#059669" else "#DC2626"
            tvAmount.setTextColor(Color.parseColor(color))

            tvDate.text = launch.date

            //Clicar no card
            card.setOnClickListener {
                showActionDialog(launch)
            }

            containerList.addView(card)
        }
    }

    private fun showActionDialog(launch: LaunchEntity) {
        val options = arrayOf(
            if (launch.isPaid) "Marcar como NÃO pago" else "Marcar como pago",
            "Editar título e valor",
            "Excluir",
            "Cancelar"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Ações")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> togglePaid(launch)
                    1 -> showEditDialog(launch)
                    2 -> confirmDelete(launch)
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun togglePaid(launch: LaunchEntity) {
        val updated = launch.copy(isPaid = !launch.isPaid)
        dao.update(updated)
        updateList()
    }

    private fun showEditDialog(launch: LaunchEntity) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 10)
        }

        val etTitle = EditText(context).apply {
            hint = "Título"
            setText(launch.title)
        }

        val etAmount = EditText(context).apply {
            hint = "Valor"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(launch.amount.toString())
        }

        layout.addView(etTitle)
        layout.addView(etAmount)

        AlertDialog.Builder(context)
            .setTitle("Editar lançamento")
            .setView(layout)
            .setPositiveButton("Salvar") { dialog, _ ->
                val newTitle = etTitle.text.toString()
                val newAmount = etAmount.text.toString().toDoubleOrNull()

                if (newTitle.isNotBlank() && newAmount != null) {
                    val updated = launch.copy(
                        title = newTitle,
                        amount = newAmount
                    )
                    dao.update(updated)
                    updateList()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun confirmDelete(launch: LaunchEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir")
            .setMessage("Tem certeza que deseja excluir “${launch.title}”?")
            .setPositiveButton("Excluir") { dialog, _ ->
                dao.delete(launch)
                updateList()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
