package restoapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;

// =========================================================
// IMPLEMENTASI MODUL 3, 4, 5, 6 (LOGIKA BISNIS PBO)
// =========================================================

// Modul 6: Interface
interface ITipePesanan {
    int hitungTotalAkhir(int subTotal);
    int getNomorMeja(int mejaPilihan);
    String getStatusPesan();
}

// Modul 6: Abstract Class & Modul 4: Enkapsulasi
abstract class BasePesanan implements ITipePesanan {
    private String namaTipe; // Enkapsulasi (private)

    public BasePesanan(String namaTipe) {
        this.namaTipe = namaTipe;
    }

    public String getNamaTipe() { // Getter
        return namaTipe;
    }
}

// Modul 3: Inheritance (Mewarisi BasePesanan)
class DineIn extends BasePesanan {
    public DineIn() {
        super("Makan di Tempat");
    }

    // Modul 5: Polymorphism (Override metode untuk perilaku berbeda)
    @Override
    public int hitungTotalAkhir(int subTotal) {
        return subTotal; // Tidak ada tambahan biaya
    }

    @Override
    public int getNomorMeja(int mejaPilihan) {
        return mejaPilihan;
    }

    @Override
    public String getStatusPesan() {
        return "proses";
    }
}

// Modul 3: Inheritance 
class TakeAway extends BasePesanan {
    private final int BIAYA_PACKAGING = 2000; // Enkapsulasi konstanta

    public TakeAway() {
        super("Take Away");
    }

    // Modul 5: Polymorphism
    @Override
    public int hitungTotalAkhir(int subTotal) {
        return subTotal + BIAYA_PACKAGING; // Otomatis tambah 2000
    }

    @Override
    public int getNomorMeja(int mejaPilihan) {
        return 0; // Take away selalu meja 0
    }

    @Override
    public String getStatusPesan() {
        return "selesai"; // Langsung selesai
    }
}
// =========================================================

public class OrderPanel extends JPanel {
    private KasirDashboardFrame mainFrame; 
    
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JSpinner qtySpinner;
    private JLabel totalLabel, lblEditItem, lblSelectedMeja;
    private int selectedCartRow = -1;
    private int selectedMeja = 1; 
    
    private JPanel menuGridPanel;
    private JButton[] btnMejas = new JButton[20];
    
    // Objek Polimorfisme untuk menampung status pesanan saat ini
    private ITipePesanan tipePesananSaatIni;
    private int subTotalCart = 0; // Menyimpan total sebelum biaya tambahan

    public OrderPanel(KasirDashboardFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.tipePesananSaatIni = new DineIn(); // Default adalah Dine In
        setLayout(null);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JLabel pageTitle = Theme.label("Buat Order Baru", new Font("Segoe UI", Font.BOLD, 22), Theme.TEXT_WHITE);
        pageTitle.setBounds(28, 24, 400, 30);
        add(pageTitle);

        JPanel pnlKiri = new JPanel(null);
        pnlKiri.setBounds(28, 70, 600, 550);
        pnlKiri.setBackground(Theme.BG_PANEL);
        add(pnlKiri);

        JLabel menuTitle = Theme.label("■ Daftar Menu (Klik Tambah)", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        menuTitle.setBounds(20, 20, 300, 22);
        pnlKiri.add(menuTitle);

        menuGridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        menuGridPanel.setBackground(Theme.BG_PANEL);

        JScrollPane menuScroll = new JScrollPane(menuGridPanel);
        menuScroll.setBounds(20, 50, 560, 260); 
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.getVerticalScrollBar().setUnitIncrement(16);
        pnlKiri.add(menuScroll);

        JLabel cartTitle = Theme.label("■ Keranjang Pesanan", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        cartTitle.setBounds(20, 320, 300, 22);
        pnlKiri.add(cartTitle);

        String[] cartCols = {"Nama Menu", "Harga", "Qty", "Total"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        cartTable = new JTable(cartModel);
        KasirDashboardFrame.styleTable(cartTable);
        
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBounds(20, 350, 560, 180);
        pnlKiri.add(cartScroll);

        JPanel pnlKanan = new JPanel(null);
        pnlKanan.setBounds(650, 70, 320, 550);
        pnlKanan.setBackground(Theme.BG_PANEL);
        add(pnlKanan);

        JLabel formTitle = Theme.label("■ Edit Item Keranjang", new Font("Segoe UI", Font.BOLD, 16), Theme.TEXT_WHITE);
        formTitle.setBounds(20, 20, 280, 25);
        pnlKanan.add(formTitle);

        lblEditItem = Theme.label("Pilih item di keranjang...", Theme.FONT_LABEL, Theme.TEXT_MUTED);
        lblEditItem.setBounds(20, 60, 280, 20);
        pnlKanan.add(lblEditItem);

        JLabel lblQty = Theme.label("Ubah Qty:", Theme.FONT_LABEL, Theme.ACCENT_ORANGE);
        lblQty.setBounds(20, 90, 80, 26);
        pnlKanan.add(lblQty);

        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        qtySpinner.setBounds(100, 90, 80, 26);
        qtySpinner.setEnabled(false);
        pnlKanan.add(qtySpinner);

        Theme.StyledButton btnUpdateQty = new Theme.StyledButton("Update Qty", new Color(40, 180, 90), Color.WHITE);
        btnUpdateQty.setBounds(20, 130, 130, 35);
        btnUpdateQty.addActionListener(e -> updateQtyCart());
        pnlKanan.add(btnUpdateQty);

        Theme.StyledButton btnHapus = new Theme.StyledButton("Hapus Item", Theme.ACCENT_RED, Color.WHITE);
        btnHapus.setBounds(160, 130, 130, 35);
        btnHapus.addActionListener(e -> removeFromCart());
        pnlKanan.add(btnHapus);

        JSeparator sep = Theme.separator();
        sep.setBounds(20, 180, 280, 1);
        pnlKanan.add(sep);

        JLabel checkoutTitle = Theme.label("■ Checkout Order", new Font("Segoe UI", Font.BOLD, 16), Theme.TEXT_WHITE);
        checkoutTitle.setBounds(20, 195, 280, 25);
        pnlKanan.add(checkoutTitle);

        JLabel mejaLabel = Theme.label("Pilih Meja:", Theme.FONT_LABEL, Theme.TEXT_WHITE);
        mejaLabel.setBounds(20, 230, 80, 22);
        pnlKanan.add(mejaLabel);

        lblSelectedMeja = Theme.label("Meja: 1", new Font("Segoe UI", Font.BOLD, 14), Theme.ACCENT_ORANGE);
        lblSelectedMeja.setBounds(100, 230, 180, 22);
        pnlKanan.add(lblSelectedMeja);

        JPanel pnlMeja = new JPanel(new GridLayout(4, 5, 5, 5));
        pnlMeja.setBounds(20, 260, 280, 130);
        pnlMeja.setOpaque(false);
        for (int i = 0; i < 20; i++) {
            int mejaNo = i + 1;
            JButton btn = new JButton(String.valueOf(mejaNo));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBackground(Theme.BG_CARD);
            btn.setForeground(Theme.ACCENT_ORANGE);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                tipePesananSaatIni = new DineIn(); // Kembali ke Dine In
                updateSelectedMeja(mejaNo);
            });
            btnMejas[i] = btn;
            pnlMeja.add(btn);
        }
        pnlKanan.add(pnlMeja);

        // Tombol Take Away
        JButton btnTakeAway = new JButton("Take Away (+Rp 2.000)");
        btnTakeAway.setBounds(20, 395, 280, 30);
        btnTakeAway.setBackground(Theme.BG_CARD);
        btnTakeAway.setForeground(Theme.ACCENT_ORANGE);
        btnTakeAway.setFocusPainted(false);
        btnTakeAway.addActionListener(e -> {
            tipePesananSaatIni = new TakeAway(); // Terapkan Polimorfisme
            updateSelectedMeja(0); // 0 merepresentasikan Take Away
        });
        pnlKanan.add(btnTakeAway);

        totalLabel = Theme.label("Total: Rp 0", new Font("Segoe UI", Font.BOLD, 22), Theme.ACCENT_ORANGE);
        totalLabel.setBounds(20, 430, 280, 30); // Turun sedikit menyesuaikan tombol Take Away
        pnlKanan.add(totalLabel);

        Theme.StyledButton orderBtn = new Theme.StyledButton("Proses Order", Theme.ACCENT_BLUE, Color.WHITE);
        orderBtn.setBounds(20, 470, 280, 45);
        orderBtn.addActionListener(e -> processOrder());
        pnlKanan.add(orderBtn);

        cartTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && cartTable.getSelectedRow() != -1) {
                selectedCartRow = cartTable.getSelectedRow();
                lblEditItem.setText("Item: " + cartModel.getValueAt(selectedCartRow, 0).toString());
                lblEditItem.setForeground(Theme.TEXT_WHITE);
                qtySpinner.setValue(Integer.parseInt(cartModel.getValueAt(selectedCartRow, 2).toString()));
                qtySpinner.setEnabled(true);
            }
        });

        loadMenu();
        refreshMejaStatus(); 
    }

    private void updateSelectedMeja(int no) {
        selectedMeja = no;
        if (no == 0) {
            lblSelectedMeja.setText(((BasePesanan)tipePesananSaatIni).getNamaTipe());
            lblSelectedMeja.setForeground(Color.GREEN);
            // Reset warna semua meja
            for (int i = 0; i < 20; i++) {
                if (btnMejas[i].isEnabled()) {
                    btnMejas[i].setBackground(Theme.BG_CARD);
                    btnMejas[i].setForeground(Theme.ACCENT_ORANGE);
                }
            }
        } else if (no != -1) {
            lblSelectedMeja.setText("Meja: " + no);
            lblSelectedMeja.setForeground(Theme.ACCENT_ORANGE);
            for (int i = 0; i < 20; i++) {
                if (!btnMejas[i].isEnabled()) continue;
                btnMejas[i].setBackground((i + 1) == no ? Theme.ACCENT_ORANGE : Theme.BG_CARD);
                btnMejas[i].setForeground((i + 1) == no ? Color.BLACK : Theme.ACCENT_ORANGE);
            }
        } else {
            lblSelectedMeja.setText("Pilih Meja / Take Away!");
            lblSelectedMeja.setForeground(Theme.ACCENT_RED);
        }
        
        updateTotal(); // Hitung ulang total untuk mengecek penambahan biaya
    }

    // [Fungsi refreshMejaStatus(), loadMenu(), createMenuCard() tetap sama seperti aslinya]

    public void refreshMejaStatus() {
        for (int i = 0; i < 20; i++) {
            btnMejas[i].setEnabled(true);
            btnMejas[i].setBackground(Theme.BG_CARD);
            btnMejas[i].setForeground(Theme.ACCENT_ORANGE);
            btnMejas[i].setToolTipText("Meja Tersedia");
        }

        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT DISTINCT no_meja FROM orders " +
                         "WHERE COALESCE(status_bayar, 'belum lunas') != 'lunas' " +
                         "AND DATE(tanggal) = CURDATE() AND no_meja != 0"; // Abaikan meja 0
            ResultSet rs = conn.createStatement().executeQuery(sql);
            
            while (rs.next()) {
                int noMeja = rs.getInt("no_meja");
                if (noMeja >= 1 && noMeja <= 20) {
                    int idx = noMeja - 1;
                    btnMejas[idx].setEnabled(false); 
                    btnMejas[idx].setBackground(new Color(40, 45, 55)); 
                    btnMejas[idx].setForeground(new Color(100, 100, 100)); 
                    btnMejas[idx].setToolTipText("Meja sedang digunakan (Belum Lunas)");
                    
                    if (selectedMeja == noMeja) {
                        updateSelectedMeja(-1);
                    }
                }
            }
            
            if (selectedMeja != -1 && selectedMeja != 0 && btnMejas[selectedMeja - 1].isEnabled()) {
                updateSelectedMeja(selectedMeja);
            } else if (selectedMeja == 0) {
                updateSelectedMeja(0); // Tetap di state Take Away
            } else {
                for (int i = 0; i < 20; i++) {
                    if (btnMejas[i].isEnabled()) {
                        updateSelectedMeja(i + 1);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadMenu() {
        menuGridPanel.removeAll();
        try {
            Connection conn = Database.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM view_menu WHERE status='tersedia'");
            while (rs.next()) {
                int id = rs.getInt("id_menu");
                String nama = rs.getString("nama_menu");
                int harga = rs.getInt("harga");
                String imgPath = rs.getString("gambar");
                
                menuGridPanel.add(createMenuCard(id, nama, harga, imgPath));
            }
            menuGridPanel.revalidate();
            menuGridPanel.repaint();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private JPanel createMenuCard(int id_menu, String nama, int harga, String imgPath) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        int imgW = 180;
        int imgH = 150; 

        JLabel lblImg = new JLabel(); 
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setOpaque(true);
        lblImg.setBackground(new Color(25, 35, 45)); 
        lblImg.setPreferredSize(new Dimension(imgW, imgH));

        boolean imageLoaded = false;
        if (imgPath != null && !imgPath.isBlank()) {
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                try {
                    ImageIcon icon = new ImageIcon(imgPath);
                    Image img = icon.getImage().getScaledInstance(imgW, imgH, Image.SCALE_SMOOTH);
                    lblImg.setIcon(new ImageIcon(img));
                    imageLoaded = true;
                } catch (Exception e) {
                }
            }
        }

        if (!imageLoaded) {
            lblImg.setText("\uD83D\uDCF7"); 
            lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            lblImg.setForeground(Theme.TEXT_MUTED);
        }

        card.add(lblImg, BorderLayout.NORTH);

        JPanel botPanel = new JPanel(new GridLayout(3, 1, 0, 2));
        botPanel.setOpaque(false);
        botPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel lblNama = new JLabel(nama, SwingConstants.CENTER);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNama.setForeground(Theme.TEXT_WHITE);
        botPanel.add(lblNama);

        JLabel lblHarga = new JLabel("Rp " + String.format("%,d", harga), SwingConstants.CENTER);
        lblHarga.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHarga.setForeground(Theme.ACCENT_ORANGE);
        botPanel.add(lblHarga);

        Theme.StyledButton btnAdd = new Theme.StyledButton("Tambah", Theme.ACCENT_ORANGE, Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAdd.addActionListener(e -> {
            if (this instanceof OrderPanel) {
                ((OrderPanel)this).addToCart(nama, harga);
            }
        });
        botPanel.add(btnAdd);

        card.add(botPanel, BorderLayout.CENTER);
        return card;
    }

    private void addToCart(String namaMenu, int harga) {
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if (cartModel.getValueAt(i, 0).equals(namaMenu)) {
                int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString()) + 1;
                cartModel.setValueAt(qty, i, 2);
                cartModel.setValueAt("Rp " + String.format("%,d", qty * harga), i, 3);
                updateTotal();
                return;
            }
        }
        cartModel.addRow(new Object[]{namaMenu, "Rp " + String.format("%,d", harga), 1, "Rp " + String.format("%,d", harga)});
        updateTotal();
    }

    private void updateQtyCart() {
        if (selectedCartRow < 0) return;
        int qtyBaru = (int) qtySpinner.getValue();
        int hargaSatuan = Integer.parseInt(cartModel.getValueAt(selectedCartRow, 1).toString().replaceAll("[^\\d]", ""));
        cartModel.setValueAt(qtyBaru, selectedCartRow, 2);
        cartModel.setValueAt("Rp " + String.format("%,d", qtyBaru * hargaSatuan), selectedCartRow, 3);
        updateTotal();
    }

    private void removeFromCart() {
        if (selectedCartRow < 0) return;
        cartModel.removeRow(selectedCartRow);
        cartTable.clearSelection();
        selectedCartRow = -1;
        lblEditItem.setText("Pilih item di keranjang...");
        qtySpinner.setEnabled(false);
        updateTotal();
    }

    private void updateTotal() {
        subTotalCart = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            subTotalCart += Integer.parseInt(cartModel.getValueAt(i, 3).toString().replaceAll("[^\\d]", ""));
        }
        
        // MODUL 5: POLIMORFISME - Panggil hitungTotalAkhir
        // Total akan otomatis bertambah 2000 jika objeknya adalah TakeAway
        int totalAkhir = 0;
        if (subTotalCart > 0) {
            totalAkhir = tipePesananSaatIni.hitungTotalAkhir(subTotalCart);
        }
        
        totalLabel.setText("Total: Rp " + String.format("%,d", totalAkhir));
    }

    private void processOrder() {
        if (cartModel.getRowCount() == 0) return;

        if (selectedMeja == -1) {
            JOptionPane.showMessageDialog(this, "Tidak ada meja yang dipilih!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil No Meja Berdasarkan Polimorfisme (Dine in = meja asli, Take Away = 0)
        int noMejaFinal = tipePesananSaatIni.getNomorMeja(selectedMeja); 
        int totalBayarFinal = Integer.parseInt(totalLabel.getText().replaceAll("[^\\d]", ""));
        
        Connection conn = null;

        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            // Panggil stored procedure
            CallableStatement cs = conn.prepareCall("{CALL tambah_order(?, ?, ?)}");
            cs.setInt(1, noMejaFinal); // Mengirim 0 jika Take Away
            cs.setInt(2, totalBayarFinal);
            cs.registerOutParameter(3, java.sql.Types.INTEGER);
            cs.execute();

            int idOrder = cs.getInt(3);

            // Insert detail order
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String nama = cartModel.getValueAt(i, 0).toString();
                int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                int tot = Integer.parseInt(cartModel.getValueAt(i, 3).toString().replaceAll("[^\\d]", ""));

                // Ambil id_menu
                PreparedStatement psMenu = conn.prepareStatement(
                    "SELECT id_menu FROM menu WHERE nama_menu = ? LIMIT 1"
                );
                psMenu.setString(1, nama);
                ResultSet rsMenu = psMenu.executeQuery();

                int idMenu = 0;
                if (rsMenu.next()) idMenu = rsMenu.getInt("id_menu");

                // Insert detail
                PreparedStatement psDetail = conn.prepareStatement(
                    "INSERT INTO detail_order (id_order, id_menu, qty, total) VALUES (?, ?, ?, ?)"
                );
                psDetail.setInt(1, idOrder);
                psDetail.setInt(2, idMenu);
                psDetail.setInt(3, qty);
                psDetail.setInt(4, tot);
                psDetail.executeUpdate();
            }

            // UPDATE STATUS KHUSUS TAKE AWAY
            String statusFinal = tipePesananSaatIni.getStatusPesan();
            if (statusFinal.equals("selesai")) {
                // Asumsi field di database bernama 'status_pesan'
                PreparedStatement psUpdate = conn.prepareStatement(
                    "UPDATE orders SET status_pesan = ? WHERE id_order = ?"
                );
                psUpdate.setString(1, statusFinal);
                psUpdate.setInt(2, idOrder);
                psUpdate.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            JOptionPane.showMessageDialog(this, "Order #" + idOrder + " berhasil diproses!\nTipe: " + ((BasePesanan)tipePesananSaatIni).getNamaTipe());

            // Reset kembali ke keadaan awal (Dine In)
            cartModel.setRowCount(0);
            tipePesananSaatIni = new DineIn();
            updateTotal();
            refreshMejaStatus();

            mainFrame.refreshBerandaDanTransaksi();

        } catch (Exception e) {
            try {
                conn.rollback();
                System.out.println("Transaksi gagal, rollback dilakukan!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}