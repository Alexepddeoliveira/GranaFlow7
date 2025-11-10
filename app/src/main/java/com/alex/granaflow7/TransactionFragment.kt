package com.alex.granaflow7

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class TransactionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotal)
        val containerList = view.findViewById<LinearLayout>(R.id.containerList)

        // pega os dados do banco
        val dao = AppDatabase.getDatabase(requireContext()).launchDao()
        val launches = dao.getAll()

        // mostra total
        val total = launches.size
        tvTotal.text = "Total de lançamentos: $total"

        // limpa a lista
        containerList.removeAllViews()

        for (launch in launches) {
            // usa o mesmo layout que você já tinha
            val card = layoutInflater.inflate(R.layout.item_launch, containerList, false) as MaterialCardView
            val tvTitle = card.findViewById<TextView>(R.id.tvItemTitle)
            val tvAmount = card.findViewById<TextView>(R.id.tvItemAmount)

            tvTitle.text = launch.title
            val color = if (launch.isIncome) "#059669" else "#DC2626"
            tvAmount.text = "R$ %.2f".format(launch.amount)
            tvAmount.setTextColor(Color.parseColor(color))

            containerList.addView(card)
        }

        return view
    }
}
