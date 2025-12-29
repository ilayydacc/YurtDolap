package com.ilayda.yurtdolap

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KayitActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Spinnerları sınıf seviyesinde tanımladık
    private lateinit var spSehir: Spinner
    private lateinit var spUni: Spinner
    private lateinit var spYurt: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kayit)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Bileşenleri Tanımla
        val etAdSoyad = findViewById<EditText>(R.id.etAdSoyad)
        val etEmail = findViewById<EditText>(R.id.etKayitEmail)
        val etSifre = findViewById<EditText>(R.id.etKayitSifre)
        val btnKayit = findViewById<Button>(R.id.btnKayitOl)
        val tvGirisYap = findViewById<TextView>(R.id.tvGirisYap)

        spSehir = findViewById(R.id.spinnerSehir)
        spUni = findViewById(R.id.spinnerUniversite)
        spYurt = findViewById(R.id.spinnerYurt)

        // 1. ŞEHİRLERİ DOLDUR
        val sehirler = listOf("Şehir Seçiniz", "Karaman", "Burdur", "Antalya")
        val adapterSehir = ArrayAdapter(this, android.R.layout.simple_spinner_item, sehirler)
        adapterSehir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSehir.adapter = adapterSehir

        // 2. ŞEHİR SEÇİLİNCE DİĞERLERİNİ GÜNCELLE
        spSehir.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val secilenSehir = parent.getItemAtPosition(position).toString()
                listeleriGuncelle(secilenSehir)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        // 3. KAYIT OL BUTONU
        btnKayit.setOnClickListener {
            val adSoyad = etAdSoyad.text.toString()
            val email = etEmail.text.toString()
            val sifre = etSifre.text.toString()

            val secilenSehir = if (spSehir.selectedItem != null) spSehir.selectedItem.toString() else ""
            val secilenUni = if (spUni.selectedItem != null) spUni.selectedItem.toString() else ""
            val secilenYurt = if (spYurt.selectedItem != null) spYurt.selectedItem.toString() else ""

            if (email.isEmpty() || sifre.isEmpty() || adSoyad.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm bilgileri doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (secilenSehir == "Şehir Seçiniz") {
                Toast.makeText(this, "Lütfen şehir seçimi yapın", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Kayıt
            auth.createUserWithEmailAndPassword(email, sifre)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid
                    val yeniKullanici = Kullanici(uid, adSoyad, email, secilenSehir, secilenUni, secilenYurt)

                    firestore.collection("users").document(uid).set(yeniKullanici)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Kayıt Başarısız: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Giriş Yap linki
        tvGirisYap.setOnClickListener { finish() }

    }

    private fun listeleriGuncelle(sehir: String) {
        var uniListesi = listOf<String>()
        var yurtListesi = listOf<String>()

        when (sehir) {
            "Karaman" -> {
                uniListesi = listOf("Üniversite Seçiniz", "Karamanoğlu Mehmetbey Üni")
                yurtListesi = listOf("Yurt Seçiniz", "KYK Piri Reis", "Hatuniye Kız Yurdu", "Karaman Erkek Yurdu")
            }
            "Burdur" -> {
                uniListesi = listOf("Üniversite Seçiniz", "Mehmet Akif Ersoy Üni (MAKÜ)")
                yurtListesi = listOf("Yurt Seçiniz", "KYK Safahat", "Asiye Sultan", "Burdur Erkek Yurdu")
            }
            "Antalya" -> {
                uniListesi = listOf("Üniversite Seçiniz", "Akdeniz Üniversitesi", "Antalya Bilim Üni")
                yurtListesi = listOf("Yurt Seçiniz", "Elmalılı Hamdi Yazır KYK", "Muratpaşa Yurdu", " Bezm-i Alem Valide Sultan KYK")
            }
            else -> {
                uniListesi = listOf("Önce Şehir Seçiniz")
                yurtListesi = listOf("Önce Şehir Seçiniz")
            }
        }

        val adapterUni = ArrayAdapter(this, android.R.layout.simple_spinner_item, uniListesi)
        adapterUni.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spUni.adapter = adapterUni

        val adapterYurt = ArrayAdapter(this, android.R.layout.simple_spinner_item, yurtListesi)
        adapterYurt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spYurt.adapter = adapterYurt
    }

}