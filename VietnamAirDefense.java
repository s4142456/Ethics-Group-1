import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
// Change enemies' shooting speed in this file

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;


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
    // Show 'Enemy archive updated!' message
    private boolean showArchiveMsg = false;
    private long archiveMsgTime = 0;
    // Track if enemy index overlay is open
    private boolean showEnemyIndex = false;
    // Track destroyed enemy types for index
    private java.util.Set<String> destroyedTypes = new java.util.HashSet<>();

    // Aircraft index data
    private static final class AircraftInfo {
        final String key, name, desc, spriteKey;
        final int level;
        AircraftInfo(String key, String name, String desc, String spriteKey, int level) {
            this.key = key; this.name = name; this.desc = desc; this.spriteKey = spriteKey; this.level = level;
        }
    }
    private static final AircraftInfo[] AIRCRAFT_INDEX = {
        // Level 1
        new AircraftInfo("morane", "Morane-Saulnier MS.406", "Light fighter, outdated but nimble. Once France's pride in WWII, now a relic in Indochina skies.", "morane.png", 1),
        new AircraftInfo("spitfire", "Spitfire", "Legendary WWII fighter. Fast, agile, and iconic. A dangerous foe in the early war.", "spitfire.png", 1),
        new AircraftInfo("bearcat", "F8F Bearcat", "Beast of the sky: powerful engine, high speed. France's top fighter by 1951.", "bearcat.png", 1),
        new AircraftInfo("dakota", "Dakota (C-47)", "Slow but vital transport. Carried supplies into Dien Bien Phu. Prime target for AA fire.", "dakota.png", 1),
        // Level 2
        new AircraftInfo("f105", "F-105 Thunderchief", "Supersonic strike jet. Built for speed and bombing runs. Vulnerable to AA fire in low passes.", "f105.png", 2),
        new AircraftInfo("f4phantom", "F-4 Phantom II", "All-round powerhouse: twin engines, missiles, and guns. Backbone of US air power.", "f4phantom.png", 2),
        new AircraftInfo("b26", "B-26 Invader", "Old-school bomber. Twin props, heavy bombs. Mostly used for close support early in the war.", "b26.png", 2),
        // Level 3
        new AircraftInfo("skyhawk", "A-4 Skyhawk", "Compact attack jet. Small, fast, and hard to hit. Nicknamed 'Scooter' by its pilots.", "skyhawk.png", 3),
        // Level 4
        new AircraftInfo("b52", "B-52 Stratofortress", "Flying fortress of the jet age. Massive payload, devastating raids. Huge target for SAMs.", "b52.png", 4),
    };
    // Show historical info at level start
    private boolean showLevelIntro = false;
    private String levelIntroText = "";
    private void showLevelIntro(int level) {
        showLevelIntro = true;
        switch (level) {
            case 1 -> levelIntroText = "Level 1 - First Indochina War (1946-1954)\nAfter the August Revolution, French colonists returned to invade Vietnam. Despite limited resources, our forces innovatively created an air defense system using infantry guns, heavy machine guns, and 37mm anti-aircraft artillery.\nThe Battle of Dien Bien Phu in 1954 was the pinnacle: 37mm anti-aircraft guns shot down dozens of Dakota transport planes, contributing decisively to the historic victory.\n\nMission: Use the 37mm anti-aircraft gun to protect the battlefield from enemy aircraft.";
            case 2 -> levelIntroText = "Level 2 - Early Vietnam War (1965-1968)\nThe US deployed troops directly to South Vietnam and expanded the war to North Vietnam. Modern aircraft like F-105s and F-4 Phantoms continuously conducted bombing raids.\nVietnamese air defense was upgraded with 100mm anti-aircraft guns, coordinating with radar and militia forces.\n\nMission: Use anti-aircraft artillery to prevent bombing raids and protect the homeland.";
            case 3 -> levelIntroText = "Level 3 - MiG-21 Air Combat (1969-1972)\nThe Vietnam People's Air Force officially entered the air combat phase. Young pilots flying MiG-21s directly engaged US fighters in the skies.\nAir battles became contests of skill and strategy between pilots from both sides.\n\nMission: Control the MiG-21 to destroy enemy aircraft in intense dogfights.";
            case 4 -> levelIntroText = "Level 4 - 'Hanoi's Dien Bien Phu in the Air' (1972)\nThe US launched Operation Linebacker II, deploying hundreds of B-52 bombers to bomb Hanoi. This was the fiercest battle, where SAM-2 missiles and anti-aircraft artillery coordinated closely to protect the capital.\nIn 12 days and nights, our forces shot down 81 US aircraft, including 34 B-52s - an unprecedented achievement.\n\nMission: Control SAM-2 missiles to shoot down B-52s and protect Hanoi from aerial devastation.";
        }
        repaint();
    }

    // Helper to wrap text for the level intro overlay
    private java.util.List<String> wrapLines(String text, Font bodyFont, int maxWidth, Graphics2D g2) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        Font missionFont = new Font("Arial", Font.BOLD, 20);
        for (String paragraph : text.split("\\n")) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }
            boolean isMission = paragraph.startsWith("Mission:");
            Font useFont = isMission ? missionFont : bodyFont;
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String testLine = line.length() == 0 ? word : line + " " + word;
                if (g2.getFontMetrics(useFont).stringWidth(testLine) > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(testLine);
                }
            }
            if (line.length() > 0) lines.add(line.toString());
        }
        return lines;
    }

    private void drawLevelIntro(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 230));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        int margin = 60;
        int maxWidth = WIDTH - 2 * margin;
        int y = 100;
        Font titleFont = new Font("Arial", Font.BOLD, 28);
        Font bodyFont = new Font("Arial", Font.PLAIN, 18);
        Font missionFont = new Font("Arial", Font.BOLD, 20);
        // Title
        g2.setFont(titleFont);
        g2.setColor(Color.YELLOW);
        java.util.List<String> lines = wrapLines(levelIntroText, bodyFont, maxWidth, g2);
        boolean titleDrawn = false;
        boolean inMission = false;
        for (String line : lines) {
            if (!titleDrawn) {
                g2.setFont(titleFont);
                g2.setColor(Color.YELLOW);
                titleDrawn = true;
            } else if (line.startsWith("Mission:")) {
                g2.setFont(missionFont);
                g2.setColor(Color.CYAN);
                inMission = true;
            } else if (inMission && !line.isEmpty()) {
                g2.setFont(missionFont);
                g2.setColor(Color.CYAN);
            } else {
                g2.setFont(bodyFont);
                g2.setColor(Color.WHITE);
                inMission = false;
            }
            int x = margin;
            g2.drawString(line, x, y);
            y += 28;
        }
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(Color.GREEN);
        String prompt = "Press SPACE to start";
        int px = (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2;
        g2.drawString(prompt, px, HEIGHT - 60);
    }
    // Store previous state for returning from instructions
    private GameState prevState = null;
    // Draw the next level screen overlay
    private void drawNextLevelScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String msg = "Level Complete!";
        int msgX = (WIDTH - g2.getFontMetrics().stringWidth(msg)) / 2;
        g2.drawString(msg, msgX, HEIGHT/2 - 40);
        g2.setFont(new Font("Arial", Font.PLAIN, 28));
        String next = "Press SPACE for next level";
        int nextX = (WIDTH - g2.getFontMetrics().stringWidth(next)) / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(next, nextX, HEIGHT/2 + 20);
    }
    // Draw the next level screen overlay
    // Next level screen state
    private boolean showNextLevelScreen = false;
    private int nextLevelToStart = 0;
    // Volume control for bullet sounds
    private float bulletVolume = 1.0f; // 0.0 (mute) to 1.0 (max)
    private JSlider volumeSlider;
    private void setupVolumeSlider() {
        if (volumeSlider == null) {
            volumeSlider = new JSlider(0, 100, (int)(bulletVolume * 100));
            volumeSlider.setBounds(WIDTH/2 - 100, HEIGHT/2 + 30, 200, 40);
            volumeSlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    bulletVolume = volumeSlider.getValue() / 100f;
                }
            });
            volumeSlider.setFocusable(false);
        }
        add(volumeSlider);
        volumeSlider.setVisible(true);
    }

    private void hideVolumeSlider() {
        if (volumeSlider != null) {
            volumeSlider.setVisible(false);
        }
    }
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
    // The minimum interval, in milliseconds, between enemy shots
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
    showLevelIntro(level);

    // Enemy fire rate per level. Only faster shooting for level 4
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
    // Set weapon mode per level: 1=AA,2=SAM,3=MiG,4=SAM
        int weaponMode = 1;
        if (level == 1) weaponMode = 1;
        else if (level == 2) weaponMode = 2;
        else if (level == 3) weaponMode = 3;
        else if (level == 4) weaponMode = 2;
        player.setWeaponMode(weaponMode);
    // Adjust shot cooldowns per weapon
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
    // Estimate total planes from waves
        totalPlanes = 0;
        for (LevelManager.Wave w : wavePlan) totalPlanes += w.count;
        planesShot = 0;
        
    // Reset state
        enemySpeedMultiplier = 1.0;
        
    // Start level music (force WAV key)
        String musicKey = levelData.music.endsWith(".wav") ? levelData.music.replace(".wav", "") : levelData.music;

        // For level 4, play alarm FIRST, then start music after 5 seconds
        if (level == 4) {
            System.out.println("[DEBUG] Stopping ALL music and sounds before alarm");
            AssetManager.getInstance().stopMusic();
            AssetManager.getInstance().stopAllSounds(); // You may need to implement this in AssetManager if not present
            // Play alarm sound at full volume with NO background music interference
            AssetManager.getInstance().playSound("Alarm");
            // Start music after 5 seconds when alarm ends
            new javax.swing.Timer(5000, e -> {
                AssetManager.getInstance().playMusic(musicKey, true);
                ((javax.swing.Timer)e.getSource()).stop();
            }).start();
        } else {
            // For other levels, play music normally
            AssetManager.getInstance().playMusic(musicKey, true);
        }

    // Reset history panel so it will be created on paint for the intro
        historyPanel = null;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Do not update game logic if paused, but still repaint to show pause overlay
        if (state == GameState.PLAYING) {
            updateGame();
        }
        repaint();
    }
    
    private void updateGame() {
    // Player movement
        double dx = 0, dy = 0;
        if (currentLevel == 3) {
            // Free two-axis movement in level 3
            if (leftPressed) dx -= player.getSpeed();
            if (rightPressed) dx += player.getSpeed();
            if (upPressed) dy -= player.getSpeed();
            if (downPressed) dy += player.getSpeed();
        } else {
            // Only horizontal movement elsewhere
            if (leftPressed) dx -= player.getSpeed();
            if (rightPressed) dx += player.getSpeed();
        }
    // Normalize diagonal speed to avoid faster diagonal movement
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
            playerBullets.add(player.shoot(levelData.weaponSound, bulletVolume));
            lastShotTime = now;
        }
        
    // If there are no active enemies but waves remain, spawn next wave immediately
        if (wavePlan != null && currentWaveIndex < wavePlan.size() && enemies.isEmpty()) {
            nextWaveTime = System.currentTimeMillis();
        }

    // Spawn waves if needed
        if (wavePlan != null && currentWaveIndex < wavePlan.size() && System.currentTimeMillis() >= nextWaveTime) {
            LevelManager.Wave w = wavePlan.get(currentWaveIndex);
            // Create w.count enemies spread across the top area
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
                enemyBullets.add(new Bullet(bulletX, bulletY, 5.0, false, "mig_shoot", bulletVolume));
            }
            lastEnemyShot = now;
        }
        
        // Update bullets
        updateBullets();

        // Update explosions (remove finished ones)
        updateExplosions();

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
        // Check enemy bullets vs player
        Iterator<Bullet> itEB = enemyBullets.iterator();
        while (itEB.hasNext()) {
            Bullet bullet = itEB.next();
            if (player.getBounds().intersects(bullet.getBounds())) {
                itEB.remove();
                lives--;
                if (lives <= 0) {
                    gameOver(false);
                }
                break;
            }
        }
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
                    // Track destroyed type and show archive message if new
                    String type = enemy.getSpriteKey();
                    if (!destroyedTypes.contains(type)) {
                        destroyedTypes.add(type);
                        showArchiveMsg = true;
                        archiveMsgTime = System.currentTimeMillis();
                    }
                    // Determine explosion color based on aircraft type
                    Color explosionColor = getExplosionColorForAircraft(type);
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
        // ...existing code...
    }

    // Helper to get explosion color for aircraft type
    private Color getExplosionColorForAircraft(String key) {
        switch (key) {
            // Level 1
            case "morane":
                return new Color(180, 180, 180); // Gray - old fighter
            case "spitfire":
                return new Color(120, 200, 255); // Blue - iconic fighter
            case "bearcat":
                return new Color(255, 255, 80); // Yellow - top fighter
            case "dakota":
                return new Color(150, 150, 255); // Purple-blue - transport aircraft
            // Level 2 - Early US aircraft
            case "f105":
                return new Color(255, 80, 80); // Dark red - tactical bomber
            case "f4phantom":
                return new Color(200, 100, 255); // Purple - multirole aircraft
            case "b26":
                return new Color(255, 165, 0); // Orange - medium bomber
            // Level 3 - Air combat
            case "skyhawk":
                return new Color(0, 255, 150); // Bright green - light attack aircraft
            // Level 4 - Large aircraft
            case "b52":
                return new Color(255, 50, 50); // Bright red - giant strategic bomber
            default:
                return new Color(255, 180, 60); // Default color (old orange/yellow)
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
    AssetManager.getInstance().stopMusic();
        if (won) {
            AssetManager.getInstance().playSound("victory");
        } else {
            AssetManager.getInstance().playSoundInSequence("gameover1", "gameover2");
        }
    }
    
    private void victory() {
        if (currentLevel < LevelData.LEVELS.length) {
            // Show next level screen after history summary
            showNextLevelScreen = true;
            nextLevelToStart = currentLevel + 1;
            if (timer != null) timer.stop();
            AssetManager.getInstance().stopMusic();
            AssetManager.getInstance().playSound("victory");
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

        // Show enemy index overlay above all if active
        if (showEnemyIndex) {
            drawEnemyIndexOverlay(g2);
        } else if (showLevelIntro) {
            drawLevelIntro(g2);
        } else if (showNextLevelScreen) {
            drawNextLevelScreen(g2);
        } else {
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
                    // Use printAll to render the HistoryPanel into the offscreen Graphics
                    // which avoids RepaintManager volatile buffer issues when no Window is present.
                    historyPanel.printAll(pg);
                    pg.dispose();
                }
                case PLAYING -> drawGame(g2);
                case PAUSED -> {
                    drawGame(g2);
                    drawPauseOverlay(g2);
                }
                case GAME_OVER -> drawGameOver(g2);
                case VICTORY -> drawVictory(g2);
            }
        }

        // Draw 'Enemy archive updated!' message if needed
        if (showArchiveMsg) {
            long now = System.currentTimeMillis();
            if (now - archiveMsgTime < 2000) {
                String msg = "Enemy archive updated!";
                g2.setFont(new Font("Arial", Font.BOLD, 28));
                g2.setColor(Color.ORANGE);
                int msgX = (WIDTH - g2.getFontMetrics().stringWidth(msg)) / 2;
                int msgY = HEIGHT - 80;
                g2.drawString(msg, msgX, msgY);
            } else {
                showArchiveMsg = false;
            }
        }

        g2.dispose();
    }

    private void drawPauseOverlay(Graphics2D g2) {
        // Draw semi-transparent background
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw pause menu box
        int menuWidth = 340;
        int menuHeight = 260;
        int menuX = (WIDTH - menuWidth) / 2;
        int menuY = (HEIGHT - menuHeight) / 2;
        g2.setColor(new Color(40, 40, 40, 240));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        // Draw "Paused" title (largest)
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 54));
        String title = "Paused";
        int titleX = (WIDTH - g2.getFontMetrics().stringWidth(title)) / 2;
        g2.drawString(title, titleX, menuY + 70);

        // Draw "Press P to resume" message (medium)
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        g2.setColor(Color.WHITE);
        String resumeMsg = "Press P to resume";
        int resumeX = (WIDTH - g2.getFontMetrics().stringWidth(resumeMsg)) / 2;
        g2.drawString(resumeMsg, resumeX, menuY + 110);

        // Draw "Adjust volume" label (smaller)
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        String adjustLabel = "Adjust volume:";
        int adjustX = (WIDTH - g2.getFontMetrics().stringWidth(adjustLabel)) / 2;
        g2.drawString(adjustLabel, adjustX, menuY + 145);

        // Draw "Bullet Volume:" label above slider (smallest)
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        String volLabel = "Bullet Volume:";
        int volx = (WIDTH - g2.getFontMetrics().stringWidth(volLabel)) / 2;
        g2.drawString(volLabel, volx, menuY + 170);

        // Position and show the volume slider below the label
        if (volumeSlider == null) setupVolumeSlider();
        volumeSlider.setBounds(WIDTH/2 - 100, menuY + 185, 200, 40);
        setupVolumeSlider();

    // Draw 'Press E for Enemy Index' prompt below the volume bar
    g2.setFont(new Font("Arial", Font.PLAIN, 16));
    String eMsg = "Press E for Enemy Index";
    int eMsgX = (WIDTH - g2.getFontMetrics().stringWidth(eMsg)) / 2;
    g2.setColor(Color.CYAN);
    g2.drawString(eMsg, eMsgX, menuY + 245);
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
        g2.drawString("\u2022 LEFT/RIGHT Arrow Keys: Move aircraft", leftMargin, y); y += 25;
        g2.drawString("\u2022 SPACE Key: Fire weapon", leftMargin, y); y += 25;
        g2.drawString("\u2022 In Level 3: UP/DOWN Arrow Keys: Move aircraft vertically", leftMargin, y); y += 25;
        g2.drawString("\u2022 R Key: Return to menu after defeat or victory", leftMargin, y); y += 40;
        
        // Game objectives
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Objectives:", leftMargin, y); y += 30;
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("\u2022 Destroy all enemy aircraft in each level", leftMargin, y); y += 25;
        g2.drawString("\u2022 Prevent enemy aircraft from reaching the bottom", leftMargin, y); y += 25;
        g2.drawString("\u2022 Avoid getting hit by enemy fire", leftMargin, y); y += 40;
        
        // Levels info
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString("Levels:", leftMargin, y); y += 30;
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("\u2022 Level 1: 1946 - French invasion - Anti-aircraft guns", leftMargin, y); y += 25;
        g2.drawString("\u2022 Level 2: 1965-1968 - US bombing North Vietnam - SAM missiles", leftMargin, y); y += 25;
        g2.drawString("\u2022 Level 3: 1972 - Vietnamese MiG fighters in combat", leftMargin, y); y += 25;
        g2.drawString("\u2022 Level 4: 1972 - Counter-attack, 'Dien Bien Phu in the Air'", leftMargin, y); y += 40;
        
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
        // Advance from history intro or summary to gameplay
        if ((state == GameState.HISTORY_INTRO || state == GameState.HISTORY_SUMMARY) && e.getKeyCode() == KeyEvent.VK_SPACE) {
            state = GameState.PLAYING;
            if (timer != null) timer.start();
            repaint();
            return;
        }
        // Handle enemy index overlay open/close
        if (showEnemyIndex) {
            if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                showEnemyIndex = false;
                // Resume game if it was paused for index overlay
                if (state == GameState.PAUSED && timer != null) timer.stop();
                if (state == GameState.PAUSED) setupVolumeSlider();
                if (state == GameState.PLAYING && timer != null) timer.start();
                repaint();
                return;
            }
            // Prevent any other key from doing anything while overlay is open
            return;
        }
        // Open enemy index overlay with E in PAUSED or PLAYING
        if ((state == GameState.PAUSED || state == GameState.PLAYING) && e.getKeyCode() == KeyEvent.VK_E) {
            showEnemyIndex = true;
            hideVolumeSlider();
            // Pause game if in PLAYING state
            if (state == GameState.PLAYING && timer != null) timer.stop();
            repaint();
            return;
        }
        // Dismiss level intro on SPACE
        if (showLevelIntro && e.getKeyCode() == KeyEvent.VK_SPACE) {
            showLevelIntro = false;
            state = GameState.HISTORY_INTRO;
            if (timer != null) timer.start();
            repaint();
            return;
        }
        // Start game from menu
        if (state == GameState.MENU) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                startLevel(1);
                state = GameState.PLAYING;
                if (timer != null) timer.start();
                repaint();
                return;
            }
            // Shortcuts: 1-4 to jump to levels 1-4
            if (e.getKeyCode() == KeyEvent.VK_1) {
                startLevel(1);
                state = GameState.PLAYING;
                if (timer != null) timer.start();
                repaint();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_2) {
                startLevel(2);
                state = GameState.PLAYING;
                if (timer != null) timer.start();
                repaint();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_3) {
                startLevel(3);
                state = GameState.PLAYING;
                if (timer != null) timer.start();
                repaint();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_4) {
                startLevel(4);
                state = GameState.PLAYING;
                if (timer != null) timer.start();
                repaint();
                return;
            }
        }
        // Resume from pause (P or ESC)
        if (state == GameState.PAUSED && (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            state = GameState.PLAYING;
            hideVolumeSlider();
            if (timer != null) timer.start();
            repaint();
            return;
        }
        // Advance from next level screen
        if (showNextLevelScreen && e.getKeyCode() == KeyEvent.VK_SPACE) {
            showNextLevelScreen = false;
            startLevel(nextLevelToStart);
            state = GameState.PLAYING;
            if (timer != null) timer.start();
            repaint();
            return;
        }
        // Resume from victory/game over with R
        if ((state == GameState.GAME_OVER || state == GameState.VICTORY) && e.getKeyCode() == KeyEvent.VK_R) {
            state = GameState.MENU;
            repaint();
            return;
        }
        // Instructions screen: ESC to return to menu
        if (state == GameState.INSTRUCTIONS && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            state = GameState.MENU;
            repaint();
            return;
        }
        // Instructions screen: I to return to menu
        if (state == GameState.INSTRUCTIONS && e.getKeyCode() == KeyEvent.VK_I) {
            state = GameState.MENU;
            repaint();
            return;
        }
        // Menu: I for instructions
        if (state == GameState.MENU && e.getKeyCode() == KeyEvent.VK_I) {
            state = GameState.INSTRUCTIONS;
            repaint();
            return;
        }
        // Pause game with P
        if (state == GameState.PLAYING && e.getKeyCode() == KeyEvent.VK_P) {
            state = GameState.PAUSED;
            setupVolumeSlider();
            if (timer != null) timer.stop();
            repaint();
            return;
        }
        // Movement and shooting keys
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_DOWN -> downPressed = true;
            case KeyEvent.VK_SPACE -> spacePressed = true;
        }
    }

    // Draw the enemy index overlay (only show info for defeated types)
    private void drawEnemyIndexOverlay(Graphics2D g2) {
    // Draw background first so sprites and text appear on top
    g2.setColor(new Color(0, 0, 0, 230));
    g2.fillRect(0, 0, WIDTH, HEIGHT);

    int margin = 60;
    int y = 80;
    int maxWidth = WIDTH - 2 * margin - 60; // leave space for sprite
    int spriteSize = 48;
    g2.setFont(new Font("Arial", Font.BOLD, 28));
    g2.setColor(Color.CYAN);
    String title = "Enemy Aircraft Index";
    int titleX = (WIDTH - g2.getFontMetrics().stringWidth(title)) / 2;
    g2.drawString(title, titleX, y);
    y += 40;
    g2.setFont(new Font("Arial", Font.PLAIN, 16));
        for (AircraftInfo info : AIRCRAFT_INDEX) {
            boolean destroyed = destroyedTypes.contains(info.key);
            String label = destroyed ? (info.name + ": " + info.desc) : "???";
            g2.setColor(destroyed ? new Color(80, 255, 80) : Color.LIGHT_GRAY);
            // Draw sprite if unlocked
            int textX = margin + (destroyed ? spriteSize + 10 : 0);
            int spriteY = y - 8;
            if (destroyed) {
                String baseName = info.spriteKey.replaceFirst("\\.png$", "");
                System.out.println("[EnemyIndex] Using sprite key: " + baseName);
                Image sprite = AssetManager.getInstance().getImage(baseName);
                if (sprite != null) {
                    g2.drawImage(sprite, margin, spriteY, spriteSize, spriteSize, null);
                } else {
                    // Draw placeholder rectangle and name if image not found
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(margin, spriteY, spriteSize, spriteSize);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    String shortName = info.name.split(" ")[0];
                    int tx = margin + 2;
                    int ty = spriteY + spriteSize/2 + 4;
                    g2.drawString(shortName, tx, ty);
                }
            }
            // Wrap label if too long
            java.util.List<String> lines = new java.util.ArrayList<>();
            if (destroyed) {
                String[] words = label.split(" ");
                StringBuilder line = new StringBuilder();
                for (String word : words) {
                    String testLine = line.length() == 0 ? word : line + " " + word;
                    if (g2.getFontMetrics().stringWidth(testLine) > maxWidth) {
                        lines.add(line.toString());
                        line = new StringBuilder(word);
                    } else {
                        line = new StringBuilder(testLine);
                    }
                }
                if (line.length() > 0) lines.add(line.toString());
            } else {
                lines.add(label);
            }
            int lineY = y + 16;
            for (String l : lines) {
                g2.drawString(l, textX, lineY);
                lineY += 20;
            }
            y += Math.max(spriteSize, lines.size() * 20 + 8);
        }
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.YELLOW);
        String closeMsg = "Press E or ESC to close";
        int closeX = (WIDTH - g2.getFontMetrics().stringWidth(closeMsg)) / 2;
        g2.drawString(closeMsg, closeX, HEIGHT - 60);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
            case KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_DOWN -> downPressed = false;
            case KeyEvent.VK_SPACE -> spacePressed = false;
            // No action needed for P on release
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
