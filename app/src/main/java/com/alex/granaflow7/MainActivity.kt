package com.alex.granaflow7

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        // Atualiza os valores
        updateHomeCards()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Atalhos
        val shortcutAdd = findViewById<LinearLayout>(R.id.shortcutAdd)
        val shortcutList = findViewById<LinearLayout>(R.id.shortcutList)
        val shortcutSummary = findViewById<LinearLayout>(R.id.shortcutSummary)
        val shortcutAbout = findViewById<LinearLayout>(R.id.shortcutAbout)

        // visão rapida
        val btnGoSummary = findViewById<Button>(R.id.btnGoSummary)

        // Ações de navegação
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

        // Atualiza ao abrir o app
        updateHomeCards()
    }

    private fun updateHomeCards() {
        val tvBalance = findViewById<TextView>(R.id.tvHomeBalance)
        val tvIncome = findViewById<TextView>(R.id.tvHomeIncome)
        val tvExpense = findViewById<TextView>(R.id.tvHomeExpense)

        // Pega o DAO do banco
        val dao = AppDatabase.getDatabase(this).launchDao()

        // Calcula valores
        val income = dao.totalIncome() ?: 0.0
        val expense = dao.totalExpense() ?: 0.0
        val balance = income - expense

        // Mostra na tela com 2 casas decimais
        tvBalance.text = "R$ %.2f".format(balance)
        val color = if (balance >= 0) "#059669" else "#DC2626"
        tvBalance.setTextColor(android.graphics.Color.parseColor(color))
        tvIncome.text = "R$ %.2f".format(income)
        tvExpense.text = "R$ %.2f".format(expense)
    }
}
