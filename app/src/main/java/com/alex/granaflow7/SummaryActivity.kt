package com.alex.granaflow7

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        val tvIncome = findViewById<TextView>(R.id.tvIncome)
        val tvExpense = findViewById<TextView>(R.id.tvExpense)
        val tvBalance = findViewById<TextView>(R.id.tvBalance)
        val btnShare = findViewById<Button>(R.id.btnShare)

        // busca dados do banco de dados
        val dao = AppDatabase.getDatabase(this).launchDao()

        val income = dao.totalIncome() ?: 0.0
        val expense = dao.totalExpense() ?: 0.0
        val balance = income - expense

        // Mostra os valores
        tvIncome.text = "R$ %.2f".format(income)
        tvExpense.text = "R$ %.2f".format(expense)
        tvBalance.text = "R$ %.2f".format(balance)
        val color = if (balance >= 0) "#059669" else "#DC2626"
        tvBalance.setTextColor(android.graphics.Color.parseColor(color))


        // Botão de compartilhar
        btnShare.setOnClickListener {
            val text = "Resumo financeiro:\n" +
                    "Receitas: R$ %.2f\nDespesas: R$ %.2f\nSaldo: R$ %.2f"
                        .format(income, expense, balance)

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }

            startActivity(Intent.createChooser(sendIntent, "Compartilhar resumo"))
        }
    }
}
