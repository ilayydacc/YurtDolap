package com.ilayda.yurtdolap

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etSifre = findViewById<EditText>(R.id.etSifre)
        val btnGiris = findViewById<Button>(R.id.btnGiris)
        val tvKayitOl = findViewById<TextView>(R.id.tvKayitOl)

        // ✅ Yazı rengi: boşken normal, yazınca daha koyu (accent koyu)
        val normalColor = ContextCompat.getColor(this, R.color.textPrimary)
        val activeColor = android.graphics.Color.parseColor("#C2185B") // koyu pembe (isteğe göre daha da koyulaştırılır)

        fun applyLiveColor(et: EditText) {
            et.setTextColor(if (et.text.isNullOrEmpty()) normalColor else activeColor)

            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    et.setTextColor(if (s.isNullOrEmpty()) normalColor else activeColor)
                }
            })
        }

        applyLiveColor(etEmail)
        applyLiveColor(etSifre)

        // 1) GİRİŞ
        btnGiris.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val sifre = etSifre.text.toString().trim()

            if (email.isEmpty() || sifre.isEmpty()) {
                Toast.makeText(this, "Lütfen e-posta ve şifre giriniz.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, sifre)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // 2) KAYIT OL
        tvKayitOl.setOnClickListener {
            Toast.makeText(this, "Kayıt ekranına gidiliyor...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, KayitActivity::class.java))
        }
    }
}
