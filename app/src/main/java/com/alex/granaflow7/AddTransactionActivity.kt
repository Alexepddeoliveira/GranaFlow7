package com.alex.granaflow7

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var rbIncome: RadioButton
    private lateinit var rbExpense: RadioButton
    private lateinit var cbPaid: CheckBox
    private lateinit var spCategory: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    // novos
    private lateinit var tvDate: TextView
    private lateinit var btnPickDate: Button

    private lateinit var dao: LaunchDao

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val selectedCal: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        rbIncome = findViewById(R.id.rbIncome)
        rbExpense = findViewById(R.id.rbExpense)
        cbPaid = findViewById(R.id.cbPaid)
        spCategory = findViewById(R.id.spCategory)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        tvDate = findViewById(R.id.tvDate)
        btnPickDate = findViewById(R.id.btnPickDate)

        dao = AppDatabase.getDatabase(this).launchDao()

        // mostra data de hoje
        tvDate.text = sdf.format(selectedCal.time)

        setupCategorySpinner()
        setupDatePicker()

        btnCancel.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupDatePicker() {
        btnPickDate.setOnClickListener {
            val year = selectedCal.get(Calendar.YEAR)
            val month = selectedCal.get(Calendar.MONTH)
            val day = selectedCal.get(Calendar.DAY_OF_MONTH)

            val dp = DatePickerDialog(
                this,
                { _, y, m, d ->
                    // atualiza o calendar e o texto
                    selectedCal.set(Calendar.YEAR, y)
                    selectedCal.set(Calendar.MONTH, m)
                    selectedCal.set(Calendar.DAY_OF_MONTH, d)
                    tvDate.text = sdf.format(selectedCal.time)
                },
                year,
                month,
                day
            )
            dp.show()
        }

        // clicar no texto abre calendario
        tvDate.setOnClickListener {
            btnPickDate.performClick()
        }
    }

    private fun setupCategorySpinner() {
        val defaultCategories = mutableListOf(
            "Alimentação",
            "Transporte",
            "Educação",
            "Lazer",
            "Saúde",
            "Moradia",
            "Outra..."
        )

        val existing = dao.getAll().map { it.category }.distinct()
        val categories = (existing + defaultCategories).distinct().toMutableList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.adapter = adapter

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                if (categories[pos] == "Outra...") {
                    showNewCategoryDialog(categories, adapter)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showNewCategoryDialog(categories: MutableList<String>, adapter: ArrayAdapter<String>) {
        val input = EditText(this)
        input.hint = "Nova categoria"

        AlertDialog.Builder(this)
            .setTitle("Adicionar nova categoria")
            .setView(input)
            .setPositiveButton("Salvar") { dialog, _ ->
                val newCat = input.text.toString().trim()
                if (newCat.isNotEmpty()) {
                    categories.add(categories.size - 1, newCat)
                    adapter.notifyDataSetChanged()
                    spCategory.setSelection(categories.indexOf(newCat))
                } else {
                    spCategory.setSelection(0)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                spCategory.setSelection(0)
                dialog.dismiss()
            }
            .show()
    }

    private fun saveTransaction() {
        val title = etTitle.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val category = spCategory.selectedItem.toString()
        val isIncome = rbIncome.isChecked
        val isPaid = cbPaid.isChecked
        val amount = amountText.toDoubleOrNull()

        if (title.isEmpty() || amount == null) {
            Toast.makeText(this, "Preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show()
            return
        }

        // data calendario
        val chosenDate = sdf.format(selectedCal.time)

        val newLaunch = LaunchEntity(
            id = 0,
            title = title,
            amount = amount,
            isIncome = isIncome,
            isPaid = isPaid,
            category = category,
            date = chosenDate
        )

        dao.insert(newLaunch)
        Toast.makeText(this, "Lançamento adicionado!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
