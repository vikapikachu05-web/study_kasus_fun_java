import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class TransaksiPenjualan
 * Sesuai spesifikasi OOP:
 * - noNota      : Nomor nota unik (misal: NOTA-20260926-001)
 * - waktu       : Waktu/tanggal terjadinya transaksi
 * - totalBayar  : Total nominal yang harus dibayar
 * - nominalTunai: Uang tunai yang diserahkan pelanggan
 * - kembalian   : Uang kembalian yang harus dikembalikan kasir
 */
public class TransaksiPenjualan {
    private String noNota;
    private String waktu;
    private double totalBayar;
    private double nominalTunai;
    private double kembalian;
    private List<ItemBelanja> daftarItemTerjual;

    public TransaksiPenjualan(String noNota, List<ItemBelanja> items, double totalBayar, double nominalTunai) {
        this.noNota = noNota;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.waktu = LocalDateTime.now().format(dtf);
        this.daftarItemTerjual = new ArrayList<>();
        // Salin item
        if (items != null) {
            for (ItemBelanja item : items) {
                this.daftarItemTerjual.add(new ItemBelanja(item.getBarang(), item.getJumlahBeli()));
            }
        }
        this.totalBayar = totalBayar;
        this.nominalTunai = nominalTunai;
        this.kembalian = Math.max(0, nominalTunai - totalBayar);
    }

    public TransaksiPenjualan(String noNota, String waktu, double totalBayar, double nominalTunai, double kembalian, List<ItemBelanja> items) {
        this.noNota = noNota;
        this.waktu = waktu;
        this.totalBayar = totalBayar;
        this.nominalTunai = nominalTunai;
        this.kembalian = kembalian;
        this.daftarItemTerjual = items != null ? items : new ArrayList<>();
    }

    // Getters and Setters
    public String getNoNota() {
        return noNota;
    }

    public void setNoNota(String noNota) {
        this.noNota = noNota;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public double getTotalBayar() {
        return totalBayar;
    }

    public void setTotalBayar(double totalBayar) {
        this.totalBayar = totalBayar;
    }

    public double getNominalTunai() {
        return nominalTunai;
    }

    public void setNominalTunai(double nominalTunai) {
        this.nominalTunai = nominalTunai;
        this.kembalian = Math.max(0, nominalTunai - totalBayar);
    }

    public double getKembalian() {
        return kembalian;
    }

    public void setKembalian(double kembalian) {
        this.kembalian = kembalian;
    }

    public List<ItemBelanja> getDaftarItemTerjual() {
        return daftarItemTerjual;
    }

    public static String formatRupiah(double nominal) {
        return String.format("Rp %,.0f", nominal).replace(',', '.');
    }

    /**
     * Menghasilkan teks struk kasir minimarket profesional (Digital Thermal Receipt)
     */
    public String generateStruk() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("               KOPMA MART                \n");
        sb.append("      Koperasi Mahasiswa Universitas     \n");
        sb.append("     Jl. Kampus Terpadu No. 1, Gd. SAC   \n");
        sb.append("          Telp: (021) 7890-KOPMA         \n");
        sb.append("=========================================\n");
        sb.append(String.format("No. Nota : %-28s\n", noNota));
        sb.append(String.format("Kasir    : %-28s\n", "Kasir-01 (Kopma)"));
        sb.append(String.format("Waktu    : %-28s\n", waktu));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s %8s\n", "Item", "Qty", "Harga", "Total"));
        sb.append("-----------------------------------------\n");

        int totalPcs = 0;
        for (ItemBelanja item : daftarItemTerjual) {
            String nama = item.getBarang().getNamaBarang();
            if (nama.length() > 18) {
                nama = nama.substring(0, 15) + "...";
            }
            sb.append(String.format("%-18s %4d %,8.0f %,8.0f\n",
                    nama,
                    item.getJumlahBeli(),
                    item.getBarang().getHargaJual(),
                    item.hitungSubtotal()));
            totalPcs += item.getJumlahBeli();
        }

        sb.append("-----------------------------------------\n");
        sb.append(String.format("Total Item (%d pcs)        : %12s\n", totalPcs, formatRupiah(totalBayar)));
        sb.append(String.format("Tunai                     : %12s\n", formatRupiah(nominalTunai)));
        sb.append(String.format("Kembali                   : %12s\n", formatRupiah(kembalian)));
        sb.append("=========================================\n");
        sb.append("       TERIMA KASIH ATAS KUNJUNGANNYA    \n");
        sb.append("    Mendukung Koperasi Mahasiswa Mandiri \n");
        sb.append("    Barang yang dibeli tidak dapat ditukar\n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    /**
     * Konversi data transaksi ke format baris rekap_transaksi_kasir.txt
     * Format: noNota|waktu|totalBayar|nominalTunai|kembalian|itemDetails
     * itemDetails format: [barcode:nama:qty:subtotal;...]
     */
    public String toRekapString() {
        StringBuilder itemsSummary = new StringBuilder();
        for (int i = 0; i < daftarItemTerjual.size(); i++) {
            ItemBelanja it = daftarItemTerjual.get(i);
            itemsSummary.append(it.getBarang().getBarcodeId())
                    .append(":")
                    .append(it.getBarang().getNamaBarang().replace("|", " ").replace(";", " "))
                    .append(":")
                    .append(it.getJumlahBeli())
                    .append(":")
                    .append((long)it.hitungSubtotal());
            if (i < daftarItemTerjual.size() - 1) {
                itemsSummary.append(";");
            }
        }
        return noNota + "|" + waktu + "|" + (long)totalBayar + "|" + (long)nominalTunai + "|" + (long)kembalian + "|" + itemsSummary.toString();
    }
}
