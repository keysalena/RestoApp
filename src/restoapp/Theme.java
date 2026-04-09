package restoapp;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

public class Theme {
    public static final Color BG_DARK        = new Color(13, 20, 38);
    public static final Color BG_PANEL       = new Color(20, 30, 58);
    public static final Color BG_CARD        = new Color(28, 42, 74);
    public static final Color BG_SIDEBAR     = new Color(18, 26, 50);
    public static final Color BG_INPUT       = new Color(14, 22, 44);
    public static final Color BG_TABLE_HDR   = new Color(22, 35, 65);
    public static final Color BG_TABLE_ROW   = new Color(24, 36, 64);
    public static final Color BG_TABLE_ALT   = new Color(20, 30, 55);
    public static final Color ACCENT_ORANGE  = new Color(234, 127, 42);
    public static final Color ACCENT_ORANGE2 = new Color(255, 165, 80);
    public static final Color ACCENT_BLUE    = new Color(60, 130, 240);
    public static final Color ACCENT_GREEN   = new Color(45, 200, 120);
    public static final Color ACCENT_RED     = new Color(210, 65, 65);
    public static final Color ACCENT_YELLOW  = new Color(255, 200, 50);
    public static final Color TEXT_WHITE     = new Color(225, 232, 248);
    public static final Color TEXT_MUTED     = new Color(120, 145, 185);
    public static final Color TEXT_DIM       = new Color(70, 95, 140);
    public static final Color BORDER_DIM     = new Color(40, 58, 100);
    public static final Color BORDER_FOCUS   = ACCENT_ORANGE;

    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setOpaque(false);
        return l;
    }
    public static JLabel iconLabel(String text, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI Symbol", Font.PLAIN, size)); 
        l.setForeground(color);
        l.setOpaque(false);
        return l;
    }
    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER_DIM);
        s.setBackground(BORDER_DIM);
        return s;
    }

    public static class RoundedBorder extends AbstractBorder {
        private final int r, t;
        private final Color c;
        public RoundedBorder(int radius, Color color, int thickness) {
            r = radius; c = color; t = thickness;
        }
        @Override
        public void paintBorder(Component comp, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(t));
            g2.drawRoundRect(x + t/2, y + t/2, w - t, h - t, r, r);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(t+4, t+8, t+4, t+8); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    public static class StyledButton extends JButton {
        private Color bg, fg;
        public StyledButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg; this.fg = fg;
            setOpaque(false); setContentAreaFilled(false);
            setBorderPainted(false); setFocusPainted(false);
            setFont(FONT_LABEL);
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = getModel().isRollover() ? bg.brighter() : bg;
            GradientPaint gp = new GradientPaint(0, 0, base, 0, getHeight(), base.darker());
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            if (getModel().isPressed()) {
                g2.setColor(new Color(0,0,0,60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(2, 2, getWidth()-4, getHeight()/2, 6, 6);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class StyledTextField extends JTextField {
        private String placeholder;
        private boolean showPlaceholder = true;
        public StyledTextField(String ph) {
            placeholder = ph;
            if (ph != null && !ph.isEmpty()) { setText(ph); setForeground(TEXT_MUTED); }
            else setForeground(TEXT_WHITE);
            setBackground(BG_INPUT);
            setCaretColor(ACCENT_ORANGE);
            setFont(FONT_BODY);
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_DIM, 1),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (showPlaceholder) { setText(""); setForeground(TEXT_WHITE); showPlaceholder = false; }
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(8, BORDER_FOCUS, 2),
                        BorderFactory.createEmptyBorder(2, 10, 2, 10)));
                }
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty() && placeholder != null) {
                        setText(placeholder); setForeground(TEXT_MUTED); showPlaceholder = true;
                    }
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(8, BORDER_DIM, 1),
                        BorderFactory.createEmptyBorder(2, 10, 2, 10)));
                }
            });
        }
        public boolean isShowingPlaceholder() { return showPlaceholder; }
        public String getRealText() { return showPlaceholder ? "" : getText(); }
    }

    public static class StyledPasswordField extends JPasswordField {
        public StyledPasswordField() {
            setEchoChar('●');
            setBackground(BG_INPUT);
            setForeground(TEXT_WHITE);
            setCaretColor(ACCENT_ORANGE);
            setFont(FONT_BODY);
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_DIM, 1),
                BorderFactory.createEmptyBorder(2, 10, 2, 10)));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(8, BORDER_FOCUS, 2),
                        BorderFactory.createEmptyBorder(2, 10, 2, 10)));
                }
                public void focusLost(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(8, BORDER_DIM, 1),
                        BorderFactory.createEmptyBorder(2, 10, 2, 10)));
                }
            });
        }
    }

    public static class StyledComboBox extends JComboBox<String> {
        public StyledComboBox(String[] items) {
            super(items);
            
            setBackground(Color.BLACK); 
            setForeground(Color.BLACK);
            setFont(FONT_BODY);
            setOpaque(true);
            
            setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_DIM, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));

            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    
                    if (isSelected) {
                        label.setBackground(Theme.ACCENT_ORANGE); 
                        label.setForeground(Color.WHITE);
                    } else {
                        label.setBackground(Color.BLACK); 
                        label.setForeground(Color.WHITE); 
                    }
                    
                    label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                    return label;
                }
            });
        }
    }

    public static class GlowPanel extends JPanel {
        private Color glowColor;
        public GlowPanel(Color bg, Color glow) {
            super();
            setBackground(bg);
            this.glowColor = glow;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            GradientPaint gp = new GradientPaint(0,0,glowColor,getWidth(),0,new Color(0,0,0,0));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), 3, 4, 4);
            g2.dispose();
        }
    }

    public static JLabel statusBadge(String text) {
        JLabel b = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getText().equalsIgnoreCase("Tersedia") ? new Color(30,100,60) : new Color(110,30,30);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setForeground(text.equalsIgnoreCase("Tersedia") ? ACCENT_GREEN : ACCENT_RED);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        b.setPreferredSize(new Dimension(80, 22));
        return b;
    }
}