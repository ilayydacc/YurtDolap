
# YurtDolap Mobil Uygulama

YurtDolap, üniversite öğrencilerinin kendi aralarında ikinci el eşya alım-satım ve kiralama yapabileceği, gerçek zamanlı mesajlaşma ve bildirim özelliklerine sahip modern bir Android uygulamasıdır.

## 🚀 Özellikler

- **Kullanıcı Kimlik Doğrulaması:** Firebase Auth ile e-posta ve şifre tabanlı güvenli giriş/kayıt sistemi.
- **İlan Yönetimi:** Fotoğraflı ilan ekleme, düzenleme ve silme.
- **Kategori ve Filtreleme:** İlanları kategorilere göre filtreleyebilme.
- **Gerçek Zamanlı Sohbet:** Alıcı ve satıcı arasında Firebase Firestore tabanlı anlık mesajlaşma.
- **Favoriler:** Beğenilen ilanları favorilere ekleme ve daha sonra görüntüleme.
- **Push Bildirimler:** Yeni mesaj ve güncellemeler için Firebase Cloud Messaging (FCM) entegrasyonu.
- **Karanlık Mod (Dark Mode):** Sistem temasına uygun veya manuel ayarlanabilen karanlık/aydınlık mod desteği.
- **Modern UI/UX:** Jetpack Compose ve Material Design 3 (M3) prensipleriyle tasarlanmış kullanıcı arayüzü.

## 🛠️ Teknolojiler & Mimari

Proje, modern Android geliştirme standartlarına uygun olarak **Clean Architecture** prensipleriyle (Domain, Data, Presentation katmanları) geliştirilmiştir.

- **Dil:** [Kotlin](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Mimari:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection:** [Dagger Hilt](https://dagger.dev/hilt/)
- **Asenkron İşlemler:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlin.org/docs/flow.html)
- **Görsel Yükleme:** [Coil](https://coil-kt.github.io/coil/)
- **Backend & Veritabanı:** [Firebase](https://firebase.google.com/) (Auth, Firestore, Storage, Cloud Messaging)
- **Navigasyon:** Compose Navigation API

## 📂 Proje Yapısı

```
app/src/main/java/com/yurtdolap/app/
├── data/           # Remote (Firebase) ve Local veri kaynakları, Repository implementasyonları
├── domain/         # Modeller, Repository arayüzleri ve UseCase'ler (İş mantığı)
├── presentation/   # UI Katmanı (Jetpack Compose ekranları, ViewModel'ler, Tema ve Navigasyon)
├── di/             # Hilt Dependency Injection modülleri
└── utils/          # Yardımcı fonksiyonlar, uzantılar ve sabitler
```

## 📱 Ekran Görüntüleri

*(Projenin açık kaynak veya tanıtım reposu ise buraya ekran görüntüleri eklenebilir)*

## ⚙️ Kurulum ve Çalıştırma

1. Projeyi bilgisayarınıza klonlayın:
   ```bash
   git clone https://github.com/kullaniciadi/YurtDolap.git
   ```
2. Projeyi **Android Studio** ile açın.
3. Firebase entegrasyonu için:
   - Firebase Console üzerinden yeni bir Android projesi oluşturun.
   - Uygulama paket adını (`com.yurtdolap.app`) girerek projenizi kaydedin.
   - İndirdiğiniz `google-services.json` dosyasını `app/` dizini altına yerleştirin.
   - Firebase Console'dan Authentication (E-posta/Şifre), Firestore Database ve Storage servislerini aktifleştirin.
4. Android Studio üzerinden projeyi build edin (Sync Project with Gradle Files).
5. Bir Android emülatöründe veya fiziksel cihazda projeyi çalıştırın (Run 'app').

## 📝 Lisans

Bu proje [MIT Lisansı](LICENSE) altında lisanslanmıştır. Daha fazla bilgi için `LICENSE` dosyasına bakabilirsiniz.
=======
# YurtDolap

