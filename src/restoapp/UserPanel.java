package restoapp;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class UserPanel extends JPanel implements DashboardFrame.Refreshable {

    private JTable table;
    private DefaultTableModel tableModel;
    private Theme.StyledTextField namaField, usernameField;
    private Theme.StyledPasswordField passField;
    private Theme.StyledComboBox roleCombo; // statusCombo dihapus
    private int selectedId = -1;
    private JLabel formTitle;

    public UserPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(null);
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 60));
        JLabel title = Theme.label("Data User", new Font("Segoe UI", Font.BOLD, 20), Theme.TEXT_WHITE);
        title.setBounds(20, 14, 300, 28); header.add(title);
        JLabel sub = Theme.label("Kelola pengguna sistem RestoApp", Theme.FONT_BODY, Theme.TEXT_MUTED);
        sub.setBounds(20, 40, 300, 18); header.add(sub);
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(null);
        body.setOpaque(false);
        body.add(buildTableSection());
        body.add(buildFormSection());
        add(body, BorderLayout.CENTER);
    }

    private JPanel buildTableSection() {
        JPanel p = buildCard(Theme.ACCENT_GREEN);
        p.setBounds(20, 10, 560, 560);

        JLabel t = Theme.label("■  Daftar Pengguna", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        t.setBounds(14, 14, 300, 22); p.add(t);

        String[] cols = {"ID", "Nama Lengkap", "Username", "Role"};
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
        HomePanel.styleTable(table, cols);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) onSelect();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(14, 46, 532, 500);
        sp.setOpaque(false); sp.getViewport().setBackground(Theme.BG_TABLE_ROW);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DIM));
        p.add(sp);
        return p;
    }

    private JPanel buildFormSection() {
        JPanel p = buildCard(Theme.ACCENT_GREEN);
        p.setBounds(594, 10, 360, 560);

        formTitle = Theme.label("■  Tambah User Baru", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        formTitle.setBounds(14, 14, 330, 22); p.add(formTitle);

        JSeparator sep = Theme.separator(); sep.setBounds(14, 44, 332, 1); p.add(sep);

        int fx = 14, fw = 332, fy = 60;

        p.add(Theme.label("Nama Lengkap *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);
        namaField = new Theme.StyledTextField(null);
        namaField.setBounds(fx, fy+18, fw, 36); p.add(namaField); fy += 68;

        p.add(Theme.label("Username *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);
        usernameField = new Theme.StyledTextField(null);
        usernameField.setBounds(fx, fy+18, fw, 36); p.add(usernameField); fy += 68;

        p.add(Theme.label("Password *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);
        passField = new Theme.StyledPasswordField();
        passField.setBounds(fx, fy+18, fw, 36); p.add(passField); fy += 68;

        p.add(Theme.label("Role *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);
        roleCombo = new Theme.StyledComboBox(new String[]{"Admin", "Kasir"});
        roleCombo.setBounds(fx, fy+18, fw, 36); p.add(roleCombo); fy += 68;

        JSeparator sep2 = Theme.separator(); sep2.setBounds(fx, fy+8, fw, 1); p.add(sep2); fy += 22;

        Theme.StyledButton save = new Theme.StyledButton("\uD83D\uDCBE  Simpan", Theme.ACCENT_GREEN, Color.WHITE);
        save.setBounds(fx, fy, 98, 38); 
        save.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12)); 
        save.addActionListener(e -> save()); 
        p.add(save);

        Theme.StyledButton del = new Theme.StyledButton("\uD83D\uDDD1  Hapus", Theme.ACCENT_RED, Color.WHITE);
        del.setBounds(fx+104, fy, 98, 38); 
        del.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        del.addActionListener(e -> delete()); 
        p.add(del);

        Theme.StyledButton clr = new Theme.StyledButton("\u2716  Batal", new Color(70,90,130), Color.WHITE);
        clr.setBounds(fx+208, fy, 100, 38); 
        clr.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        clr.addActionListener(e -> clearForm()); 
        p.add(clr);

        return p;
    }

    private JPanel buildCard(Color accent) {
        return new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setPaint(new java.awt.GradientPaint(0,0,accent,200,0,new Color(0,0,0,0)));
                g2.fillRoundRect(0,0,getWidth(),3,4,4);
                g2.dispose();
            }
        };
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        try {
            java.sql.Connection conn = Database.getConnection();
            String sql = "SELECT id_user, nama_user, username, role FROM users ORDER BY id_user DESC";
            java.sql.Statement st = conn.createStatement();
            java.sql.ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id_user"), 
                    rs.getString("nama_user"), 
                    rs.getString("username"), 
                    rs.getString("role")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data user: " + e.getMessage());
        }
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row != -1) {
            selectedId = (int) tableModel.getValueAt(row, 0);
            namaField.setText(tableModel.getValueAt(row, 1).toString());
            usernameField.setText(tableModel.getValueAt(row, 2).toString());
            passField.setText(""); // Kosongkan demi keamanan
            roleCombo.setSelectedItem(tableModel.getValueAt(row, 3).toString());

            formTitle.setText("■  Edit User (ID: " + selectedId + ")");
            namaField.setForeground(Theme.TEXT_WHITE);
            usernameField.setForeground(Theme.TEXT_WHITE);
        }
    }

    private void save() {
        String nama = namaField.getText().trim();
        String uname = usernameField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String roleName = (String) roleCombo.getSelectedItem();

        if (nama.isEmpty() || uname.isEmpty() || roleName == null) {
            JOptionPane.showMessageDialog(this, "Nama, Username, dan Role wajib diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedId == -1 && pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password wajib diisi untuk user baru!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            java.sql.Connection conn = Database.getConnection();

            if (selectedId == -1) {
                java.sql.PreparedStatement checkUser = conn.prepareStatement("SELECT id_user FROM users WHERE username = ?");
                checkUser.setString(1, uname);
                if (checkUser.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this, "Username sudah digunakan!", "Validasi", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String sql = "INSERT INTO users (nama_user, username, password, role) VALUES (?, ?, ?, ?)";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setString(2, uname);
                pst.setString(3, pass); 
                pst.setString(4, roleName);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "User berhasil ditambahkan!");
            } else {
                if (pass.isEmpty()) {
                    String sql = "UPDATE users SET nama_user=?, username=?, role=? WHERE id_user=?";
                    java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, nama);
                    pst.setString(2, uname);
                    pst.setString(3, roleName);
                    pst.setInt(4, selectedId);
                    pst.executeUpdate();
                } else {
                    String sql = "UPDATE users SET nama_user=?, username=?, password=?, role=? WHERE id_user=?";
                    java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                    pst.setString(1, nama);
                    pst.setString(2, uname);
                    pst.setString(3, pass);
                    pst.setString(4, roleName);
                    pst.setInt(5, selectedId);
                    pst.executeUpdate();
                }
                JOptionPane.showMessageDialog(this, "User berhasil diperbarui!");
            }
            clearForm(); 
            loadTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Database: " + e.getMessage());
        }
    }

    private void delete() {
        if (selectedId == -1) { 
            JOptionPane.showMessageDialog(this, "Pilih user terlebih dahulu!", "Info", JOptionPane.INFORMATION_MESSAGE); 
            return; 
        }

        int c = JOptionPane.showConfirmDialog(this, "Yakin hapus user ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try {
                java.sql.Connection conn = Database.getConnection();
                String sql = "DELETE FROM users WHERE id_user=?";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setInt(1, selectedId);
                pst.executeUpdate();

                clearForm(); 
                loadTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus user: " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        namaField.setText(""); usernameField.setText(""); passField.setText("");
        if (roleCombo.getItemCount() > 0) roleCombo.setSelectedIndex(0);
        formTitle.setText("■  Tambah User Baru");
        table.clearSelection();
    }

    @Override 
    public void refresh() { 
        loadTable(); 
    }
}