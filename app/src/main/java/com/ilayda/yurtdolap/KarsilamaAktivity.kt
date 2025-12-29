package com.ilayda.yurtdolap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class KarsilamaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Artık her zaman bu ekran gözükecek
        setContentView(R.layout.activity_karsilama)

        val btnDevamEt = findViewById<Button>(R.id.btnDevamEt)

        btnDevamEt.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // ✅ Giriş yaptıysa devam et -> Home
                startActivity(Intent(this, HomeActivity::class.java))
            } else {
                // ✅ Giriş yapmadıysa devam et -> Login
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
    }
}
