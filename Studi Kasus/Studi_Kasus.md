# 🛒 Studi Kasus: Kopma Mart (Koperasi Mahasiswa) POS — Kasir Cepat Minimarket Kampus

## 1. Latar Belakang
Kasir Koperasi Mahasiswa (Kopma Mart) memerlukan transaksi kasir yang cepat, dapat mengurangi stok secara otomatis, dan mendukung pencarian kode barcode.

## 2. Rancangan OOP (Object-Oriented Programming)
- **Class `Barang`**: 
  - Atribut: `barcodeId`, `namaBarang`, `kategori`, `hargaJual`, `stokGudang`
  - Method: `kurangiStok(int jumlah)`, `tambahStok(int jumlah)`, method formatting & parser teks
- **Class `ItemBelanja`**:
  - Atribut: `barang` (`Barang`), `jumlahBeli` (`int`)
  - Method: `hitungSubtotal()`, `tambahJumlah(int qty)`
- **Class `KeranjangBelanja`**: 
  - Atribut: `daftarItem` (`List<ItemBelanja>`)
  - Method: `tambahItem()`, `hapusItem()`, `hitungSubtotal()`, `kosongkanKeranjang()`
- **Class `TransaksiPenjualan`**: 
  - Atribut: `noNota`, `waktu`, `totalBayar`, `nominalTunai`, `kembalian`
  - Method: `generateStruk()`, export data riwayat rekap
- **Class `DataManager`**:
  - Mengelola data persistence untuk `master_stok_barang.txt` dan `rekap_transaksi_kasir.txt`

## 3. Antarmuka GUI (Java Swing)
- **Input Barcode Cepat**: Kolom teks input kode barcode dengan listener tombol Enter (otomatis menambah item ke tabel tanpa perlu klik mouse).
- **Display Total Bayar**: Panel ringkasan total bayar dengan huruf berukuran besar (*Font Size 28*).
- **Validasi Stok Real-Time**: Memunculkan peringatan jika pembelian melebihi stok yang ada di gudang, dan memblokir penambahan jika stok habis.
- **Tabel Kasir Interaktif**: Menampilkan daftar belanjaan, kuantitas, harga satuan, dan subtotal.
- **Struk Kasir Digital**: Menampilkan nota thermal belanja lengkap dengan rincian pembelian, uang tunai, dan kembalian.

## 4. Data Persistence
- File `master_stok_barang.txt`: Database persediaan barang; stok otomatis berkurang saat transaksi berhasil diselesaikan.
- File `rekap_transaksi_kasir.txt`: File rekapitulasi audit dan riwayat transaksi kasir (Append mode).

## 5. WOW Factor Demo Expo
- Memperagakan pengetikan kode barcode lalu menekan Enter, barang seketika muncul di baris tabel kasir dan stok gudang otomatis berkurang.
- Tombol Quick Barcode Demo untuk presentasi interaktif di depan penguji / dosen.
- Audio feedback berupa bunyi scanner beep dan suara sukses transaksi.
