package com.alex.granaflow7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.widget.Toast

class AddTransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // pega as views
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val rbIncome = findViewById<RadioButton>(R.id.rbIncome)
        val rbExpense = findViewById<RadioButton>(R.id.rbExpense)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // opções do spinner
        val categorias = arrayOf("Alimentação", "Transporte", "Lazer", "Salário", "Outros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = adapter

        // pega o DAO do banco
        val db = AppDatabase.getDatabase(this)
        val dao = db.launchDao()

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amountText = etAmount.text.toString()

            if (title.isBlank() || amountText.isBlank()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isIncome = rbIncome.isChecked

            val category = spCategory.selectedItem?.toString() ?: "Outros"

            // monta o item do banco
            val launch = LaunchEntity(
                title = title,
                amount = amount,
                isIncome = isIncome,
                category = category
            )

            // salva no banco de dados
            dao.insert(launch)

            Toast.makeText(this, "Lançamento salvo!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}
