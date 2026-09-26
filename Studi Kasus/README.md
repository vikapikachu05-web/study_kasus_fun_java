# 🛒 Kopma Mart (Koperasi Mahasiswa) POS — Kasir Cepat Minimarket Kampus

Aplikasi Sistem Informasi Kasir Cepat (Point of Sale / POS) berbasis **Java Desktop (Swing)** dan **Object-Oriented Programming (OOP)**, dirancang khusus untuk operasional kasir Koperasi Mahasiswa dengan transaksi cepat barcode tanpa mouse, validasi stok gudang real-time, dan penyimpanan data persisten.

---

## 📋 Daftar Isi
1. [Latar Belakang & Fitur Utama](#-latar-belakang--fitur-utama)
2. [Rancangan OOP (Object-Oriented Programming)](#-rancangan-oop)
3. [Alur & Antarmuka GUI](#-antarmuka-gui)
4. [Data Persistence (File I/O)](#-data-persistence)
5. [Panduan Menjalankan Aplikasi](#-panduan-menjalankan-aplikasi)
6. [Skenario Demo Expo (WOW Factor)](#-skenario-demo-expo-wow-factor)

---

## 🚀 Latar Belakang & Fitur Utama
Kasir Koperasi Mahasiswa memerlukan transaksi yang sangat cepat pada jam istirahat kuliah:
- **Scan Barcode Cepat Tanpa Mouse:** Kasir cukup mengetik atau menembakkan scanner barcode lalu menekan tombol **Enter**, barang seketika masuk ke tabel transaksi.
- **Validasi Stok Otomatis:** Sistem secara proaktif memvalidasi sisa stok gudang. Jika stok habis atau permintaan melebihi sisa fisik di toko, sistem mengeluarkan peringatan tegas dan menolak transaksi berlebih.
- **Display Total Bayar Ukuran Besar (Font Size 28):** Tampilan nominal belanja sangat jelas terlihat oleh kasir dan pembeli.
- **Digital Thermal Receipt:** Cetak struk belanja ala minimarket asli lengkap dengan rincian per item, nomor nota unik, dan uang kembalian.
- **Audio Feedback:** Bunyi sintetis *beep* barcode dan *chime* sukses pembayaran untuk menambah kepuasan operasional kasir.

---

## 🏛️ Rancangan OOP

Struktur file program di dalam folder `src/`:

```
src/
├── Barang.java              # Entitas Barang / Produk Minimarket
├── ItemBelanja.java         # Objek Item di Keranjang Belanja
├── KeranjangBelanja.java    # Koleksi Item Belanja & Perhitungan Subtotal
├── TransaksiPenjualan.java  # Objek Transaksi, Pembayaran, Struk & Rekap
├── DataManager.java         # Pengelola File I/O master & rekap
├── KopmaMartPOS.java        # Antarmuka Utama GUI (Java Swing)
└── TestPOS.java             # Pengujian Unit Logika Bisnis & File I/O
```

### 1. Class `Barang`
- **Atribut:**
  - `barcodeId` (String): Kode unik barcode produk (misal: `8991001`).
  - `namaBarang` (String): Nama lengkap produk (misal: `Indomie Goreng Spesial`).
  - `kategori` (String): Kategori barang (`Makanan`, `Minuman`, `Alat Tulis`, `Kebutuhan`).
  - `hargaJual` (double): Harga jual per unit dalam Rupiah.
  - `stokGudang` (int): Jumlah stok fisik yang tersedia di toko/gudang.
- **Method Utama:**
  - `kurangiStok(int jumlah)`: Mengurangi stok gudang secara otomatis jika mencukupi.
  - `tambahStok(int jumlah)`: Menambah stok barang saat restock.
  - `getFormattedHarga()`: Menghasilkan format mata uang `Rp X.XXX`.
  - `toFileFormat()` & `fromFileFormat()`: Serialisasi/deserialisasi baris file teks.

### 2. Class `KeranjangBelanja`
- **Atribut:**
  - `daftarItem` (`List<ItemBelanja>`): Daftar seluruh barang yang sedang di-scan kasir.
- **Method Utama:**
  - `tambahItem(Barang barang, int qty)`: Menambahkan barang ke keranjang atau mengakumulasikan kuantitas jika barang sudah ada.
  - `hapusItem(String barcodeId)`: Menghapus item dari keranjang berdasarkan barcode.
  - `ubahKuantitas(String barcodeId, int newQty)`: Mengubah kuantitas item di keranjang.
  - `hitungSubtotal()`: Menghitung total keseluruhan nilai belanjaan.
  - `kosongkanKeranjang()`: Membersihkan keranjang setelah transaksi selesai.

### 3. Class `TransaksiPenjualan`
- **Atribut:**
  - `noNota` (String): Kode unik transaksi (format: `KM-YYYYMMDD-XXXX`).
  - `waktu` (String): Tanggal dan jam terjadinya transaksi.
  - `totalBayar` (double): Total nominal belanja yang harus dibayar.
  - `nominalTunai` (double): Uang tunai yang diserahkan pembeli.
  - `kembalian` (double): Uang kembali (`nominalTunai - totalBayar`).
  - `daftarItemTerjual` (`List<ItemBelanja>`): Snapshot barang yang dibeli.
- **Method Utama:**
  - `generateStruk()`: Menghasilkan format teks struk belanja minimarket rapi.
  - `toRekapString()`: Menghasilkan format satu baris untuk arsip `rekap_transaksi_kasir.txt`.

---

## 🖥️ Antarmuka GUI

Antarmuka dibangun menggunakan **Java Swing** dengan palet modern:
1. **Header Kasir:** Menampilkan identitas Kopma Mart, jam digital real-time, dan identitas kasir yang bertugas.
2. **Kolom Input Barcode & Listener Enter:**
   - Komponen `JTextField` yang mendengarkan event tombol `ENTER`.
   - Seketika tombol Enter ditekan: sistem mencari barang di `master_stok_barang.txt`, memvalidasi sisa stok gudang, membunyikan suara scanner (*beep*), menambahkan item ke tabel, memperbarui total belanja, mengosongkan kolom input, dan memfokuskan kembali kursor ke input barcode tanpa sentuhan mouse!
3. **Tombol Cepat Demo Expo (One-Click Scan):**
   - Baris tombol demo cepat (`[8991001] Indomie Goreng`, `[8992001] Aqua 600ml`, `[8992003] Ultra Milk`, `[8993001] Pulpen Faster`, dll.) untuk mempermudah peragaan di depan penguji/dosen.
4. **Panel Ringkasan Total Bayar:**
   - Kotak display warna gelap kontras dengan teks hijau neon berukuran **Font Size 28** bold.
5. **Panel Pembayaran & Uang Tunai:**
   - Input uang tunai dengan tombol pecahan cepat (`Uang Pas`, `Rp 10.000`, `Rp 20.000`, `Rp 50.000`, `Rp 100.000`).
   - Perhitungan uang kembalian secara *real-time* saat kasir mengetik nominal.
6. **Tab Master Stok Gudang:**
   - Menampilkan seluruh katalog barang, harga, sisa stok, status stok (`Aman`, `Menipis`, `[Habis]`), dan fitur penambahan stok gudang.
7. **Tab Rekap Transaksi Penjualan:**
   - Menampilkan riwayat transaksi kasir, akumulasi omset harian, dan detail barang terjual.

---

## 💾 Data Persistence

Aplikasi menyimpan seluruh data secara permanen dalam file teks:

### 1. `master_stok_barang.txt`
Menyimpan database produk dan kuantitas stok:
```text
# Format: barcodeId|namaBarang|kategori|hargaJual|stokGudang
8991001|Indomie Goreng Spesial|Makanan|3500|50
8992001|Air Mineral Aqua 600ml|Minuman|4000|80
8992003|Ultra Milk Cokelat 250ml|Minuman|6500|35
8993001|Pulpen Faster C600 Hitam|Alat Tulis|4000|100
```
> **Penting:** Stok pada file ini akan **otomatis berkurang** secara permanen saat transaksi pembayaran kasir berhasil diselesaikan.

### 2. `rekap_transaksi_kasir.txt`
Menyimpan riwayat seluruh penjualan kasir secara append:
```text
# Format: noNota|waktu|totalBayar|nominalTunai|kembalian|itemDetails[barcode:nama:qty:subtotal;...]
KM-20260926-0001|2026-09-26 08:15:20|14500|20000|5500|8991001:Indomie Goreng Spesial:2:7000;8992001:Air Mineral Aqua 600ml:1:4000
```

---

## 🏃 Panduan Menjalankan Aplikasi

### Cara 1: Menggunakan Script `run.bat` (Paling Cepat & Praktis)
Cukup klik ganda (double-click) file **`run.bat`** di folder proyek ini. Script akan otomatis mengompilasi seluruh source code dan langsung membuka aplikasi GUI.

### Cara 2: Melalui Terminal / Command Prompt
```bash
# Kompilasi source code Java
"C:\Program Files\Java\jdk-21.0.12\bin\javac.exe" -d bin -encoding UTF-8 src/*.java

# Jalankan aplikasi kasir
"C:\Program Files\Java\jdk-21.0.12\bin\java.exe" -cp bin KopmaMartPOS
```

### Cara 3: Menjalankan Unit Test Logika OOP
```bash
"C:\Program Files\Java\jdk-21.0.12\bin\java.exe" -cp bin TestPOS
```

---

## 🌟 Skenario Demo Expo (WOW Factor)

Untuk memukau dosen/juri saat sesi demonstrasi:
1. **Peragakan Input Barcode Cepat:**
   - Ketik barcode `8991001` pada kolom barcode, lalu tekan tombol **Enter**.
   - Tunjukkan bagaimana suara *beep* berbunyi, baris *Indomie Goreng Spesial* seketika muncul di tabel keranjang belanja, kolom input otomatis bersih kembali dan kursor tetap aktif di situ tanpa klik mouse!
2. **Peragakan Tambah Kuantitas Otomatis:**
   - Tekan Enter lagi atau ketik `8992001` lalu tekan Enter. Subtotal dan display **Font Size 28** langsung ter-update secara instan.
3. **Peragakan Validasi Stok:**
   - Tambah produk hingga melebihi sisa stok gudang, tunjukkan bagaimana sistem secara protektif menolak penambahan dengan kotak peringatan stok tidak mencukupi.
4. **Peragakan Pembayaran & Pengurangan Stok:**
   - Klik tombol pecahan cepat `Rp 50.000` atau ketik nominal uang.
   - Tekan **Enter** untuk menyelesaikan transaksi.
   - Struk kasir digital thermal muncul secara elegan.
   - Pindah ke tab **Master Stok Gudang** dan perlihatkan bahwa stok barang di `master_stok_barang.txt` telah **berkurang secara otomatis** dan tercatat di tab **Rekap Penjualan Kasir**!
#   s t u d y _ k a s u s _ f u n _ j a v a  
 