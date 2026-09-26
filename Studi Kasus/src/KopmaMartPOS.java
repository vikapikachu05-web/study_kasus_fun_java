import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Class KopmaMartPOS
 * Antarmuka GUI Utama Kasir Cepat Minimarket Koperasi Mahasiswa (Kopma Mart).
 * 
 * Fitur Utama:
 * 1. Kolom teks input kode barcode dengan listener tombol Enter (otomatis tambah item tanpa klik mouse).
 * 2. Panel ringkasan total bayar dengan huruf berukuran besar (Font Size 28).
 * 3. Validasi stok real-time (peringatan jika kuantitas melebihi stok gudang).
 * 4. Data Persistence:
 *    - master_stok_barang.txt (stok otomatis berkurang saat transaksi selesai).
 *    - rekap_transaksi_kasir.txt (mencatat semua riwayat penjualan kasir).
 * 5. Fitur WOW Factor Demo Expo:
 *    - Quick Demo Barcode buttons untuk demo expo instan.
 *    - Audio beep scan & suara kasir transaksi sukses.
 *    - Tampilan struk kasir digital thermal dengan opsi cetak/salin.
 *    - Tab Manajemen Master Stok & Riwayat Rekap Penjualan.
 */
public class KopmaMartPOS extends JFrame {

    // Palet Warna Modern POS
    private static final Color COLOR_PRIMARY = new Color(15, 23, 42);     // Slate 900
    private static final Color COLOR_HEADER = new Color(30, 41, 59);      // Slate 800
    private static final Color COLOR_ACCENT = new Color(13, 148, 136);    // Teal 600
    private static final Color COLOR_EMERALD = new Color(16, 185, 129);   // Emerald 500
    private static final Color COLOR_BG = new Color(241, 245, 249);       // Slate 100
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_TEXT_DARK = new Color(15, 23, 42);
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);
    private static final Color COLOR_DANGER = new Color(239, 68, 68);

    // Model dan State
    private Map<String, Barang> katalogBarang;
    private KeranjangBelanja keranjang;
    private int counterNota = 1;
    private String currentNoNota;

    // Komponen GUI Kasir Transaksi
    private JTextField txtBarcode;
    private JTable tblKeranjang;
    private DefaultTableModel modelKeranjang;
    private JLabel lblTotalBayar;
    private JLabel lblTotalItemCount;
    private JLabel lblNoNota;
    private JTextField txtNominalTunai;
    private JLabel lblKembalian;
    private JLabel lblStatusInfo;

    // Komponen Tab Stok & Rekap
    private JTable tblMasterStok;
    private DefaultTableModel modelMasterStok;
    private JTextField txtCariStok;
    private JTable tblRekap;
    private DefaultTableModel modelRekap;
    private JLabel lblTotalOmset;

    public KopmaMartPOS() {
        super("Kopma Mart (Koperasi Mahasiswa) POS - Kasir Cepat Minimarket Kampus");
        this.keranjang = new KeranjangBelanja();
        this.katalogBarang = DataManager.muatDataBarang();

        inisialisasiWindow();
        inisialisasiKomponen();
        generateNewNota();
        refreshTabelMasterStok();
        refreshTabelRekap();

        // Fokuskan kursor ke input barcode saat pertama kali terbuka
        SwingUtilities.invokeLater(() -> txtBarcode.requestFocusInWindow());
    }

    private void inisialisasiWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        try {
            // Terapkan look and feel modern
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        getContentPane().setBackground(COLOR_BG);
        setLayout(new BorderLayout());
    }

    private void inisialisasiKomponen() {
        // 1. Header Atas
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Tab Konten Utama
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(COLOR_BG);

        tabbedPane.addTab("Kasir Transaksi (POS)", createKasirPanel());
        tabbedPane.addTab("Master Stok Gudang", createMasterStokPanel());
        tabbedPane.addTab("Rekap Penjualan Kasir", createRekapPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // 3. Status Bar Bawah
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    /**
     * Membuat Header aplikasi dengan branding Kopma Mart dan jam digital
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_PRIMARY);
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Kiri: Logo & Nama
        JPanel pnlBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlBrand.setOpaque(false);

        JLabel lblLogo = new JLabel("[MART]");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlTitle.setOpaque(false);
        JLabel lblName = new JLabel("KOPMA MART POS");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Kasir Cepat Minimarket Kampus - Koperasi Mahasiswa Mandiri");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));

        pnlTitle.add(lblName);
        pnlTitle.add(lblSub);

        pnlBrand.add(lblLogo);
        pnlBrand.add(pnlTitle);

        // Kanan: Jam Digital & Status Kasir
        JPanel pnlRight = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlRight.setOpaque(false);

        JLabel lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblClock.setForeground(new Color(56, 189, 248)); // Sky 400
        lblClock.setHorizontalAlignment(SwingConstants.RIGHT);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm:ss", new Locale("id", "ID"));
        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> lblClock.setText(sdf.format(new Date())));
        clockTimer.start();
        lblClock.setText(sdf.format(new Date()));

        JLabel lblKasir = new JLabel("Kasir: Vika Nisa (Shift Pagi) | Loket 01");
        lblKasir.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKasir.setForeground(new Color(203, 213, 225));
        lblKasir.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlRight.add(lblClock);
        pnlRight.add(lblKasir);

        panel.add(pnlBrand, BorderLayout.WEST);
        panel.add(pnlRight, BorderLayout.EAST);

        return panel;
    }

    /**
     * Panel Utama: Transaksi Kasir
     */
    private JPanel createKasirPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel Kiri: Input Barcode + Tabel Belanja + Quick Demo Barcode
        JPanel pnlKiri = new JPanel(new BorderLayout(0, 12));
        pnlKiri.setOpaque(false);

        // 1. Barcode Bar (Input & Demo Ribbon)
        pnlKiri.add(createBarcodeSection(), BorderLayout.NORTH);

        // 2. Tabel Keranjang Belanja
        pnlKiri.add(createTabelKeranjangSection(), BorderLayout.CENTER);

        // Panel Kanan: Ringkasan Total Bayar (Font 28) + Pembayaran Tunai
        JPanel pnlKanan = createPanelRingkasanBayar();
        pnlKanan.setPreferredSize(new Dimension(390, 0));

        panel.add(pnlKiri, BorderLayout.CENTER);
        panel.add(pnlKanan, BorderLayout.EAST);

        return panel;
    }

    /**
     * Kolom teks input kode barcode dengan listener tombol Enter
     */
    private JPanel createBarcodeSection() {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setOpaque(false);

        // Card Input
        JPanel pnlInput = new JPanel(new BorderLayout(10, 0));
        pnlInput.setBackground(COLOR_CARD_BG);
        pnlInput.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblIcon = new JLabel("SCAN / INPUT BARCODE:");
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIcon.setForeground(COLOR_HEADER);

        txtBarcode = new JTextField();
        txtBarcode.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtBarcode.setForeground(COLOR_PRIMARY);
        txtBarcode.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_ACCENT, 2, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        txtBarcode.setToolTipText("Ketik kode barcode barang lalu tekan ENTER");

        // Action Listener tombol ENTER pada TextField Barcode
        txtBarcode.addActionListener(e -> prosesScanBarcode());

        // Tombol Bantuan Scan
        JButton btnScan = new JButton("Enter / Tambah");
        btnScan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnScan.setBackground(COLOR_ACCENT);
        btnScan.setForeground(Color.WHITE);
        btnScan.setFocusPainted(false);
        btnScan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnScan.addActionListener(e -> prosesScanBarcode());

        JButton btnCariBarang = new JButton("Katalog...");
        btnCariBarang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCariBarang.setFocusPainted(false);
        btnCariBarang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCariBarang.addActionListener(e -> bukaDialogPilihBarang());

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnCariBarang);
        pnlButtons.add(btnScan);

        pnlInput.add(lblIcon, BorderLayout.WEST);
        pnlInput.add(txtBarcode, BorderLayout.CENTER);
        pnlInput.add(pnlButtons, BorderLayout.EAST);

        // Ribbon Quick Demo Barcode (WOW Factor Demo Expo)
        JPanel pnlDemo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        pnlDemo.setOpaque(false);

        JLabel lblDemoTitle = new JLabel("DEMO EXPO QUICK SCAN (KLIK/ENTER):");
        lblDemoTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDemoTitle.setForeground(COLOR_TEXT_MUTED);
        pnlDemo.add(lblDemoTitle);

        // Tombol cepat demo
        pnlDemo.add(createQuickDemoButton("Indomie Goreng", "8991001"));
        pnlDemo.add(createQuickDemoButton("Aqua 600ml", "8992001"));
        pnlDemo.add(createQuickDemoButton("Ultra Milk", "8992003"));
        pnlDemo.add(createQuickDemoButton("Pulpen Faster", "8993001"));
        pnlDemo.add(createQuickDemoButton("Tipe-X Joyko", "8993005"));
        pnlDemo.add(createQuickDemoButton("Tissue Paseo", "8994001"));

        container.add(pnlInput, BorderLayout.NORTH);
        container.add(pnlDemo, BorderLayout.SOUTH);

        return container;
    }

    private JButton createQuickDemoButton(String label, String barcode) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(COLOR_HEADER);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Klik untuk scan langsung: " + barcode);

        btn.addActionListener(e -> {
            txtBarcode.setText(barcode);
            prosesScanBarcode();
        });
        return btn;
    }

    /**
     * Tabel Keranjang Belanja
     */
    private JPanel createTabelKeranjangSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Header Tabel Keranjang
        JPanel pnlHeaderTbl = new JPanel(new BorderLayout());
        pnlHeaderTbl.setOpaque(false);

        JLabel lblTblTitle = new JLabel("DAFTAR ITEM KERANJANG BELANJA");
        lblTblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTblTitle.setForeground(COLOR_HEADER);

        lblTotalItemCount = new JLabel("0 item (0 pcs)");
        lblTotalItemCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalItemCount.setForeground(COLOR_ACCENT);

        pnlHeaderTbl.add(lblTblTitle, BorderLayout.WEST);
        pnlHeaderTbl.add(lblTotalItemCount, BorderLayout.EAST);

        // Definisi Model Tabel
        String[] columns = {"No", "Barcode", "Nama Barang", "Kategori", "Harga Satuan", "Qty", "Subtotal"};
        modelKeranjang = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only di tabel, manipulasi via tombol
            }
        };

        tblKeranjang = new JTable(modelKeranjang);
        tblKeranjang.setRowHeight(32);
        tblKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblKeranjang.setShowGrid(true);
        tblKeranjang.setGridColor(new Color(241, 245, 249));

        // Styling Header Tabel
        JTableHeader th = tblKeranjang.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(COLOR_HEADER);
        th.setPreferredSize(new Dimension(0, 34));

        // Lebar kolom
        tblKeranjang.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblKeranjang.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblKeranjang.getColumnModel().getColumn(2).setPreferredWidth(230);
        tblKeranjang.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblKeranjang.getColumnModel().getColumn(4).setPreferredWidth(100);
        tblKeranjang.getColumnModel().getColumn(5).setPreferredWidth(55);
        tblKeranjang.getColumnModel().getColumn(6).setPreferredWidth(110);

        // Alignment kolom
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblKeranjang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblKeranjang.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblKeranjang.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tblKeranjang);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1));

        // Action Toolbar Bawah Tabel Keranjang
        JPanel pnlTool = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTool.setOpaque(false);

        JButton btnTambahQty = new JButton("[+] Tambah Qty");
        btnTambahQty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTambahQty.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTambahQty.addActionListener(e -> tambahQtyItemTerpilih());

        JButton btnKurangQty = new JButton("[-] Kurangi Qty");
        btnKurangQty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnKurangQty.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKurangQty.addActionListener(e -> kurangiQtyItemTerpilih());

        JButton btnHapusItem = new JButton("Hapus Item (Del)");
        btnHapusItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapusItem.setForeground(COLOR_DANGER);
        btnHapusItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapusItem.addActionListener(e -> hapusItemTerpilih());

        JButton btnReset = new JButton("Reset Keranjang (F5)");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetKeranjang());

        pnlTool.add(btnTambahQty);
        pnlTool.add(btnKurangQty);
        pnlTool.add(btnHapusItem);
        pnlTool.add(btnReset);

        // Shortcut keyboard Delete untuk hapus item
        tblKeranjang.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "hapusRow");
        tblKeranjang.getActionMap().put("hapusRow", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hapusItemTerpilih();
            }
        });

        panel.add(pnlHeaderTbl, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(pnlTool, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Panel Ringkasan Total Bayar dengan HURUF BESAR (Font Size 28) & Form Pembayaran Kasir
     */
    private JPanel createPanelRingkasanBayar() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // 1. DISPLAY TOTAL BAYAR DENGAN FONT SIZE 28
        JPanel pnlDisplayTotal = new JPanel(new BorderLayout(0, 4));
        pnlDisplayTotal.setBackground(COLOR_PRIMARY);
        pnlDisplayTotal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTag = new JLabel("TOTAL YANG HARUS DIBAYAR:");
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTag.setForeground(new Color(148, 163, 184)); // Slate 400

        // Label Total Bayar Berukuran FONT SIZE 28 Sesuai Spesifikasi!
        lblTotalBayar = new JLabel("Rp 0");
        lblTotalBayar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotalBayar.setForeground(new Color(52, 211, 153)); // Emerald Neon 400
        lblTotalBayar.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlDisplayTotal.add(lblTag, BorderLayout.NORTH);
        pnlDisplayTotal.add(lblTotalBayar, BorderLayout.CENTER);

        // 2. Form Pembayaran Kasir
        JPanel pnlFormBayar = new JPanel(new GridBagLayout());
        pnlFormBayar.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Row 1: Nomor Nota
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel lblNotaTitle = new JLabel("No. Nota:");
        lblNotaTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlFormBayar.add(lblNotaTitle, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        lblNoNota = new JLabel("NOTA-2026-0001");
        lblNoNota.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNoNota.setForeground(COLOR_ACCENT);
        pnlFormBayar.add(lblNoNota, gbc);

        // Row 2: Diskon / Promo Kopma (Member)
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblDiskon = new JLabel("Diskon Anggota:");
        lblDiskon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlFormBayar.add(lblDiskon, gbc);

        gbc.gridx = 1;
        JLabel lblDiskonVal = new JLabel("Rp 0 (Khusus Tunai)");
        lblDiskonVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiskonVal.setForeground(COLOR_TEXT_MUTED);
        pnlFormBayar.add(lblDiskonVal, gbc);

        // Row 3: Input Nominal Tunai
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblTunai = new JLabel("Nominal Tunai:");
        lblTunai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlFormBayar.add(lblTunai, gbc);

        gbc.gridx = 1;
        txtNominalTunai = new JTextField();
        txtNominalTunai.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtNominalTunai.setForeground(COLOR_PRIMARY);
        txtNominalTunai.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        // Hitung kembalian live saat mengetik nominal
        txtNominalTunai.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                hitungKembalianLive();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    prosesSelesaikanTransaksi();
                }
            }
        });
        pnlFormBayar.add(txtNominalTunai, gbc);

        // Row 4: Tombol Pecahan Tunai Cepat
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel pnlPecahan = new JPanel(new GridLayout(2, 3, 4, 4));
        pnlPecahan.setOpaque(false);
        pnlPecahan.add(createPecahanButton("Uang Pas", 0));
        pnlPecahan.add(createPecahanButton("Rp 10.000", 10000));
        pnlPecahan.add(createPecahanButton("Rp 20.000", 20000));
        pnlPecahan.add(createPecahanButton("Rp 50.000", 50000));
        pnlPecahan.add(createPecahanButton("Rp 100.000", 100000));
        pnlPecahan.add(createPecahanButton("Rp 200.000", 200000));
        pnlFormBayar.add(pnlPecahan, gbc);

        // Row 5: Display Kembalian
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel lblKembaliTitle = new JLabel("Kembalian:");
        lblKembaliTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlFormBayar.add(lblKembaliTitle, gbc);

        gbc.gridx = 1;
        lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblKembalian.setForeground(COLOR_EMERALD);
        lblKembalian.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlFormBayar.add(lblKembalian, gbc);

        // 3. Tombol Proses Transaksi (Besar & Menonjol)
        JButton btnProses = new JButton("PROSES BAYAR (ENTER)");
        btnProses.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnProses.setBackground(COLOR_EMERALD);
        btnProses.setForeground(Color.WHITE);
        btnProses.setPreferredSize(new Dimension(0, 52));
        btnProses.setFocusPainted(false);
        btnProses.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProses.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btnProses.addActionListener(e -> prosesSelesaikanTransaksi());

        // Assembly
        JPanel pnlAtas = new JPanel(new BorderLayout(0, 12));
        pnlAtas.setOpaque(false);
        pnlAtas.add(pnlDisplayTotal, BorderLayout.NORTH);
        pnlAtas.add(pnlFormBayar, BorderLayout.CENTER);

        panel.add(pnlAtas, BorderLayout.NORTH);
        panel.add(btnProses, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createPecahanButton(String text, double nominal) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(COLOR_HEADER);
        btn.setBorder(new LineBorder(new Color(203, 213, 225), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (nominal == 0) {
                // Uang Pas
                txtNominalTunai.setText(String.valueOf((long)keranjang.hitungSubtotal()));
            } else {
                txtNominalTunai.setText(String.valueOf((long)nominal));
            }
            hitungKembalianLive();
        });
        return btn;
    }

    /**
     * Tab: Master Stok Gudang
     */
    private JPanel createMasterStokPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Panel Stok
        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setOpaque(false);

        JPanel pnlCari = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlCari.setOpaque(false);
        JLabel lblCari = new JLabel("Cari Barang / Barcode:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtCariStok = new JTextField(20);
        txtCariStok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCariStok.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTabelMasterStok(txtCariStok.getText());
            }
        });
        pnlCari.add(lblCari);
        pnlCari.add(txtCariStok);

        JPanel pnlAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlAksi.setOpaque(false);

        JButton btnTambahStok = new JButton("+ Tambah Stok Barang");
        btnTambahStok.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTambahStok.setBackground(COLOR_ACCENT);
        btnTambahStok.setForeground(Color.WHITE);
        btnTambahStok.setFocusPainted(false);
        btnTambahStok.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTambahStok.addActionListener(e -> dialogTambahStok());

        JButton btnReloadStok = new JButton("Muat Ulang File");
        btnReloadStok.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReloadStok.setFocusPainted(false);
        btnReloadStok.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReloadStok.addActionListener(e -> {
            this.katalogBarang = DataManager.muatDataBarang();
            refreshTabelMasterStok();
            JOptionPane.showMessageDialog(this, "Master stok berhasil dimuat ulang dari " + DataManager.FILE_MASTER_STOK, "Informasi", JOptionPane.INFORMATION_MESSAGE);
        });

        pnlAksi.add(btnTambahStok);
        pnlAksi.add(btnReloadStok);

        pnlTop.add(pnlCari, BorderLayout.WEST);
        pnlTop.add(pnlAksi, BorderLayout.EAST);

        // Tabel Master Stok
        String[] columns = {"Barcode ID", "Nama Barang", "Kategori", "Harga Jual", "Stok Gudang", "Status Stok"};
        modelMasterStok = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblMasterStok = new JTable(modelMasterStok);
        tblMasterStok.setRowHeight(30);
        tblMasterStok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblMasterStok.setShowGrid(true);
        tblMasterStok.setGridColor(new Color(241, 245, 249));

        JTableHeader th = tblMasterStok.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(248, 250, 252));
        th.setPreferredSize(new Dimension(0, 32));

        // Center / Right alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblMasterStok.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblMasterStok.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblMasterStok.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblMasterStok.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblMasterStok.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tblMasterStok);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1));

        JLabel lblInfoStok = new JLabel("Data tersimpan secara persisten di: " + (new File(DataManager.FILE_MASTER_STOK).getAbsolutePath()));
        lblInfoStok.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfoStok.setForeground(COLOR_TEXT_MUTED);

        panel.add(pnlTop, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(lblInfoStok, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Tab: Rekap Penjualan Kasir
     */
    private JPanel createRekapPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Rekap
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("RIWAYAT TRANSAKSI PENJUALAN KASIR");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(COLOR_HEADER);

        JPanel pnlKanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlKanan.setOpaque(false);

        lblTotalOmset = new JLabel("Total Omset: Rp 0");
        lblTotalOmset.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalOmset.setForeground(COLOR_ACCENT);

        JButton btnReloadRekap = new JButton("Refresh Rekap");
        btnReloadRekap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReloadRekap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReloadRekap.addActionListener(e -> refreshTabelRekap());

        pnlKanan.add(lblTotalOmset);
        pnlKanan.add(btnReloadRekap);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlKanan, BorderLayout.EAST);

        // Tabel Rekap
        String[] columns = {"No. Nota", "Waktu Transaksi", "Total Bayar", "Nominal Tunai", "Kembalian", "Rincian Item"};
        modelRekap = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblRekap = new JTable(modelRekap);
        tblRekap.setRowHeight(30);
        tblRekap.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblRekap.setShowGrid(true);
        tblRekap.setGridColor(new Color(241, 245, 249));

        JTableHeader th = tblRekap.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setBackground(new Color(248, 250, 252));
        th.setPreferredSize(new Dimension(0, 32));

        // Lebar kolom
        tblRekap.getColumnModel().getColumn(0).setPreferredWidth(140);
        tblRekap.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblRekap.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblRekap.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblRekap.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblRekap.getColumnModel().getColumn(5).setPreferredWidth(350);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblRekap.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tblRekap.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tblRekap.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(tblRekap);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1));

        JLabel lblInfoRekap = new JLabel("Data tersimpan secara append di: " + (new File(DataManager.FILE_REKAP_TRANSAKSI).getAbsolutePath()));
        lblInfoRekap.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfoRekap.setForeground(COLOR_TEXT_MUTED);

        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(lblInfoRekap, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Status Bar Bawah
     */
    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(226, 232, 240));
        bar.setBorder(new EmptyBorder(6, 16, 6, 16));

        lblStatusInfo = new JLabel("Siap melayani transaksi. Arahkan scanner atau ketik barcode.");
        lblStatusInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatusInfo.setForeground(COLOR_HEADER);

        JLabel lblShortcuts = new JLabel("[ENTER] Scan/Tambah | [Del] Hapus Item | [F5] Reset Keranjang");
        lblShortcuts.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblShortcuts.setForeground(COLOR_TEXT_MUTED);

        bar.add(lblStatusInfo, BorderLayout.WEST);
        bar.add(lblShortcuts, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // LOGIKA BISNIS & OPERASI POS
    // =========================================================================

    /**
     * Listener Tombol Enter pada Teks Barcode:
     * 1. Mencari barang berdasarkan barcode.
     * 2. Validasi stok gudang.
     * 3. Jika valid, otomatis menambah item ke tabel tanpa perlu klik mouse!
     * 4. Memutar audio beep dan memfokuskan kembali ke input barcode.
     */
    private void prosesScanBarcode() {
        String input = txtBarcode.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        // Cari di katalog
        Barang barang = katalogBarang.get(input.toUpperCase());
        if (barang == null) {
            playBeepError();
            JOptionPane.showMessageDialog(this,
                    "[!] Barcode [" + input + "] TIDAK DITEMUKAN!\n\nSilakan periksa kembali kode barcode atau gunakan tombol 'Katalog...' untuk mencari.",
                    "Barcode Tidak Ditemukan",
                    JOptionPane.ERROR_MESSAGE);
            txtBarcode.selectAll();
            txtBarcode.requestFocusInWindow();
            return;
        }

        // VALIDASI STOK: Cek stok yang ada di gudang vs kuantitas di keranjang
        int qtySaatIni = keranjang.getKuantitasSaatIni(barang.getBarcodeId());
        int stokTersedia = barang.getStokGudang();

        if (stokTersedia <= 0) {
            playBeepError();
            JOptionPane.showMessageDialog(this,
                    "[PERINGATAN] STOK HABIS!\n\nBarang: " + barang.getNamaBarang() + " [" + barang.getBarcodeId() + "]\nStok Gudang saat ini: 0 pcs.\nTransaksi untuk barang ini tidak dapat dilanjutkan.",
                    "Peringatan Stok Habis",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.setText("");
            txtBarcode.requestFocusInWindow();
            return;
        }

        if (qtySaatIni + 1 > stokTersedia) {
            playBeepError();
            JOptionPane.showMessageDialog(this,
                    "[PERINGATAN] STOK TIDAK MENCUKUPI!\n\nBarang: " + barang.getNamaBarang() + " [" + barang.getBarcodeId() + "]\n" +
                            "Stok Gudang: " + stokTersedia + " pcs\n" +
                            "Sudah di Keranjang: " + qtySaatIni + " pcs\n\n" +
                            "Penambahan melebihi stok yang ada!",
                    "Validasi Stok Gudang",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.setText("");
            txtBarcode.requestFocusInWindow();
            return;
        }

        // Tambah ke keranjang
        keranjang.tambahItem(barang, 1);
        playBeepSuccess();

        // Refresh UI
        refreshTabelKeranjang();

        // Update status info
        lblStatusInfo.setText("[OK] " + barang.getNamaBarang() + " berhasil ditambahkan. (Stok sisa: " + (stokTersedia - (qtySaatIni + 1)) + " pcs)");

        // Kosongkan textfield dan kembalikan fokus agar siap scan barcode berikutnya seketika!
        txtBarcode.setText("");
        txtBarcode.requestFocusInWindow();
    }

    /**
     * Memperbarui tabel keranjang belanja dan panel ringkasan total bayar (Font 28)
     */
    private void refreshTabelKeranjang() {
        modelKeranjang.setRowCount(0);
        List<ItemBelanja> list = keranjang.getDaftarItem();

        int no = 1;
        for (ItemBelanja item : list) {
            Barang b = item.getBarang();
            modelKeranjang.addRow(new Object[]{
                    no++,
                    b.getBarcodeId(),
                    b.getNamaBarang(),
                    b.getKategori(),
                    b.getFormattedHarga(),
                    item.getJumlahBeli(),
                    item.getFormattedSubtotal()
            });
        }

        // UPDATE PANEL RINGKASAN TOTAL BAYAR (FONT SIZE 28)
        lblTotalBayar.setText(keranjang.getFormattedSubtotal());
        lblTotalItemCount.setText(keranjang.getJumlahJenisItem() + " item (" + keranjang.getTotalPcs() + " pcs)");

        hitungKembalianLive();
    }

    /**
     * Menghitung uang kembalian secara real-time berdasarkan input nominal tunai
     */
    private void hitungKembalianLive() {
        double total = keranjang.hitungSubtotal();
        String tunaiStr = txtNominalTunai.getText().trim().replace(".", "").replace(",", "");

        if (tunaiStr.isEmpty()) {
            lblKembalian.setText("Rp 0");
            lblKembalian.setForeground(COLOR_EMERALD);
            return;
        }

        try {
            double tunai = Double.parseDouble(tunaiStr);
            double kembali = tunai - total;
            if (kembali >= 0) {
                lblKembalian.setText(TransaksiPenjualan.formatRupiah(kembali));
                lblKembalian.setForeground(COLOR_EMERALD);
            } else {
                lblKembalian.setText("Kurang " + TransaksiPenjualan.formatRupiah(Math.abs(kembali)));
                lblKembalian.setForeground(COLOR_DANGER);
            }
        } catch (NumberFormatException e) {
            lblKembalian.setText("Nominal tidak valid");
            lblKembalian.setForeground(COLOR_DANGER);
        }
    }

    /**
     * Tambah kuantitas item baris terpilih pada tabel keranjang
     */
    private void tambahQtyItemTerpilih() {
        int selectedRow = tblKeranjang.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu item pada tabel keranjang!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String barcode = (String) modelKeranjang.getValueAt(selectedRow, 1);
        Barang b = katalogBarang.get(barcode);
        if (b != null) {
            int qtySaatIni = keranjang.getKuantitasSaatIni(barcode);
            if (qtySaatIni + 1 > b.getStokGudang()) {
                playBeepError();
                JOptionPane.showMessageDialog(this,
                        "[PERINGATAN] Tidak dapat menambah lagi!\nStok gudang hanya tersedia " + b.getStokGudang() + " pcs.",
                        "Validasi Stok",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            keranjang.tambahItem(b, 1);
            refreshTabelKeranjang();
            tblKeranjang.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    /**
     * Kurangi kuantitas item baris terpilih pada tabel keranjang
     */
    private void kurangiQtyItemTerpilih() {
        int selectedRow = tblKeranjang.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu item pada tabel keranjang!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String barcode = (String) modelKeranjang.getValueAt(selectedRow, 1);
        int qtySaatIni = keranjang.getKuantitasSaatIni(barcode);
        if (qtySaatIni <= 1) {
            hapusItemTerpilih();
        } else {
            keranjang.ubahKuantitas(barcode, qtySaatIni - 1);
            refreshTabelKeranjang();
            if (selectedRow < tblKeranjang.getRowCount()) {
                tblKeranjang.setRowSelectionInterval(selectedRow, selectedRow);
            }
        }
    }

    /**
     * Hapus item dari keranjang
     */
    private void hapusItemTerpilih() {
        int selectedRow = tblKeranjang.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu item pada tabel keranjang!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String barcode = (String) modelKeranjang.getValueAt(selectedRow, 1);
        String nama = (String) modelKeranjang.getValueAt(selectedRow, 2);

        keranjang.hapusItem(barcode);
        refreshTabelKeranjang();
        lblStatusInfo.setText("Item '" + nama + "' dihapus dari keranjang.");
        txtBarcode.requestFocusInWindow();
    }

    /**
     * Mengosongkan keranjang belanja
     */
    private void resetKeranjang() {
        if (keranjang.getDaftarItem().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin mengosongkan seluruh keranjang belanja?",
                "Konfirmasi Reset",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            keranjang.kosongkanKeranjang();
            txtNominalTunai.setText("");
            lblKembalian.setText("Rp 0");
            refreshTabelKeranjang();
            lblStatusInfo.setText("Keranjang belanja dikosongkan.");
            txtBarcode.requestFocusInWindow();
        }
    }

    /**
     * Proses Penyelesaian Transaksi:
     * 1. Validasi keranjang & pembayaran tunai.
     * 2. Mengurangi stok gudang secara otomatis dan menyimpannya ke master_stok_barang.txt.
     * 3. Menyimpan log transaksi ke rekap_transaksi_kasir.txt.
     * 4. Memunculkan struk digital modal (Thermal Receipt).
     * 5. Reset kasir untuk pelanggan berikutnya.
     */
    private void prosesSelesaikanTransaksi() {
        if (keranjang.getDaftarItem().isEmpty()) {
            playBeepError();
            JOptionPane.showMessageDialog(this, "Keranjang belanja masih kosong! Scan barang terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtBarcode.requestFocusInWindow();
            return;
        }

        double totalBayar = keranjang.hitungSubtotal();
        String tunaiStr = txtNominalTunai.getText().trim().replace(".", "").replace(",", "");

        if (tunaiStr.isEmpty()) {
            playBeepError();
            JOptionPane.showMessageDialog(this, "Masukkan nominal tunai yang diserahkan pembeli!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtNominalTunai.requestFocusInWindow();
            return;
        }

        double nominalTunai;
        try {
            nominalTunai = Double.parseDouble(tunaiStr);
        } catch (NumberFormatException e) {
            playBeepError();
            JOptionPane.showMessageDialog(this, "Nominal tunai tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            txtNominalTunai.requestFocusInWindow();
            return;
        }

        if (nominalTunai < totalBayar) {
            playBeepError();
            JOptionPane.showMessageDialog(this,
                    "Uang tunai kurang!\nTotal: " + TransaksiPenjualan.formatRupiah(totalBayar) +
                            "\nTunai: " + TransaksiPenjualan.formatRupiah(nominalTunai) +
                            "\nKekurangan: " + TransaksiPenjualan.formatRupiah(totalBayar - nominalTunai),
                    "Uang Kurang",
                    JOptionPane.WARNING_MESSAGE);
            txtNominalTunai.requestFocusInWindow();
            return;
        }

        // VALIDASI ULANG STOK SEBELUM FINALISASI PENGURANGAN
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            Barang b = katalogBarang.get(item.getBarang().getBarcodeId().toUpperCase());
            if (b == null || b.getStokGudang() < item.getJumlahBeli()) {
                playBeepError();
                JOptionPane.showMessageDialog(this,
                        "Gagal menyelesaikan transaksi!\nStok barang '" + item.getBarang().getNamaBarang() + "' tidak mencukupi di gudang.",
                        "Gagal Transaksi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 1. KURANGI STOK GUDANG SECARA OTOMATIS
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            Barang b = katalogBarang.get(item.getBarang().getBarcodeId().toUpperCase());
            if (b != null) {
                b.kurangiStok(item.getJumlahBeli());
            }
        }

        // 2. SIMPAN PERUBAHAN STOK KE FILE master_stok_barang.txt
        boolean stokSaved = DataManager.simpanDataBarang(katalogBarang);
        if (!stokSaved) {
            System.err.println("Peringatan: Gagal menyimpan data stok ke file!");
        }

        // 3. BUAT OBJEK TRANSAKSI PENJUALAN
        TransaksiPenjualan transaksi = new TransaksiPenjualan(currentNoNota, keranjang.getDaftarItem(), totalBayar, nominalTunai);

        // 4. SIMPAN REKAP TRANSAKSI KE FILE rekap_transaksi_kasir.txt
        boolean rekapSaved = DataManager.catatTransaksi(transaksi);
        if (!rekapSaved) {
            System.err.println("Peringatan: Gagal mencatat rekap transaksi!");
        }

        // Putar suara transaksi sukses
        playChimeTransaction();

        // 5. TAMPILKAN STRUK KASIR DIGITAL
        tampilkanDialogStruk(transaksi);

        // 6. PERBARUI DATA UNTUK TRANSAKSI BERIKUTNYA
        keranjang.kosongkanKeranjang();
        txtNominalTunai.setText("");
        lblKembalian.setText("Rp 0");
        refreshTabelKeranjang();
        refreshTabelMasterStok();
        refreshTabelRekap();
        generateNewNota();

        lblStatusInfo.setText("[OK] Transaksi " + transaksi.getNoNota() + " berhasil disimpan. Stok gudang otomatis diperbarui.");
        txtBarcode.requestFocusInWindow();
    }

    /**
     * Dialog Struk Kasir Digital (Thermal Receipt)
     */
    private void tampilkanDialogStruk(TransaksiPenjualan transaksi) {
        JDialog dialog = new JDialog(this, "Struk Kasir - Kopma Mart", true);
        dialog.setSize(420, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextArea txtStruk = new JTextArea(transaksi.generateStruk());
        txtStruk.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtStruk.setBackground(new Color(254, 252, 232)); // Warm receipt paper color
        txtStruk.setForeground(new Color(28, 25, 23));
        txtStruk.setEditable(false);
        txtStruk.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(txtStruk);

        JPanel pnlAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlAksi.setBackground(COLOR_BG);

        JButton btnCopy = new JButton("Salin Struk");
        btnCopy.setFocusPainted(false);
        btnCopy.addActionListener(e -> {
            txtStruk.selectAll();
            txtStruk.copy();
            JOptionPane.showMessageDialog(dialog, "Teks struk berhasil disalin ke clipboard!", "Informasi", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnTutup = new JButton("Selesai & Transaksi Baru");
        btnTutup.setBackground(COLOR_EMERALD);
        btnTutup.setForeground(Color.WHITE);
        btnTutup.setFocusPainted(false);
        btnTutup.addActionListener(e -> dialog.dispose());

        pnlAksi.add(btnCopy);
        pnlAksi.add(btnTutup);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(pnlAksi, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Dialog Pencarian Barang Cepat dari Katalog
     */
    private void bukaDialogPilihBarang() {
        JDialog dialog = new JDialog(this, "Pilih Barang dari Katalog Kopma Mart", true);
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel lblCari = new JLabel("Ketik Nama / Kategori:");
        lblCari.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JTextField txtFilter = new JTextField(20);
        pnlTop.add(lblCari);
        pnlTop.add(txtFilter);

        String[] cols = {"Barcode", "Nama Barang", "Kategori", "Harga", "Stok"};
        DefaultTableModel modelDialog = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Barang b : katalogBarang.values()) {
            modelDialog.addRow(new Object[]{b.getBarcodeId(), b.getNamaBarang(), b.getKategori(), b.getFormattedHarga(), b.getStokGudang()});
        }

        JTable tblDialog = new JTable(modelDialog);
        tblDialog.setRowHeight(26);
        tblDialog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        txtFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String q = txtFilter.getText().toLowerCase().trim();
                modelDialog.setRowCount(0);
                for (Barang b : katalogBarang.values()) {
                    if (b.getNamaBarang().toLowerCase().contains(q) || b.getBarcodeId().contains(q) || b.getKategori().toLowerCase().contains(q)) {
                        modelDialog.addRow(new Object[]{b.getBarcodeId(), b.getNamaBarang(), b.getKategori(), b.getFormattedHarga(), b.getStokGudang()});
                    }
                }
            }
        });

        JPanel pnlBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnPilih = new JButton("Masukkan ke Keranjang");
        btnPilih.setBackground(COLOR_ACCENT);
        btnPilih.setForeground(Color.WHITE);
        btnPilih.setFocusPainted(false);

        btnPilih.addActionListener(e -> {
            int sel = tblDialog.getSelectedRow();
            if (sel >= 0) {
                String barcode = (String) modelDialog.getValueAt(sel, 0);
                txtBarcode.setText(barcode);
                dialog.dispose();
                prosesScanBarcode();
            } else {
                JOptionPane.showMessageDialog(dialog, "Pilih salah satu barang!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
        });

        pnlBawah.add(btnPilih);

        dialog.add(pnlTop, BorderLayout.NORTH);
        dialog.add(new JScrollPane(tblDialog), BorderLayout.CENTER);
        dialog.add(pnlBawah, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Dialog Tambah Stok Gudang
     */
    private void dialogTambahStok() {
        int sel = tblMasterStok.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu barang pada tabel master stok terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String barcode = (String) modelMasterStok.getValueAt(sel, 0);
        Barang b = katalogBarang.get(barcode);
        if (b == null) return;

        String input = JOptionPane.showInputDialog(this,
                "Tambah stok untuk: " + b.getNamaBarang() + "\nStok saat ini: " + b.getStokGudang() + " pcs\n\nMasukkan jumlah tambahan stok:",
                "Tambah Stok Gudang",
                JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.trim().isEmpty()) {
            try {
                int jml = Integer.parseInt(input.trim());
                if (jml > 0) {
                    b.tambahStok(jml);
                    DataManager.simpanDataBarang(katalogBarang);
                    refreshTabelMasterStok();
                    JOptionPane.showMessageDialog(this, "Stok berhasil ditambahkan. Stok baru: " + b.getStokGudang() + " pcs.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Jumlah harus lebih besar dari 0!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Input harus berupa angka bulat!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void filterTabelMasterStok(String query) {
        modelMasterStok.setRowCount(0);
        String q = query.toLowerCase().trim();

        for (Barang b : katalogBarang.values()) {
            if (q.isEmpty() || b.getNamaBarang().toLowerCase().contains(q) || b.getBarcodeId().toLowerCase().contains(q) || b.getKategori().toLowerCase().contains(q)) {
                String status;
                if (b.getStokGudang() <= 0) {
                    status = "[Habis]";
                } else if (b.getStokGudang() <= 10) {
                    status = "Menipis (" + b.getStokGudang() + ")";
                } else {
                    status = "Aman (" + b.getStokGudang() + ")";
                }

                modelMasterStok.addRow(new Object[]{
                        b.getBarcodeId(),
                        b.getNamaBarang(),
                        b.getKategori(),
                        b.getFormattedHarga(),
                        b.getStokGudang(),
                        status
                });
            }
        }
    }

    private void refreshTabelMasterStok() {
        filterTabelMasterStok(txtCariStok != null ? txtCariStok.getText() : "");
    }

    private void refreshTabelRekap() {
        if (modelRekap == null) return;
        modelRekap.setRowCount(0);

        List<String> lines = DataManager.muatRekapTransaksiRaw();
        double totalOmset = 0;

        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length >= 6) {
                String noNota = parts[0].trim();
                String waktu = parts[1].trim();
                double total = Double.parseDouble(parts[2].trim());
                double tunai = Double.parseDouble(parts[3].trim());
                double kembali = Double.parseDouble(parts[4].trim());
                String details = parts[5].trim();

                totalOmset += total;

                // Format details agar rapi dibaca
                String formattedDetails = details.replace(";", ", ");

                modelRekap.addRow(new Object[]{
                        noNota,
                        waktu,
                        TransaksiPenjualan.formatRupiah(total),
                        TransaksiPenjualan.formatRupiah(tunai),
                        TransaksiPenjualan.formatRupiah(kembali),
                        formattedDetails
                });
            }
        }

        if (lblTotalOmset != null) {
            lblTotalOmset.setText("Total Omset: " + TransaksiPenjualan.formatRupiah(totalOmset));
        }
    }

    private void generateNewNota() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String tgl = sdf.format(new Date());
        currentNoNota = String.format("KM-%s-%04d", tgl, counterNota++);
        if (lblNoNota != null) {
            lblNoNota.setText(currentNoNota);
        }
    }

    // =========================================================================
    // AUDIO FEEDBACK SYNTHESIZER
    // =========================================================================

    private void playTone(int hz, int msecs, double vol) {
        new Thread(() -> {
            try {
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                for (int i = 0; i < msecs * 8; i++) {
                    double angle = i / (8000f / hz) * 2.0 * Math.PI;
                    buf[0] = (byte) (Math.sin(angle) * 127.0 * vol);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
            }
        }).start();
    }

    private void playBeepSuccess() {
        // Suara beep scanner barcode minimarket (1200 Hz, 60 ms)
        playTone(1200, 60, 0.4);
    }

    private void playBeepError() {
        // Suara nada rendah peringatan (350 Hz, 180 ms)
        playTone(350, 180, 0.6);
    }

    private void playChimeTransaction() {
        // Dua nada sukses pembayaran
        new Thread(() -> {
            playTone(800, 80, 0.4);
            try { Thread.sleep(90); } catch (Exception ignored) {}
            playTone(1200, 120, 0.5);
        }).start();
    }

    // =========================================================================
    // MAIN ENTRY POINT
    // =========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KopmaMartPOS pos = new KopmaMartPOS();
            pos.setVisible(true);
        });
    }
}
