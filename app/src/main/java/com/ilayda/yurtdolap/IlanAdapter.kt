package com.ilayda.yurtdolap

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class IlanAdapter(private val ilanListesi: ArrayList<Ilan>) : RecyclerView.Adapter<IlanAdapter.IlanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IlanViewHolder {
        // Tasarım dosyasını (item_ilan.xml) bağlıyoruz
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ilan, parent, false)
        return IlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: IlanViewHolder, position: Int) {
        val ilan = ilanListesi[position]

        // --- VERİLERİ EKRANA YAZMA KISMI ---
        holder.tvAd.text = ilan.urunAdi
        holder.tvFiyat.text = "${ilan.fiyat} TL"


        holder.tvAciklama.text = ilan.aciklama


        // --- GLIDE İLE RESİM YÜKLEME KISMI ---
        if (ilan.gorselUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(ilan.gorselUrl) // Linki ver
                .centerCrop() // Kutuya sığdır
                .placeholder(android.R.drawable.ic_menu_gallery) // Yüklenirken bekleme ikonu
                .error(android.R.drawable.ic_menu_report_image) // Hata olursa kırmızı ikon
                .into(holder.ivResim) // Resmi koy
        } else {
            holder.ivResim.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // --- TIKLAMA OLAYI (Detay Sayfasına Git) ---
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, IlanDetayActivity::class.java)
            intent.putExtra("urunAdi", ilan.urunAdi)
            intent.putExtra("fiyat", ilan.fiyat)
            intent.putExtra("aciklama", ilan.aciklama)
            intent.putExtra("saticiEmail", ilan.saticiEmail)
            intent.putExtra("gorselUrl", ilan.gorselUrl)
            intent.putExtra("kategori", ilan.kategori)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return ilanListesi.size
    }

    // --- VIEWHOLDER (Tasarım Elemanlarını Tanımlama) ---
    class IlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAd: TextView = itemView.findViewById(R.id.tvUrunAdi)
        val tvFiyat: TextView = itemView.findViewById(R.id.tvFiyat)
        val ivResim: ImageView = itemView.findViewById(R.id.ivUrunResmi)


        val tvAciklama: TextView = itemView.findViewById(R.id.tvAciklama)
    }
}