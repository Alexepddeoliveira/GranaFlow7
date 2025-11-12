package com.alex.granaflow7

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class HotbarFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hotbar, container, false)

        val btnHome = view.findViewById<LinearLayout>(R.id.btnHome)
        val btnAdd = view.findViewById<LinearLayout>(R.id.btnAdd)
        val btnList = view.findViewById<LinearLayout>(R.id.btnList)
        val btnSummary = view.findViewById<LinearLayout>(R.id.btnSummary)

        btnHome.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        btnAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddTransactionActivity::class.java))
        }

        btnList.setOnClickListener {
            startActivity(Intent(requireContext(), ListActivity::class.java))
        }

        btnSummary.setOnClickListener {
            startActivity(Intent(requireContext(), SummaryActivity::class.java))
        }

        highlightCurrentScreen(view)

        // aplica o padding de acordo com a barra de navegação / gestos
        applyBottomInsets(view)

        return view
    }

    private fun applyBottomInsets(root: View) {
        // guarda o padding original UMA vez
        val startPaddingBottom = root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val extraBottom = maxOf(imeBottom, navBottom)
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                startPaddingBottom + extraBottom
            )
            insets
        }

        // garante que o listener rode ao anexar a view
        ViewCompat.requestApplyInsets(root)
    }


    private fun highlightCurrentScreen(root: View) {
        val current = activity?.javaClass?.simpleName ?: return

        val btnHome = root.findViewById<LinearLayout>(R.id.btnHome)
        val btnAdd = root.findViewById<LinearLayout>(R.id.btnAdd)
        val btnList = root.findViewById<LinearLayout>(R.id.btnList)
        val btnSummary = root.findViewById<LinearLayout>(R.id.btnSummary)

        val all = listOf(btnHome, btnAdd, btnList, btnSummary)
        all.forEach { it.background = requireContext().getDrawable(R.drawable.bg_hotbar_item) }

        when (current) {
            "MainActivity" -> btnHome.background =
                requireContext().getDrawable(R.drawable.bg_hotbar_item_selected)
            "AddTransactionActivity" -> btnAdd.background =
                requireContext().getDrawable(R.drawable.bg_hotbar_item_selected)
            "ListActivity" -> btnList.background =
                requireContext().getDrawable(R.drawable.bg_hotbar_item_selected)
            "SummaryActivity" -> btnSummary.background =
                requireContext().getDrawable(R.drawable.bg_hotbar_item_selected)
        }
    }
}
