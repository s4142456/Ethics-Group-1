import java.awt.*;

/**
 * Explosion class creates explosion effects with customizable colors
 * Supports multiple colors for different types of aircraft
 */
public class Explosion {
    private double x, y;
    private int frame = 0;
    private int maxFrames = 12;
    private Color baseColor;
    
    // Default constructor with old orange/yellow color (for backward compatibility)
    public Explosion(double x, double y) {
        this(x, y, new Color(255, 180, 60));
    }
    
    // New constructor with custom color based on aircraft type
    public Explosion(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.baseColor = color;
    }
    
    public boolean update() {
        frame++;
        return frame >= maxFrames;
    }
    
    public void draw(Graphics2D g2) {
        int alpha = Math.max(0, 255 - frame * 20);
    // Draw outer layer with main color
        g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha));
        int size = 20 + frame * 6;
        g2.fillOval((int)x - size/2, (int)y - size/2, size, size);
        
    // Draw inner layer with lighter color to create depth effect
        if (frame < 8) {
            int innerAlpha = Math.max(0, (255 - frame * 30) / 2);
            Color innerColor = new Color(
                Math.min(255, baseColor.getRed() + 50),
                Math.min(255, baseColor.getGreen() + 50), 
                Math.min(255, baseColor.getBlue() + 50),
                innerAlpha
            );
            g2.setColor(innerColor);
            int innerSize = (20 + frame * 6) / 2;
            g2.fillOval((int)x - innerSize/2, (int)y - innerSize/2, innerSize, innerSize);
        }
    }
}
