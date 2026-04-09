package restoapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class OrderPanel extends JPanel {
    private KasirDashboardFrame mainFrame; // Referensi ke Frame Utama
    
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JSpinner qtySpinner;
    private JLabel totalLabel, lblEditItem, lblSelectedMeja;
    private int selectedCartRow = -1;
    private int selectedMeja = 1; 
    
    private JPanel menuGridPanel;
    private JButton[] btnMejas = new JButton[20];

    public OrderPanel(KasirDashboardFrame mainFrame) {
        this.mainFrame = mainFrame;
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

        JLabel mejaLabel = Theme.label("Pilih Nomor Meja:", Theme.FONT_LABEL, Theme.TEXT_WHITE);
        mejaLabel.setBounds(20, 230, 150, 22);
        pnlKanan.add(mejaLabel);

        lblSelectedMeja = Theme.label("Meja: 1", new Font("Segoe UI", Font.BOLD, 14), Theme.ACCENT_ORANGE);
        lblSelectedMeja.setBounds(220, 230, 80, 22);
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
            btn.addActionListener(e -> updateSelectedMeja(mejaNo));
            btnMejas[i] = btn;
            pnlMeja.add(btn);
        }
        pnlKanan.add(pnlMeja);

        totalLabel = Theme.label("Total: Rp 0", new Font("Segoe UI", Font.BOLD, 22), Theme.ACCENT_ORANGE);
        totalLabel.setBounds(20, 410, 280, 30);
        pnlKanan.add(totalLabel);

        Theme.StyledButton orderBtn = new Theme.StyledButton("\u2714  Proses Order", Theme.ACCENT_BLUE, Color.WHITE);
        orderBtn.setBounds(20, 450, 280, 45);
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
        refreshMejaStatus(); // Panggil pengecekan meja saat pertama kali dibuka
    }

    private void updateSelectedMeja(int no) {
        selectedMeja = no;
        if (no != -1) {
            lblSelectedMeja.setText("Meja: " + no);
            lblSelectedMeja.setForeground(Theme.ACCENT_ORANGE);
        } else {
            lblSelectedMeja.setText("Pilih Meja!");
            lblSelectedMeja.setForeground(Theme.ACCENT_RED);
        }
        
        for (int i = 0; i < 20; i++) {
            if (!btnMejas[i].isEnabled()) continue; // Abaikan meja yang disabled
            btnMejas[i].setBackground((i + 1) == no ? Theme.ACCENT_ORANGE : Theme.BG_CARD);
            btnMejas[i].setForeground((i + 1) == no ? Color.BLACK : Theme.ACCENT_ORANGE);
        }
    }

    // --- FUNGSI: Disable meja yang belum lunas ---
    public void refreshMejaStatus() {
        // 1. Kembalikan semua tombol meja ke kondisi normal terlebih dahulu
        for (int i = 0; i < 20; i++) {
            btnMejas[i].setEnabled(true);
            btnMejas[i].setBackground(Theme.BG_CARD);
            btnMejas[i].setForeground(Theme.ACCENT_ORANGE);
            btnMejas[i].setToolTipText("Meja Tersedia");
        }

        try {
            Connection conn = Database.getConnection();
            // Cari nomor meja dari order hari ini yang status bayarnya bukan 'lunas'
            String sql = "SELECT DISTINCT o.no_meja FROM orders o " +
                         "LEFT JOIN transaksi t ON o.id_order = t.id_order " +
                         "WHERE COALESCE(t.status, 'belum lunas') != 'lunas' AND DATE(o.tanggal) = CURDATE()";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            
            while (rs.next()) {
                int noMeja = rs.getInt("no_meja");
                if (noMeja >= 1 && noMeja <= 20) {
                    int idx = noMeja - 1;
                    btnMejas[idx].setEnabled(false); // Matikan tombol
                    btnMejas[idx].setBackground(new Color(40, 45, 55)); // Warna gelap
                    btnMejas[idx].setForeground(new Color(100, 100, 100)); // Teks pudar
                    btnMejas[idx].setToolTipText("Meja sedang digunakan (Belum Lunas)");
                    
                    // Jika meja yang ter-disable kebetulan sedang dipilih, batalkan pilihannya
                    if (selectedMeja == noMeja) {
                        updateSelectedMeja(-1);
                    }
                }
            }
            
            // 2. Jika meja yang dipilih masih valid, warnai oranye. Jika tidak, cari meja kosong terdekat.
            if (selectedMeja != -1 && btnMejas[selectedMeja - 1].isEnabled()) {
                updateSelectedMeja(selectedMeja);
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
            // PERUBAHAN: Menambahkan kolom 'gambar' pada Query SELECT
            ResultSet rs = conn.createStatement().executeQuery("SELECT id_menu, nama_menu, harga, gambar, status FROM menu WHERE status='tersedia' ORDER BY nama_menu");
            while (rs.next()) {
                int id = rs.getInt("id_menu");
                String nama = rs.getString("nama_menu");
                int harga = rs.getInt("harga");
                String imgPath = rs.getString("gambar");
                
                // PERUBAHAN: Memasukkan imgPath ke dalam parameter createMenuCard
                menuGridPanel.add(createMenuCard(id, nama, harga, imgPath));
            }
            menuGridPanel.revalidate();
            menuGridPanel.repaint();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // PERUBAHAN: Penambahan argumen imgPath dan logika muat gambar
    private JPanel createMenuCard(int id, String nama, int harga, String imgPath) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.BG_CARD);
        card.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel lblImg = new JLabel(); 
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        lblImg.setOpaque(true);
        lblImg.setBackground(new Color(25, 35, 45)); 
        lblImg.setPreferredSize(new Dimension(100, 70));
        
        // Logika untuk menampilkan gambar
        boolean imageLoaded = false;
        if (imgPath != null && !imgPath.isBlank()) {
            File imgFile = new File(imgPath);
            if (imgFile.exists()) {
                try {
                    // Skala gambar agar sesuai dengan ukuran kotak (misal 150x70)
                    Image img = new ImageIcon(imgPath).getImage().getScaledInstance(150, 70, Image.SCALE_SMOOTH);
                    lblImg.setIcon(new ImageIcon(img));
                    imageLoaded = true;
                } catch (Exception e) {
                    // Abaikan dan gunakan fallback
                }
            }
        }

        // Jika gambar gagal dimuat atau tidak ada di database (Fallback Emoji Kamera)
        if (!imageLoaded) {
            lblImg.setText("\uD83D\uDCF7"); 
            lblImg.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            lblImg.setForeground(Theme.TEXT_MUTED);
        }

        card.add(lblImg, BorderLayout.NORTH);

        JPanel botPanel = new JPanel(new GridLayout(3, 1));
        botPanel.setOpaque(false);

        JLabel lblNama = new JLabel(nama, SwingConstants.CENTER);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNama.setForeground(Theme.TEXT_WHITE);
        botPanel.add(lblNama);

        JLabel lblHarga = new JLabel("Rp " + String.format("%,d", harga), SwingConstants.CENTER);
        lblHarga.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHarga.setForeground(Theme.ACCENT_ORANGE);
        botPanel.add(lblHarga);

        Theme.StyledButton btnAdd = new Theme.StyledButton("Tambah", Theme.ACCENT_ORANGE, Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAdd.addActionListener(e -> addToCart(nama, harga));
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
        int total = 0;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            total += Integer.parseInt(cartModel.getValueAt(i, 3).toString().replaceAll("[^\\d]", ""));
        }
        totalLabel.setText("Total: Rp " + String.format("%,d", total));
    }

    private void processOrder() {
        if (cartModel.getRowCount() == 0) return;
        if (selectedMeja == -1) {
            JOptionPane.showMessageDialog(this, "Tidak ada meja yang dipilih atau semua meja penuh!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int noMeja = selectedMeja; 
        int totalBayar = Integer.parseInt(totalLabel.getText().replaceAll("[^\\d]", ""));
        
        try {
            Connection conn = Database.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement psOrder = conn.prepareStatement("INSERT INTO orders (no_meja, tanggal, total_bayar, status) VALUES (?, CURDATE(), ?, 'belum selesai')", Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, noMeja); psOrder.setInt(2, totalBayar); psOrder.executeUpdate();

            ResultSet keys = psOrder.getGeneratedKeys();
            int idOrder = 0; if (keys.next()) idOrder = keys.getInt(1);

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                String nama = cartModel.getValueAt(i, 0).toString();
                int qty = Integer.parseInt(cartModel.getValueAt(i, 2).toString());
                int tot = Integer.parseInt(cartModel.getValueAt(i, 3).toString().replaceAll("[^\\d]", ""));
                
                ResultSet rsMenu = conn.createStatement().executeQuery("SELECT id_menu FROM menu WHERE nama_menu = '" + nama.replace("'","''") + "' LIMIT 1");
                int idMenu = 0; if (rsMenu.next()) idMenu = rsMenu.getInt(1);

                PreparedStatement psDetail = conn.prepareStatement("INSERT INTO detail_order (id_order, id_menu, qty, total, status) VALUES (?, ?, ?, ?, 'belum selesai')");
                psDetail.setInt(1, idOrder); psDetail.setInt(2, idMenu); psDetail.setInt(3, qty); psDetail.setInt(4, tot); psDetail.executeUpdate();
            }

            conn.commit(); conn.setAutoCommit(true);
            JOptionPane.showMessageDialog(this, "Order #" + idOrder + " berhasil diproses!");
            cartModel.setRowCount(0); totalLabel.setText("Total: Rp 0");
            
            // Panggil method dari Frame Utama untuk memperbarui panel lain
            mainFrame.refreshBerandaDanTransaksi();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}