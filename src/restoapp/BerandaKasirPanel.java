package restoapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;

public class BerandaKasirPanel extends JPanel {
    private JLabel orderHariIni, totalPendapatan, menuTersedia, transaksiSelesai;
    private DefaultTableModel modelTbl;

    public BerandaKasirPanel() {
        setLayout(null);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JLabel pageTitle = Theme.label("Beranda Kasir", new Font("Segoe UI", Font.BOLD, 22), Theme.TEXT_WHITE);
        pageTitle.setBounds(28, 24, 400, 30);
        add(pageTitle);
        JLabel pageSub = Theme.label("Ringkasan aktivitas kasir hari ini", Theme.FONT_BODY, Theme.TEXT_MUTED);
        pageSub.setBounds(28, 56, 500, 20);
        add(pageSub);

        orderHariIni      = addStatCard(28,  94, "Order Hari Ini",    "0", Theme.ACCENT_ORANGE,  "\uD83D\uDED2");
        totalPendapatan   = addStatCard(228, 94, "Pendapatan (Rp)",   "0", Theme.ACCENT_BLUE,    "\uD83D\uDCB0");
        menuTersedia      = addStatCard(428, 94, "Menu Tersedia",      "0", Theme.ACCENT_ORANGE, "\uD83C\uDF74");
        transaksiSelesai  = addStatCard(628, 94, "Transaksi Selesai", "0", new Color(150,80,220),"\u2705");

        JLabel tableTitle = Theme.label("Order Terbaru Hari Ini", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        tableTitle.setBounds(28, 224, 400, 22);
        add(tableTitle);

        JScrollPane tbl = buildOrderTable();
        tbl.setBounds(28, 252, 880, 340);
        add(tbl);

        refreshData();
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
        KasirDashboardFrame.styleTable(table); // Memanggil helper dari KasirDashboardFrame
        return new JScrollPane(table);
    }

    public void refreshData() {
        try {
            Connection conn = Database.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM orders WHERE DATE(tanggal)=CURDATE()");
            if (rs.next()) orderHariIni.setText(String.valueOf(rs.getInt(1)));

            rs = conn.createStatement().executeQuery("SELECT COALESCE(SUM(o.total_bayar),0) FROM orders o JOIN transaksi t ON o.id_order = t.id_order WHERE DATE(o.tanggal)=CURDATE() AND t.status='lunas'");
            if (rs.next()) totalPendapatan.setText(String.format("%,d", rs.getInt(1)));

            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM menu WHERE status='tersedia'");
            if (rs.next()) menuTersedia.setText(String.valueOf(rs.getInt(1)));

            rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM transaksi WHERE DATE(tanggal)=CURDATE() AND status='lunas'");
            if (rs.next()) transaksiSelesai.setText(String.valueOf(rs.getInt(1)));

            if (modelTbl != null) {
                modelTbl.setRowCount(0);
                String sql = "SELECT id_order, no_meja, tanggal, total_bayar, status FROM orders WHERE DATE(tanggal) = CURDATE() ORDER BY id_order ASC LIMIT 20";
                ResultSet rsTab = conn.createStatement().executeQuery(sql);
                while (rsTab.next()) {
                    modelTbl.addRow(new Object[]{
                        "#" + rsTab.getInt("id_order"),
                        "Meja " + rsTab.getInt("no_meja"),
                        rsTab.getDate("tanggal"),
                        "Rp " + String.format("%,d", rsTab.getInt("total_bayar")),
                        rsTab.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}