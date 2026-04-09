package restoapp;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;

public class MasakanPanel extends JPanel implements DashboardFrame.Refreshable {

    private JTable table;
    private DefaultTableModel tableModel;
    private Theme.StyledTextField searchField;
    private Theme.StyledTextField namaField;
    private Theme.StyledTextField hargaField;
    private Theme.StyledComboBox kategoriCombo;
    private Theme.StyledComboBox statusCombo;
    private int selectedId = -1;
    private JLabel formTitle;

    // ── Gambar ────────────────────────────────────────────────────────
    private String selectedImagePath = null; // menyimpan path file gambar
    private JLabel gambarPreview;

    public MasakanPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
    }

    private void buildUI() {
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(null);
        body.setOpaque(false);

        JPanel tableSection = buildTableSection();
        tableSection.setBounds(20, 10, 560, 560);
        body.add(tableSection);

        JPanel formSection = buildFormSection();
        formSection.setBounds(594, 10, 360, 560);
        body.add(formSection);

        add(body, BorderLayout.CENTER);
        loadTableData(null);
    }

    // ── Header ────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(null);
        h.setOpaque(false);
        h.setPreferredSize(new Dimension(0, 60));

        JLabel title = Theme.label("Data Masakan", new Font("Segoe UI", Font.BOLD, 20), Theme.TEXT_WHITE);
        title.setBounds(20, 14, 300, 28);
        h.add(title);

        JLabel sub = Theme.label("Kelola menu masakan restoran", Theme.FONT_BODY, Theme.TEXT_MUTED);
        sub.setBounds(20, 40, 300, 18);
        h.add(sub);

        return h;
    }

    // ── Table Section ─────────────────────────────────────────────────
    private JPanel buildTableSection() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT_ORANGE, 200, 0, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 3, 4, 4);
                g2.dispose();
            }
        };

        JLabel tbl = Theme.label("\u25A0  Daftar Masakan", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        tbl.setBounds(14, 14, 300, 22);
        p.add(tbl);

        searchField = new Theme.StyledTextField("\uD83D\uDD0D  Cari nama masakan...");
        searchField.setBounds(14, 44, 340, 36);
        searchField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        p.add(searchField);

        Theme.StyledButton refreshBtn = new Theme.StyledButton("\u21BB Refresh", Theme.ACCENT_BLUE, Color.WHITE);
        refreshBtn.setBounds(362, 44, 100, 36);
        refreshBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        refreshBtn.addActionListener(e -> { searchField.setText(""); loadTableData(null); });
        p.add(refreshBtn);

        String[] cols = {"ID", "Nama Masakan", "Kategori", "Harga (Rp)", "Status", "Gambar"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 5 ? ImageIcon.class : Object.class; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                c.setBackground(isRowSelected(row) ? new Color(234, 127, 42, 60)
                        : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                c.setForeground(Theme.TEXT_WHITE);
                if (c instanceof JLabel) ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        HomePanel.styleTable(table, cols);
        table.setRowHeight(48);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setMaxWidth(70);

        // Renderer gambar di kolom tabel
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = new JLabel();
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(isSelected ? new Color(234, 127, 42, 60)
                        : (row % 2 == 0 ? Theme.BG_TABLE_ROW : Theme.BG_TABLE_ALT));
                lbl.setOpaque(true);
                if (value instanceof ImageIcon) {
                    lbl.setIcon((ImageIcon) value);
                } else {
                    lbl.setText("-");
                    lbl.setForeground(Theme.TEXT_MUTED);
                }
                return lbl;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) onRowSelect();
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(14, 90, 532, 454);
        sp.setOpaque(false);
        sp.getViewport().setBackground(Theme.BG_TABLE_ROW);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DIM));
        p.add(sp);

        return p;
    }

    // ── Form Section ──────────────────────────────────────────────────
    private JPanel buildFormSection() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT_ORANGE, 200, 0, new Color(0, 0, 0, 0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), 3, 4, 4);
                g2.dispose();
            }
        };

        formTitle = Theme.label("\u25A0  Tambah Masakan Baru", Theme.FONT_HEADING, Theme.TEXT_WHITE);
        formTitle.setBounds(14, 14, 330, 22);
        p.add(formTitle);

        JSeparator sep = Theme.separator();
        sep.setBounds(14, 44, 332, 1);
        p.add(sep);

        int fx = 14, fw = 332, fy = 56;

        // Nama
        p.add(Theme.label("Nama Masakan *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);
        namaField = new Theme.StyledTextField(null);
        namaField.setBounds(fx, fy + 18, fw, 36);
        p.add(namaField);
        fy += 62;

        // Kategori
        p.add(Theme.label("Kategori *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);
        kategoriCombo = new Theme.StyledComboBox(new String[]{});
        kategoriCombo.setBounds(fx, fy + 18, fw, 36);
        p.add(kategoriCombo);
        fy += 62;

        // Harga & Status
        p.add(Theme.label("Harga (Rp) *", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw / 2 - 4, 16);
        hargaField = new Theme.StyledTextField(null);
        hargaField.setBounds(fx, fy + 18, fw / 2 - 4, 36);
        p.add(hargaField);

        p.add(Theme.label("Status", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx + fw / 2 + 4, fy, fw / 2 - 4, 16);
        statusCombo = new Theme.StyledComboBox(new String[]{"Tersedia", "Habis"});
        statusCombo.setBounds(fx + fw / 2 + 4, fy + 18, fw / 2 - 4, 36);
        p.add(statusCombo);
        fy += 62;

        // ── Gambar ────────────────────────────────────────────────────
        p.add(Theme.label("Gambar Masakan", Theme.FONT_LABEL, Theme.ACCENT_ORANGE)).setBounds(fx, fy, fw, 16);

        gambarPreview = new JLabel("Belum ada gambar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 35, 50));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        gambarPreview.setHorizontalAlignment(SwingConstants.CENTER);
        gambarPreview.setVerticalAlignment(SwingConstants.CENTER);
        gambarPreview.setForeground(Theme.TEXT_MUTED);
        gambarPreview.setFont(Theme.FONT_BODY);
        gambarPreview.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DIM, 1));
        gambarPreview.setBounds(fx, fy + 18, 200, 120);
        gambarPreview.setOpaque(false);
        p.add(gambarPreview);

        Theme.StyledButton pilihBtn = new Theme.StyledButton("\uD83D\uDCC2 Pilih Gambar", Theme.ACCENT_BLUE, Color.WHITE);
        pilihBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        pilihBtn.setBounds(fx + 208, fy + 18, 122, 36);
        pilihBtn.addActionListener(e -> pilihGambar());
        p.add(pilihBtn);

        Theme.StyledButton hapusGambarBtn = new Theme.StyledButton("\u2716 Hapus Gambar", new Color(90, 50, 50), Color.WHITE);
        hapusGambarBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        hapusGambarBtn.setBounds(fx + 208, fy + 62, 122, 36);
        hapusGambarBtn.addActionListener(e -> {
            selectedImagePath = null;
            gambarPreview.setIcon(null);
            gambarPreview.setText("Belum ada gambar");
        });
        p.add(hapusGambarBtn);

        JLabel infoGambar = Theme.label("JPG / PNG / GIF, maks 2MB", Theme.FONT_LABEL, Theme.TEXT_MUTED);
        infoGambar.setBounds(fx + 208, fy + 104, 122, 16);
        p.add(infoGambar);

        fy += 148;

        JSeparator sep2 = Theme.separator();
        sep2.setBounds(fx, fy, fw, 1);
        p.add(sep2);
        fy += 12;

        Theme.StyledButton save = new Theme.StyledButton("\uD83D\uDCBE  Simpan", Theme.ACCENT_GREEN, Color.WHITE);
        save.setBounds(fx, fy, 98, 38);
        save.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        save.addActionListener(e -> save());
        p.add(save);

        Theme.StyledButton del = new Theme.StyledButton("\uD83D\uDDD1  Hapus", Theme.ACCENT_RED, Color.WHITE);
        del.setBounds(fx + 104, fy, 98, 38);
        del.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        del.addActionListener(e -> delete());
        p.add(del);

        Theme.StyledButton clr = new Theme.StyledButton("\u2716  Batal", new Color(70, 90, 130), Color.WHITE);
        clr.setBounds(fx + 208, fy, 100, 38);
        clr.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        clr.addActionListener(e -> clearForm());
        p.add(clr);

        return p;
    }

    // ── Pilih Gambar ──────────────────────────────────────────────────
    private void pilihGambar() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Pilih Gambar Masakan");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Gambar (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (file.length() > 2 * 1024 * 1024) {
            JOptionPane.showMessageDialog(this,
                    "Ukuran gambar terlalu besar! Maksimal 2MB.", "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Salin ke folder images/ di direktori project
            String folder = "images/";
            new File(folder).mkdirs();
            String fileName = System.currentTimeMillis() + "_" + file.getName();
            File dest = new File(folder + fileName);
            java.nio.file.Files.copy(file.toPath(), dest.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = dest.getPath();
            updateGambarPreview(selectedImagePath);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan gambar: " + e.getMessage());
        }
    }

    /** Update preview dari path file */
    private void updateGambarPreview(String path) {
        if (path == null || path.isBlank() || !new File(path).exists()) {
            gambarPreview.setIcon(null);
            gambarPreview.setText("Belum ada gambar");
            return;
        }
        try {
            Image img = new ImageIcon(path).getImage().getScaledInstance(198, 118, Image.SCALE_SMOOTH);
            gambarPreview.setIcon(new ImageIcon(img));
            gambarPreview.setText(null);
        } catch (Exception e) {
            gambarPreview.setIcon(null);
            gambarPreview.setText("Gagal memuat gambar");
        }
    }

    /** Thumbnail 44x44 untuk kolom tabel */
    private ImageIcon makeThumbnail(String path) {
        if (path == null || path.isBlank() || !new File(path).exists()) return null;
        try {
            Image img = new ImageIcon(path).getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    // ── Load Data ─────────────────────────────────────────────────────
    private void loadTableData(String filter) {
        tableModel.setRowCount(0);
        try {
            java.sql.Connection conn = Database.getConnection();
            String sql = "SELECT menu.*, kategori.nama_kategori FROM menu "
                    + "JOIN kategori ON menu.id_kategori = kategori.id_kategori ";
            if (filter != null && !filter.isBlank()) {
                sql += "WHERE nama_menu LIKE ? OR nama_kategori LIKE ? ";
            }
            sql += "ORDER BY id_menu DESC";

            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            if (filter != null && !filter.isBlank()) {
                pst.setString(1, "%" + filter + "%");
                pst.setString(2, "%" + filter + "%");
            }

            java.sql.ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String imgPath = rs.getString("gambar");
                tableModel.addRow(new Object[]{
                    rs.getInt("id_menu"),
                    rs.getString("nama_menu"),
                    rs.getString("nama_kategori"),
                    "Rp " + String.format("%,d", rs.getInt("harga")),
                    rs.getString("status").toUpperCase(),
                    makeThumbnail(imgPath)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data: " + e.getMessage());
        }
    }

    private void filterTable() {
        if (!searchField.isShowingPlaceholder())
            loadTableData(searchField.getText());
        else
            loadTableData(null);
    }

    // ── Row Select ────────────────────────────────────────────────────
    private void onRowSelect() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);

        try {
            java.sql.Connection conn = Database.getConnection();
            String sql = "SELECT menu.*, kategori.nama_kategori FROM menu "
                    + "JOIN kategori ON menu.id_kategori = kategori.id_kategori WHERE id_menu = ?";
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selectedId);
            java.sql.ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                namaField.setText(rs.getString("nama_menu"));
                hargaField.setText(String.valueOf(rs.getInt("harga")));
                kategoriCombo.setSelectedItem(rs.getString("nama_kategori"));
                String status = rs.getString("status");
                statusCombo.setSelectedItem(status.substring(0, 1).toUpperCase() + status.substring(1));
                formTitle.setText("\u25A0  Edit Masakan (ID: " + selectedId + ")");

                // Muat gambar dari path
                selectedImagePath = rs.getString("gambar");
                updateGambarPreview(selectedImagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Save ──────────────────────────────────────────────────────────
    private void save() {
        String nama = namaField.getText().trim();
        String hargaStr = hargaField.getText().trim();
        String katNama = (String) kategoriCombo.getSelectedItem();
        String status = ((String) statusCombo.getSelectedItem()).toLowerCase();

        if (nama.isEmpty() || hargaStr.isEmpty() || katNama == null) {
            JOptionPane.showMessageDialog(this, "Nama, Harga, dan Kategori wajib diisi!");
            return;
        }

        try {
            int harga = Integer.parseInt(hargaStr);
            if (harga < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka positif!");
            return;
        }

        try {
            java.sql.Connection conn = Database.getConnection();

            int idKat = -1;
            java.sql.PreparedStatement pstKat = conn.prepareStatement(
                    "SELECT id_kategori FROM kategori WHERE nama_kategori = ?");
            pstKat.setString(1, katNama);
            java.sql.ResultSet rsKat = pstKat.executeQuery();
            if (rsKat.next()) idKat = rsKat.getInt("id_kategori");

            if (idKat == -1) {
                JOptionPane.showMessageDialog(this, "Kategori tidak ditemukan!");
                return;
            }

            if (selectedId == -1) {
                // INSERT — gambar disimpan sebagai path (VARCHAR)
                String sql = "INSERT INTO menu (nama_menu, harga, status, id_kategori, gambar) VALUES (?, ?, ?, ?, ?)";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setInt(2, Integer.parseInt(hargaStr));
                pst.setString(3, status);
                pst.setInt(4, idKat);
                pst.setString(5, selectedImagePath); // path atau null jika tidak ada gambar
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Masakan berhasil ditambahkan!");
            } else {
                // UPDATE — gambar disimpan sebagai path (VARCHAR)
                String sql = "UPDATE menu SET nama_menu=?, harga=?, status=?, id_kategori=?, gambar=? WHERE id_menu=?";
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, nama);
                pst.setInt(2, Integer.parseInt(hargaStr));
                pst.setString(3, status);
                pst.setInt(4, idKat);
                pst.setString(5, selectedImagePath); // path atau null jika tidak ada gambar
                pst.setInt(6, selectedId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Masakan berhasil diperbarui!");
            }
            clearForm();
            loadTableData(null);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // ── Delete ────────────────────────────────────────────────────────
    private void delete() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih masakan yang ingin dihapus terlebih dahulu!");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this,
                "Yakin hapus masakan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            try {
                java.sql.Connection conn = Database.getConnection();
                java.sql.PreparedStatement pst = conn.prepareStatement(
                        "DELETE FROM menu WHERE id_menu = ?");
                pst.setInt(1, selectedId);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Masakan berhasil dihapus!");
                clearForm();
                loadTableData(null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus: " + e.getMessage());
            }
        }
    }

    // ── Clear Form ────────────────────────────────────────────────────
    private void clearForm() {
        selectedId = -1;
        namaField.setText("");
        hargaField.setText("");
        if (kategoriCombo.getItemCount() > 0) kategoriCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        selectedImagePath = null;
        updateGambarPreview(null);
        formTitle.setText("\u25A0  Tambah Masakan Baru");
        table.clearSelection();
    }

    // ── Refresh ───────────────────────────────────────────────────────
    @Override
    public void refresh() {
        kategoriCombo.removeAllItems();
        try {
            java.sql.Connection conn = Database.getConnection();
            java.sql.ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT nama_kategori FROM kategori ORDER BY nama_kategori");
            while (rs.next()) {
                kategoriCombo.addItem(rs.getString("nama_kategori"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadTableData(null);
    }
}