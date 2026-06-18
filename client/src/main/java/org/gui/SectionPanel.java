package org.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

final class SectionPanel extends JPanel {
    private final String title;

    SectionPanel(String title) {
        this.title = title;
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.BORDER),
            BorderFactory.createEmptyBorder(28, 0, 0, 0)
        ));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setColor(UiTheme.HEADER);
        g.fillRect(0, 0, getWidth(), 28);
        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g.drawString(title, 12, 19);
        g.dispose();
    }
}
