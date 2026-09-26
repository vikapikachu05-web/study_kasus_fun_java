/**
 * Class Barang
 * Merepresentasikan entitas barang di minimarket Kopma Mart.
 * Sesuai spesifikasi OOP:
 * - barcodeId : Kode unik identifikasi barang / barcode
 * - namaBarang: Nama produk barang
 * - kategori  : Kategori barang (Makanan, Minuman, Alat Tulis, Kebutuhan)
 * - hargaJual : Harga jual barang per unit (Rupiah)
 * - stokGudang: Jumlah persediaan barang di gudang/toko
 */
public class Barang {
    private String barcodeId;
    private String namaBarang;
    private String kategori;
    private double hargaJual;
    private int stokGudang;

    public Barang(String barcodeId, String namaBarang, String kategori, double hargaJual, int stokGudang) {
        this.barcodeId = barcodeId;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.hargaJual = hargaJual;
        this.stokGudang = stokGudang;
    }

    // Getter dan Setter
    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public int getStokGudang() {
        return stokGudang;
    }

    public void setStokGudang(int stokGudang) {
        this.stokGudang = Math.max(0, stokGudang);
    }

    /**
     * Mengurangi stok barang di gudang.
     * Mengembalikan true jika pengurangan berhasil, false jika stok tidak mencukupi.
     */
    public boolean kurangiStok(int jumlah) {
        if (jumlah <= 0) return false;
        if (this.stokGudang >= jumlah) {
            this.stokGudang -= jumlah;
            return true;
        }
        return false;
    }

    /**
     * Menambah stok barang di gudang.
     */
    public void tambahStok(int jumlah) {
        if (jumlah > 0) {
            this.stokGudang += jumlah;
        }
    }

    /**
     * Format harga menjadi string Rupiah (misal Rp 15.000)
     */
    public String getFormattedHarga() {
        return String.format("Rp %,.0f", hargaJual).replace(',', '.');
    }

    /**
     * Konversi ke baris file master_stok_barang.txt
     */
    public String toFileFormat() {
        return barcodeId + "|" + namaBarang + "|" + kategori + "|" + (long)hargaJual + "|" + stokGudang;
    }

    /**
     * Parsing baris dari file master_stok_barang.txt
     */
    public static Barang fromFileFormat(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            String barcode = parts[0].trim();
            String nama = parts[1].trim();
            String kat = parts[2].trim();
            double harga = Double.parseDouble(parts[3].trim());
            int stok = Integer.parseInt(parts[4].trim());
            return new Barang(barcode, nama, kat, harga, stok);
        }
        return null;
    }

    @Override
    public String toString() {
        return "[" + barcodeId + "] " + namaBarang + " (" + kategori + ") - " + getFormattedHarga() + " (Stok: " + stokGudang + ")";
    }
}
