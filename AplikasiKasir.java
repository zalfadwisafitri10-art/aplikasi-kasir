import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.awt.*;
import javax.swing.*;

/**
 * Aplikasi Kasir sederhana (Java Swing).
 * Compile : javac AplikasiKasir.java
 * Jalankan: java AplikasiKasir
 */
public class AplikasiKasir extends JFrame {

    // ====== Model data ======
    static class Produk {
        String kode, nama;
        long harga;
        int stok;

        Produk(String kode, String nama, long harga, int stok) {
            this.kode = kode;
            this.nama = nama;
            this.harga = harga;
            this.stok = stok;
        }
    }

    static class ItemKeranjang {
        Produk produk;
        int qty;

        ItemKeranjang(Produk produk, int qty) {
            this.produk = produk;
            this.qty = qty;
        }

        long subtotal() {
            return produk.harga * qty;
        }
    }

    private final List<Produk> daftarProduk = new ArrayList<>();
    private final List<ItemKeranjang> keranjang = new ArrayList<>();
    private int nomorTransaksi = 1;

    // ====== Komponen UI ======
    private final DefaultTableModel modelProduk = buatModel("Kode", "Nama", "Harga", "Stok");
    private final DefaultTableModel modelKeranjang = buatModel("Nama", "Harga", "Qty", "Subtotal");
    private final JTable tabelProduk = new JTable(modelProduk);
    private final JTable tabelKeranjang = new JTable(modelKeranjang);
    private final JSpinner spinQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JLabel lblTotal = new JLabel("Rp 0", SwingConstants.RIGHT);
    private final JLabel lblKembalian = new JLabel("Rp 0", SwingConstants.RIGHT);
    private final JTextField txtBayar = new JTextField();

    public AplikasiKasir() {
        super("Aplikasi Kasir");
        isiDataAwal();
        susunTampilan();
        refreshProduk();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 560);
        setLocationRelativeTo(null);
    }

    private static DefaultTableModel buatModel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private void isiDataAwal() {
        daftarProduk.add(new Produk("P001", "Beras 5 Kg", 68000, 20));
        daftarProduk.add(new Produk("P002", "Minyak Goreng 1 L", 18000, 30));
        daftarProduk.add(new Produk("P003", "Gula Pasir 1 Kg", 16000, 25));
        daftarProduk.add(new Produk("P004", "Telur 1 Kg", 28000, 15));
        daftarProduk.add(new Produk("P005", "Mie Instan", 3500, 100));
        daftarProduk.add(new Produk("P006", "Kopi Sachet", 2000, 80));
        daftarProduk.add(new Produk("P007", "Air Mineral 600 ml", 3000, 60));
    }

    // ====== Tampilan ======
    private void susunTampilan() {
        setLayout(new GridLayout(1, 2, 10, 0));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Panel kiri: daftar produk ---
        JPanel kiri = new JPanel(new BorderLayout(5, 5));
        kiri.setBorder(BorderFactory.createTitledBorder("Daftar Produk"));
        tabelProduk.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        kiri.add(new JScrollPane(tabelProduk), BorderLayout.CENTER);

        JPanel aksiKiri = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aksiKiri.add(new JLabel("Qty:"));
        spinQty.setPreferredSize(new Dimension(70, 26));
        aksiKiri.add(spinQty);
        JButton btnTambah = new JButton("Tambah ke Keranjang");
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        aksiKiri.add(btnTambah);
        JButton btnProdukBaru = new JButton("Produk Baru");
        btnProdukBaru.addActionListener(e -> tambahProdukBaru());
        aksiKiri.add(btnProdukBaru);
        kiri.add(aksiKiri, BorderLayout.SOUTH);

        // --- Panel kanan: keranjang & pembayaran ---
        JPanel kanan = new JPanel(new BorderLayout(5, 5));
        kanan.setBorder(BorderFactory.createTitledBorder("Keranjang Belanja"));
        kanan.add(new JScrollPane(tabelKeranjang), BorderLayout.CENTER);

        JPanel bawah = new JPanel(new GridLayout(0, 2, 5, 5));
        Font besar = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        lblTotal.setFont(besar);
        lblKembalian.setFont(besar);
        bawah.add(new JLabel("Total:"));
        bawah.add(lblTotal);
        bawah.add(new JLabel("Uang Bayar:"));
        bawah.add(txtBayar);
        bawah.add(new JLabel("Kembalian:"));
        bawah.add(lblKembalian);

        JButton btnHapus = new JButton("Hapus Item");
        btnHapus.addActionListener(e -> hapusItem());
        JButton btnBatal = new JButton("Kosongkan");
        btnBatal.addActionListener(e -> {
            keranjang.clear();
            refreshKeranjang();
        });
        JButton btnBayar = new JButton("BAYAR");
        btnBayar.setFont(besar);
        btnBayar.addActionListener(e -> prosesBayar());
        bawah.add(btnHapus);
        bawah.add(btnBatal);

        JPanel selatan = new JPanel(new BorderLayout(5, 5));
        selatan.add(bawah, BorderLayout.CENTER);
        selatan.add(btnBayar, BorderLayout.SOUTH);
        kanan.add(selatan, BorderLayout.SOUTH);

        // Hitung kembalian otomatis saat mengetik
        txtBayar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { hitungKembalian(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { hitungKembalian(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { hitungKembalian(); }
        });

        add(kiri);
        add(kanan);
    }

    // ====== Logika ======
    private String rp(long angka) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(angka)
                .replace("Rp", "Rp ").replace("  ", " ");
    }

    private long totalBelanja() {
        return keranjang.stream().mapToLong(ItemKeranjang::subtotal).sum();
    }

    private void refreshProduk() {
        modelProduk.setRowCount(0);
        for (Produk p : daftarProduk) {
            modelProduk.addRow(new Object[]{p.kode, p.nama, rp(p.harga), p.stok});
        }
    }

    private void refreshKeranjang() {
        modelKeranjang.setRowCount(0);
        for (ItemKeranjang i : keranjang) {
            modelKeranjang.addRow(new Object[]{i.produk.nama, rp(i.produk.harga), i.qty, rp(i.subtotal())});
        }
        lblTotal.setText(rp(totalBelanja()));
        hitungKembalian();
    }

    private void hitungKembalian() {
        long bayar = parseAngka(txtBayar.getText());
        long kembali = bayar - totalBelanja();
        lblKembalian.setText(kembali >= 0 ? rp(kembali) : "Kurang " + rp(-kembali));
    }

    private long parseAngka(String s) {
        String digit = s.replaceAll("[^0-9]", "");
        return digit.isEmpty() ? 0 : Long.parseLong(digit);
    }

    private void tambahKeKeranjang() {
        int baris = tabelProduk.getSelectedRow();
        if (baris < 0) {
            pesan("Pilih produk terlebih dahulu.");
            return;
        }
        Produk p = daftarProduk.get(baris);
        int qty = (int) spinQty.getValue();

        ItemKeranjang ada = keranjang.stream().filter(i -> i.produk == p).findFirst().orElse(null);
        int sudahDiKeranjang = ada == null ? 0 : ada.qty;
        if (sudahDiKeranjang + qty > p.stok) {
            pesan("Stok " + p.nama + " tidak cukup (sisa " + p.stok + ").");
            return;
        }
        if (ada == null) keranjang.add(new ItemKeranjang(p, qty));
        else ada.qty += qty;

        spinQty.setValue(1);
        refreshKeranjang();
    }

    private void hapusItem() {
        int baris = tabelKeranjang.getSelectedRow();
        if (baris < 0) {
            pesan("Pilih item di keranjang yang ingin dihapus.");
            return;
        }
        keranjang.remove(baris);
        refreshKeranjang();
    }

    private void tambahProdukBaru() {
        JTextField kode = new JTextField();
        JTextField nama = new JTextField();
        JTextField harga = new JTextField();
        JTextField stok = new JTextField();
        Object[] form = {"Kode:", kode, "Nama:", nama, "Harga:", harga, "Stok:", stok};
        int ok = JOptionPane.showConfirmDialog(this, form, "Produk Baru", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            if (kode.getText().isBlank() || nama.getText().isBlank())
                throw new IllegalArgumentException("Kode dan nama wajib diisi.");
            boolean kembar = daftarProduk.stream().anyMatch(p -> p.kode.equalsIgnoreCase(kode.getText().trim()));
            if (kembar) throw new IllegalArgumentException("Kode produk sudah ada.");
            daftarProduk.add(new Produk(kode.getText().trim(), nama.getText().trim(),
                    Long.parseLong(harga.getText().trim()), Integer.parseInt(stok.getText().trim())));
            refreshProduk();
        } catch (NumberFormatException ex) {
            pesan("Harga dan stok harus berupa angka.");
        } catch (IllegalArgumentException ex) {
            pesan(ex.getMessage());
        }
    }

    private void prosesBayar() {
        if (keranjang.isEmpty()) {
            pesan("Keranjang masih kosong.");
            return;
        }
        long total = totalBelanja();
        long bayar = parseAngka(txtBayar.getText());
        if (bayar < total) {
            pesan("Uang bayar kurang.");
            return;
        }

        String struk = buatStruk(total, bayar);

        // Kurangi stok
        for (ItemKeranjang i : keranjang) i.produk.stok -= i.qty;

        JTextArea area = new JTextArea(struk);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Struk Pembayaran", JOptionPane.PLAIN_MESSAGE);

        nomorTransaksi++;
        keranjang.clear();
        txtBayar.setText("");
        refreshProduk();
        refreshKeranjang();
    }

    private String buatStruk(long total, long bayar) {
        StringBuilder sb = new StringBuilder();
        String garis = "-".repeat(44) + "\n";
        sb.append("            TOKO SERBA ADA\n");
        sb.append("  Struk #").append(String.format("%05d", nomorTransaksi)).append("   ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append(garis);
        for (ItemKeranjang i : keranjang) {
            sb.append(i.produk.nama).append("\n");
            sb.append(String.format("   %d x %-14s %20s%n", i.qty, rp(i.produk.harga), rp(i.subtotal())));
        }
        sb.append(garis);
        sb.append(String.format("%-20s %23s%n", "TOTAL", rp(total)));
        sb.append(String.format("%-20s %23s%n", "BAYAR", rp(bayar)));
        sb.append(String.format("%-20s %23s%n", "KEMBALI", rp(bayar - total)));
        sb.append(garis);
        sb.append("        Terima kasih atas kunjungan Anda!\n");
        return sb.toString();
    }
    
    private void pesan(String teks) {
        JOptionPane.showMessageDialog(this, teks);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AplikasiKasir().setVisible(true));
    }
}
