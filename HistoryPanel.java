import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class HistoryPanel extends JPanel {
    private String title;
    private String description;
    private String backgroundKey;
    private boolean isIntro;
    private int planesShot;
    private int totalPlanes;

    public HistoryPanel(LevelData level, boolean isIntro) {
        this(level, isIntro, 0, 0);
    }

    public HistoryPanel(LevelData level, boolean isIntro, int planesShot, int totalPlanes) {
        this.title = level.name;
        this.description = isIntro ? level.historicalIntro : level.historicalSummary;
        this.backgroundKey = level.background;
        this.isIntro = isIntro;
        this.planesShot = planesShot;
        this.totalPlanes = totalPlanes;
        setPreferredSize(new Dimension(800, 600));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Draw background
        Image bg = AssetManager.getInstance().getImage(backgroundKey);
        if (bg != null) {
            g2.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        }
        
        // Semi-transparent overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw text
        g2.setColor(Color.WHITE);
        if (isIntro) {
            // Intro screen: level title + intro text
            g2.setFont(new Font("Arial", Font.BOLD, 32));
            drawCenteredString(g2, title, getHeight() / 3);

            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            drawWrappedText(g2, description, 100, getHeight() / 2, getWidth() - 200);

            g2.setFont(new Font("Arial", Font.ITALIC, 16));
            String prompt = "Press SPACE to start mission";
            drawCenteredString(g2, prompt, getHeight() - 50);
        } else {
            // Summary screen: mission report + stats + historical summary
            String reportTitle = "Mission Report";
            g2.setFont(new Font("Arial", Font.BOLD, 36));
            drawCenteredString(g2, reportTitle, getHeight() / 4);

            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            String shotLine = String.format("You shot %d of %d enemy planes (%d%%)",
                    planesShot, totalPlanes, totalPlanes > 0 ? (planesShot * 100 / totalPlanes) : 0);
            drawCenteredString(g2, shotLine, getHeight() / 4 + 60);

            // Draw the historical summary text below
            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            drawWrappedText(g2, description, 80, getHeight() / 4 + 110, getWidth() - 160);

            g2.setFont(new Font("Arial", Font.ITALIC, 16));
            String prompt = "Press SPACE to continue";
            drawCenteredString(g2, prompt, getHeight() - 50);
        }
        
        g2.dispose();
    }
    
    private void drawCenteredString(Graphics2D g2, String text, int y) {
        FontMetrics metrics = g2.getFontMetrics();
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    private void drawWrappedString(Graphics2D g2, String text, Rectangle rect, Font font) {
        FontMetrics fm = g2.getFontMetrics(font);
        int lineHeight = fm.getHeight();
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > rect.width) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) lines.add(line.toString());

        int y = rect.y + fm.getAscent();
        for (String l : lines) {
            g2.drawString(l, rect.x, y);
            y += lineHeight;
            if (y > rect.y + rect.height) break;
        }
    }
    
    private void drawWrappedText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics metrics = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineHeight = metrics.getHeight();
        int currentY = y;
        
        for (String word : words) {
            if (metrics.stringWidth(line + word) < maxWidth) {
                line.append(word).append(" ");
            } else {
                g2.drawString(line.toString(), x, currentY);
                line = new StringBuilder(word + " ");
                currentY += lineHeight;
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), x, currentY);
        }
    }
}
