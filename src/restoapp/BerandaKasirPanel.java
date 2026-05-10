package restoapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BerandaKasirPanel extends JPanel {
    private JLabel orderHariIni, totalPendapatan, menuTersedia, transaksiSelesai;
    private DefaultTableModel modelTbl;

    // === Komponen date picker ===
    private JSpinner dateSpinner;
    private java.util.Date selectedDate;

    // === Komponen menu terlaris ===
    private JLabel lblMenuTerlaris;
    private JLabel lblNamaMenu;
    private JLabel lblHargaMenu;
    private JLabel lblStatusMenu;

    public BerandaKasirPanel() {
        setLayout(null);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        // === HEADER ===
        JLabel pageTitle = Theme.label("Beranda Kasir", new Font("Segoe UI", Font.BOLD, 22), Theme.TEXT_WHITE);
        pageTitle.setBounds(28, 24, 400, 30);
        add(pageTitle);

        JLabel pageSub = Theme.label("Ringkasan aktivitas kasir berdasarkan tanggal", Theme.FONT_BODY, Theme.TEXT_MUTED);
        pageSub.setBounds(28, 56, 500, 20);
        add(pageSub);

        // === DATE PICKER ===
        JLabel lblTanggal = Theme.label("Tanggal:", Theme.FONT_LABEL, Theme.TEXT_WHITE);
        lblTanggal.setBounds(28, 86, 70, 28);
        add(lblTanggal);

        // Spinner dengan model tanggal, default hari ini
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateModel.setValue(new java.util.Date());
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setBounds(100, 86, 150, 28);
        dateSpinner.setBackground(Theme.BG_PANEL);
        dateSpinner.setForeground(Theme.TEXT_WHITE);
        add(dateSpinner);

        Theme.StyledButton btnFilter = new Theme.StyledButton("Filter", Theme.ACCENT_BLUE, Color.WHITE);
        btnFilter.setBounds(262, 86, 90, 28);
        btnFilter.addActionListener(e -> {
            selectedDate = (java.util.Date) dateSpinner.getValue();
            refreshData();
        });
        add(btnFilter);

        Theme.StyledButton btnHariIni = new Theme.StyledButton("Hari Ini", Theme.ACCENT_ORANGE, Color.WHITE);
        btnHariIni.setBounds(362, 86, 90, 28);
        btnHariIni.addActionListener(e -> {
            dateSpinner.setValue(new java.util.Date());
            selectedDate = new java.util.Date();
            refreshData();
        });
        add(btnHariIni);

        // === STAT CARDS ===
        orderHariIni     = addStatCard(28,  124, "Order",           "0", Theme.ACCENT_ORANGE,  "\uD83D\uDED2");
        totalPendapatan  = addStatCard(228, 124, "Pendapatan (Rp)", "0", Theme.ACCENT_BLUE,    "\uD83D\uDCB0");
        menuTersedia     = addStatCard(428, 124, "Menu Tersedia",   "0", Theme.ACCENT_ORANGE,  "\uD83C\uDF74");
        transaksiSelesai = addStatCard(628, 124, "Lunas",           "0", new Color(150,80,220),"\u2705");

        // === TABEL ORDER ===
        JLabel tableTitle = Theme.label("Riwayat Order", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        tableTitle.setBounds(28, 252, 400, 22);
        add(tableTitle);

        JScrollPane tbl = buildOrderTable();
        tbl.setBounds(28, 278, 560, 310);
        add(tbl);

        // === PANEL MENU TERLARIS (SUBQUERY) ===
        buildMenuTerlarisPanel();

        // Default: hari ini
        selectedDate = new java.util.Date();
        refreshData();
    }

    private void buildMenuTerlarisPanel() {
        // Panel menu terlaris di sebelah kanan tabel
        JPanel pnlTerlaris = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_PANEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Theme.ACCENT_ORANGE);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        pnlTerlaris.setBounds(608, 278, 300, 240);
        pnlTerlaris.setOpaque(false);
        add(pnlTerlaris);

        JLabel title = Theme.label("Menu Terlaris", new Font("Segoe UI", Font.BOLD, 15), Theme.TEXT_WHITE);
        title.setBounds(16, 16, 260, 22);
        pnlTerlaris.add(title);

        JLabel subTitle = Theme.label("Berdasarkan total qty terjual", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        subTitle.setBounds(16, 40, 260, 16);
        pnlTerlaris.add(subTitle);

        JSeparator sep = new JSeparator();
        sep.setBounds(16, 64, 268, 1);
        sep.setForeground(new Color(255,255,255,30));
        pnlTerlaris.add(sep);

        JLabel lblNamaTitle = Theme.label("Nama Menu", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        lblNamaTitle.setBounds(16, 78, 260, 16);
        pnlTerlaris.add(lblNamaTitle);

        lblNamaMenu = Theme.label("Memuat...", new Font("Segoe UI", Font.BOLD, 18), Theme.ACCENT_ORANGE);
        lblNamaMenu.setBounds(16, 96, 268, 28);
        pnlTerlaris.add(lblNamaMenu);

        JLabel lblHargaTitle = Theme.label("Harga", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        lblHargaTitle.setBounds(16, 136, 260, 16);
        pnlTerlaris.add(lblHargaTitle);

        lblHargaMenu = Theme.label("-", Theme.FONT_LABEL, Theme.TEXT_WHITE);
        lblHargaMenu.setBounds(16, 154, 260, 20);
        pnlTerlaris.add(lblHargaMenu);

        JLabel lblStatusTitle = Theme.label("Status", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        lblStatusTitle.setBounds(16, 186, 260, 16);
        pnlTerlaris.add(lblStatusTitle);

        lblStatusMenu = Theme.label("-", Theme.FONT_LABEL, Theme.TEXT_WHITE);
        lblStatusMenu.setBounds(16, 204, 260, 20);
        pnlTerlaris.add(lblStatusMenu);

        lblMenuTerlaris = title;
    }

    private JLabel addStatCard(int x, int y, String title, String value, Color accent, String icon) {
        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.setPaint(new GradientPaint(5, 0, new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),18), getWidth(), 0, new Color(0,0,0,0)));
                g2.fillRoundRect(5, 0, getWidth()-5, getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setBounds(x, y, 185, 108);
        JLabel ico = Theme.label(icon, new Font("Segoe UI Symbol", Font.PLAIN, 28), new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
        ico.setBounds(140, 20, 40, 36);
        card.add(ico);
        JLabel ttl = Theme.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED);
        ttl.setBounds(16, 20, 130, 16);
        card.add(ttl);
        JLabel val = Theme.label(value, new Font("Segoe UI", Font.BOLD, 28), Theme.TEXT_WHITE);
        val.setBounds(16, 44, 160, 36);
        card.add(val);
        add(card);
        return val;
    }

    private JScrollPane buildOrderTable() {
        String[] cols = {"No. Order","No. Meja","Tanggal","Total","Status"};
        modelTbl = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelTbl);
        KasirDashboardFrame.styleTable(table);
        return new JScrollPane(table);
    }

    private String toSqlDate(java.util.Date date) {
        if (date == null) return LocalDate.now().toString();
        java.time.Instant instant = date.toInstant();
        java.time.LocalDate ld = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return ld.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    public void refreshData() {
        String sqlDate = (selectedDate != null) ? toSqlDate(selectedDate) : LocalDate.now().toString();

        try {
            Connection conn = Database.getConnection();

            // 1. Order pada tanggal terpilih
            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT COUNT(*) FROM orders WHERE DATE(tanggal) = ?");
            ps1.setString(1, sqlDate);
            ResultSet rs = ps1.executeQuery();
            if (rs.next()) orderHariIni.setText(String.valueOf(rs.getInt(1)));

            // 2. Total pendapatan pada tanggal terpilih
            PreparedStatement ps2 = conn.prepareStatement(
                "SELECT COALESCE(SUM(total_bayar),0) FROM orders " +
                "WHERE DATE(tanggal) = ? AND status_bayar = 'lunas'");
            ps2.setString(1, sqlDate);
            rs = ps2.executeQuery();
            if (rs.next()) totalPendapatan.setText(String.format("%,d", rs.getInt(1)));

            // 3. Menu tersedia (tidak bergantung tanggal)
            rs = conn.createStatement().executeQuery(
                "SELECT COUNT(*) FROM menu WHERE status = 'tersedia'");
            if (rs.next()) menuTersedia.setText(String.valueOf(rs.getInt(1)));

            // 4. Transaksi lunas pada tanggal terpilih
            PreparedStatement ps4 = conn.prepareStatement(
                "SELECT COUNT(*) FROM orders " +
                "WHERE DATE(tanggal) = ? AND status_bayar = 'lunas'");
            ps4.setString(1, sqlDate);
            rs = ps4.executeQuery();
            if (rs.next()) transaksiSelesai.setText(String.valueOf(rs.getInt(1)));

            // 5. Tabel order pada tanggal terpilih
            if (modelTbl != null) {
                modelTbl.setRowCount(0);
                PreparedStatement ps5 = conn.prepareStatement(
                    "SELECT id_order, no_meja, tanggal, total_bayar, status_bayar " +
                    "FROM orders WHERE DATE(tanggal) = ? ORDER BY id_order ASC LIMIT 50");
                ps5.setString(1, sqlDate);
                ResultSet rsTab = ps5.executeQuery();
                while (rsTab.next()) {
                    modelTbl.addRow(new Object[]{
                        "#" + rsTab.getInt("id_order"),
                        "Meja " + rsTab.getInt("no_meja"),
                        rsTab.getDate("tanggal"),
                        "Rp " + String.format("%,d", rsTab.getInt("total_bayar")),
                        rsTab.getString("status_bayar")
                    });
                }
            }

            //Menu Terlaris
            ResultSet rsMenu = conn.createStatement().executeQuery(
                "SELECT * " +
                "FROM menu " +
                "WHERE id_menu = ( " +
                "    SELECT id_menu " +
                "    FROM detail_order " +
                "    GROUP BY id_menu " +
                "    ORDER BY SUM(qty) DESC " +
                "    LIMIT 1 " +
                ")"
            );

            if (rsMenu.next()) {
                lblNamaMenu.setText(rsMenu.getString("nama_menu"));
                lblHargaMenu.setText("Rp " + String.format("%,d", rsMenu.getInt("harga")));
                String status = rsMenu.getString("status");
                lblStatusMenu.setText(status);
                lblStatusMenu.setForeground(
                    "tersedia".equalsIgnoreCase(status) ? new Color(60, 200, 100) : new Color(220, 80, 80)
                );
            } else {
                lblNamaMenu.setText("Belum ada data");
                lblHargaMenu.setText("-");
                lblStatusMenu.setText("-");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}