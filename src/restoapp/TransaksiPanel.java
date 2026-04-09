package restoapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TransaksiPanel extends JPanel {
    private KasirDashboardFrame mainFrame; // Referensi ke Frame Utama
    
    private DefaultTableModel orderModel;
    private JTable orderTable;
    private JComboBox<String> cbOrder, cbBayar;
    private JLabel lblEditId, lblEditTotal;
    private int selectedIdOrder = -1;
    private int selectedTotal = 0;

    public TransaksiPanel(KasirDashboardFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setOpaque(false);
        buildUI();
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); 
                return label;
            }
        });
    }

    private void buildUI() {
        JLabel pageTitle = Theme.label("Kelola Transaksi", new Font("Segoe UI", Font.BOLD, 22), Theme.TEXT_WHITE);
        pageTitle.setBounds(28, 24, 400, 30);
        add(pageTitle);

        JPanel pnlKiri = new JPanel(null);
        pnlKiri.setBounds(28, 70, 650, 550);
        pnlKiri.setBackground(Theme.BG_PANEL);
        add(pnlKiri);

        JLabel lblKiri = Theme.label("■ Daftar Transaksi", new Font("Segoe UI", Font.BOLD, 16), Theme.TEXT_WHITE);
        lblKiri.setBounds(20, 20, 400, 25);
        pnlKiri.add(lblKiri);

        String[] cols = {"ID Order","No. Meja","Tanggal","Total Bayar","Status Order", "Status Bayar"};
        orderModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        
        orderTable = new JTable(orderModel);
        KasirDashboardFrame.styleTable(orderTable);
        JScrollPane scroll = new JScrollPane(orderTable);
        scroll.setBounds(20, 60, 610, 470);
        pnlKiri.add(scroll);

        // ================ PANEL KANAN ================
        JPanel pnlKanan = new JPanel(null);
        pnlKanan.setBounds(700, 70, 280, 550);
        pnlKanan.setBackground(Theme.BG_PANEL);
        add(pnlKanan);

        JLabel formTitle = Theme.label("■ Update Status", new Font("Segoe UI", Font.BOLD, 16), Theme.TEXT_WHITE);
        formTitle.setBounds(20, 20, 240, 25);
        pnlKanan.add(formTitle);

        lblEditId = Theme.label("Pilih transaksi...", Theme.FONT_LABEL, Theme.TEXT_MUTED);
        lblEditId.setBounds(20, 60, 240, 20);
        pnlKanan.add(lblEditId);

        lblEditTotal = Theme.label("Total: -", Theme.FONT_LABEL, Theme.ACCENT_ORANGE);
        lblEditTotal.setBounds(20, 85, 240, 20);
        pnlKanan.add(lblEditTotal);

        JLabel lblStatOrder = Theme.label("Status Order:", Theme.FONT_LABEL, Theme.TEXT_WHITE);
        lblStatOrder.setBounds(20, 130, 240, 20);
        pnlKanan.add(lblStatOrder);
        
        cbOrder = new JComboBox<>(new String[]{"Belum Selesai", "Selesai"}); 
        cbOrder.setBounds(20, 155, 240, 38);
        styleComboBox(cbOrder);
        pnlKanan.add(cbOrder);

        JLabel lblStatBayar = Theme.label("Status Bayar:", Theme.FONT_LABEL, Theme.TEXT_WHITE);
        lblStatBayar.setBounds(20, 210, 240, 20);
        pnlKanan.add(lblStatBayar);
        
        cbBayar = new JComboBox<>(new String[]{"Belum Lunas", "Lunas"});
        cbBayar.setBounds(20, 235, 240, 38);
        styleComboBox(cbBayar);
        pnlKanan.add(cbBayar);

        Theme.StyledButton btnSimpan = new Theme.StyledButton("Simpan Status", new Color(40, 180, 90), Color.WHITE);
        btnSimpan.setBounds(20, 300, 240, 42);
        btnSimpan.addActionListener(e -> simpanPerubahanStatus());
        pnlKanan.add(btnSimpan);

        // --- TAMBAHAN TOMBOL LIHAT DETAIL ---
        Theme.StyledButton btnDetail = new Theme.StyledButton("\uD83D\uDCC4 Lihat Detail Item", Theme.ACCENT_BLUE, Color.WHITE);
        btnDetail.setBounds(20, 355, 240, 42);
        btnDetail.addActionListener(e -> tampilkanDetailOrder());
        pnlKanan.add(btnDetail);

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && orderTable.getSelectedRow() != -1) {
                int row = orderTable.getSelectedRow();
                selectedIdOrder = Integer.parseInt(orderModel.getValueAt(row, 0).toString());
                selectedTotal = Integer.parseInt(orderModel.getValueAt(row, 3).toString().replaceAll("[^\\d]", ""));
                lblEditId.setText("ID Order : #" + selectedIdOrder);
                lblEditTotal.setText("Total    : " + orderModel.getValueAt(row, 3).toString());
                
                String dStatOrder = orderModel.getValueAt(row, 4).toString();
                cbOrder.setSelectedItem(dStatOrder.equalsIgnoreCase("selesai") ? "Selesai" : "Belum Selesai");
                String dStatBayar = orderModel.getValueAt(row, 5).toString();
                cbBayar.setSelectedItem(dStatBayar.equalsIgnoreCase("lunas") ? "Lunas" : "Belum Lunas");
            }
        });

        loadOrders();
    }

    public void loadOrders() {
        orderModel.setRowCount(0);
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT o.id_order, o.no_meja, o.tanggal, o.total_bayar, o.status as status_order, " +
                         "COALESCE(t.status, 'belum lunas') as status_bayar " +
                         "FROM orders o LEFT JOIN transaksi t ON o.id_order = t.id_order " +
                         "ORDER BY o.id_order ASC LIMIT 50";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                orderModel.addRow(new Object[]{
                    rs.getInt("id_order"), "Meja " + rs.getInt("no_meja"), rs.getDate("tanggal").toString(),
                    "Rp " + String.format("%,d", rs.getInt("total_bayar")), rs.getString("status_order"), rs.getString("status_bayar")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void simpanPerubahanStatus() {
        if (selectedIdOrder == -1) return;
        String statOrder = cbOrder.getSelectedItem().toString().toLowerCase();
        String statBayar = cbBayar.getSelectedItem().toString().toLowerCase();

        try {
            Connection conn = Database.getConnection();
            PreparedStatement ps1 = conn.prepareStatement("UPDATE orders SET status=? WHERE id_order=?");
            ps1.setString(1, statOrder); ps1.setInt(2, selectedIdOrder); ps1.executeUpdate();
            PreparedStatement ps2 = conn.prepareStatement("UPDATE detail_order SET status=? WHERE id_order=?");
            ps2.setString(1, statOrder); ps2.setInt(2, selectedIdOrder); ps2.executeUpdate();

            ResultSet rsCek = conn.createStatement().executeQuery("SELECT id_transaksi FROM transaksi WHERE id_order=" + selectedIdOrder);
            boolean transaksiAda = rsCek.next();

            if (statBayar.equals("lunas") && !transaksiAda) {
                PreparedStatement psTrans = conn.prepareStatement("INSERT INTO transaksi (id_order, tanggal, total_bayar, status) VALUES (?, CURDATE(), ?, 'lunas')");
                psTrans.setInt(1, selectedIdOrder); psTrans.setInt(2, selectedTotal); psTrans.executeUpdate();
            } else if (statBayar.equals("belum lunas") && transaksiAda) {
                conn.createStatement().executeUpdate("DELETE FROM transaksi WHERE id_order=" + selectedIdOrder);
            }

            JOptionPane.showMessageDialog(this, "Status diperbarui!");
            loadOrders(); 
            // Panggil method dari Frame Utama untuk memperbarui panel lain
            mainFrame.refreshBerandaDanTransaksi(); 
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- FUNGSI BARU: Menampilkan Pop-Up Detail Order ---
    private void tampilkanDetailOrder() {
        if (selectedIdOrder == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih transaksi pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Membuat Dialog (Jendela Pop-up)
        JDialog dialog = new JDialog(mainFrame, "Detail Rincian Order #" + selectedIdOrder, true);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelUtama = new JPanel(new BorderLayout(0, 15));
        panelUtama.setBackground(Theme.BG_PANEL);
        panelUtama.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = Theme.label("Daftar Item Pesanan", new Font("Segoe UI", Font.BOLD, 18), Theme.TEXT_WHITE);
        panelUtama.add(lblTitle, BorderLayout.NORTH);

        // Membuat Tabel Detail
        String[] cols = {"Nama Menu", "Harga Satuan", "Qty", "Subtotal"};
        DefaultTableModel detailModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable detailTable = new JTable(detailModel);
        KasirDashboardFrame.styleTable(detailTable);

        // Mengambil data dari Database (JOIN detail_order dan menu)
        try {
            Connection conn = Database.getConnection();
            String sql = "SELECT m.nama_menu, m.harga, d.qty, d.total FROM detail_order d " +
                         "JOIN menu m ON d.id_menu = m.id_menu WHERE d.id_order = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selectedIdOrder);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                detailModel.addRow(new Object[]{
                    rs.getString("nama_menu"),
                    "Rp " + String.format("%,d", rs.getInt("harga")),
                    rs.getInt("qty"),
                    "Rp " + String.format("%,d", rs.getInt("total"))
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat detail pesanan.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Memasukkan tabel ke ScrollPane
        JScrollPane scroll = new JScrollPane(detailTable);
        scroll.getViewport().setBackground(Theme.BG_TABLE_ROW);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DIM));
        panelUtama.add(scroll, BorderLayout.CENTER);

        // Tombol Tutup di bawah
        Theme.StyledButton btnTutup = new Theme.StyledButton("Tutup", Theme.ACCENT_RED, Color.WHITE);
        btnTutup.setPreferredSize(new Dimension(100, 35));
        btnTutup.addActionListener(e -> dialog.dispose());
        
        JPanel pnlBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBawah.setOpaque(false);
        pnlBawah.add(btnTutup);
        
        panelUtama.add(pnlBawah, BorderLayout.SOUTH);

        dialog.add(panelUtama);
        dialog.setVisible(true);
    }
}