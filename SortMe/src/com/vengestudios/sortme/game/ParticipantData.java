package com.vengestudios.sortme.game;

/**
 * Stores the in game information and
 * performance of a participant
 */
public class ParticipantData implements Cloneable{

    private String participantId;
    private String participantName;
    private String participantImageURL;

    private int position;
    private int score;
    private int linesSorted;

    private boolean usedPowerup;

    private int timesBlockedByOthers  [] = new int[PowerupType.TOTAL_TYPES];
    private int timesAttackedByOthers [] = new int[PowerupType.TOTAL_TYPES];

    private int timesBlockSuccessful  [] = new int[PowerupType.TOTAL_TYPES];
    private int timesAttackSuccessful [] = new int[PowerupType.TOTAL_TYPES];

    private int totalTimesBlockedByOthers;
    private int totalTimesAttackedByOthers;
    private int totalTimesBlockSuccessful;
    private int totalTimesAttackSuccessful;

    /**
     * Constructor
     *
     * Creates an instance of ParticipantData
     *
     * @param participantId   The ID of the participant
     * @param participantName The name of the participant
     */
    public ParticipantData(String participantId, String participantName) {
        this.participantId             = participantId;
        this.participantName           = participantName;
        this.participantImageURL = "";
    }

    /**
     * Returns a new ParticipantData with the same information
     * as the ParticipantData passed in
     * @param other  The ParticipantData to copy
     */
    private ParticipantData(ParticipantData other) {
        this(other.participantId, other.participantName);
        this.participantImageURL = other.participantImageURL;
        this.position             = other.position;
        this.score                = other.score;
        this.linesSorted          = other.linesSorted;
        this.usedPowerup          = other.usedPowerup;
        for (int i=0; i<PowerupType.TOTAL_TYPES; ++i) {
            this.timesBlockedByOthers [i] = other.timesBlockedByOthers [i];
            this.timesAttackedByOthers[i] = other.timesAttackedByOthers[i];
            this.timesBlockSuccessful [i] = other.timesBlockSuccessful [i];
            this.timesAttackSuccessful[i] = other.timesAttackSuccessful[i];
        }
        this.totalTimesBlockedByOthers  = other.totalTimesBlockedByOthers;
        this.totalTimesAttackedByOthers = other.totalTimesAttackedByOthers;
        this.totalTimesBlockSuccessful  = other.totalTimesBlockSuccessful;
        this.totalTimesAttackSuccessful = other.totalTimesAttackSuccessful;
    }

    @Override
    public ParticipantData clone() {
        return new ParticipantData(this);
    }

    /**
     * Increments the number of times blocked by others
     * @param powerupType  The PowerupType of the attack being blocked
     */
    public void incrementTimesBlockedByOthers(PowerupType powerupType) {
        timesBlockedByOthers[powerupType.ordinal()]++;
        totalTimesBlockedByOthers++;
    }

    /**
     * Gets the number of times blocked by others
     * @param powerupType  The PowerupType of the attack being blocked
     * @return             The number of times blocked by others for the attack
     */
    public int getTimesBlockedByOthers(PowerupType powerupType) {
        return timesBlockedByOthers[powerupType.ordinal()];
    }

    /**
     * @return The total times being blocked by others
     */
    public int getTotalTimesBlockedByOthers() {
        return totalTimesBlockedByOthers;
    }

    /**
     * Increments the times attacked by others
     * @param powerupType  The PowerupType of the attack
     */
    public void incrementTimesAttackedByOthers(PowerupType powerupType) {
        timesAttackedByOthers[powerupType.ordinal()]++;
        totalTimesAttackedByOthers++;
    }

    /**
     * Gets the number of times attack by others successfully
     * @param powerupType  The PowerupType of the attack
     * @return             The number of times attacked by others for the attack
     */
    public int getTimesAttackedByOthers(PowerupType powerupType) {
        return timesAttackedByOthers[powerupType.ordinal()];
    }

    /**
     * @return The total times being attacked by others successfully
     */
    public int getTotalTimesAttackedByOthers() {
        return totalTimesAttackedByOthers;
    }

    /**
     * Increments the number of times blocking an attack
     * @param powerupType  The PowerupType of the attack
     */
    public void incrementTimesBlockSuccessful(PowerupType powerupType) {
        timesBlockSuccessful[powerupType.ordinal()]++;
        totalTimesBlockSuccessful++;
    }

    /**
     * Get the number of times successfully blocked an attack
     * @param powerupType  The PowerupType of the attack
     * @return             The number of times blocked the attack
     *                     for the PowerupType
     */
    public int getTimesBlockSuccessful(PowerupType powerupType) {
        return timesBlockSuccessful[powerupType.ordinal()];
    }

    /**
     * @return The total amount of times blocked other's attacks
     */
    public int getTotalTimesBlockSuccessful() {
        return totalTimesBlockSuccessful;
    }

    /**
     * Increments the number of times successfully attacking others
     * @param powerupType  The PowerupType of the attack
     */
    public void incrementTimesAttackSuccessful(PowerupType powerupType) {
        timesAttackSuccessful[powerupType.ordinal()]++;
        totalTimesAttackSuccessful++;
    }

    /**
     * Gets the number of times successfully attacking others
     * @param powerupType  The PowerupType of the attack
     * @return             The number of times successfully attacking others
     *                     for the PowerupType
     */
    public int getTimesAttackSuccessful(PowerupType powerupType) {
        return timesAttackSuccessful[powerupType.ordinal()];
    }

    /**
     * @return The total times successfully attacking others
     */
    public int getTotalTimesAttackSuccessful() {
        return totalTimesAttackSuccessful;
    }

    public String getParticipantId() {
        return participantId;
    }
    public String getParticipantName() {
        return participantName;
    }
    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }
    public String getParticipantImageURL() {
        return participantImageURL;
    }
    public void setParticipantImageURL(String participantImageURL) {
        this.participantImageURL = participantImageURL;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public int getLinesSorted() {
        return linesSorted;
    }
    public void setLinesSorted(int linesSorted) {
        this.linesSorted = linesSorted;
    }
    public void incrementScoreAndLinesSorted(int score) {
        this.score += score;
        ++linesSorted;
    }
    public void setScoreAndLinesSorted(int score, int linesSorted) {
        this.score = score;
        this.linesSorted = linesSorted;
    }
    public void setUsedPowerupToTrue() {
        usedPowerup = true;
    }
    public boolean getUsedPowerup() {
        return usedPowerup;
    }
}
