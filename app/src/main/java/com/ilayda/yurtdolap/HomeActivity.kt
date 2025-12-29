package com.ilayda.yurtdolap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var ilanListesi: ArrayList<Ilan>
    private lateinit var ilanAdapter: IlanAdapter

    // Kategori butonları
    private lateinit var btnHepsi: Button
    private lateinit var btnGiyim: Button
    private lateinit var btnAksesuar: Button
    private lateinit var btnKitap: Button
    private lateinit var btnElektronik: Button
    private lateinit var kategoriButonlari: List<Button>

    // Alt bar + orta FAB
    private var bottomNav: BottomNavigationView? = null
    private var fabOrtaEkle: FloatingActionButton? = null

    // Seçili kategori
    private var seciliKategori: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firestore = FirebaseFirestore.getInstance()

        // Recycler
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ilanListesi = ArrayList()
        ilanAdapter = IlanAdapter(ilanListesi)
        recyclerView.adapter = ilanAdapter

        // Kategori butonlarını bağla
        btnHepsi = findViewById(R.id.btnKatHepsi)
        btnGiyim = findViewById(R.id.btnKatGiyim)
        btnAksesuar = findViewById(R.id.btnKatAksesuar)
        btnKitap = findViewById(R.id.btnKatKitap)
        btnElektronik = findViewById(R.id.btnKatElektronik)

        kategoriButonlari = listOf(btnHepsi, btnGiyim, btnAksesuar, btnKitap, btnElektronik)

        // Kategori click
        btnHepsi.setOnClickListener {
            kategoriSec(btnHepsi)
            seciliKategori = null
            verileriGetir(seciliKategori)
        }
        btnGiyim.setOnClickListener {
            kategoriSec(btnGiyim)
            seciliKategori = "Giyim"
            verileriGetir(seciliKategori)
        }
        btnAksesuar.setOnClickListener {
            kategoriSec(btnAksesuar)
            seciliKategori = "Aksesuar"
            verileriGetir(seciliKategori)
        }
        btnKitap.setOnClickListener {
            kategoriSec(btnKitap)
            seciliKategori = "Kitap/Kırtasiye"
            verileriGetir(seciliKategori)
        }
        btnElektronik.setOnClickListener {
            kategoriSec(btnElektronik)
            seciliKategori = "Elektronik"
            verileriGetir(seciliKategori)
        }

        // Default
        kategoriSec(btnHepsi)

        // Çıkış butonu
        findViewById<android.view.View>(R.id.btnCikisYap).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Alt bar (activity_home içinde include ettiysen id’ler burada bulunur)
        bottomNav = findViewById(R.id.bottomNav)
        fabOrtaEkle = findViewById(R.id.fabOrtaEkle)

        fabOrtaEkle?.setOnClickListener {
            startActivity(Intent(this, IlanEkleActivity::class.java))
        }

        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_anasayfa -> true

                R.id.nav_sohbet -> {
                    // Eğer SohbetActivity varsa aç
                    // startActivity(Intent(this, SohbetActivity::class.java))
                    Toast.makeText(this, "Sohbet (sayfa eklemedin)", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_ilanlarim -> {
                    // startActivity(Intent(this, IlanlarimActivity::class.java))
                    Toast.makeText(this, "İlanlarım (sayfa eklemedin)", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_hesabim -> {
                    // startActivity(Intent(this, HesabimActivity::class.java))
                    Toast.makeText(this, "Hesabım (sayfa eklemedin)", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        // İlk veri çek
        verileriGetir(seciliKategori)
    }

    override fun onResume() {
        super.onResume()
        verileriGetir(seciliKategori)
    }

    private fun kategoriSec(selected: Button) {
        kategoriButonlari.forEach { it.isSelected = false }
        selected.isSelected = true
    }

    private fun verileriGetir(kategori: String? = null) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Oturum yok, tekrar giriş yap.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        firestore.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Kullanıcı bilgisi bulunamadı!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val benimYurdum = document.getString("yurt") ?: ""
                if (benimYurdum.isBlank()) {
                    Toast.makeText(this, "Yurt bilgisi boş! (users/yurt alanını kontrol et)", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                var sorgu = firestore.collection("ilanlar")
                    .whereEqualTo("yurt", benimYurdum)

                if (kategori != null) {
                    sorgu = sorgu.whereEqualTo("kategori", kategori)
                }

                sorgu.get()
                    .addOnSuccessListener { result ->
                        ilanListesi.clear()
                        for (doc in result) {
                            val ilan = doc.toObject(Ilan::class.java)
                            ilanListesi.add(ilan)
                        }
                        ilanAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Sorgu Hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "User Okuma Hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
