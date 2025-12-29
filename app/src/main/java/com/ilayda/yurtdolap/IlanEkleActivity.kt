package com.ilayda.yurtdolap

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class IlanEkleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var ivResim: ImageView
    private lateinit var btnKaydet: Button
    private lateinit var spKategori: Spinner

    private var secilenGorselUri: Uri? = null

    private val galeriBaslatici =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                secilenGorselUri = uri

                ivResim.setPadding(0, 0, 0, 0)
                ivResim.imageTintList = null

                Glide.with(this)
                    .load(uri)
                    .fitCenter()
                    .into(ivResim)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ilan_ekle)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val etAd = findViewById<EditText>(R.id.etIlanAd)
        val etFiyat = findViewById<EditText>(R.id.etIlanFiyat)
        val etAciklama = findViewById<EditText>(R.id.etIlanAciklama)

        spKategori = findViewById(R.id.spinnerKategori)

        btnKaydet = findViewById(R.id.btnIlanVer)
        ivResim = findViewById(R.id.ivIlanResim)

        val kategoriler = listOf(
            "Kategori Seçiniz",
            "Giyim",
            "Aksesuar",
            "Kitap/Kırtasiye",
            "Elektronik",
            "Ev Eşyası",
            "Yiyecek",
            "Diğer"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriler)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spKategori.adapter = adapter

        ivResim.setOnClickListener {
            galeriBaslatici.launch("image/*")
        }

        btnKaydet.setOnClickListener {

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Oturum süresi dolmuş, lütfen tekrar giriş yapın.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (secilenGorselUri == null) {
                Toast.makeText(this, "Lütfen ürünün fotoğrafını seçin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ad = etAd.text.toString().trim()
            val fiyatString = etFiyat.text.toString().trim()
            val aciklama = etAciklama.text.toString().trim()
            val kategori = spKategori.selectedItem?.toString() ?: "Diğer"

            if (ad.isEmpty() || fiyatString.isEmpty() || aciklama.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (kategori == "Kategori Seçiniz") {
                Toast.makeText(this, "Lütfen bir kategori seçin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fiyatInt = fiyatString.toIntOrNull()
            if (fiyatInt == null) {
                Toast.makeText(this, "Lütfen fiyata sadece sayı girin (Örn: 200)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // UI kilitle
            btnKaydet.isEnabled = false
            Toast.makeText(this, "İlan yükleniyor, lütfen bekleyin...", Toast.LENGTH_LONG).show()

            val uuid = UUID.randomUUID().toString()
            val resimAdi = "$uuid.jpg"
            val reference = storage.reference.child("ilan_resimleri/$resimAdi")

            // ✅ BURASI: TAM DÜZELTİLMİŞ YÜKLEME + LİNK ALMA BLOĞU

            reference.putFile(secilenGorselUri!!)
                .addOnSuccessListener {

                    reference.downloadUrl
                        .addOnSuccessListener { uri ->

                            veritabaninaKaydet(
                                ad = ad,
                                fiyat = fiyatInt,
                                aciklama = aciklama,
                                kategori = kategori,
                                gorselUrl = uri.toString(),
                                userId = currentUser.uid,
                                userEmail = currentUser.email ?: ""
                            )
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Link alınamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            btnKaydet.isEnabled = true
                        }

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Resim yüklenemedi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    btnKaydet.isEnabled = true
                }
        }
    }

    private fun veritabaninaKaydet(
        ad: String,
        fiyat: Int,
        aciklama: String,
        kategori: String,
        gorselUrl: String,
        userId: String,
        userEmail: String
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->

                var sehir = "Belirtilmemiş"
                var yurt = "Belirtilmemiş"

                if (document.exists()) {
                    sehir = document.getString("sehir") ?: "Belirtilmemiş"
                    yurt = document.getString("yurt") ?: "Belirtilmemiş"
                }

                val yeniIlan = Ilan(
                    ilanId = UUID.randomUUID().toString(),
                    urunAdi = ad,
                    fiyat = fiyat,
                    aciklama = aciklama,
                    saticiId = userId,
                    saticiEmail = userEmail,
                    gorselUrl = gorselUrl,
                    sehir = sehir,
                    yurt = yurt,
                    kategori = kategori
                )

                firestore.collection("ilanlar").document(yeniIlan.ilanId).set(yeniIlan)
                    .addOnSuccessListener {
                        Toast.makeText(this, "İlan başarıyla yayınlandı! 🎉", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Kayıt Hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        btnKaydet.isEnabled = true
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Veritabanı bağlantı hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                btnKaydet.isEnabled = true
            }
    }
}
