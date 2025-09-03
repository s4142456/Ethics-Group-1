import java.awt.*;

public class Player extends Aircraft {
    private int firePower = 1;
    private int weaponMode = 1; // 1=AA gun,2=SAM,3=MiG

    public Player(double x, double y, int w, int h, int hp, double speed, String spriteKey) {
        super(x, y, w, h, hp, speed, spriteKey, true);
    }

    public Bullet shoot(String soundKey) {
        double bx = x + w/2 - 2;
        double by = y - 8;
        if (weaponMode == 1) {
            return new Bullet(bx, by, -10.0, true, soundKey, 4, 10, 1, Color.WHITE);
        } else if (weaponMode == 2) {
            // SAM - larger, slower, more damage
            return new Bullet(bx, by, -6.0, true, soundKey, 6, 14, 3, new Color(200,200,60));
        } else {
            // MiG - faster, narrower
            return new Bullet(bx, by, -14.0, true, soundKey, 3, 8, 1, Color.CYAN);
        }
    }

    public void setWeaponMode(int mode) { this.weaponMode = mode; }
    public int getWeaponMode() { return weaponMode; }

    public void setFirePower(int p) { this.firePower = p; }
    public int getFirePower() { return firePower; }
}
