// Change health of enemies in this file
public class LevelData {
    public final int level;
    public final String name;
    public final String description;
    public final String playerSprite;
    public final String[] enemySprites;
    public final String background;
    public final String music;
    public final String weaponSound;
    public final String playerBulletSprite; // new: bullet sprite per level
    public final double enemyBaseSpeed;
    public final int[] enemyHealth;
    public final String historicalIntro;
    public final String historicalSummary;
    
    // class for level data. Including enem health, enemy speed
    public LevelData(int level, String name, String description, String playerSprite,
                    String[] enemySprites, String background, String music, String weaponSound,
                    String playerBulletSprite, double enemyBaseSpeed, int[] enemyHealth, String historicalIntro,
                    String historicalSummary) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.playerSprite = playerSprite;
        this.enemySprites = enemySprites;
        this.background = background;
        this.music = music;
        this.weaponSound = weaponSound;
        this.playerBulletSprite = playerBulletSprite;
        this.enemyBaseSpeed = enemyBaseSpeed;
        this.enemyHealth = enemyHealth;
        this.historicalIntro = historicalIntro;
        this.historicalSummary = historicalSummary;
    }
    
    public static final LevelData[] LEVELS = {
        new LevelData(
            1,
            "First Indochina War (1946-1954)",
            "Anti-aircraft Defense at Dien Bien Phu",
            "level1_player",
            new String[]{"morane", "spitfire", "bearcat", "dakota"},
            "bg_level1",
            "bgm_level1",
            "gunfire",
            "bullet_level1",
            1.0,
            new int[]{1, 2, 2},
            "1954: Battle of Dien Bien Phu - Vietnamese forces use 37mm anti-aircraft guns to deny French air superiority and cut off crucial supply lines.",
            "Historical result: 62 French aircraft shot down or damaged. The disruption of air supply routes contributed significantly to the French defeat at Dien Bien Phu."
        ),
        new LevelData(
            2,
            "Early Vietnam War (1965-1968)",
            "SAM Sites vs American Aircraft",
            "level2_player",
            new String[]{"f105", "f4phantom", "b26"},
            "bg_level2",
            "bgm_level2",
            "sam_launch",
            "bullet_level2",
            1.3,
            new int[]{2, 3, 2},
            "1965: Operation Rolling Thunder - North Vietnamese air defenses face intense American bombing campaign. SAM sites prove effective against high-flying aircraft.",
            "Historical result: SAM batteries shot down hundreds of US aircraft, forcing them to change tactics and fly at lower altitudes."
        ),
        new LevelData(
            3,
            "Air Combat (1966-1972)",
            "MiG-21 vs American Fighters",
            "level3_player",
            new String[]{"f4phantom", "f105"},
            "bg_level3",
            "bgm_level3",
            "mig_shoot",
            "bullet_level3",
            1.5,
            new int[]{3, 2},
            "1966-1972: Air battles over North Vietnam - Vietnamese MiG-21 pilots engage in dogfights against American F-4 Phantoms and F-105 Thunderchiefs.",
            "Historical result: Vietnamese pilots claimed 17 aerial victories against American aircraft during Operation Linebacker I."
        ),
        new LevelData(
            4,
            "Operation Linebacker II (1972)",
            "SAM Defense vs B-52 Bombers",
            "level4_player",
            new String[]{"b52", "f4phantom"},
            "bg_level4",
            "bgm_level4",
            "sam_launch",
            "bullet_level4",
            1.5,
            // need to provide 2 health values for 2 enemy types
            new int[]{6,6},
            "December 1972: The Christmas Bombing - B-52 bombers face intense SAM defenses over Hanoi in the largest bombing campaign of the war.",
            "Historical result: 15 B-52s shot down during 11 days of bombing. The heavy losses contributed to bringing the US back to peace negotiations."
        )
    };
}
