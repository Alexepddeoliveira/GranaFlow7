package com.alex.granaflow7

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var rgQ1: RadioGroup
    private lateinit var rgQ2: RadioGroup
    private lateinit var rgQ3: RadioGroup
    private lateinit var tvResultadoQuiz: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        rgQ1 = findViewById(R.id.rgQ1)
        rgQ2 = findViewById(R.id.rgQ2)
        rgQ3 = findViewById(R.id.rgQ3)
        tvResultadoQuiz = findViewById(R.id.tvResultadoQuiz)

        val btnEnviar = findViewById<Button>(R.id.btnEnviarQuiz)

        btnEnviar.setOnClickListener {
            avaliarQuiz()
        }
    }

    private fun avaliarQuiz() {
        val id1 = rgQ1.checkedRadioButtonId
        val id2 = rgQ2.checkedRadioButtonId
        val id3 = rgQ3.checkedRadioButtonId

        if (id1 == -1 || id2 == -1 || id3 == -1) {
            Toast.makeText(this, "Responda todas as perguntas 😊", Toast.LENGTH_SHORT).show()
            return
        }

        var score = 0

        if (id1 == R.id.rbQ1Op3) score++
        if (id2 == R.id.rbQ2Op2) score++
        if (id3 == R.id.rbQ3Op1) score++

        val msg = when (score) {
            3 -> "Perfeito! Sua consciência financeira tá no nível mestre! 💸🔥"
            2 -> "Boa! Só uns ajustes e você fica no topo. 😉"
            1 -> "Hmm… dá pra melhorar bastante! 👀"
            else -> "Hora de aprender mais sobre dinheiro 😅"
        }

        tvResultadoQuiz.text = "Pontuação: $score/3\n$msg"
    }
}
