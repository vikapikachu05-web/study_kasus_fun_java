import java.util.ArrayList;
import java.util.List;

/**
 * Class KeranjangBelanja
 * Sesuai spesifikasi OOP:
 * - atribut: daftarItem
 * - method: tambahItem(), hapusItem(), hitungSubtotal()
 */
public class KeranjangBelanja {
    private List<ItemBelanja> daftarItem;

    public KeranjangBelanja() {
        this.daftarItem = new ArrayList<>();
    }

    public List<ItemBelanja> getDaftarItem() {
        return daftarItem;
    }

    /**
     * Menambahkan barang ke dalam keranjang.
     * Jika barang sudah ada di keranjang, jumlahnya diakumulasikan.
     * Mengembalikan true jika sukses.
     */
    public boolean tambahItem(Barang barang, int qty) {
        if (barang == null || qty <= 0) return false;

        // Cek apakah item sudah ada di dalam keranjang
        for (ItemBelanja item : daftarItem) {
            if (item.getBarang().getBarcodeId().equalsIgnoreCase(barang.getBarcodeId())) {
                item.tambahJumlah(qty);
                return true;
            }
        }

        // Jika belum ada, buat entri item baru
        daftarItem.add(new ItemBelanja(barang, qty));
        return true;
    }

    /**
     * Overload method tambahItem dengan default 1 qty
     */
    public boolean tambahItem(Barang barang) {
        return tambahItem(barang, 1);
    }

    /**
     * Menghapus item dari keranjang berdasarkan barcodeId.
     */
    public boolean hapusItem(String barcodeId) {
        if (barcodeId == null) return false;
        return daftarItem.removeIf(item -> item.getBarang().getBarcodeId().equalsIgnoreCase(barcodeId));
    }

    /**
     * Menghapus item dari keranjang berdasarkan index baris tabel.
     */
    public boolean hapusItem(int index) {
        if (index >= 0 && index < daftarItem.size()) {
            daftarItem.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Mengubah kuantitas item pada keranjang
     */
    public boolean ubahKuantitas(String barcodeId, int qtyBaru) {
        for (ItemBelanja item : daftarItem) {
            if (item.getBarang().getBarcodeId().equalsIgnoreCase(barcodeId)) {
                if (qtyBaru <= 0) {
                    hapusItem(barcodeId);
                } else {
                    item.setJumlahBeli(qtyBaru);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Menghitung total akumulasi subtotal seluruh item di keranjang.
     */
    public double hitungSubtotal() {
        double total = 0;
        for (ItemBelanja item : daftarItem) {
            total += item.hitungSubtotal();
        }
        return total;
    }

    /**
     * Format total subtotal menjadi Rupiah
     */
    public String getFormattedSubtotal() {
        return String.format("Rp %,.0f", hitungSubtotal()).replace(',', '.');
    }

    /**
     * Menghitung total pcs barang belanjaan
     */
    public int getTotalPcs() {
        int total = 0;
        for (ItemBelanja item : daftarItem) {
            total += item.getJumlahBeli();
        }
        return total;
    }

    /**
     * Menghitung total jenis item yang ada di keranjang
     */
    public int getJumlahJenisItem() {
        return daftarItem.size();
    }

    /**
     * Mengosongkan seluruh isi keranjang belanja
     */
    public void kosongkanKeranjang() {
        daftarItem.clear();
    }

    /**
     * Mencari kuantitas barang yang saat ini sudah masuk ke keranjang
     */
    public int getKuantitasSaatIni(String barcodeId) {
        for (ItemBelanja item : daftarItem) {
            if (item.getBarang().getBarcodeId().equalsIgnoreCase(barcodeId)) {
                return item.getJumlahBeli();
            }
        }
        return 0;
    }
}
