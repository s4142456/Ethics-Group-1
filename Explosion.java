import java.awt.*;

public class Explosion {
    private double x, y;
    private int frame = 0;
    private int maxFrames = 12;
    
    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public boolean update() {
        frame++;
        return frame >= maxFrames;
    }
    
    public void draw(Graphics2D g2) {
        int alpha = Math.max(0, 255 - frame * 20);
        g2.setColor(new Color(255, 180, 60, alpha));
        int size = 20 + frame * 6;
        g2.fillOval((int)x - size/2, (int)y - size/2, size, size);
    }
}
