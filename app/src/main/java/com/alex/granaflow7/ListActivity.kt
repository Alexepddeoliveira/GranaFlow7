package com.alex.granaflow7

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Carrega o fragmento de transações
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, TransactionFragment())
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Atualiza o fragmento
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment is TransactionFragment) {
            fragment.onResume() // força o banco a recarregar
        }
    }
}
