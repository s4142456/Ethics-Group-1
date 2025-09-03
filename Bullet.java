import java.awt.*;

public class Bullet {
    protected double x, y;
    protected double velocityX;
    protected double velocityY;
    protected double speed;
    protected double angle;
    protected int width = 4;
    protected int height = 10;
    protected boolean isPlayerBullet;
    protected String soundEffect;
    protected int damage = 1;
    protected Color color = Color.WHITE;
    protected boolean isGuided = false;
    
    public Bullet(double x, double y, double velocityY, boolean isPlayerBullet, String soundEffect) {
        this.x = x;
        this.y = y;
        this.speed = Math.abs(velocityY);
        this.angle = velocityY < 0 ? -Math.PI/2 : Math.PI/2;
        this.velocityX = 0;
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

    public Bullet(double x, double y, double speed, boolean isPlayerBullet, String soundEffect, int width, int height, int damage, Color color, double angle) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = angle;
        this.velocityX = speed * Math.cos(angle);
        this.velocityY = speed * Math.sin(angle);
        this.isPlayerBullet = isPlayerBullet;
        this.soundEffect = soundEffect;
        this.width = width;
        this.height = height;
        this.damage = damage;
        this.color = color;
        this.isGuided = true;
        
        AssetManager.getInstance().playSound(soundEffect);
    }
    
    public void update() {
        if (isGuided) {
            x += velocityX;
            y += velocityY;
        } else {
            y += velocityY;
        }
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
