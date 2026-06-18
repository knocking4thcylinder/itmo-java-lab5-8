package org.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

final class VisualizationPanel extends JPanel {
    private List<MovieRow> rows = List.of();
    private MovieRow selected;
    private java.util.function.Consumer<MovieRow> listener = row -> {};
    private float alpha = 1.0f;

    VisualizationPanel() {
        setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                for (MovieRow row : rows) {
                    if (circle(row).contains(event.getPoint())) {
                        listener.accept(row);
                        return;
                    }
                }
            }
        });
    }

    void setRows(List<MovieRow> rows) {
        this.rows = rows;
        alpha = 0.0f;
        Timer timer = new Timer(20, null);
        timer.addActionListener(event -> {
            alpha = Math.min(1.0f, alpha + 0.12f);
            repaint();
            if (alpha >= 1.0f) {
                timer.stop();
            }
        });
        timer.start();
    }

    void setSelected(MovieRow selected) {
        this.selected = selected;
        repaint();
    }

    void addMovieClickListener(java.util.function.Consumer<MovieRow> listener) {
        this.listener = listener;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        for (MovieRow row : rows) {
            java.awt.geom.Ellipse2D circle = circle(row);
            g.setClip(circle);
            paintFlag(g, row.country(), circle);
            g.setClip(null);
            g.setStroke(new BasicStroke(row.equals(selected) ? 4f : 1.5f));
            g.setColor(row.equals(selected) ? new Color(37, 99, 235) : ownerColor(row.owner()));
            g.draw(circle);
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(11f));
            g.drawString(row.name(), (int) circle.getCenterX() - 18, (int) circle.getMaxY() + 14);
        }
        g.dispose();
    }

    private java.awt.geom.Ellipse2D circle(MovieRow row) {
        int size = Math.max(34, Math.min(82, row.oscars() * 8 + 30));
        int x = 30 + Math.floorMod(row.x(), Math.max(1, getWidth() - size - 60));
        int y = 30 + Math.floorMod(row.y(), Math.max(1, getHeight() - size - 70));
        return new java.awt.geom.Ellipse2D.Double(x, y, size, size);
    }

    private Color ownerColor(String owner) {
        int hash = owner == null ? 0 : owner.hashCode();
        float hue = Math.floorMod(hash, 360) / 360f;
        return Color.getHSBColor(hue, 0.68f, 0.72f);
    }

    private void paintFlag(Graphics2D g, String country, java.awt.geom.Ellipse2D circle) {
        int x = (int) circle.getX();
        int y = (int) circle.getY();
        int w = (int) circle.getWidth();
        int h = (int) circle.getHeight();
        if ("ITALY".equals(country)) {
            g.setColor(new Color(0, 146, 70));
            g.fillRect(x, y, w / 3, h);
            g.setColor(Color.WHITE);
            g.fillRect(x + w / 3, y, w / 3, h);
            g.setColor(new Color(206, 43, 55));
            g.fillRect(x + 2 * w / 3, y, w, h);
        } else if ("CHINA".equals(country)) {
            g.setColor(new Color(222, 41, 16));
            g.fillRect(x, y, w, h);
            g.setColor(new Color(255, 222, 0));
            g.fillPolygon(star(x + w * 0.28, y + h * 0.32, w * 0.13, w * 0.055));
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, w, h);
            g.setColor(new Color(188, 0, 45));
            g.fillOval(x + w / 2 - w / 5, y + h / 2 - h / 5, 2 * w / 5, 2 * h / 5);
        }
    }

    private Polygon star(double centerX, double centerY, double outerRadius, double innerRadius) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            double radius = i % 2 == 0 ? outerRadius : innerRadius;
            polygon.addPoint(
                (int) Math.round(centerX + Math.cos(angle) * radius),
                (int) Math.round(centerY + Math.sin(angle) * radius)
            );
        }
        return polygon;
    }
}
