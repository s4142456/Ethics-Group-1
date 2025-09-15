// Create new pattern and edit pattern in this file

import java.awt.*;
import java.util.Random;

enum MovementPattern { HORIZONTAL, HORIZONTAL_DRIFT_DOWN, SINE, ZIGZAG, RANDOM, DIVE, SWOOP, FLANK, CIRCLE, WAVE, STALK, AMBUSH, SNAKE_DOWN_RIGHT, SNAKE_DOWN_LEFT }

public class EnemyAircraft extends Aircraft {
    private double baseSpeed;
    private int formationX, formationY;
    private int moveDir = 1;
    private int maxHp;
    private MovementPattern pattern = MovementPattern.HORIZONTAL;
    private double t = 0; // time accumulator for patterns
    private Random rnd = new Random();
    private double diveTargetY = -1;
    // Per-enemy drift/jitter used by HORIZONTAL_DRIFT_DOWN to avoid tailing
    private double driftDownFactor = 0.0; // fraction of speed applied to vertical drift
    private double jitterAmp = 0.0;       // horizontal jitter amplitude
    private double jitterFreq = 1.0;      // horizontal jitter frequency multiplier
    private double phase = 0.0;           // per-enemy phase offset
    // Cooldown frames after bouncing off a wall to avoid repeated flips and sticking
    private int edgeCooldown = 0;
    
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
        // Initialize per-enemy randomness to reduce synchronized motion
        if (pattern == MovementPattern.HORIZONTAL_DRIFT_DOWN) {
            // Randomize initial horizontal direction a bit so rows split
            this.moveDir = rnd.nextBoolean() ? 1 : -1;
            // Vertical drift: slow descent so enemies spend more time moving horizontally
            this.driftDownFactor = 0.03 + rnd.nextDouble() * 0.02; // ~0.03–0.05 of speed per tick
            // Small horizontal jitter so paths don't overlap exactly
            this.jitterAmp = 0.6 + rnd.nextDouble() * 0.8;   // 0.6–1.4 px per frame sin component
            this.jitterFreq = 0.6 + rnd.nextDouble() * 0.9;  // vary frequency
            this.phase = rnd.nextDouble() * Math.PI * 2.0;
        }
    }
    
    public void updateFormation(double screenWidth, double screenHeight, Player player) {
        // time advance
        t += 0.1 * speed;
        if (edgeCooldown > 0) edgeCooldown--;

        switch (pattern) {
            case HORIZONTAL -> {
                x += moveDir * speed;
                if (x <= 20 || x + w >= screenWidth - 20) {
                    moveDir *= -1;
                    y += 20;
                }
            }
            case HORIZONTAL_DRIFT_DOWN -> {
                // Smooth horizontal bounce with continuous downward drift and slight per-enemy jitter
                // Reduce jitter influence near edges to prevent sticking
                double leftBound = 20;
                double rightBound = screenWidth - 20;
                double leftDist = Math.max(0, x - leftBound);
                double rightDist = Math.max(0, rightBound - (x + w));
                double edgeDist = Math.min(leftDist, rightDist);
                double jitterScale = edgeDist < 30 ? Math.max(0.25, edgeDist / 30.0) : 1.0;
                double jitter = Math.sin(t * jitterFreq + phase) * (jitterAmp * jitterScale);

                x += moveDir * speed + jitter;
                y += speed * (driftDownFactor > 0 ? driftDownFactor : 0.2);

                // Bounce with small push inside and cooldown to avoid flipping every frame
                if (edgeCooldown == 0 && (x <= leftBound || x + w >= rightBound)) {
                    moveDir *= -1;
                    if (x <= leftBound) {
                        x = leftBound + 1; // nudge inside
                        moveDir = 1;
                    } else if (x + w >= rightBound) {
                        x = rightBound - w - 1; // nudge inside
                        moveDir = -1;
                    }
                    phase += Math.PI * 0.5; // change jitter phase to break resonance at wall
                    edgeCooldown = 8; // small cooldown
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
            // Added a new movement pattern called FLANK
            case FLANK -> {
                // Example flanking logic
                double flankOffset = 80;
                boolean isLeftFlank = (formationX % 2 == 0); // or another way to assign left/right
                double targetX = player != null ? player.getX() + (isLeftFlank ? -flankOffset : flankOffset) : x;
                double targetY = player != null ? player.getY() : y;

                // Move horizontally toward targetX
                if (Math.abs(x - targetX) > 5) {
                    x += Math.signum(targetX - x) * speed * 0.6;
                }
                // Vertical movement when FLANK
                if (Math.abs(y - targetY) > 5) {
                    y += Math.signum(targetY - y) * speed * 0.1;
                }
            }
            // Move in circle
            case CIRCLE -> {
                double radius = 70;
                double centerX = formationX;
                double centerY = formationY;
                // control speed of movement
                t += 0.02;
                x = centerX + Math.cos(t) * radius;
                y = centerY + Math.sin(t) * radius;
            }
            case WAVE -> {
                t += 0.05;
                x += speed;
                y = formationY + Math.sin(t * 2) * 40;
            }
            case STALK -> {
                if (player != null) {
                    if (Math.abs(x - player.getX()) > 5) {
                        x += Math.signum(player.getX() - x) * speed * 0.7;
                    }
                }
                // y stays near formationY
                if (Math.abs(y - formationY) > 2) {
                    y += Math.signum(formationY - y) * speed * 0.5;
                }
            }
            // AMBUSH IS SUPPOSED TO FOLLOW THE PLAYER SUDDENLY, BUT IN PRACTICE IT LOOKS BAD
            case AMBUSH -> {
                if (player != null) {
                    // Move towards player if within certain range
                    double distX = player.getX() - x;
                    double distY = player.getY() - y;
                    double distance = Math.sqrt(distX * distX + distY * distY);
                    if (distance < 200) {
                        x += (distX / distance) * speed * 1.2;
                        y += (distY / distance) * speed * 1.2;
                    } else {
                        // Return to formation position
                        if (Math.abs(x - formationX) > 5) {
                            x += Math.signum(formationX - x) * speed * 0.5;
                        }
                        if (Math.abs(y - formationY) > 5) {
                            y += Math.signum(formationY - y) * speed * 0.5;
                        }
                    }
                }
            }
            case SNAKE_DOWN_RIGHT -> {
                // Move horizontally
                x += moveDir * speed * 0.7;
                // Move downward slowly
                y += speed * 0.2;
                // Change direction and move down more when hitting borders
                if (x <= 20 || x + w >= screenWidth - 20) {
                    moveDir *= -1;
                    y += 18; // Drop down a bit more when changing direction
                }
            }
            case SNAKE_DOWN_LEFT -> {
                // Move horizontally
                x += moveDir * speed * 0.7 * -1;
                // Move downward slowly
                y += speed * 0.2;
                // Change direction and move down more when hitting borders
                if (x <= 20 || x + w >= screenWidth - 20) {
                    moveDir *= -1;
                    y += 18; // Drop down a bit more when changing direction
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
    
    // Getter cho spriteKey để xác định loại máy bay
    public String getSpriteKey() {
        return spriteKey;
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
