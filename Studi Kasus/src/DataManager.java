import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class DataManager
 * Mengelola Data Persistence:
 * 1. master_stok_barang.txt : Database inventaris dan stok barang. Stok berkurang saat transaksi berhasil.
 * 2. rekap_transaksi_kasir.txt : Log riwayat transaksi kasir untuk pelaporan & audit omset.
 */
public class DataManager {
    public static final String FILE_MASTER_STOK = "master_stok_barang.txt";
    public static final String FILE_REKAP_TRANSAKSI = "rekap_transaksi_kasir.txt";

    /**
     * Membaca seluruh data barang dari file master_stok_barang.txt.
     * Jika file belum ada, akan membuat file default dengan data minimarket kampus.
     */
    public static Map<String, Barang> muatDataBarang() {
        Map<String, Barang> katalog = new LinkedHashMap<>();
        File file = new File(FILE_MASTER_STOK);

        if (!file.exists()) {
            inisialisasiMasterStokDefault();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                Barang b = Barang.fromFileFormat(line);
                if (b != null) {
                    katalog.put(b.getBarcodeId().toUpperCase(), b);
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca file master stok: " + e.getMessage());
        }

        return katalog;
    }

    /**
     * Menyimpan seluruh perubahan data barang (termasuk pengurangan stok)
     * ke dalam file master_stok_barang.txt.
     */
    public static synchronized boolean simpanDataBarang(Map<String, Barang> katalog) {
        File file = new File(FILE_MASTER_STOK);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            writer.write("# MASTER DATA STOK BARANG - KOPMA MART\n");
            writer.write("# Format: barcodeId|namaBarang|kategori|hargaJual|stokGudang\n");
            for (Barang b : katalog.values()) {
                writer.write(b.toFileFormat());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Gagal menyimpan perubahan master stok: " + e.getMessage());
            return false;
        }
    }

    /**
     * Menambahkan catatan transaksi baru ke file rekap_transaksi_kasir.txt (Append).
     */
    public static synchronized boolean catatTransaksi(TransaksiPenjualan transaksi) {
        File file = new File(FILE_REKAP_TRANSAKSI);
        boolean isNew = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (isNew) {
                writer.write("# REKAP TRANSAKSI PENJUALAN - KOPMA MART\n");
                writer.write("# Format: noNota|waktu|totalBayar|nominalTunai|kembalian|itemDetails[barcode:nama:qty:subtotal;...]\n");
            }
            writer.write(transaksi.toRekapString());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Gagal mencatat transaksi ke rekap: " + e.getMessage());
            return false;
        }
    }

    /**
     * Membaca seluruh baris rekap transaksi
     */
    public static List<String> muatRekapTransaksiRaw() {
        List<String> list = new ArrayList<>();
        File file = new File(FILE_REKAP_TRANSAKSI);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca rekap transaksi: " + e.getMessage());
        }
        return list;
    }

    /**
     * Inisialisasi awal produk minimarket Kopma Mart jika belum ada file.
     */
    private static void inisialisasiMasterStokDefault() {
        List<Barang> defaultItems = new ArrayList<>();
        // Makanan
        defaultItems.add(new Barang("8991001", "Indomie Goreng Spesial", "Makanan", 3500, 50));
        defaultItems.add(new Barang("8991002", "Indomie Kuah Ayam Bawang", "Makanan", 3500, 45));
        defaultItems.add(new Barang("8991003", "Pop Mie Ayam Spesial 75g", "Makanan", 6000, 30));
        defaultItems.add(new Barang("8991004", "Beng-Beng Wafer Cokelat 25g", "Makanan", 2500, 60));
        defaultItems.add(new Barang("8991005", "Oreo Sandwich Vanilla 133g", "Makanan", 9000, 25));
        defaultItems.add(new Barang("8991006", "Roti Aoka Panggang Cokelat", "Makanan", 3000, 40));

        // Minuman
        defaultItems.add(new Barang("8992001", "Air Mineral Aqua 600ml", "Minuman", 4000, 80));
        defaultItems.add(new Barang("8992002", "Teh Pucuk Harum 350ml", "Minuman", 4500, 50));
        defaultItems.add(new Barang("8992003", "Ultra Milk Cokelat 250ml", "Minuman", 6500, 35));
        defaultItems.add(new Barang("8992004", "Kopi Good Day Freeze 250ml", "Minuman", 7000, 30));
        defaultItems.add(new Barang("8992005", "Pocari Sweat Can 330ml", "Minuman", 7500, 25));
        defaultItems.add(new Barang("8992006", "Nescafe Can Coffee 240ml", "Minuman", 9500, 20));

        // Alat Tulis (ATK Kuliah)
        defaultItems.add(new Barang("8993001", "Pulpen Faster C600 Hitam", "Alat Tulis", 4000, 100));
        defaultItems.add(new Barang("8993002", "Pulpen Gel Standard AE7 Hitam", "Alat Tulis", 3500, 85));
        defaultItems.add(new Barang("8993003", "Buku Tulis Sinar Dunia 38 Lbr", "Alat Tulis", 5500, 40));
        defaultItems.add(new Barang("8993004", "Buku Folio Bergaris 100 Lbr", "Alat Tulis", 16000, 20));
        defaultItems.add(new Barang("8993005", "Tipe-X Joyko Kertas (Correction Tape)", "Alat Tulis", 8500, 25));
        defaultItems.add(new Barang("8993006", "Pensil 2B Faber-Castell", "Alat Tulis", 4500, 40));
        defaultItems.add(new Barang("8993007", "Stabilo Boss Highlighter Kuning", "Alat Tulis", 10500, 15));

        // Kebutuhan Kampus & Personal Care
        defaultItems.add(new Barang("8994001", "Tissue Paseo Smart 250 Sheets", "Kebutuhan", 12500, 20));
        defaultItems.add(new Barang("8994002", "Handsanitizer Dettol 50ml", "Kebutuhan", 11000, 25));
        defaultItems.add(new Barang("8994003", "Stopmap Kertas Folio Biru", "Kebutuhan", 2000, 60));
        defaultItems.add(new Barang("8994004", "Map Plastik L-Folder Bening", "Kebutuhan", 3500, 50));

        Map<String, Barang> map = new LinkedHashMap<>();
        for (Barang b : defaultItems) {
            map.put(b.getBarcodeId().toUpperCase(), b);
        }
        simpanDataBarang(map);
    }
}
