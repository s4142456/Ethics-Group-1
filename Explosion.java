import java.awt.*;

/**
 * Lớp Explosion tạo hiệu ứng vụ nổ với màu sắc tùy chỉnh
 * Hỗ trợ nhiều màu khác nhau cho các loại máy bay khác nhau
 */
public class Explosion {
    private double x, y;
    private int frame = 0;
    private int maxFrames = 12;
    private Color baseColor;
    
    // Default constructor với màu cam/vàng cũ (để tương thích ngược)
    public Explosion(double x, double y) {
        this(x, y, new Color(255, 180, 60));
    }
    
    // Constructor mới với màu tùy chỉnh theo loại máy bay
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
        // Vẽ lớp ngoài với màu chính
        g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha));
        int size = 20 + frame * 6;
        g2.fillOval((int)x - size/2, (int)y - size/2, size, size);
        
        // Vẽ lớp trong với màu sáng hơn để tạo hiệu ứng depth
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
