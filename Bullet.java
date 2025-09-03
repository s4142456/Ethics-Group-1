import java.awt.*;

public class Bullet {
    protected double x, y;
    protected double velocityY;
    protected int width = 4;
    protected int height = 10;
    protected boolean isPlayerBullet;
    protected String soundEffect;
    protected int damage = 1;
    protected Color color = Color.WHITE;
    
    public Bullet(double x, double y, double velocityY, boolean isPlayerBullet, String soundEffect) {
        this.x = x;
        this.y = y;
        this.velocityY = velocityY;
        this.isPlayerBullet = isPlayerBullet;
        this.soundEffect = soundEffect;
        
        // Play sound effect when bullet is created
        AssetManager.getInstance().playSound(soundEffect);
    }

    public Bullet(double x, double y, double velocityY, boolean isPlayerBullet, String soundEffect, int width, int height, int damage, Color color) {
        this(x, y, velocityY, isPlayerBullet, soundEffect);
        this.width = width;
        this.height = height;
        this.damage = damage;
        this.color = color;
    }
    
    public void update() {
        y += velocityY;
    }
    
    public boolean isOffscreen(int screenHeight) {
        return y < -height || y > screenHeight;
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
    
    public void draw(Graphics2D g2) {
    g2.setColor(color != null ? color : (isPlayerBullet ? Color.WHITE : new Color(255, 80, 80)));
    g2.fillRect((int)x, (int)y, width, height);
    }
    
    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }

    public int getDamage() { return damage; }
}
