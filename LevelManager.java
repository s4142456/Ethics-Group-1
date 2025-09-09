// Manages waves of enemies and enemies movement patterns based on level in this file

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    public static class Wave {
        public final int count;
        public final int cols;
        public final int rows;
        public final int delayMs; // delay before this wave after previous
        public Wave(int count, int cols, int rows, int delayMs) { this.count = count; this.cols = cols; this.rows = rows; this.delayMs = delayMs; }
    }
    public static List<EnemyAircraft> createEnemiesFor(LevelData level, int cols, int rows, int startX, int startY, int spacingX, int spacingY) {
        List<EnemyAircraft> list = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            // For level 1, ensure we use different enemy types within each wave
            String enemyType;
            int hp;
            if (level.level == 1) {
                // Mix up enemy types more evenly
                int enemyIndex = (row + cols) % level.enemySprites.length;
                enemyType = level.enemySprites[enemyIndex];
                hp = level.enemyHealth[enemyIndex % level.enemyHealth.length];
            } else {
                enemyType = level.enemySprites[row % level.enemySprites.length];
                hp = level.enemyHealth[row % level.enemyHealth.length];
            }
            for (int col = 0; col < cols; col++) {
                double x = startX + col * spacingX;
                double y = startY + row * spacingY;
                // choose movement pattern based on level and row
                MovementPattern pattern = MovementPattern.HORIZONTAL;
                if (level.level == 1) {
                    // simpler for level 1
                    pattern = MovementPattern.HORIZONTAL;
                } else if (level.level == 2) {
                    pattern = (row % 2 == 0) ? MovementPattern.SINE : MovementPattern.ZIGZAG;
                } else if (level.level == 3) {
                    // Air combat: mix of random and zigzag to make dogfights unpredictable
                    pattern = (col % 3 == 0) ? MovementPattern.RANDOM : MovementPattern.ZIGZAG;
                } else if (level.level == 4) {
                    // B-52s mostly slow with occasional dive (simulated)
                    // Testing DIVE & FLANK for level 4
                    pattern = (row % 2 == 0) ? MovementPattern.DIVE : MovementPattern.FLANK;
                }
                list.add(new EnemyAircraft(x, y, enemyType, hp, level.enemyBaseSpeed, pattern));
            }
        }
        return list;
    }

    // This is where we specified the number of enemies in a stage
    // count = number of enemies
    // cols = number of columns in the enemy waves formation
    // delayMs = time delay (in miliseconds) between waves
    public static List<Wave> wavesFor(LevelData level) {
        List<Wave> waves = new ArrayList<>();
        if (level.level == 1) {
            // Wave 1: First two types of aircraft
            waves.add(new Wave(6, 6, 2, 0));
            // Wave 2: Next two types of aircraft
            waves.add(new Wave(6, 6, 2, 5000));
            // Wave 3: All four types mixed
            waves.add(new Wave(8, 8, 4, 7000));
        } else if (level.level == 2) {
            waves.add(new Wave(6, 2, 3, 0));     // 3 rows for all aircraft types
            waves.add(new Wave(9, 3, 3, 4000));  // 3 rows for all aircraft types
            waves.add(new Wave(9, 3, 3, 6000));  // 3 rows for all aircraft types
        } else if (level.level == 3) {
            waves.add(new Wave(4, 2, 2, 0));     // 2 rows for both aircraft types
            waves.add(new Wave(6, 3, 2, 3000));  // 2 rows for both aircraft types
            waves.add(new Wave(8, 4, 2, 4000));  // 2 rows for both aircraft types
        } else if (level.level == 4) {
            waves.add(new Wave(8, 7, 2, 0));
            waves.add(new Wave(14, 7, 2, 0));
        }
        return waves;
    }
}
