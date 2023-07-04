package com.example.magister

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import com.example.magister.databinding.ActivityTelaRecSenhaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class TelaRecSenha : AppCompatActivity() {

    private lateinit var binding: ActivityTelaRecSenhaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaRecSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val firebaseAuth = FirebaseAuth.getInstance()

        binding.btRecuperar.setOnClickListener { view ->
            val email = binding.editEmail2.text.toString()

            val InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            InputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

            if (email.isEmpty()) {
                val snackbar = Snackbar.make(
                    view,
                    "Insira um e-mail para iniciar o processo de redefinição de senha!",
                    Snackbar.LENGTH_SHORT
                )
                snackbar.setBackgroundTint(Color.parseColor("#562D8B"))
                snackbar.setTextColor(Color.WHITE)
                snackbar.show()
            } else {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snackbar = Snackbar.make(
                            view,
                            "Redefinição de senha iniciada, um e-mail foi enviado!",
                            Snackbar.LENGTH_SHORT
                        )
                        snackbar.setBackgroundTint(Color.parseColor("#562D8B"))
                        snackbar.setTextColor(Color.WHITE)
                        snackbar.show()
                    } else {
                        val snackbar = Snackbar.make(
                            view,
                            "Houve um erro, verifique se o e-mail está correto.",
                            Snackbar.LENGTH_SHORT
                        )
                        snackbar.setBackgroundTint(Color.parseColor("#562D8B"))
                        snackbar.setTextColor(Color.WHITE)
                        snackbar.show()
                    }
                    binding.editEmail2.text = null
                }
            }
        }
    }
}