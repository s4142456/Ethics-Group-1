// Change enemies' shooting speed in this file

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class VietnamAirDefense extends JFrame {
    private static final String GAME_TITLE = "Vietnam Air Defense (1946-1972)";
    private GamePanel gamePanel;
    
    public VietnamAirDefense() {
        // JavaFX check removed; background music is disabled in this build.
        setTitle(GAME_TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        
        // Preload all assets
        AssetManager.getInstance();
        
        // Clean up resources on window close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                AssetManager.getInstance().cleanup();
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VietnamAirDefense game = new VietnamAirDefense();
            game.setVisible(true);
            game.gamePanel.start();
        });
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    // Screen dimensions
    static final int WIDTH = 800;
    static final int HEIGHT = 600;
    
    // Game state
    private GameState state = GameState.MENU;
    private int currentLevel = 0;
    private LevelData levelData;
    private int score = 0;
    private int lives = 3;
    
    // Game objects
    private Player player;
    private List<EnemyAircraft> enemies = new ArrayList<>();
    private List<LevelManager.Wave> wavePlan = null;
    private int currentWaveIndex = 0;
    private long nextWaveTime = 0;
    private List<Bullet> playerBullets = new ArrayList<>();
    private List<Bullet> enemyBullets = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private int planesShot = 0;
    private int totalPlanes = 0;
    private HistoryPanel historyPanel;
    
    // Control flags
    private boolean leftPressed, rightPressed, spacePressed;
    private boolean upPressed, downPressed;
    private long lastShotTime = 0;
    private long shotCooldown = 250; // Adjustable per level
    
    // Enemy behavior
    // Increase difficulty by speeding up enemies after every plane is destroyed
    private double enemySpeedMultiplier = 1.0;
    // Time tracking for enemy shots
    private long lastEnemyShot = 0;
    // The minimum interval, in miliseconds, between enemy shots
    private long enemyShotCooldown = 900;
    private Random random = new Random();
    
    // Game loop
    private Timer timer;
    private static final int FPS = 60;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
    }
    
    public void start() {
        timer = new Timer(1000 / FPS, this);
        timer.start();
        AssetManager.getInstance().playMusic("bgm_menu", true);
    }
    
    private void startLevel(int level) {
        currentLevel = level;
        levelData = LevelData.LEVELS[level - 1];

        // enemy fire rate per level. Only faster shooting for level 4
        if (level == 4) {
            enemyShotCooldown = 150;
        } else if (level == 3){
            enemyShotCooldown = 250;
        } else {
           enemyShotCooldown = (long)(900 / levelData.enemyBaseSpeed); 
        }
        
        // Reset lives at the start of each level
        lives = 2;
        
        // Initialize player
        player = new Player(WIDTH/2 - 30, HEIGHT - 80, 60, 40, 3, 6,
            levelData.playerSprite);
        // set weapon mode per level: 1=AA,2=SAM,3=MiG,4=SAM
        int weaponMode = 1;
        if (level == 1) weaponMode = 1;
        else if (level == 2) weaponMode = 2;
        else if (level == 3) weaponMode = 3;
        else if (level == 4) weaponMode = 2;
        player.setWeaponMode(weaponMode);
        // adjust shot cooldowns per weapon
        if (weaponMode == 1) shotCooldown = 250;
        else if (weaponMode == 2) shotCooldown = 500;
        else if (weaponMode == 3) shotCooldown = 150;
        
        // Clear and create enemies
        enemies.clear();
        playerBullets.clear();
        enemyBullets.clear();
        
        // Prepare wave plan and compute total planes
        wavePlan = LevelManager.wavesFor(levelData);
        currentWaveIndex = 0;
        nextWaveTime = System.currentTimeMillis();
        enemies.clear();
        // estimate total planes from waves
        totalPlanes = 0;
        for (LevelManager.Wave w : wavePlan) totalPlanes += w.count;
        planesShot = 0;
        
        // Reset state
        enemySpeedMultiplier = 1.0;
        
        // Start level music
        AssetManager.getInstance().playMusic(levelData.music, true);

        // Reset history panel so it will be created on paint for the intro
        historyPanel = null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == GameState.PLAYING) {
            updateGame();
        }
        repaint();
    }
    
    private void updateGame() {
        // Player movement
        double dx = 0, dy = 0;
        if (currentLevel == 3) {
            // free two-axis movement in level 3
            if (leftPressed) dx -= player.getSpeed();
            if (rightPressed) dx += player.getSpeed();
            if (upPressed) dy -= player.getSpeed();
            if (downPressed) dy += player.getSpeed();
        } else {
            // only horizontal movement elsewhere
            if (leftPressed) dx -= player.getSpeed();
            if (rightPressed) dx += player.getSpeed();
        }
        // normalize diagonal speed to avoid faster diagonal movement
        if (dx != 0 && dy != 0) {
            double inv = 1.0 / Math.sqrt(2);
            dx *= inv; dy *= inv;
        }
        player.move(dx, dy);

        // Keep player in bounds
        double px = player.getX();
        px = Math.max(10, Math.min(WIDTH - player.getWidth() - 10, px));
        double py = player.getY();
        py = Math.max(10, Math.min(HEIGHT - player.getHeight() - 10, py));
        player.move(px - player.getX(), py - player.getY());
        
        // Player shooting
        long now = System.currentTimeMillis();
        if (spacePressed && now - lastShotTime > shotCooldown) {
            playerBullets.add(player.shoot(levelData.weaponSound));
            lastShotTime = now;
        }
        
        // If there are no active enemies but waves remain, spawn next wave immediately
        if (wavePlan != null && currentWaveIndex < wavePlan.size() && enemies.isEmpty()) {
            nextWaveTime = System.currentTimeMillis();
        }

        // Spawn waves if needed
        if (wavePlan != null && currentWaveIndex < wavePlan.size() && System.currentTimeMillis() >= nextWaveTime) {
            LevelManager.Wave w = wavePlan.get(currentWaveIndex);
            // create w.count enemies spread across the top area
            int cols = Math.max(1, Math.min(12, w.cols));
            int rows = Math.max(1, w.rows);
            int startX = 60 + (random.nextInt(40));
            int startY = 40 + (currentWaveIndex * 20);
            List<EnemyAircraft> newOnes = LevelManager.createEnemiesFor(levelData, cols, rows, startX, startY, 60, 50);
            enemies.addAll(newOnes);
            currentWaveIndex++;
            nextWaveTime = System.currentTimeMillis() + w.delayMs;
        }

        // Update enemies
        for (EnemyAircraft enemy : enemies) {
            enemy.updateFormation(WIDTH, HEIGHT, player);
            if (enemy.reachedBottom(HEIGHT) || enemy.getBounds().intersects(player.getBounds())) {
                gameOver(false);
                return;
            }
        }
    // (MiG vertical movement handled earlier with normalized dx/dy)
        
        // Enemy shooting
        if (now - lastEnemyShot > enemyShotCooldown && !enemies.isEmpty()) {
            EnemyAircraft shooter = enemies.get(random.nextInt(enemies.size()));
            if (random.nextDouble() < 0.3) {
                double bulletX = shooter.getX() + shooter.getWidth()/2 - 2;
                double bulletY = shooter.getY() + shooter.getHeight();
                enemyBullets.add(new Bullet(bulletX, bulletY, 5.0, false, "mig_shoot"));
            }
            lastEnemyShot = now;
        }
        
        // Update bullets
        updateBullets();
        
        // Check collisions
        checkCollisions();
        
        // Check win condition: only when all waves spawned and no enemies remain
        if (enemies.isEmpty() && (wavePlan == null || currentWaveIndex >= wavePlan.size())) {
            victory();
        }
    }
    
    private void updateBullets() {
        // Update player bullets
        Iterator<Bullet> it = playerBullets.iterator();
        while (it.hasNext()) {
            Bullet bullet = it.next();
            bullet.update();
            if (bullet.isOffscreen(HEIGHT)) {
                it.remove();
            }
        }
        
        // Update enemy bullets
        it = enemyBullets.iterator();
        while (it.hasNext()) {
            Bullet bullet = it.next();
            bullet.update();
            if (bullet.isOffscreen(HEIGHT)) {
                it.remove();
            }
        }
    }
    
    private void checkCollisions() {
        // Check player bullets vs enemies
        Iterator<Bullet> itB = playerBullets.iterator();
        while (itB.hasNext()) {
            Bullet bullet = itB.next();
            Rectangle bulletBounds = bullet.getBounds();
            
            Iterator<EnemyAircraft> itE = enemies.iterator();
            while (itE.hasNext()) {
                EnemyAircraft enemy = itE.next();
                if (bulletBounds.intersects(enemy.getBounds())) {
                    enemy.damage(bullet.getDamage());
                    itB.remove();
                    
                    // Xác định màu vụ nổ dựa trên loại máy bay
                    Color explosionColor = getExplosionColorForAircraft(enemy.getSpriteKey());
                    
                    if (enemy.isDestroyed()) {
                        itE.remove();
                        score += 100;
                        planesShot++;
                        explosions.add(new Explosion(enemy.getX() + enemy.getWidth()/2, enemy.getY() + enemy.getHeight()/2, explosionColor));
                        // Speed up remaining enemies
                        enemySpeedMultiplier += 0.08;
                        enemies.forEach(e -> e.increaseSpeed(enemySpeedMultiplier));
                    } else {
                        explosions.add(new Explosion(enemy.getX() + enemy.getWidth()/2, enemy.getY() + enemy.getHeight()/2, explosionColor));
                    }
                    break;
                }
            }
        }
        
        // Check enemy bullets vs player
        Iterator<Bullet> itEB = enemyBullets.iterator();
        while (itEB.hasNext()) {
            Bullet bullet = itEB.next();
            if (bullet.getBounds().intersects(player.getBounds())) {
        itEB.remove();
        lives -= bullet.getDamage();
                // Vụ nổ của player sử dụng màu xanh dương
                explosions.add(new Explosion(player.getX() + player.getWidth()/2, player.getY() + player.getHeight()/2, new Color(100, 150, 255)));
                if (lives <= 0) {
                    gameOver(false);
                }
            }
        }
    // Update explosion animations
    updateExplosions();
    }
    
    // Phương thức mới để xác định màu vụ nổ cho từng loại máy bay
    private Color getExplosionColorForAircraft(String aircraftType) {
        switch (aircraftType) {
            // Level 1 - Máy bay Pháp
            case "morane":
                return new Color(255, 100, 100); // Đỏ sáng - máy bay chiến đấu nhẹ
            case "spitfire":
                return new Color(255, 150, 50); // Cam đỏ - máy bay chiến đấu nổi tiếng
            case "bearcat":
                return new Color(255, 200, 0); // Vàng - máy bay mạnh mẽ
            case "dakota":
                return new Color(150, 150, 255); // Xanh tím - máy bay vận tải
                
            // Level 2 - Máy bay Mỹ thời kỳ đầu
            case "f105":
                return new Color(255, 80, 80); // Đỏ đậm - máy bay ném bom chiến thuật
            case "f4phantom":
                return new Color(200, 100, 255); // Tím - máy bay đa nhiệm
            case "b26":
                return new Color(255, 165, 0); // Cam - máy bay ném bom trung bình
                
            // Level 3 - Không chiến
            case "skyhawk":
                return new Color(0, 255, 150); // Xanh lá sáng - máy bay tấn công nhẹ
                
            // Level 4 - Máy bay lớn
            case "b52":
                return new Color(255, 50, 50); // Đỏ rực - máy bay ném bom chiến lược khổng lồ
                
            default:
                return new Color(255, 180, 60); // Màu mặc định (cam/vàng cũ)
        }
    }

    private void updateExplosions() {
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion ex = it.next();
            if (ex.update()) it.remove();
        }
    }
    
    private void gameOver(boolean won) {
        state = won ? GameState.VICTORY : GameState.GAME_OVER;
        AssetManager.getInstance().stopAllMusic();
        AssetManager.getInstance().playSound(won ? "victory" : "gameover");
    }
    
    private void victory() {
        if (currentLevel < LevelData.LEVELS.length) {
            state = GameState.HISTORY_SUMMARY;
            // ensure the history panel is recreated and visible immediately
            historyPanel = null;
            if (timer != null) timer.stop();
            AssetManager.getInstance().stopAllMusic();
            AssetManager.getInstance().playSound("victory");
            System.out.println("DEBUG: Entering HISTORY_SUMMARY for level " + currentLevel + ", planesShot=" + planesShot + ", totalPlanes=" + totalPlanes);
            repaint();
        } else {
            gameOver(true);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        switch (state) {
            case MENU -> drawMenu(g2);
            case INSTRUCTIONS -> drawInstructions(g2);
            case HISTORY_INTRO, HISTORY_SUMMARY -> {
                // Render history using HistoryPanel
                if (historyPanel == null) {
                    if (state == GameState.HISTORY_SUMMARY) {
                        int total = totalPlanes > 0 ? totalPlanes : (enemies == null ? 0 : enemies.size());
                        historyPanel = new HistoryPanel(levelData, false, planesShot, total);
                    } else {
                        historyPanel = new HistoryPanel(levelData, true);
                    }
                }
                // paint history panel into this panel
                Graphics pg = g2.create();
                // debug overlay - indicate we are in history state
                g2.setColor(new Color(255, 40, 40));
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                String dbg = "DEBUG: history state=" + state + " level=" + currentLevel;
                g2.drawString(dbg, 10, 22);
                historyPanel.setSize(getWidth(), getHeight());
                historyPanel.paint(pg);
                pg.dispose();
            }
            case PLAYING -> drawGame(g2);
            case GAME_OVER -> drawGameOver(g2);
            case VICTORY -> drawVictory(g2);
        }
        
        g2.dispose();
    }
    
    private void drawInstructions(Graphics2D g2) {
        // Draw a dark semi-transparent background
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Title
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        String title = "Game Instructions";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - titleWidth) / 2, 80);
        
        // Disclaimer
        g2.setFont(new Font("Arial", Font.ITALIC, 16));
        g2.setColor(new Color(200, 200, 200));
        String disclaimer = "This game is only a simulation based on historical events";
        int disclaimerWidth = g2.getFontMetrics().stringWidth(disclaimer);
        g2.drawString(disclaimer, (WIDTH - disclaimerWidth) / 2, 110);
        
        // Draw instructions with appropriate spacing
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Controls section
        int y = 145; // Adjusted to account for the disclaimer
        int leftMargin = 80;
        g2.setColor(Color.GREEN);
        g2.drawString("Controls:", leftMargin, y);
        y += 30;
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("• LEFT/RIGHT Arrow Keys: Move aircraft", leftMargin, y); y += 25;
        g2.drawString("• SPACE Key: Fire weapon", leftMargin, y); y += 25;
        g2.drawString("• In Level 3: UP/DOWN Arrow Keys: Move aircraft vertically", leftMargin, y); y += 25;
        g2.drawString("• R Key: Return to menu after defeat or victory", leftMargin, y); y += 40;
        
        // Game objectives
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Objectives:", leftMargin, y); y += 30;
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("• Destroy all enemy aircraft in each level", leftMargin, y); y += 25;
        g2.drawString("• Prevent enemy aircraft from reaching the bottom", leftMargin, y); y += 25;
        g2.drawString("• Avoid getting hit by enemy fire", leftMargin, y); y += 40;
        
        // Levels info
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Levels:", leftMargin, y); y += 30;
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("• Level 1: 1946 - French invasion - Anti-aircraft guns", leftMargin, y); y += 25;
        g2.drawString("• Level 2: 1965-1968 - US bombing North Vietnam - SAM missiles", leftMargin, y); y += 25;
        g2.drawString("• Level 3: 1972 - Vietnamese MiG fighters in combat", leftMargin, y); y += 25;
        g2.drawString("• Level 4: 1972 - Counter-attack, 'Dien Bien Phu in the Air'", leftMargin, y); y += 40;
        
        // Return instructions
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String backMsg = "Press ESC to return to menu";
        int backWidth = g2.getFontMetrics().stringWidth(backMsg);
        g2.drawString(backMsg, (WIDTH - backWidth) / 2, HEIGHT - 40);
    }
    
    private void drawGame(Graphics2D g2) {
        // Draw background
        Image bg = AssetManager.getInstance().getImage(levelData.background);
        if (bg != null) {
            g2.drawImage(bg, 0, 0, WIDTH, HEIGHT, null);
        }
        
        // Draw player
        player.draw(g2);
        
        // Draw enemies
        for (EnemyAircraft enemy : enemies) {
            enemy.draw(g2);
        }
        
        // Draw bullets
        g2.setColor(Color.WHITE);
        for (Bullet bullet : playerBullets) {
            bullet.draw(g2);
        }
        g2.setColor(new Color(255, 80, 80));
        for (Bullet bullet : enemyBullets) {
            bullet.draw(g2);
        }
    // Draw explosions
    for (Explosion ex : explosions) ex.draw(g2);
        
        // Draw HUD
        drawHUD(g2);
    }
    
    private void drawMenu(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "Vietnam Air Defense";
        int titleX = (WIDTH - g2.getFontMetrics().stringWidth(title)) / 2;
        g2.drawString(title, titleX, HEIGHT/3);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String prompt = "Press SPACE to Start";
        int promptX = (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2;
        g2.drawString(prompt, promptX, HEIGHT/2);
        
        String instructions = "Press I for Instructions";
        int instructionsX = (WIDTH - g2.getFontMetrics().stringWidth(instructions)) / 2;
        g2.drawString(instructions, instructionsX, HEIGHT/2 + 40);
    }
    
    private void drawHUD(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Score: " + score, 20, 30);
        g2.drawString("Lives: " + lives, WIDTH - 100, 30);
        g2.drawString("Level " + currentLevel, WIDTH/2 - 40, 30);
    }
    
    private void drawGameOver(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String text = "Game Over";
        int x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, HEIGHT/2);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        text = "Final Score: " + score;
        x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, HEIGHT/2 + 50);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Press R to Return to Menu";
        x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, HEIGHT/2 + 100);
    }
    
    private void drawVictory(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String text = "Victory!";
        int x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, HEIGHT/2);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        text = "Final Score: " + score;
        x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, HEIGHT/2 + 50);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        text = "Press R to Return to Menu";
        x = (WIDTH - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, x, HEIGHT/2 + 100);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_DOWN -> downPressed = true;
            case KeyEvent.VK_I -> {
                if (state == GameState.MENU) {
                    state = GameState.INSTRUCTIONS;
                }
            }
            case KeyEvent.VK_ESCAPE -> {
                if (state == GameState.INSTRUCTIONS) {
                    state = GameState.MENU;
                }
            }
            case KeyEvent.VK_SPACE -> {
                spacePressed = true;
                if (state == GameState.MENU) {
                    state = GameState.HISTORY_INTRO;
                    currentLevel = 1;
                    startLevel(currentLevel);
                } else if (state == GameState.HISTORY_INTRO) {
                    state = GameState.PLAYING;
                } else if (state == GameState.HISTORY_SUMMARY) {
                    currentLevel++;
                    planesShot = 0;
                    if (currentLevel <= LevelData.LEVELS.length) {
                        startLevel(currentLevel);
                        state = GameState.HISTORY_INTRO;
                        if (timer != null) timer.start();
                    } else {
                        state = GameState.VICTORY;
                    }
                }
            }
            case KeyEvent.VK_R -> {
                if (state == GameState.GAME_OVER || state == GameState.VICTORY) {
                    state = GameState.MENU;
                    score = 0;
                    currentLevel = 0;
                    lives = 2;  // Reset lives when restarting game
                    AssetManager.getInstance().playMusic("bgm_menu", true);
                }
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_DOWN -> downPressed = false;
            case KeyEvent.VK_SPACE -> spacePressed = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
}
