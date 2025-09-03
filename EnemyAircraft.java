import java.awt.*;
import java.util.Random;

enum MovementPattern { HORIZONTAL, SINE, ZIGZAG, RANDOM, DIVE, SWOOP }

public class EnemyAircraft extends Aircraft {
    private double baseSpeed;
    private int formationX, formationY;
    private int moveDir = 1;
    private int maxHp;
    private MovementPattern pattern = MovementPattern.HORIZONTAL;
    private double t = 0; // time accumulator for patterns
    private Random rnd = new Random();
    private double diveTargetY = -1;
    
    public EnemyAircraft(double x, double y, String spriteKey, int hp, double baseSpeed) {
        this(x, y, spriteKey, hp, baseSpeed, MovementPattern.HORIZONTAL);
    }

    public EnemyAircraft(double x, double y, String spriteKey, int hp, double baseSpeed, MovementPattern pattern) {
        super(x, y, 60, 40, hp, baseSpeed, spriteKey, false);
        this.baseSpeed = baseSpeed;
        this.formationX = (int)x;
        this.formationY = (int)y;
        this.maxHp = hp;
        this.pattern = pattern;
    }
    
    public void updateFormation(double screenWidth, double screenHeight, Player player) {
        // time advance
        t += 0.1 * speed;

        switch (pattern) {
            case HORIZONTAL -> {
                x += moveDir * speed;
                if (x <= 20 || x + w >= screenWidth - 20) {
                    moveDir *= -1;
                    y += 20;
                }
            }
            case SINE -> {
                x += moveDir * speed;
                y = formationY + Math.sin(t) * 18;
                if (x <= 20 || x + w >= screenWidth - 20) moveDir *= -1;
            }
            case ZIGZAG -> {
                x += moveDir * speed;
                y += Math.sin(t * 2.0) * speed * 1.5;
                if (x <= 20 || x + w >= screenWidth - 20) moveDir *= -1;
            }
            case RANDOM -> {
                // small random jitter plus horizontal drift
                x += (rnd.nextDouble() - 0.5) * speed * 1.5 + moveDir * speed * 0.6;
                y += (rnd.nextDouble() - 0.5) * speed * 0.6;
                if (rnd.nextDouble() < 0.01) moveDir *= -1;
            }
            case DIVE -> {
                // occasional dive towards lower Y
                if (diveTargetY < 0 && rnd.nextDouble() < 0.01) {
                    diveTargetY = formationY + 120 + rnd.nextInt(80);
                }
                if (diveTargetY > 0) {
                    // dive down
                    y += speed * 2.2;
                    x += Math.sin(t) * speed;
                    if (y >= diveTargetY) {
                        diveTargetY = -1; // return to formation next frame
                        y = formationY;
                    }
                } else {
                    // patrol horizontally
                    x += moveDir * speed * 0.6;
                    if (x <= 20 || x + w >= screenWidth - 20) moveDir *= -1;
                }
            }
            case SWOOP -> {
                // Swoop towards player's current position with a curved path
                double px = player != null ? player.getX() + player.getWidth() / 2.0 : screenWidth / 2.0;
                double py = player != null ? player.getY() + player.getHeight() / 2.0 : formationY + 100;
                // initialize target on first call
                if (diveTargetY < 0) {
                    diveTargetY = py + 20 + rnd.nextInt(60);
                }
                // progress parameter
                double prog = Math.min(1.0, t * 0.03);
                // quadratic interpolation from formation -> swoop apex -> target
                double apexX = formationX + (px - formationX) * 0.4 + Math.sin(t*0.7)*40;
                double apexY = formationY - 40 - Math.abs(Math.sin(t))*30;
                // simple quadratic lerp
                double ix = (1 - prog) * formationX + prog * apexX;
                double iy = (1 - prog) * formationY + prog * apexY;
                double fx = (1 - prog) * apexX + prog * px;
                double fy = (1 - prog) * apexY + prog * diveTargetY;
                x = (1 - prog) * ix + prog * fx;
                y = (1 - prog) * iy + prog * fy;
                // after completing swoop, slowly return to formation
                if (prog >= 0.98) {
                    diveTargetY = -1;
                    // gently move back towards formation position
                    x += (formationX - x) * 0.02;
                    y += (formationY - y) * 0.02;
                }
            }
        }
        // clamp to screen bounds so enemies don't leave visible area
        x = Math.max(10, Math.min(screenWidth - w - 10, x));
        y = Math.max(10, Math.min(screenHeight - h - 10, y));
    }
    
    public boolean reachedBottom(double screenHeight) {
        return y + h >= screenHeight - 90;
    }
    
    public void increaseSpeed(double amount) {
        speed = baseSpeed * (1 + amount);
    }

    @Override
    public void draw(Graphics2D g2) {
        Image sprite = AssetManager.getInstance().getImage(spriteKey);
        if (sprite != null) {
            g2.drawImage(sprite, (int)x, (int)y, w, h, null);
        } else {
            g2.setColor(Color.RED);
            g2.fillRect((int)x, (int)y, w, h);
        }
        // Draw hp bar
        int barW = w;
        int barH = 6;
        int bx = (int)x;
        int by = (int)y - barH - 4;
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(bx, by, barW, barH);
        float ratio = Math.max(0f, (float)hp / (float)maxHp);
        g2.setColor(new Color(60, 200, 60));
        g2.fillRect(bx, by, (int)(barW * ratio), barH);
        g2.setColor(Color.WHITE);
        g2.drawRect(bx, by, barW, barH);
    }
}
