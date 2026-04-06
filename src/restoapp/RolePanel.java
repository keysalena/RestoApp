package restoapp;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class RolePanel extends JPanel implements DashboardFrame.Refreshable {

    private JTable table;
    private DefaultTableModel tableModel;
    private Theme.StyledTextField namaField;
    private int selectedId = -1;
    private JLabel formTitle;

    public RolePanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(null);
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 60));
        JLabel title = Theme.label("Data Role", new Font("Segoe UI", Font.BOLD, 20), Theme.TEXT_WHITE);
        title.setBounds(20, 14, 300, 28); header.add(title);
        JLabel sub = Theme.label("Kelola role user", Theme.FONT_BODY, Theme.TEXT_MUTED);
        sub.setBounds(20, 40, 300, 18); header.add(sub);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(null);
        body.setOpaque(false);
        body.add(buildTableSection());
        body.add(buildFormSection());
        add(body, BorderLayout.CENTER);
        loadTable();
    }

    private JPanel buildTableSection() {
        JPanel p = buildCard();
        p.setBounds(20, 10, 580, 560);

        JLabel t = Theme.label("■  Daftar Role", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        t.setBounds(14, 14, 300, 22); p.add(t);

        // HANYA 2 KOLOM: ID dan Nama
        String[] cols = {"ID", "Nama Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(isRowSelected(row) ? new Color(234,127,42,60) : (row%2==0?Theme.BG_TABLE_ROW:Theme.BG_TABLE_ALT));
                c.setForeground(Theme.TEXT_WHITE);
                return c;
            }
        };

        // Styling tabel
        HomePanel.styleTable(table, cols);
        
        // PERBAIKAN: Hanya mengatur kolom 0 dan 1. Kolom 2 dihapus agar tidak error.
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(450);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) onSelect();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(14, 46, 552, 500);
        sp.setOpaque(false); sp.getViewport().setBackground(Theme.BG_TABLE_ROW);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DIM));
        p.add(sp);
        return p;
    }

    private JPanel buildFormSection() {
        JPanel p = buildCard();
        p.setBounds(614, 10, 340, 560);

        formTitle = Theme.label("■  Tambah Role Baru", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        formTitle.setBounds(14, 14, 312, 22); p.add(formTitle);

        JSeparator sep = Theme.separator(); sep.setBounds(14, 44, 312, 1); p.add(sep);

        int fx = 14, fw = 312, fy = 58;

        p.add(Theme.label("Nama Role *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);
        namaField = new Theme.StyledTextField(null);
        namaField.setBounds(fx, fy+18, fw, 36); p.add(namaField); fy += 68;

        JSeparator sep2 = Theme.separator(); sep2.setBounds(fx, fy+10, fw, 1); p.add(sep2); fy += 24;

        // Menggunakan UNICODE Escape agar lebih aman dari error encoding
        Theme.StyledButton save = new Theme.StyledButton("\uD83D\uDCBE  Simpan", Theme.ACCENT_GREEN, Color.WHITE);
        save.setBounds(fx, fy, 98, 38); 
        // SET FONT KE SEGOE UI SYMBOL
        save.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12)); 
        save.addActionListener(e -> save()); 
        p.add(save);

        Theme.StyledButton del = new Theme.StyledButton("\uD83D\uDDD1  Hapus", Theme.ACCENT_RED, Color.WHITE);
        del.setBounds(fx+104, fy, 98, 38); 
        // SET FONT KE SEGOE UI SYMBOL
        del.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        del.addActionListener(e -> delete()); 
        p.add(del);

        Theme.StyledButton clr = new Theme.StyledButton("\u2716  Batal", new Color(70,90,130), Color.WHITE);
        clr.setBounds(fx+208, fy, 100, 38); 
        // SET FONT KE SEGOE UI SYMBOL
        clr.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        clr.addActionListener(e -> clearForm()); 
        p.add(clr);

        return p;
    }

    private JPanel buildCard() {
        return new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setPaint(new GradientPaint(0,0,Theme.ACCENT_BLUE,200,0,new Color(0,0,0,0)));
                g2.fillRoundRect(0,0,getWidth(),3,4,4);
                g2.dispose();
            }
        };
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        try {
            java.sql.Connection conn = Database.getConnection();
            java.sql.Statement st = conn.createStatement();
            java.sql.ResultSet rs = st.executeQuery("SELECT * FROM role ORDER BY id_role DESC");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_role"), 
                    rs.getString("nama_role")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row != -1) {
            selectedId = (int) tableModel.getValueAt(row, 0);
            namaField.setText(tableModel.getValueAt(row, 1).toString());
            formTitle.setText("■  Edit Role (ID: " + selectedId + ")");
            namaField.setForeground(Theme.TEXT_WHITE);
        }
    }

    private void save() {
        String nama = namaField.getText().trim();
        if (nama.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Nama role wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE); 
            return; 
        }

        try {
            java.sql.Connection conn = Database.getConnection();
            if (selectedId == -1) {
                String sql = "INSERT INTO role (nama_role) VALUES (?)";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Role berhasil ditambahkan!");
            } else {
                String sql = "UPDATE role SET nama_role=? WHERE id_role=?";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setInt(2, selectedId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Role berhasil diperbarui!");
            }
            clearForm();
            loadTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void delete() {
       if (selectedId == -1) { 
           JOptionPane.showMessageDialog(this, "Pilih role terlebih dahulu!", "Info", JOptionPane.INFORMATION_MESSAGE); 
           return; 
       }

       int c = JOptionPane.showConfirmDialog(this, "Yakin hapus role ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
       if (c == JOptionPane.YES_OPTION) {
           try {
               java.sql.Connection conn = Database.getConnection();
               String sql = "DELETE FROM role WHERE id_role=?";
               java.sql.PreparedStatement pst = conn.prepareStatement(sql);
               pst.setInt(1, selectedId);
               pst.executeUpdate();

               clearForm();
               loadTable();
           } catch (Exception e) {
               JOptionPane.showMessageDialog(this, "Gagal menghapus: " + e.getMessage());
           }
       }
   }

    private void clearForm() {
        selectedId = -1;
        namaField.setText("");
        formTitle.setText("■  Tambah Role Baru");
        table.clearSelection();
    }

    @Override public void refresh() { loadTable(); }
}