package de.usd.cstchef.view.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JMenu;

public class NotificationMenu extends JMenu {

    private static final int DOT_DIAMETER = 8;
    private static final int DOT_TEXT_GAP = 6;
    private static final int EXTRA_RIGHT_PADDING = 6;

    private boolean notificationVisible;
    private Color notificationColor = new Color(0xff6633);

    public NotificationMenu(String text) {
        super(text);
    }

    public NotificationMenu(String text, boolean tearOff) {
        super(text, tearOff);
    }

    public boolean isNotificationVisible() {
        return notificationVisible;
    }

    public void setNotificationVisible(boolean notificationVisible) {
        if (this.notificationVisible != notificationVisible) {
            this.notificationVisible = notificationVisible;
            revalidate();
            repaint();
        }
    }

    public Color getNotificationColor() {
        return notificationColor;
    }

    public void setNotificationColor(Color notificationColor) {
        this.notificationColor = notificationColor;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        if (notificationVisible) {
            preferredSize = new Dimension(
                    preferredSize.width + DOT_TEXT_GAP + DOT_DIAMETER + EXTRA_RIGHT_PADDING,
                    preferredSize.height);
        }
        return preferredSize;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (!notificationVisible || getText() == null || getText().isEmpty()) {
            return;
        }

        Graphics2D graphics2d = (Graphics2D) graphics.create();
        try {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Insets insets = getInsets();
            FontMetrics fontMetrics = graphics2d.getFontMetrics(getFont());
            int textWidth = fontMetrics.stringWidth(getText());
            int dotX = insets.left + textWidth + DOT_TEXT_GAP;
            int dotY = (getHeight() - DOT_DIAMETER) / 2;

            graphics2d.setColor(notificationColor);
            graphics2d.fillOval(dotX, dotY, DOT_DIAMETER, DOT_DIAMETER);
        } finally {
            graphics2d.dispose();
        }
    }
}