import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class AssetManager {
    /**
     * Stop all currently playing sound effects (WAVs), except for music.
     */
    public void stopAllSounds() {
        for (Map.Entry<String, Clip> entry : wavSounds.entrySet()) {
            Clip clip = entry.getValue();
            if (clip != null && clip.isRunning()) {
                // Don't stop currentMusic (background music), only SFX
                if (clip != currentMusic) {
                    try { clip.stop(); } catch (Exception ignored) {}
                }
            }
        }
    }
    private static AssetManager instance;
    private Map<String, Image> images;
    private Map<String, Clip> wavSounds;
    private Clip currentMusic = null;

    private AssetManager() {
        images = new HashMap<>();
        wavSounds = new HashMap<>();
        loadAssets();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    private void loadAssets() {
        // Load background music (WAV only)
        loadWavSound("bgm_menu", "assets/bgm/bgm_menu.wav");
        loadWavSound("bgm_level1", "assets/bgm/bgm_level1.wav");
        loadWavSound("bgm_level2", "assets/bgm/bgm_level2.wav");
        loadWavSound("bgm_level3", "assets/bgm/bgm_level3.wav");
        loadWavSound("bgm_level4", "assets/bgm/bgm_level4.wav");

        // Load player images
        loadImage("level1_player", "assets/image/level1_player.png");
        loadImage("level2_player", "assets/image/level2_player.png");
        loadImage("level3_player", "assets/image/level3_player.png");
        loadImage("level4_player", "assets/image/level4_player.png");

        // Load player bullet images per level
        loadImage("bullet_level1", "assets/image/bullet_level1.png");
        loadImage("bullet_level2", "assets/image/bullet_level2.png");
        loadImage("bullet_level3", "assets/image/bullet_level3.png");
        loadImage("bullet_level4", "assets/image/bullet_level4.png");

        // Load enemy bullet sprite (single image for all levels)
        loadImage("enemy_bullet", "assets/image/enemy_bullet.png");

        // Load enemy images
        loadImage("morane", "assets/image/morane.png");
        loadImage("dakota", "assets/image/dakota.png");
        loadImage("bearcat", "assets/image/bearcat.png");
        loadImage("f105", "assets/image/f105.png");
        loadImage("f4phantom", "assets/image/f4phantom.png");
        loadImage("skyhawk", "assets/image/skyhawk.png");
        loadImage("spitfire", "assets/image/spitfire.png");
        loadImage("b26", "assets/image/b26.png");
        loadImage("b52", "assets/image/b52.png");

        // Load background images
        loadImage("bg_level1", "assets/image/bg_level1.jpg");
        loadImage("bg_level2", "assets/image/bg_level2.jpg");
        loadImage("bg_level3", "assets/image/bg_level3.jpg");
        loadImage("bg_level4", "assets/image/bg_level4.jpg");

        // Load WAV sound effects
        loadWavSound("gunfire", "assets/sfx/gunfire.wav");
        loadWavSound("mig_shoot", "assets/sfx/mig_shoot.wav");
        loadWavSound("explosion_small", "assets/sfx/explosion_small.wav");
        loadWavSound("explosion_big", "assets/sfx/explosion_big.wav");
        loadWavSound("plane_fall", "assets/sfx/plane_fall.wav");
        loadWavSound("hit", "assets/sfx/hit.wav");
        loadWavSound("warning", "assets/sfx/warning.wav");
        // Note: Background music (MP3) currently disabled to avoid JavaFX dependency.
        // Only load WAV SFX here.
    }

    // --- Background music support ---
    public void playMusic(String key, boolean loop) {
        Clip clip = wavSounds.get(key);
        if (clip == null) return;
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
        }
        currentMusic = clip;
        clip.setFramePosition(0);
        clip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
        clip.start();
    }

    public void stopMusic() {
        if (currentMusic != null && currentMusic.isRunning()) {
            currentMusic.stop();
        }
    }

    /**
     * Play two WAV sounds in sequence. Only works for loaded WAVs.
     */
    public void playSoundInSequence(String key1, String key2) {
        Clip clip1 = wavSounds.get(key1);
        Clip clip2 = wavSounds.get(key2);
        if (clip1 == null) {
            playSound(key2);
            return;
        }
        if (clip2 == null) {
            playSound(key1);
            return;
        }
        if (clip1.isRunning()) {
            clip1.stop();
        }
        clip1.setFramePosition(0);
        clip1.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip1.removeLineListener(this);
                    // Play second sound
                    if (clip2.isRunning()) {
                        clip2.stop();
                    }
                    clip2.setFramePosition(0);
                    clip2.start();
                }
            }
        });
        clip1.start();
    }

    private void loadImage(String key, String path) {
        try {
            Image img = ImageIO.read(new File(path));
            images.put(key, img);
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
        }
    }

    private void loadWavSound(String key, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                    AudioFormat format = ais.getFormat();
                    // Convert to PCM 16-bit if needed
                    if (format.getSampleSizeInBits() > 16) {
                        AudioFormat targetFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate(),
                            16,
                            format.getChannels(),
                            format.getChannels() * 2,
                            format.getSampleRate(),
                            false);
                        ais = AudioSystem.getAudioInputStream(targetFormat, ais);
                    }
                    clip.open(ais);
                    wavSounds.put(key, clip);
                } catch (Exception e) {
                    System.err.println("Warning: Could not load WAV sound: " + path);
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Error accessing WAV sound file: " + path);
        }
    }

    public Image getImage(String key) {
        return images.get(key);
    }

    public void playSound(String key) {
        playSound(key, 1.0f);
    }

    public void playSound(String key, float volume) {
        try {
            // Try WAV sounds first
            Clip clip = wavSounds.get(key);
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                // Set volume if supported
                FloatControl gainControl = null;
                try {
                    gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                } catch (Exception ignored) {}
                if (gainControl != null) {
                    float min = gainControl.getMinimum();
                    float max = gainControl.getMaximum();
                    float gain = min + (max - min) * volume;
                    gainControl.setValue(gain);
                }
                clip.start();
                return;
            }
            // Only WAV sounds supported in this build.
        } catch (Exception e) {
            System.err.println("Warning: Could not play sound: " + key);
        }
    }

    public void cleanup() {
        // Stop and close WAV clips
        for (Clip clip : wavSounds.values()) {
            try { clip.stop(); clip.close(); } catch (Exception ignored) {}
        }
    }
}
