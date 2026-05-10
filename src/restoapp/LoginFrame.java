package restoapp;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {

    private Theme.StyledTextField userField;
    private Theme.StyledPasswordField passField;
    private JCheckBox showPassCheck;
    private JLabel msgLabel;

    public LoginFrame() {
        setTitle("RestoApp - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setSize(920, 610);
        setLocationRelativeTo(null);
        setResizable(false);

        JLayeredPane lp = new JLayeredPane();
        
        lp.setPreferredSize(new Dimension(920, 580));

        BackgroundPanel bg = new BackgroundPanel();
        bg.setBounds(0, 0, 920, 580);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        BrandPanel brand = new BrandPanel();
        brand.setBounds(0, 0, 430, 580);
        lp.add(brand, JLayeredPane.PALETTE_LAYER);

        JPanel form = buildFormPanel();
        form.setBounds(430, 0, 490, 580);
        lp.add(form, JLayeredPane.PALETTE_LAYER);
        
        setContentPane(lp);
        enableDrag(lp);
        getRootPane().setDefaultButton(null);
    }

    class BackgroundPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Theme.BG_DARK, getWidth(), getHeight(), Theme.BG_PANEL));
            g2.fillRect(0, 0, getWidth(), getHeight());

            drawGlow(g2, -60, -60, 280, Theme.ACCENT_ORANGE, 0.07f);
            drawGlow(g2, 620, 380, 360, new Color(60, 100, 220), 0.05f);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.07f));
            g2.setColor(Theme.TEXT_MUTED);
            for (int x = 15; x < getWidth(); x += 28)
                for (int y = 15; y < getHeight(); y += 28)
                    g2.fillOval(x, y, 2, 2);
            g2.dispose();
        }
        private void drawGlow(Graphics2D g2, int x, int y, int d, Color c, float alpha) {
            RadialGradientPaint rp = new RadialGradientPaint(
                new Point2D.Float(x + d/2f, y + d/2f), d/2f,
                new float[]{0f, 1f}, new Color[]{new Color(c.getRed(),c.getGreen(),c.getBlue(),(int)(255*alpha)), new Color(0,0,0,0)});
            g2.setPaint(rp);
            g2.fillOval(x, y, d, d);
        }
    }

    class BrandPanel extends JPanel {
        BrandPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            g2.setPaint(new GradientPaint(0,0,new Color(20,30,58,210),getWidth(),getHeight(),new Color(13,20,38,130)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setPaint(new GradientPaint(0,0,Theme.ACCENT_ORANGE,getWidth(),0,new Color(0,0,0,0)));
            g2.fillRect(0, 0, getWidth(), 4);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g2.setColor(Theme.ACCENT_ORANGE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());

            int cx = getWidth()/2, cy = 168, r = 52;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setColor(Theme.ACCENT_ORANGE);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx-r-12, cy-r-12, (r+12)*2, (r+12)*2);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setPaint(new GradientPaint(cx-r, cy-r, Theme.ACCENT_ORANGE, cx+r, cy+r, new Color(190,75,15)));
            g2.fillOval(cx-r, cy-r, r*2, r*2);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx-14, cy-22, cx-14, cy+22);
            g2.drawLine(cx-20, cy-22, cx-20, cy-6);
            g2.drawLine(cx-8, cy-22, cx-8, cy-6);
            g2.drawLine(cx-20, cy-6, cx-8, cy-6);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx+12, cy-22, cx+12, cy+22);
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx+8, cy-22, cx+16, cy-7);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 34));
            g2.setColor(Theme.TEXT_WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("RestoApp", cx - fm.stringWidth("RestoApp")/2, cy+r+50);

            g2.setFont(Theme.FONT_BODY);
            g2.setColor(Theme.ACCENT_ORANGE);
            fm = g2.getFontMetrics();
            g2.drawString("Management System", cx - fm.stringWidth("Management System")/2, cy+r+72);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(45, cy+r+92, getWidth()-45, cy+r+92);

            String[] feats = {"✦  Manajemen Menu & Kategori","✦  Kelola Data Pengguna & Peran","✦  Laporan Penjualan Real-time","✦  Dashboard Statistik Lengkap"};
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 13));
            g2.setColor(Theme.TEXT_MUTED);
            int fy = cy+r+116;
            for (String f : feats) {
                fm = g2.getFontMetrics();
                g2.drawString(f, cx - fm.stringWidth(f)/2, fy); fy += 28;
            }

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            g2.setFont(Theme.FONT_SMALL);
            g2.setColor(Theme.TEXT_DIM);
            fm = g2.getFontMetrics();
            g2.drawString("v1.0.0  ·  " + today, cx - fm.stringWidth("v1.0.0  ·  "+today)/2, getHeight()-22);

            g2.dispose();
        }
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(26,38,70),getWidth(),getHeight(),new Color(16,24,48)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // top bar right side
                g2.setPaint(new GradientPaint(0,0,new Color(0,0,0,0),getWidth(),0,Theme.ACCENT_ORANGE));
                g2.fillRect(0, 0, getWidth(), 4);
                g2.dispose();
            }
        };

        int px = 52, fw = 490 - px*2;

        JLabel h1 = Theme.label("Selamat Datang \uD83D\uDC4B", new Font("Segoe UI Emoji", Font.BOLD, 24), Theme.TEXT_WHITE); 
        h1.setBounds(px, 75, fw, 34);
        p.add(h1);

        JLabel h2 = Theme.label("Masuk ke RestoApp Management System", Theme.FONT_BODY, Theme.TEXT_MUTED);
        h2.setBounds(px, 112, fw, 20);
        p.add(h2);

        JSeparator sep = Theme.separator();
        sep.setBounds(px, 144, fw, 1);
        p.add(sep);

        JLabel ul = Theme.label("Username", Theme.FONT_LABEL, Theme.ACCENT_ORANGE);
        ul.setBounds(px, 162, fw, 18);
        p.add(ul);
        userField = new Theme.StyledTextField("Masukkan username...");
        userField.setBounds(px, 182, fw, 42);
        p.add(userField);

        JLabel pl = Theme.label("Password", Theme.FONT_LABEL, Theme.ACCENT_ORANGE);
        pl.setBounds(px, 240, fw, 18);
        p.add(pl);
        passField = new Theme.StyledPasswordField();
        passField.setBounds(px, 260, fw, 42);
        p.add(passField);

        showPassCheck = new JCheckBox("Tampilkan Password");
        showPassCheck.setOpaque(false);
        showPassCheck.setForeground(Theme.TEXT_MUTED);
        showPassCheck.setFont(Theme.FONT_SMALL);
        showPassCheck.setBounds(px, 312, 200, 22);
        showPassCheck.addActionListener(e ->
            passField.setEchoChar(showPassCheck.isSelected() ? '\0' : '●'));
        p.add(showPassCheck);

        msgLabel = Theme.label("", Theme.FONT_SMALL, Theme.ACCENT_RED);
        msgLabel.setBounds(px, 312, fw, 18);
        msgLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(msgLabel);

        Theme.StyledButton loginBtn = new Theme.StyledButton("\uD83D\uDD13  MASUK", Theme.ACCENT_ORANGE, Color.WHITE);
        loginBtn.setBounds(px, 350, fw, 44);
        loginBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14)); 
        loginBtn.addActionListener(e -> doLogin());
        p.add(loginBtn);                                                                

        Theme.StyledButton resetBtn = new Theme.StyledButton("\u2716  RESET", Theme.ACCENT_RED, Color.WHITE);
        resetBtn.setBounds(px, 404, fw, 38);
        resetBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        resetBtn.addActionListener(e -> resetForm());
        p.add(resetBtn);

        JLabel hint = Theme.label("Pastikan username dan password benar sesuai database.", Theme.FONT_SMALL, Theme.TEXT_DIM);
        hint.setBounds(px, 455, fw, 16);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(hint);

        JLabel footer = Theme.label("© 2026 RestoApp · Hak Cipta Dilindungi", Theme.FONT_SMALL, Theme.TEXT_DIM);
        footer.setBounds(px, 530, fw, 16);
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(footer);

        return p;
    }

    private void doLogin() {
        String user = userField.getRealText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            msgLabel.setForeground(Theme.ACCENT_RED);
            msgLabel.setText("Username dan password wajib diisi!");
            return;
        }

        try {
            Connection conn = Database.getConnection();
            
            String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?) AND BINARY password = ?";            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, user);
            pst.setString(2, pass);
            
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                msgLabel.setForeground(Theme.ACCENT_GREEN);
                msgLabel.setText("Login berhasil! Membuka dashboard...");
                
                String namaUserDariDB = rs.getString("nama_user");
                String role = rs.getString("role");                
                Timer t = new Timer(600, e -> {
                    dispose(); 
                    if (role.equalsIgnoreCase("Admin")) {
                        DashboardFrame dashboard = new DashboardFrame(namaUserDariDB);
                        dashboard.setVisible(true);
                    } else if (role.equalsIgnoreCase("Kasir")) {
                        KasirDashboardFrame kasirDashboard = new KasirDashboardFrame(namaUserDariDB);
                        kasirDashboard.setVisible(true);
                    }
                });
                t.setRepeats(false); 
                t.start();
                
            } else {
                msgLabel.setForeground(Theme.ACCENT_RED);
                msgLabel.setText("Username atau password salah!");
                passField.setText("");
            }
            
        } catch (Exception ex) {
            msgLabel.setForeground(Theme.ACCENT_RED);
            msgLabel.setText("Terjadi kesalahan sistem!");
            ex.printStackTrace();
        }
    }

    private void resetForm() {
        userField.setText("Masukkan username...");
        userField.setForeground(Theme.TEXT_MUTED);
        passField.setText("");
        showPassCheck.setSelected(false);
        passField.setEchoChar('●');
        msgLabel.setText("");
    }

    private void enableDrag(JComponent c) {
        final Point[] start = {null};
        c.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { start[0] = e.getPoint(); }
        });
        c.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (start[0] != null) {
                    Point l = getLocation();
                    setLocation(l.x+e.getX()-start[0].x, l.y+e.getY()-start[0].y);
                }
            }
        });
    }
}