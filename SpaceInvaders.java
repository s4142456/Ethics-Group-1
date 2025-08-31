import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SpaceInvaders extends JFrame {
    public SpaceInvaders() {
        setTitle("Space Invaders - Java");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        panel.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpaceInvaders::new);
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Screen
    static final int WIDTH = 800;
    static final int HEIGHT = 600;

    // Player
    Player player;
    boolean leftPressed, rightPressed, spacePressed;
    long lastShotTime = 0;
    long shotCooldownMs = 250; // player fire rate

    // Invaders
    List<Alien> aliens = new ArrayList<>();
    int alienDir = 1; // 1 -> right, -1 -> left
    int stepDown = 14;
    double alienSpeed = 1.2; // base horizontal speed (pixels per tick)
    double alienSpeedIncrement = 0.05; // speed up as aliens die

    // Bullets
    List<Bullet> bullets = new ArrayList<>(); // player bullets
    List<Bullet> alienBullets = new ArrayList<>();
    long lastAlienShot = 0;
    long alienShotCooldown = 900; // ms between *possible* alien shots
    Random rng = new Random();

    // Game loop
    Timer timer;
    int fps = 60;

    // Game state
    boolean running = false;
    boolean gameOver = false;
    boolean win = false;
    int score = 0;
    int lives = 3;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        initGame();
    }

    void start() {
        timer = new Timer(1000 / fps, this);
        timer.start();
        running = true;
    }

    void initGame() {
        player = new Player(WIDTH / 2 - 20, HEIGHT - 70);
        bullets.clear();
        alienBullets.clear();
        aliens.clear();
        score = 0;
        gameOver = false;
        win = false;
        lives = 3;

        // Create alien formation (10 x 5)
        int cols = 10;
        int rows = 5;
        int startX = 80;
        int startY = 60;
        int spacingX = 60;
        int spacingY = 50;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = startX + c * spacingX;
                int y = startY + r * spacingY;
                int hp = (r < 1) ? 1 : (r < 3 ? 2 : 3); // front rows weaker
                aliens.add(new Alien(x, y, hp));
            }
        }
        alienDir = 1;
        alienSpeed = 1.2;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!running) return;
        if (gameOver) {
            repaint();
            return;
        }

        // Player movement
        if (leftPressed) player.x -= player.speed;
        if (rightPressed) player.x += player.speed;
        player.x = Math.max(10, Math.min(WIDTH - player.w - 10, player.x));

        // Player shooting
        long now = System.currentTimeMillis();
        if (spacePressed && now - lastShotTime > shotCooldownMs) {
            bullets.add(new Bullet(player.x + player.w / 2 - 2, player.y - 8, -8, true));
            lastShotTime = now;
        }

        // Aliens movement: move horizontally; on wall, move down and reverse
        boolean hitEdge = false;
        for (Alien a : aliens) {
            a.x += alienDir * alienSpeed;
            if (a.x <= 20 || a.x + a.w >= WIDTH - 20) {
                hitEdge = true;
            }
        }
        if (hitEdge) {
            for (Alien a : aliens) {
                a.y += stepDown;
            }
            alienDir *= -1;
        }

        // Aliens shooting (random chance limited by cooldown)
        if (now - lastAlienShot > alienShotCooldown && !aliens.isEmpty()) {
            // Pick a random column bottom-most alien to shoot
            Alien shooter = pickShooter();
            if (shooter != null && rng.nextDouble() < 0.8) {
                alienBullets.add(new Bullet(shooter.x + shooter.w / 2 - 2, shooter.y + shooter.h + 4, 4 + rng.nextInt(3), false));
            }
            lastAlienShot = now;
        }

        // Update bullets
        updateBullets();

        // Collisions: bullets vs aliens
        Iterator<Bullet> itB = bullets.iterator();
        while (itB.hasNext()) {
            Bullet b = itB.next();
            Rectangle br = b.getBounds();
            boolean hit = false;
            Iterator<Alien> itA = aliens.iterator();
            while (itA.hasNext()) {
                Alien a = itA.next();
                if (br.intersects(a.getBounds())) {
                    a.hp--;
                    if (a.hp <= 0) {
                        itA.remove();
                        score += 10;
                        // Speed up slightly as aliens are destroyed
                        alienSpeed += alienSpeedIncrement;
                    }
                    hit = true;
                    break;
                }
            }
            if (hit) itB.remove();
        }

        // Collisions: alien bullets vs player
        Iterator<Bullet> itAB = alienBullets.iterator();
        while (itAB.hasNext()) {
            Bullet b = itAB.next();
            if (b.getBounds().intersects(player.getBounds())) {
                itAB.remove();
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    win = false;
                }
            }
        }

        // Check loss if aliens reach bottom or touch player
        for (Alien a : aliens) {
            if (a.y + a.h >= HEIGHT - 90 || a.getBounds().intersects(player.getBounds())) {
                gameOver = true;
                win = false;
                break;
            }
        }

        // Win condition
        if (aliens.isEmpty()) {
            gameOver = true;
            win = true;
        }

        repaint();
    }

    private Alien pickShooter() {
        if (aliens.isEmpty()) return null;
        // Group aliens by column index (based on spacing)
        // Simpler: choose a random alien that has no alien directly below it (bottom-most in a small x-range)
        List<Alien> candidates = new ArrayList<>();
        for (Alien a : aliens) {
            boolean hasBelow = false;
            for (Alien b : aliens) {
                if (b == a) continue;
                if (Math.abs(b.x - a.x) < 10 && b.y > a.y) {
                    hasBelow = true; break;
                }
            }
            if (!hasBelow) candidates.add(a);
        }
        if (candidates.isEmpty()) return aliens.get(rng.nextInt(aliens.size()));
        return candidates.get(rng.nextInt(candidates.size()));
    }

    private void updateBullets() {
        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            b.y += b.vy;
            if (b.y < -20) it.remove();
        }
        Iterator<Bullet> it2 = alienBullets.iterator();
        while (it2.hasNext()) {
            Bullet b = it2.next();
            b.y += b.vy;
            if (b.y > HEIGHT + 20) it2.remove();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Stars background
        g2.setColor(new Color(18, 18, 18));
        for (int i = 0; i < 80; i++) {
            int sx = (i * 97) % WIDTH;
            int sy = (i * 53) % HEIGHT;
            g2.fillRect(sx, sy, 2, 2);
        }

        // HUD
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.PLAIN, 18));
        g2.drawString("Score: " + score, 20, 24);
        g2.drawString("Lives: " + lives, WIDTH - 120, 24);

        // Player
        player.draw(g2);

        // Aliens
        for (Alien a : aliens) a.draw(g2);

        // Bullets
        g2.setColor(Color.WHITE);
        for (Bullet b : bullets) b.draw(g2);
        g2.setColor(new Color(255, 80, 80));
        for (Bullet b : alienBullets) b.draw(g2);

        if (gameOver) {
            String msg = win ? "YOU WIN!" : "GAME OVER";
            g2.setFont(new Font("Consolas", Font.BOLD, 48));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(msg);
            g2.setColor(new Color(0, 0, 0, 170));
            g2.fillRoundRect(WIDTH/2 - tw/2 - 20, HEIGHT/2 - 70, tw + 40, 130, 20, 20);
            g2.setColor(Color.WHITE);
            g2.drawString(msg, WIDTH/2 - tw/2, HEIGHT/2 - 20);

            g2.setFont(new Font("Consolas", Font.PLAIN, 20));
            String sub = "Press R to restart";
            int sw = g2.getFontMetrics().stringWidth(sub);
            g2.drawString(sub, WIDTH/2 - sw/2, HEIGHT/2 + 16);
        }

        g2.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_SPACE -> spacePressed = true;
            case KeyEvent.VK_R -> {
                if (gameOver) initGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_SPACE -> spacePressed = false;
        }
    }
}

class Player {
    int x, y, w = 40, h = 20;
    int speed = 6;

    Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Rectangle getBounds() { return new Rectangle(x, y, w, h); }

    void draw(Graphics2D g2) {
        g2.setColor(new Color(120, 200, 255));
        g2.fillRoundRect(x, y, w, h, 6, 6);
        // cannon barrel
        g2.fillRect(x + w/2 - 3, y - 10, 6, 10);
    }
}

class Alien {
    int x, y, w = 36, h = 24;
    int hp = 1;

    Alien(int x, int y, int hp) {
        this.x = x; this.y = y; this.hp = hp;
    }

    Rectangle getBounds() { return new Rectangle(x, y, w, h); }

    void draw(Graphics2D g2) {
        // Color by HP
        Color c = switch (hp) {
            case 1 -> new Color(0x7CFC00);  // light green
            case 2 -> new Color(0xADFF2F);  // yellow-green
            default -> new Color(0xFFD700); // gold
        };
        g2.setColor(c);
        g2.fillRoundRect(x, y, w, h, 8, 8);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 8, y + 10, 6, 4);
        g2.fillRect(x + w - 14, y + 10, 6, 4);
    }
}

class Bullet {
    int x, y, w = 4, h = 10;
    int vy;
    boolean fromPlayer;

    Bullet(int x, int y, int vy, boolean fromPlayer) {
        this.x = x; this.y = y; this.vy = vy; this.fromPlayer = fromPlayer;
    }

    Rectangle getBounds() { return new Rectangle(x, y, w, h); }

    void draw(Graphics2D g2) {
        g2.fillRect(x, y, w, h);
    }
}
