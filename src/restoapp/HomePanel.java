package restoapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class HomePanel extends JPanel implements DashboardFrame.Refreshable {

    private JLabel masakanCount, kategoriCount, userCount, tersediaCount;
    private DefaultTableModel tableModel;

    public HomePanel() {
        setLayout(null);
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JLabel pageTitle = Theme.label("Beranda", new Font("Segoe UI", Font.BOLD, 22), Theme.TEXT_WHITE);
        pageTitle.setBounds(28, 24, 400, 30);
        add(pageTitle);
        JLabel pageSub = Theme.label("Ringkasan data RestoApp Management System", Theme.FONT_BODY, Theme.TEXT_MUTED);
        pageSub.setBounds(28, 56, 500, 20);
        add(pageSub);

        masakanCount = addStatCard(28, 94, "Total Masakan", "0", Theme.ACCENT_ORANGE, "\uD83C\uDF74");
        kategoriCount = addStatCard(228, 94, "Total Kategori", "0", Theme.ACCENT_BLUE, "\u2630");
        userCount     = addStatCard(428, 94, "Total User", "0", Theme.ACCENT_GREEN, "\uD83D\uDC64");
        tersediaCount = addStatCard(628, 94, "Menu Tersedia", "0", new Color(150,80,220), "\u2705");

        JLabel tableTitle = Theme.label("Daftar Masakan Terbaru", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        tableTitle.setBounds(28, 224, 400, 22);
        add(tableTitle);

        JScrollPane tbl = buildMiniTable();
        tbl.setBounds(28, 252, 880, 340);
        add(tbl);

        refresh(); 
    }

    private JLabel addStatCard(int x, int y, String title, String value, Color accent, String icon) {
        JPanel card = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                // left accent
                g2.setColor(accent);
                g2.fillRoundRect(0,0,5,getHeight(),4,4);
                // subtle gradient
                g2.setPaint(new GradientPaint(5,0,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),18),getWidth(),0,new Color(0,0,0,0)));
                g2.fillRoundRect(5,0,getWidth()-5,getHeight(),10,10);
                g2.dispose();
            }
        };
        card.setBounds(x, y, 186, 118);

        JLabel ico = Theme.iconLabel(icon, 28, accent);
        ico.setBounds(14, 12, 45, 40); 
        card.add(ico);

        JLabel ttl = Theme.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED);
        ttl.setBounds(14, 52, 160, 16);
        card.add(ttl);

        JLabel val = Theme.label(value, new Font("Segoe UI", Font.BOLD, 32), Theme.TEXT_WHITE);
        val.setBounds(14, 68, 160, 38);
        card.add(val);

        add(card);
        return val;
    }

    private JScrollPane buildMiniTable() {
        String[] cols = {"ID", "Nama Masakan", "Kategori", "Harga (Rp)", "Status"};
        
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                c.setBackground(row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT);
                c.setForeground(Theme.TEXT_WHITE);
                if (c instanceof JComponent) ((JComponent)c).setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return c;
            }
        };
        styleTable(table, cols);

        JScrollPane sp = new JScrollPane(table);
        sp.setOpaque(false);
        sp.getViewport().setBackground(Theme.BG_TABLE_ROW);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DIM, 1));
        return sp;
    }

    static void styleTable(JTable t, String[] cols) {
        t.setBackground(Theme.BG_TABLE_ROW);
        t.setForeground(Theme.TEXT_WHITE);
        t.setGridColor(Theme.BORDER_DIM);
        t.setRowHeight(36);
        t.setFont(Theme.FONT_BODY);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(234,127,42,60));
        t.setSelectionForeground(Theme.TEXT_WHITE);
        t.setIntercellSpacing(new Dimension(0, 1));

        // Header
        t.getTableHeader().setBackground(Theme.BG_TABLE_HDR);
        t.getTableHeader().setForeground(Theme.ACCENT_ORANGE);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,2,0,Theme.ACCENT_ORANGE));

        // Col widths
        int[] widths = {50, 220, 120, 130, 100};
        for (int i = 0; i < Math.min(cols.length, widths.length); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    @Override 
    public void refresh() {
        try {
            Connection conn = Database.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs;

            rs = st.executeQuery("SELECT COUNT(*) FROM menu");
            if (rs.next()) masakanCount.setText(rs.getString(1));

            rs = st.executeQuery("SELECT COUNT(*) FROM kategori");
            if (rs.next()) kategoriCount.setText(rs.getString(1));

            rs = st.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) userCount.setText(rs.getString(1));

            rs = st.executeQuery("SELECT COUNT(*) FROM menu WHERE status = 'tersedia'");
            if (rs.next()) tersediaCount.setText(rs.getString(1));

            tableModel.setRowCount(0); // Bersihkan isi tabel lama
            String sql = "SELECT m.id_menu, m.nama_menu, k.nama_kategori, m.harga, m.status " +
                         "FROM menu m JOIN kategori k ON m.id_kategori = k.id_kategori " +
                         "ORDER BY m.id_menu DESC LIMIT 10";
            rs = st.executeQuery(sql);
            
            while (rs.next()) {
                String statusStr = rs.getString("status");
                String capStatus = statusStr.substring(0, 1).toUpperCase() + statusStr.substring(1);

                tableModel.addRow(new Object[]{
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getString("nama_kategori"),
                    "Rp " + String.format("%,d", rs.getInt("harga")),
                    capStatus
                });
            }

        } catch (Exception e) {
            System.out.println("Gagal memuat data Dashboard: " + e.getMessage());
        }
    }
}