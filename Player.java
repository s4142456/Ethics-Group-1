import java.awt.*;

public class Player extends Aircraft {
    private int firePower = 1;
    private int weaponMode = 1; // 1=AA gun,2=SAM,3=MiG

    private boolean canMove4Dir = false;

    public Player(double x, double y, int w, int h, int hp, double speed, String spriteKey) {
        super(x, y, w, h, hp, speed, spriteKey, true);
        // MiG-21 can move in 4 directions
        canMove4Dir = (weaponMode == 3);
    }

    public void moveVertical(double dy) {
        if (canMove4Dir) {
            y += dy * speed;
            // Keep player within screen bounds
            if (y < 0) y = 0;
            if (y > 600 - h) y = 600 - h; // Use GamePanel.HEIGHT
        }
    }

    public Bullet shoot(String soundKey, String bulletSpriteKey, float volume) {
        // Base spawn roughly at center-top of player sprite; adjust for larger bullet sizes below
        if (weaponMode == 1) {
            // 37mm AA gun - balanced stats
            int bw = 10, bh = 24;
            double bx = x + w/2 - bw/2.0;
            double by = y - bh + 4; // slight overlap so it appears from barrel
            return new Bullet(bx, by, -11.0, true, soundKey, bw, bh, 1, Color.WHITE, bulletSpriteKey, volume);
        } else if (weaponMode == 2) {
            // 100mm AA gun - slower but more powerful, shoots upward
            int bw = 14, bh = 32;
            double bx = x + w/2 - bw/2.0;
            double by = y - bh + 6;
            return new Bullet(bx, by, -9.0, true, soundKey, bw, bh, 2, new Color(255, 200, 0), bulletSpriteKey, volume);
        } else if (weaponMode == 3) {
            // MiG-21 - fast air-to-air missiles
            int bw = 12, bh = 32;
            double bx = x + w/2 - bw/2.0;
            double by = y - bh + 6;
            return new Bullet(bx, by, -16.0, true, soundKey, bw, bh, 2, Color.CYAN, bulletSpriteKey, volume);
        } else {
            // SAM-2 - guided missile, slower but powerful
            int bw = 16, bh = 40;
            double bx = x + w/2 - bw/2.0;
            double by = y - bh + 8;
            double targetX = x;          // simple upward guidance start
            double targetY = y - 120;     // further target for initial angle
            double angle = Math.atan2(targetY - y, targetX - x);
            Bullet b = new Bullet(bx, by, -7.0, true, soundKey, bw, bh, 3, new Color(200,200,60), angle, volume);
            return b;
        }
    }

    public void setWeaponMode(int mode) { this.weaponMode = mode; }
    public int getWeaponMode() { return weaponMode; }

    public void setFirePower(int p) { this.firePower = p; }
    public int getFirePower() { return firePower; }
}
