package com.cfks.goosedroid.GooseDesktop;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;


import java.util.Random;

/**
 * Sound effects system for the cat.
 * Handles different types of meows, purrs, and special effects.
 */
public class GooseSoundEffects {

    private static final String TAG = "GooseSoundEffects";

    public enum SoundType {
        MEOW_NORMAL,
        MEOW_HAPPY,
        MEOW_ANGRY,
        MEOW_SAD,
        MEOW_EXCITED,
        MEOW_SLEEPY,
        MEOW_QUESTIONING,
        MEOW_TRIUMPHANT,
        PURR,
        HISS,
        SCRATCH,
        PAW_STEP,
        YAWN,
        SNEEZE,
        CHIRP,
        TRILL,
        SCREECH,
        ACHIEVEMENT,
        LEVEL_UP,
        SECRET_FOUND
    }

    public enum Melody {
        HAPPY_TUNE,      
        SAD_TUNE,        
        VICTORY_TUNE,    
        MORNING_TUNE,    
        NIGHT_TUNE,      
        MISCHIEF_TUNE,   
        LOVE_TUNE        
    }

    private static Context context;
    private static Random random = new Random();
    private static boolean isInitialized = false;

    // Pitch variations for different emotions (for meows)
    private static final float PITCH_HAPPY = 1.3f;
    private static final float PITCH_SAD = 0.75f;
    private static final float PITCH_ANGRY = 0.85f;
    private static final float PITCH_EXCITED = 1.5f;
    private static final float PITCH_SLEEPY = 0.6f;
    private static final float PITCH_NORMAL = 1.0f;

    // Melody notes for cat vocalizations
    private static final float[] MELODY_HAPPY = {1.0f, 1.25f, 1.5f};
    private static final float[] MELODY_SAD = {1.0f, 0.89f, 0.75f};
    private static final float[] MELODY_VICTORY = {1.0f, 1.33f, 1.5f, 2.0f};
    private static final float[] MELODY_MISCHIEF = {1.12f, 1.19f, 1.12f};

    // Current melody sequence
    private static float[] currentMelody = null;
    private static int melodyIndex = 0;
    private static long lastNoteTime = 0;
    private static final long NOTE_DELAY = 300;

    public static void init(Context appContext) {
        context = appContext.getApplicationContext();
        isInitialized = true;
    }

    /**
     * Play a specific sound type.
     */
    public static void play(SoundType type) {
        if (Sound.isSilenced()) return;

        switch (type) {
            case MEOW_NORMAL:
                playMeowWithPitch(PITCH_NORMAL);
                break;
            case MEOW_HAPPY:
                playMeowWithPitch(PITCH_HAPPY);
                break;
            case MEOW_ANGRY:
                playMeowWithPitch(PITCH_ANGRY);
                break;
            case MEOW_SAD:
                playMeowWithPitch(PITCH_SAD);
                break;
            case MEOW_EXCITED:
                playMeowWithPitch(PITCH_EXCITED);
                break;
            case MEOW_SLEEPY:
                playMeowWithPitch(PITCH_SLEEPY);
                break;
            case MEOW_QUESTIONING:
                playMeowSequence(new float[]{1.0f, 1.3f});
                break;
            case MEOW_TRIUMPHANT:
                playMeowSequence(MELODY_VICTORY);
                break;
            case PURR:
                playPurr();
                break;
            case HISS:
                playHiss();
                break;
            case SCRATCH:
                // Scratch sound effect
                break;
            case ACHIEVEMENT:
                playMeowSequence(MELODY_HAPPY);
                break;
            case SECRET_FOUND:
                playMeowSequence(new float[]{0.8f, 1.0f, 1.2f, 1.5f, 2.0f});
                break;
            default:
                Sound.HONCC(); // Fallback - use default honk sound
                break;
        }
    }

    /**
     * Play meow with specific pitch.
     */
    private static void playMeowWithPitch(float pitch) {
        // For now use basic honk - in future implement pitch shifting
        Sound.HONCC();
    }

    /**
     * Play a sequence of meows (melody).
     */
    private static void playMeowSequence(float[] melody) {
        currentMelody = melody;
        melodyIndex = 0;
        lastNoteTime = 0;
    }

    /**
     * Update melody in progress.
     */
    public static void update() {
        if (currentMelody == null || melodyIndex >= currentMelody.length) {
            currentMelody = null;
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastNoteTime >= NOTE_DELAY) {
            playMeowWithPitch(currentMelody[melodyIndex]);
            melodyIndex++;
            lastNoteTime = now;
        }
    }

    /**
     * Play a predefined melody.
     */
    public static void playMelody(Melody melody) {
        if (Sound.isSilenced()) return;

        switch (melody) {
            case HAPPY_TUNE:
                playMeowSequence(MELODY_HAPPY);
                break;
            case SAD_TUNE:
                playMeowSequence(MELODY_SAD);
                break;
            case VICTORY_TUNE:
                playMeowSequence(MELODY_VICTORY);
                break;
            case MISCHIEF_TUNE:
                playMeowSequence(MELODY_MISCHIEF);
                break;
            case MORNING_TUNE:
                playMeowSequence(new float[]{1.0f, 1.12f, 1.25f});
                break;
            case NIGHT_TUNE:
                playMeowSequence(new float[]{1.25f, 1.12f, 1.0f});
                break;
            case LOVE_TUNE:
                playMeowSequence(new float[]{1.0f, 1.25f, 1.5f, 2.0f});
                break;
        }
    }

    /**
     * Meow based on emotional state.
     */
    public static void meowForMood(float happiness) {
        if (happiness > 80) {
            play(SoundType.MEOW_EXCITED);
        } else if (happiness > 60) {
            play(SoundType.MEOW_HAPPY);
        } else if (happiness > 40) {
            play(SoundType.MEOW_NORMAL);
        } else if (happiness > 20) {
            play(SoundType.MEOW_SAD);
        } else {
            play(SoundType.MEOW_ANGRY);
        }
    }

    /**
     * Meow reaction to event.
     */
    public static void meowForEvent(GooseAI.RandomEvent event) {
        if (Sound.isSilenced()) return;

        switch (event) {
            case DANCE:
            case ZOOMIES:
                play(SoundType.MEOW_EXCITED);
                break;
            case YAWN:
                play(SoundType.MEOW_SLEEPY);
                break;
            case SHAKE:
                play(SoundType.MEOW_ANGRY);
                break;
            case RANDOM_HONK:
                play(SoundType.MEOW_NORMAL);
                break;
            case STRETCH:
                playPurr();
                break;
            case SINGING:
                playMelody(Melody.HAPPY_TUNE);
                break;
            case PLAY_DEAD:
                play(SoundType.MEOW_SAD);
                break;
            case SCRATCH_POST:
                play(SoundType.SCRATCH);
                break;
            case KNEAD:
                playPurr();
                break;
            default:
                play(SoundType.MEOW_NORMAL);
                break;
        }
    }

    /**
     * Random varied meow.
     */
    public static void randomMeow() {
        if (Sound.isSilenced()) return;

        float roll = random.nextFloat();
        if (roll < 0.3f) {
            play(SoundType.MEOW_NORMAL);
        } else if (roll < 0.5f) {
            play(SoundType.MEOW_HAPPY);
        } else if (roll < 0.65f) {
            play(SoundType.MEOW_QUESTIONING);
        } else if (roll < 0.8f) {
            play(SoundType.MEOW_EXCITED);
        } else if (roll < 0.9f) {
            playPurr();
        } else {
            playMelody(random.nextBoolean() ? Melody.HAPPY_TUNE : Melody.MISCHIEF_TUNE);
        }
    }

    /**
     * Sequence of rapid meows (for zoomies, etc).
     */
    public static void meowBurst(int count) {
        if (Sound.isSilenced()) return;

        float[] burst = new float[count];
        for (int i = 0; i < count; i++) {
            burst[i] = 1.0f + random.nextFloat() * 0.4f;
        }
        playMeowSequence(burst);
    }

    /**
     * Play purring sound.
     */
    public static void playPurr() {
        if (Sound.isSilenced()) return;
        // Soft continuous purring sound
        Sound.PlayPat(); // Use pat sound as placeholder for purr
    }

    /**
     * Play hissing sound.
     */
    public static void playHiss() {
        if (Sound.isSilenced()) return;
        // Angry hissing - sharp defensive sound
        Sound.HONCC(); // Use honk as placeholder
    }

    /**
     * Check if melody is playing.
     */
    public static boolean isMelodyPlaying() {
        return currentMelody != null && melodyIndex < currentMelody.length;
    }

    /**
     * Stop current melody.
     */
    public static void stopMelody() {
        currentMelody = null;
        melodyIndex = 0;
    }
}
