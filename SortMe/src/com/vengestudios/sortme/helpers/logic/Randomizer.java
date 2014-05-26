package com.vengestudios.sortme.helpers.logic;

import java.util.ArrayList;
import java.util.Random;

public class Randomizer {

    static Random random = new Random();

    /**
     * Returns a pseudo-random integer in the range of [min, max)
     * @param min
     * @param max
     * @return An pseudo-random int in the range of [min, max)
     */
    public static int randInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    /**
     * Shuffle the array using a Fisher Yates shuffle
     * @param ar
     * @PostConditions
     * ar contains all elements from before
     */
    public static void shuffleArray(int[] ar) {
        Random rnd = new Random();
        for (int i=ar.length-1; i>0; i--) {
            int index = rnd.nextInt(i+1);
            int a = ar[index]; ar[index] = ar[i]; ar[i] = a;
        }
    }

    /**
     * Shuffle the ArrayList using a Fisher Yates shuffle
     * @param ar
     * @PostConditions
     * ar contains all elements from before
     */
    public static <E> void shuffleArrayList(ArrayList<E> ar) {
        Random rnd = new Random();
        for (int i=ar.size()-1; i>0; i--) {
            int index = rnd.nextInt(i+1);
            E a = ar.get(index);
            ar.set(index, ar.get(i));
            ar.set(i, a);
        }
    }
}
