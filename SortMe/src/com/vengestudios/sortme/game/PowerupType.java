package com.vengestudios.sortme.game;

public enum PowerupType {

    RANDOMIZE  ("Randomize",  "Randomized",  "a", "#E74C3C", "#B53C2F"),
    BUBBLETIZE ("Bubbletize", "Bubbletized", "a", "#3E98DB", "#3787C2"),
    UPSIZE     ("Upsize",     "Upsized",     "a", "#F8CC40", "#C4A233"),
    SHIELD     ("Shield",     "Shielded",    "a", "#62CC72", "#4A9956");

    public static final int TOTAL_TYPES = 4;


    public String name;
    public String colorHexString;
    public String darkerColorHexString;
    public String prepositionString;
    public String pastTenseVerb;
    PowerupType(String name, String pastTenseVerb, String prepositionString,
    		String colorHexString, String darkerColorHexString) {
        this.name                 = name;
        this.pastTenseVerb        = pastTenseVerb;
        this.prepositionString    = prepositionString;
        this.colorHexString       = colorHexString;
        this.darkerColorHexString = darkerColorHexString;
    }
    public static PowerupType ordinalToPowerupType(int i) {
        for (PowerupType powerupType:PowerupType.values()) {
            if (powerupType.ordinal()==i)
                return powerupType;
        }
        return null;
    }
    public static PowerupType ordinalToPowerupType(String string) {
        return ordinalToPowerupType(Integer.parseInt(string));
    }
    public boolean isOffensive(){
        if (this==SHIELD) return false;
        return true;
    }
    public boolean isDefensive(){
        return !isOffensive();
    }
}
