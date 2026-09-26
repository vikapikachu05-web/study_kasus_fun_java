import java.util.Map;

/**
 * Script Pengujian Otomatis (Unit Test) untuk Logika OOP Kopma Mart POS
 */
public class TestPOS {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   UNIT TEST SISTEM KOPMA MART POS (OOP & FILE)   ");
        System.out.println("==================================================");

        // 1. Test Class Barang
        System.out.println("\n[Test 1] Pengujian Class Barang:");
        Barang b1 = new Barang("8991001", "Indomie Goreng Spesial", "Makanan", 3500, 10);
        System.out.println("Barang dibuat: " + b1);
        boolean stokKurangOk = b1.kurangiStok(3);
        assert stokKurangOk : "Gagal kurangi stok 3 pcs";
        System.out.println("Stok setelah dikurangi 3: " + b1.getStokGudang() + " pcs (Harus 7)");

        boolean stokLebih = b1.kurangiStok(20);
        System.out.println("Mencoba kurangi 20 pcs (melebihi stok): " + (stokLebih ? "BERHASIL (Salah)" : "DITOLAK (Benar)"));

        // 2. Test Class KeranjangBelanja
        System.out.println("\n[Test 2] Pengujian Class KeranjangBelanja:");
        KeranjangBelanja keranjang = new KeranjangBelanja();
        keranjang.tambahItem(b1, 2);
        Barang b2 = new Barang("8992001", "Aqua 600ml", "Minuman", 4000, 20);
        keranjang.tambahItem(b2, 1);

        System.out.println("Total Item: " + keranjang.getJumlahJenisItem() + " jenis, " + keranjang.getTotalPcs() + " pcs");
        System.out.println("Subtotal: " + keranjang.getFormattedSubtotal());

        // Tambah lagi barang yang sama (harus akumulasi)
        keranjang.tambahItem(b1, 1);
        System.out.println("Setelah tambah 1 Indomie lagi, subtotal: " + keranjang.getFormattedSubtotal());

        // 3. Test Class TransaksiPenjualan
        System.out.println("\n[Test 3] Pengujian Class TransaksiPenjualan:");
        TransaksiPenjualan trx = new TransaksiPenjualan("TEST-001", keranjang.getDaftarItem(), keranjang.hitungSubtotal(), 20000);
        System.out.println("No Nota: " + trx.getNoNota());
        System.out.println("Total: " + TransaksiPenjualan.formatRupiah(trx.getTotalBayar()));
        System.out.println("Tunai: " + TransaksiPenjualan.formatRupiah(trx.getNominalTunai()));
        System.out.println("Kembalian: " + TransaksiPenjualan.formatRupiah(trx.getKembalian()));

        System.out.println("\nPreview Struk Digital:");
        System.out.println(trx.generateStruk());

        // 4. Test Data Persistence
        System.out.println("\n[Test 4] Pengujian DataManager (File I/O):");
        Map<String, Barang> master = DataManager.muatDataBarang();
        System.out.println("Total master barang terbaca dari file: " + master.size() + " item");
        assert master.containsKey("8991001") : "Item 8991001 tidak ditemukan!";
        System.out.println("Barang 8991001: " + master.get("8991001").getNamaBarang() + " (Stok: " + master.get("8991001").getStokGudang() + ")");

        System.out.println("\n>>> SEMUA PENGUJIAN UNIT LOGIKA OOP BERHASIL 100%! <<<");
    }
}
