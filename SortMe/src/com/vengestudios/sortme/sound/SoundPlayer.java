package com.vengestudios.sortme.sound;

import java.util.HashMap;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

/**
 * A singleton used to cache sounds for efficient playing in the game
 */
public class SoundPlayer {
    private static final int MAX_STREAMS = 10;

    private SoundPool                soundPool;
    private HashMap<Object, Integer> soundMap;

    private static final SoundPlayer instance = new SoundPlayer();

    private SoundPlayer() {
        soundPool  = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        soundMap   = new HashMap<Object, Integer>();
    }

    /**
     * Loads a sound into the SoundPlayer
     * @param key     The Object that acts as a key to play the sound later on
     * @param context The context of the application
     * @param resId   The resource ID of the application
     */
    public static void loadSound(Object key, Context context, int resId) {
        if (instance.soundMap.containsKey(key)) return;
        instance.soundMap.put(key, instance.soundPool.load(context, resId, 1));
    }

    /**
     * Plays a sound that has been loaded before into the SoundPlayer
     * @param key         The Object used as a Key mapping to the sound
     * @param context     The context of the application
     * @param timesToLoop The number of times to loop
     * @param playRate    The rate at which the sound is played
     */
    public static void play(Object key, Context context, int timesToLoop, float playRate) {
        Integer soundID = instance.soundMap.get(key);
        if (soundID!=null) {
            Log.e("sss", soundID.toString());
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            float maxVolume   = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float curVolume   = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float leftVolume  = curVolume/maxVolume;
            float rightVolume = leftVolume;
            int priority      = 1;
            instance.soundPool.play(soundID, leftVolume, rightVolume, priority, timesToLoop, playRate);
        }
    }
}
