package com.guardiansofangkor.renderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches per-word artwork — e.g. the word "moon" maps to
 * {@code /images/words/moon.png} — independent of which enemy type is
 * carrying that word.
 *
 * <p>Follows the same conventions as {@link SpriteCache}: missing art is not
 * an error. A word with no PNG yet returns null so the renderer can draw its
 * existing placeholder box, and dropping the real file into
 * {@code src/main/resources/images/words/} later needs no code change.
 *
 * <p>Words are matched case-insensitively and trimmed to their content the
 * same way monster sprites are, via {@link SpriteCache#trim}.
 */
public class WordArtCache {

    private static final String WORD_IMAGE_DIR = "/images/words/";

    private final Map<String, BufferedImage> images = new HashMap<>();
    private final Map<String, Boolean> loadAttempted = new HashMap<>();

    /**
     * The trimmed art for {@code word}, or null when no matching PNG has been
     * added yet. Loaded once per word; a failed load is not retried every frame.
     */
    public BufferedImage forWord(String word) {
        if (word == null || word.isBlank()) {
            return null;
        }
        String key = word.trim().toLowerCase();

        if (Boolean.TRUE.equals(loadAttempted.get(key))) {
            return images.get(key);
        }
        loadAttempted.put(key, Boolean.TRUE);

        String path = WORD_IMAGE_DIR + key + ".png";
        BufferedImage raw = read(path);
        if (raw == null) {
            System.out.println("[WordArtCache] No art for word \"" + word
                    + "\" (" + path + ") — drawing a placeholder.");
            return null;
        }
        BufferedImage trimmed = SpriteCache.trim(raw);
        images.put(key, trimmed);
        return trimmed;
    }

    /** True when this word has real art, as opposed to a placeholder. */
    public boolean hasArt(String word) {
        return forWord(word) != null;
    }

    /**
     * On-screen width for this word's art at the given height, preserving its
     * own aspect ratio. Falls back to a square when there is no art.
     */
    public int widthFor(String word, int height) {
        BufferedImage image = forWord(word);
        if (image == null || image.getHeight() == 0) {
            return height;
        }
        return Math.max(1,
                (int) Math.round(height * (image.getWidth() / (double) image.getHeight())));
    }

    private BufferedImage read(String path) {
        try (InputStream in = WordArtCache.class.getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException | RuntimeException e) {
            System.err.println("[WordArtCache] Could not read " + path
                    + " (" + e.getMessage() + ").");
            return null;
        }
    }
}