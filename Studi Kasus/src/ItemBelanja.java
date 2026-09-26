/**
 * Class ItemBelanja
 * Merepresentasikan satu baris item dalam keranjang belanja kasir.
 */
public class ItemBelanja {
    private Barang barang;
    private int jumlahBeli;

    public ItemBelanja(Barang barang, int jumlahBeli) {
        this.barang = barang;
        this.jumlahBeli = jumlahBeli;
    }

    public Barang getBarang() {
        return barang;
    }

    public void setBarang(Barang barang) {
        this.barang = barang;
    }

    public int getJumlahBeli() {
        return jumlahBeli;
    }

    public void setJumlahBeli(int jumlahBeli) {
        this.jumlahBeli = jumlahBeli;
    }

    public void tambahJumlah(int qty) {
        this.jumlahBeli += qty;
    }

    /**
     * Menghitung subtotal belanja untuk item ini (hargaJual * jumlahBeli)
     */
    public double hitungSubtotal() {
        return barang.getHargaJual() * jumlahBeli;
    }

    public String getFormattedSubtotal() {
        return String.format("Rp %,.0f", hitungSubtotal()).replace(',', '.');
    }

    @Override
    public String toString() {
        return barang.getNamaBarang() + " x" + jumlahBeli + " = " + getFormattedSubtotal();
    }
}
