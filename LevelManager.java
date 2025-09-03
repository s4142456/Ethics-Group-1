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
            String enemyType = level.enemySprites[row % level.enemySprites.length];
            int hp = level.enemyHealth[row % level.enemyHealth.length];
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
                    pattern = MovementPattern.DIVE;
                }
                list.add(new EnemyAircraft(x, y, enemyType, hp, level.enemyBaseSpeed, pattern));
            }
        }
        return list;
    }

    // Produce a wave plan for the given level
    public static List<Wave> wavesFor(LevelData level) {
        List<Wave> waves = new ArrayList<>();
        if (level.level == 1) {
            waves.add(new Wave(8, 8, 1, 0));
            waves.add(new Wave(8, 8, 1, 5000));
            waves.add(new Wave(8, 8, 2, 7000));
        } else if (level.level == 2) {
            waves.add(new Wave(6, 6, 1, 0));
            waves.add(new Wave(8, 8, 1, 4000));
            waves.add(new Wave(10, 10, 1, 6000));
        } else if (level.level == 3) {
            waves.add(new Wave(4, 4, 1, 0));
            waves.add(new Wave(6, 6, 1, 3000));
            waves.add(new Wave(8, 8, 1, 4000));
        } else if (level.level == 4) {
            waves.add(new Wave(2, 2, 1, 0));
            waves.add(new Wave(3, 3, 1, 6000));
        }
        return waves;
    }
}
