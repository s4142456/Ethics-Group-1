import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static AssetManager instance;
    private Map<String, Image> images;
    private Map<String, Clip> wavSounds;
    
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
        // Load player images
        loadImage("level1_player", "assets/image/level1_player.png");
        loadImage("level2_player", "assets/image/level2_player.png");
        loadImage("level3_player", "assets/image/level3_player.png");
        loadImage("level4_player", "assets/image/level4_player.png");
        
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
        loadImage("bg_level1", "assets/image/bg_level1.png");
        loadImage("bg_level2", "assets/image/bg_level2.jpg");
        loadImage("bg_level3", "assets/image/bg_level3.jpeg");
        loadImage("bg_level4", "assets/image/bg_level4.png");
        
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
    
    private void loadImage(String key, String path) {
        try {
            Image img = ImageIO.read(new File(path));
            images.put(key, img);
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
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
    
    // MP3 and background music loading removed to avoid JavaFX dependency.
    
    public Image getImage(String key) {
        return images.get(key);
    }
    
    public void playSound(String key) {
        try {
            // Try WAV sounds first
            Clip clip = wavSounds.get(key);
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.setFramePosition(0);
                clip.start();
                return;
            }
            
            // Only WAV sounds supported in this build.
        } catch (Exception e) {
            System.err.println("Warning: Could not play sound: " + key);
        }
    }
    
    public void playMusic(String key, boolean loop) {
        // Background music disabled in this build. Call playSound for SFX only.
    }

    public void stopAllMusic() {
        // No-op for now
    }

    public void cleanup() {
        // Stop and close WAV clips
        for (Clip clip : wavSounds.values()) {
            try { clip.stop(); clip.close(); } catch (Exception ignored) {}
        }
    }
}
