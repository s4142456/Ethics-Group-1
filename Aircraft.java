import java.awt.*;

public class Aircraft {
    protected double x, y;
    protected int w, h;
    protected int hp;
    protected double speed;
    protected String spriteKey;
    protected boolean isPlayer;
    
    public Aircraft(double x, double y, int w, int h, int hp, double speed, String spriteKey, boolean isPlayer) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.hp = hp;
        this.speed = speed;
        this.spriteKey = spriteKey;
        this.isPlayer = isPlayer;
    }
    
    public void move(double dx, double dy) {
        x += dx;
        y += dy;
    }
    
    public void damage(int amount) {
        hp -= amount;
    }
    
    public boolean isDestroyed() {
        return hp <= 0;
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, w, h);
    }
    
    public void draw(Graphics2D g2) {
        Image sprite = AssetManager.getInstance().getImage(spriteKey);
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, w, h, null);
        } else {
            // Fallback rendering if sprite not loaded
            g2.setColor(isPlayer ? new Color(120, 200, 255) : Color.RED);
            g2.fillRect((int)x, (int)y, w, h);
        }
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return w; }
    public int getHeight() { return h; }
    public int getHp() { return hp; }
    public double getSpeed() { return speed; }
}
