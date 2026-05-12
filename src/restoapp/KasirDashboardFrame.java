package restoapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class KasirDashboardFrame extends JFrame {

    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel clockLabel;
    private JButton[] navButtons;
    private final String loggedInName;

    // Instance panel terpisah
    private BerandaKasirPanel pnlBeranda;
    private OrderPanel pnlOrder;
    private TransaksiPanel pnlTransaksi;

    private static final String[] NAV_KEYS   = {"BERANDA", "ORDER", "TRANSAKSI"};
    private static final String[][] NAV_ITEMS = {
        {"BERANDA",    "\u229E  Beranda"},
        {"ORDER",      "\uD83D\uDED2  Buat Order"},
        {"TRANSAKSI",  "\uD83D\uDCB3  Transaksi"}
    };

    public KasirDashboardFrame(String namaUser) {
        this.loggedInName = namaUser;
        setTitle("RestoApp - Kasir");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Theme.BG_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);

        root.add(buildSidebar(), BorderLayout.WEST);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(buildTopBar(), BorderLayout.NORTH);

        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setOpaque(false);

        // Inisialisasi Panel (Melempar 'this' agar panel bisa memanggil fungsi di frame utama)
        pnlBeranda = new BerandaKasirPanel();
        pnlOrder = new OrderPanel(this);
        pnlTransaksi = new TransaksiPanel(this);

        contentArea.add(pnlBeranda, "BERANDA");
        contentArea.add(pnlOrder, "ORDER");
        contentArea.add(pnlTransaksi, "TRANSAKSI");

        center.add(contentArea, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
        enableDrag(root);
        showPanel("BERANDA");
        
        Timer t = new Timer(1000, e -> updateClock());
        t.start();
    }

    // Fungsi ini akan dipanggil oleh OrderPanel dan TransaksiPanel
    public void refreshBerandaDanTransaksi() {
        if (pnlBeranda != null) pnlBeranda.refreshData();
        if (pnlTransaksi != null) pnlTransaksi.loadOrders();
        // Tambahkan baris di bawah ini agar OrderPanel juga ikut refresh meja:
        if (pnlOrder != null) pnlOrder.refreshMejaStatus(); 
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(14, 32, 44), getWidth(), getHeight(), new Color(10, 22, 36)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER_DIM);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.setPaint(new GradientPaint(0, 0, Theme.ACCENT_ORANGE, 0, 4, new Color(0,0,0,0)));
                g2.fillRect(0, 0, getWidth(), 4);
                g2.dispose();
            }
        };
        sb.setPreferredSize(new Dimension(220, 0));
        
        JPanel logo = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(20, 20, Theme.ACCENT_ORANGE, 60, 60, new Color(200, 100, 20)));
                g2.fillOval(15, 12, 44, 44);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("$", 15 + (44 - fm.stringWidth("$"))/2, 12 + 30);
                g2.dispose();
            }
        };
        logo.setOpaque(false);
        logo.setBounds(0, 8, 220, 68);
        
        JLabel appName = Theme.label("RestoApp", new Font("Segoe UI", Font.BOLD, 18), Theme.TEXT_WHITE);
        appName.setBounds(70, 18, 140, 24);
        logo.add(appName);
        JLabel appSub = Theme.label("Kasir Dashboard", Theme.FONT_SMALL, Theme.ACCENT_ORANGE);
        appSub.setBounds(70, 42, 140, 16);
        logo.add(appSub);
        sb.add(logo);

        JSeparator sep = Theme.separator();
        sep.setBounds(14, 76, 192, 1);
        sb.add(sep);

        JLabel navLbl = Theme.label("MENU KASIR", new Font("Segoe UI", Font.BOLD, 10), Theme.TEXT_DIM);
        navLbl.setBounds(16, 88, 188, 16);
        sb.add(navLbl);

        navButtons = new JButton[NAV_ITEMS.length];
        int ny = 110;
        for (int i = 0; i < NAV_ITEMS.length; i++) {
            final String key = NAV_ITEMS[i][0];
            JButton btn = createNavButton(NAV_ITEMS[i][1], key.equals("BERANDA"), Theme.ACCENT_ORANGE);
            btn.setBounds(10, ny, 200, 42);
            btn.addActionListener(e -> showPanel(key));
            navButtons[i] = btn;
            sb.add(btn);
            ny += 48;
        }

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
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        bottom.add(logoutBtn);
        sb.add(bottom);

        return sb;
    }

    private JButton createNavButton(String text, boolean selected, Color accentColor) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 180),
                            getWidth(), 0, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 50)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(accentColor);
                    g2.fillRoundRect(0, 8, 4, getHeight()-16, 2, 2);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        b.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        b.setForeground(selected ? Theme.TEXT_WHITE : Theme.TEXT_MUTED);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setSelected(selected);
        return b;
    }

    private JPanel buildTopBar() {
        JPanel tb = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_PANEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER_DIM);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        tb.setPreferredSize(new Dimension(0, 52));

        JLabel title = Theme.label("Kasir Dashboard", new Font("Segoe UI", Font.BOLD, 18), Theme.TEXT_WHITE);
        title.setBounds(20, 12, 300, 28);
        tb.add(title);
        
        clockLabel = Theme.label(getClockText(), Theme.FONT_SMALL, Theme.TEXT_MUTED);
        clockLabel.setBounds(300, 18, 280, 18);
        tb.add(clockLabel);
        
        JLabel kasirBadge = new JLabel("  " + loggedInName + "  ", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                        0, 0,
                        Theme.ACCENT_ORANGE,
                        getWidth(), getHeight(),
                        new Color(190,75,15)
                ));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        FontMetrics fm = kasirBadge.getFontMetrics(kasirBadge.getFont());
        int width = fm.stringWidth("  " + loggedInName + "  ") + 20;

        kasirBadge.setPreferredSize(new Dimension(width, 30));
        kasirBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        kasirBadge.setForeground(Color.WHITE);
        kasirBadge.setOpaque(false);
        kasirBadge.setBounds(780, 12, 110, 28);
        tb.add(kasirBadge);

        enableDrag(tb);
        return tb;
    }

    private JButton smallWinBtn(Color c, String icon) {
        JButton b = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? c.brighter() : c);
                g2.fillOval(0, 0, getWidth(), getHeight());
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

    public void showPanel(String key) {
        cardLayout.show(contentArea, key);
        for (int i = 0; i < NAV_KEYS.length; i++) {
            boolean sel = NAV_KEYS[i].equals(key);
            navButtons[i].setSelected(sel);
            navButtons[i].setForeground(sel ? Theme.TEXT_WHITE : Theme.TEXT_MUTED);
            navButtons[i].setFont(new Font("Segoe UI Symbol", sel ? Font.BOLD : Font.PLAIN, 14));
            navButtons[i].repaint();
        }
    }

    private String getClockText() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss  |  dd-MM-yyyy"));
    }
    
    private void updateClock() { 
        if (clockLabel != null) clockLabel.setText(getClockText());
    }

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

    // Helper Static untuk Styling Table yang bisa dipakai oleh class lain
    public static void styleTable(JTable table) {
        table.setBackground(Theme.BG_TABLE_ROW);
        table.setForeground(Theme.TEXT_WHITE);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(28);
        table.setGridColor(Theme.BORDER_DIM); 
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(new Color(234, 127, 42, 60)); 
        table.setSelectionForeground(Theme.TEXT_WHITE);
        table.getTableHeader().setBackground(Theme.BG_TABLE_HDR);
        table.getTableHeader().setForeground(Theme.ACCENT_ORANGE);
        table.getTableHeader().setFont(Theme.FONT_LABEL);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIM));
        table.setFillsViewportHeight(true); 
        table.setOpaque(true);

        if (table.getParent() instanceof JViewport) {
            JViewport vp = (JViewport) table.getParent();
            vp.setBackground(Theme.BG_TABLE_ROW);
            vp.setOpaque(true);
        }
        
        SwingUtilities.invokeLater(() -> {
            if (table.getParent() != null && table.getParent().getParent() instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) table.getParent().getParent();
                Container parent = sp.getParent();
                sp.setBackground(Theme.BG_TABLE_ROW);
                sp.getViewport().setBackground(Theme.BG_TABLE_ROW);
                
                if (parent != null && !parent.getClass().getName().contains("CardWrapper")) {
                    Rectangle bounds = sp.getBounds();
                    class CardWrapper extends JPanel {
                        public CardWrapper() { setLayout(null); setOpaque(false); setBounds(bounds); }
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(Theme.BG_CARD); 
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                            int lineWidth = getWidth() / 4;
                            GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT_ORANGE, lineWidth, 0, new Color(0, 0, 0, 0));
                            g2.setPaint(gp);
                            g2.fillRoundRect(0, 0, lineWidth, 4, 12, 12);
                            g2.dispose();
                        }
                    }
                    CardWrapper card = new CardWrapper();
                    sp.setBounds(15, 15, bounds.width - 30, bounds.height - 30);
                    sp.setBorder(BorderFactory.createEmptyBorder()); 
                    parent.remove(sp);
                    card.add(sp);
                    parent.add(card);
                    parent.revalidate();
                    parent.repaint();
                }
            }
        });
    }
}