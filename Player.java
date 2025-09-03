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

    public Bullet shoot(String soundKey) {
        double bx = x + w/2 - 2;
        double by = y - 8;
        if (weaponMode == 1) {
            // 37mm AA gun - balanced stats
            return new Bullet(bx, by, -10.0, true, soundKey, 4, 10, 1, Color.WHITE);
        } else if (weaponMode == 2) {
            // 100mm AA gun - slower but more powerful, shoots upward
            return new Bullet(bx, by, -8.0, true, soundKey, 5, 12, 2, new Color(255, 200, 0));
        } else if (weaponMode == 3) {
            // MiG-21 - fast air-to-air missiles
            return new Bullet(bx, by, -14.0, true, soundKey, 3, 8, 2, Color.CYAN);
        } else {
            // SAM-2 - guided missile, slower but powerful
            double targetX = x;
            double targetY = y - 100; // Shoot upward
            double angle = Math.atan2(targetY - y, targetX - x);
            return new Bullet(bx, by, -6.0, true, soundKey, 6, 14, 3, new Color(200,200,60), angle);
        }
    }

    public void setWeaponMode(int mode) { this.weaponMode = mode; }
    public int getWeaponMode() { return weaponMode; }

    public void setFirePower(int p) { this.firePower = p; }
    public int getFirePower() { return firePower; }
}
