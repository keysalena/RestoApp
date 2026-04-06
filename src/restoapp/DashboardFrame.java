package restoapp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardFrame extends JFrame {

    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel clockLabel;
    private JButton[] navButtons;
    private String[] navKeys = {"HOME", "MASAKAN", "KATEGORI", "ROLE", "USER"};
    private final String loggedInName;

    public DashboardFrame(String namaUser) {
        this.loggedInName = namaUser;
        setTitle("RestoApp - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        // Root
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);

        // Sidebar
        root.add(buildSidebar(), BorderLayout.WEST);

        // Center (topbar + content)
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(buildTopBar(), BorderLayout.NORTH);

        // Content area with CardLayout
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);
        contentArea.add(new HomePanel(), "HOME");
        contentArea.add(new MasakanPanel(), "MASAKAN");
        contentArea.add(new KategoriPanel(), "KATEGORI");
        contentArea.add(new RolePanel(), "ROLE");
        contentArea.add(new UserPanel(), "USER");

        center.add(contentArea, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
        enableDrag(root);
        showPanel("HOME");

        // Clock timer
        Timer t = new Timer(1000, e -> updateClock());
        t.start();
    }

    //  Sidebar
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,Theme.BG_SIDEBAR,getWidth(),getHeight(),new Color(15,22,45)));
                g2.fillRect(0,0,getWidth(),getHeight());
                // right border
                g2.setColor(Theme.BORDER_DIM);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                // orange accent top
                g2.setPaint(new GradientPaint(0,0,Theme.ACCENT_ORANGE,0,4,new Color(0,0,0,0)));
                g2.fillRect(0,0,getWidth(),4);
                g2.dispose();
            }
        };
        sb.setPreferredSize(new Dimension(220, 0));

        // Logo area
        JPanel logo = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // circle
                g2.setPaint(new GradientPaint(20,20,Theme.ACCENT_ORANGE,60,60,new Color(190,75,15)));
                g2.fillOval(15, 12, 44, 44);
                // fork icon
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(30, 18, 30, 50); g2.drawLine(26, 18, 26, 28); g2.drawLine(34, 18, 34, 28); g2.drawLine(26, 28, 34, 28);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(44, 18, 44, 50);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(40, 18, 48, 28);
                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setBounds(0, 8, 220, 68);

        JLabel appName = Theme.label("RestoApp", new Font("Segoe UI", Font.BOLD, 18), Theme.TEXT_WHITE);
        appName.setBounds(70, 18, 140, 24);
        logo.add(appName);
        JLabel appSub = Theme.label("Management System", Theme.FONT_SMALL, Theme.ACCENT_ORANGE);
        appSub.setBounds(70, 42, 140, 16);
        logo.add(appSub);
        sb.add(logo);

        // Separator
        JSeparator sep = Theme.separator();
        sep.setBounds(14, 76, 192, 1);
        sb.add(sep);

        // Nav label
        JLabel navLbl = Theme.label("NAVIGASI", new Font("Segoe UI", Font.BOLD, 10), Theme.TEXT_DIM);
        navLbl.setBounds(16, 88, 188, 16);
        sb.add(navLbl);

        // Nav buttons
        String[][] navItems = {
            {"HOME",    "\u229E  Beranda"},   // Ikon ⊞
            {"MASAKAN", "\uD83C\uDF74  Masakan"}, // Ikon 🍽
            {"KATEGORI","\u2630  Kategori"},  // Ikon ☰
            {"ROLE",    "\uD83D\uDEE1  Role"},    // Ikon 🛡
            {"USER",    "\uD83D\uDC64  User"}     // Ikon 👤
        };
        navButtons = new JButton[navItems.length];
        int ny = 110;
        for (int i = 0; i < navItems.length; i++) {
            final String key = navItems[i][0];
            JButton btn = createNavButton(navItems[i][1], key.equals("HOME"));
            btn.setBounds(10, ny, 200, 42);
            btn.addActionListener(e -> showPanel(key));
            navButtons[i] = btn;
            sb.add(btn);
            ny += 48;
        }

        // Bottom: logout
        JPanel bottom = new JPanel(null);
        bottom.setOpaque(false);
        bottom.setBounds(0, 620, 220, 80);

        JSeparator sep2 = Theme.separator();
        sep2.setBounds(14, 0, 192, 1);
        bottom.add(sep2);

        Theme.StyledButton logoutBtn = new Theme.StyledButton("\u23FB  Keluar", Theme.ACCENT_RED, Color.WHITE);
        logoutBtn.setBounds(14, 12, 192, 38);
        logoutBtn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14)); 
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin keluar?", "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        bottom.add(logoutBtn);
        sb.add(bottom);

        return sb;
    }

    private JButton createNavButton(String text, boolean selected) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setPaint(new GradientPaint(0,0,new Color(234,127,42,200),getWidth(),0,new Color(234,127,42,60)));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(Theme.ACCENT_ORANGE);
                    g2.fillRoundRect(0,8,4,getHeight()-16,2,2);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,12));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0,16,0,0));
        b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        b.setForeground(selected ? Theme.TEXT_WHITE : Theme.TEXT_MUTED);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setSelected(selected);
        return b;
    }

    //  Top Bar
    private JPanel buildTopBar() {
        JPanel tb = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_PANEL);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(Theme.BORDER_DIM);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        tb.setPreferredSize(new Dimension(0, 52));

        JLabel title = Theme.label("Dashboard", new Font("Segoe UI", Font.BOLD, 18), Theme.TEXT_WHITE);
        title.setBounds(20, 12, 300, 28);
        tb.add(title);

        clockLabel = Theme.label(getClockText(), Theme.FONT_SMALL, Theme.TEXT_MUTED);
        clockLabel.setBounds(300, 18, 280, 18);
        tb.add(clockLabel);

        // Admin badge
                JLabel adminBadge = new JLabel("  " + loggedInName + "  ", SwingConstants.CENTER) {            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,Theme.ACCENT_ORANGE,getWidth(),getHeight(),new Color(190,75,15)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        adminBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        adminBadge.setForeground(Color.WHITE);
        adminBadge.setOpaque(false);
        adminBadge.setBounds(900, 12, 90, 28);
        tb.add(adminBadge);

        JButton maxBtn = smallWinBtn(new Color(50, 180, 80), "□");
        maxBtn.setBounds(1034, 14, 18, 18);
        maxBtn.addActionListener(e -> {
            if (getExtendedState() == JFrame.MAXIMIZED_BOTH) setExtendedState(JFrame.NORMAL);
            else setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
        tb.add(maxBtn);

        JButton closeBtn = smallWinBtn(Theme.ACCENT_RED, "✕");
        closeBtn.setBounds(1058, 14, 18, 18);
        closeBtn.addActionListener(e -> System.exit(0));
        tb.add(closeBtn);

        enableDrag(tb);
        return tb;
    }

    private JButton smallWinBtn(Color c, String icon) {
        JButton b = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? c.brighter() : c);
                g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    //  Navigation
    public void showPanel(String key) {
        cardLayout.show(contentArea, key);
        for (int i = 0; i < navKeys.length; i++) {
            boolean sel = navKeys[i].equals(key);
            navButtons[i].setSelected(sel);
            navButtons[i].setForeground(sel ? Theme.TEXT_WHITE : Theme.TEXT_MUTED);
            navButtons[i].setFont(new Font("Segoe UI Symbol", sel ? Font.BOLD : Font.PLAIN, 14));
            navButtons[i].repaint();
        }

        Component comp = contentArea.getComponent(java.util.Arrays.asList(navKeys).indexOf(key));
        if (comp instanceof Refreshable) ((Refreshable)comp).refresh();
    }

    interface Refreshable { void refresh(); }

    private String getClockText() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss  |  dd-MM-yyyy"));
    }
    private void updateClock() { if (clockLabel != null) clockLabel.setText(getClockText()); }

    private void enableDrag(JComponent c) {
        final Point[] start = {null};
        c.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { start[0] = e.getPoint(); }});
        c.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (start[0] != null && getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    Point l = getLocation();
                    setLocation(l.x+e.getX()-start[0].x, l.y+e.getY()-start[0].y);
                }
            }
        });
    }
}
