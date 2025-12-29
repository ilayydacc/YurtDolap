package com.ilayda.yurtdolap

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class IlanDetayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ilan_detay)

        // Tasarımdaki elemanları tanıyalım
        val ivResim = findViewById<ImageView>(R.id.ivDetayResim)
        val tvBaslik = findViewById<TextView>(R.id.tvDetayBaslik)
        val tvFiyat = findViewById<TextView>(R.id.tvDetayFiyat)
        val tvAciklama = findViewById<TextView>(R.id.tvDetayAciklama)
        val tvSatici = findViewById<TextView>(R.id.tvDetaySatici)

        // 1. ADIM: Verileri "Paket"ten (Intent) alıyoruz
        val baslik = intent.getStringExtra("urunAdi")
        val aciklama = intent.getStringExtra("aciklama")
        val satici = intent.getStringExtra("saticiEmail")
        val gorselUrl = intent.getStringExtra("gorselUrl") // Resim linkini aldık

        val fiyat = intent.getIntExtra("fiyat", 0)


        tvBaslik.text = baslik
        tvAciklama.text = aciklama
        tvSatici.text = "Satıcı: $satici"

        // Fiyatı "200 TL" formatında yaz
        tvFiyat.text = "$fiyat TL"
        // DENEMEEEEE
        //  Resmi Göster (Glide ile)
        if (gorselUrl != null && gorselUrl.isNotEmpty()) {
            Glide.with(this)
                .load(gorselUrl)
                .into(ivResim)
        } else {
            // Resim yoksa varsayılan bir şey göster
            ivResim.setImageResource(R.drawable.ic_launcher_background)
        }
    }
}