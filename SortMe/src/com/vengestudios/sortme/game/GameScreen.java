package com.vengestudios.sortme.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.android.gms.games.multiplayer.Participant;
import com.vengestudios.sortme.MainActivity;
import com.vengestudios.sortme.MessageReceiver;
import com.vengestudios.sortme.MessageSender;
import com.vengestudios.sortme.Screen;

import android.widget.RelativeLayout;

/**
 * GameScreen is an implementation of Screen.
 *
 * Responsible for:
 *
 *  - Initializing the GameElements and setting up their interrelationships
 *    registering GameElements with the GameElements they depend on
 *
 *  - Passing outgoing game messages from GameMessageSender to MainActivity
 *
 *  - Passing incoming game messages from MainAcitvity to GameMEssageReceiver
 *
 *  - Passing high scores and achievement unlocks/increments between
 *    GameResultsSubmitter and MainActivity
 *
 *  - Showing/hiding the GameElements
 */
public class GameScreen implements Screen, MessageReceiver, MessageSender {

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
    private RelativeLayout         relativeLayout;
    private MainActivity           mainActivity;

    // On-screen GameElements
    private TileSorterControl      tileSorterControl;
    private ParticipantCoordinator participantCoordinator;
    private PowerupButtonManager   powerupButtonManager;
    private MPBar                  mpBar;
    private GameTimer              gameTimer;
    private OwnPositionDisplay     ownPositionDisplay;
    private PowerupActivator       powerupActivator;
    private NotificationDisplay    notificationDisplay;
    private ScoreBoard             scoreBoard;

    // Non-screen GameElement management objects and fields
    private GameMessageSender      gameMessageSender;
    private GameMessageReceiver    gameMessageReceiver;
    private GameResultsSubmitter   gameResultsSubmitter;

    private MessageSender          messageSender;

    private ArrayList<GameElement> gameElements;
    private boolean                normalHideDisabled;
    private boolean                shown;

    /**
     * Constructor
     *
     * Initializes the various GameElements and sets up their interrelationships
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements
     * @param mainActivity The MainActivity
     *
     * @PostConditions
     *
     * It is crucial that all the following post conditions are satisfied,
     * for they determine that each GameElement can perform their essential
     * functionality that depend on other GameElements
     *
     * All the GameElements have been set up
     *
     * relativeLayout         != null;   mainActivity           != null;
     * tileSorterControl      != null;   participantCoordinator != null;
     * powerupButtonManager   != null;   mpBar                  != null;
     * gameTimer              != null;   ownPositionDisplay     != null;
     * powerupActivator       != null;   notificationDisplay    != null;
     * scoreBoard             != null;   gameMessageSender      != null;
     * gameMessageReceiver    != null;   gameResultsSubmitter   != null;
     * messageSender          != null;   gameElements           != null;
     *
     * The GameElement management objects and fields have been set up
     *
     * gameElements != null
     * gameElements.contains(participantCoordinator) == true;
     * gameElements.contains(powerupActivator)       == true;
     * gameElements.contains(mpBar)                  == true;
     * gameElements.contains(powerupButtonManager)   == true;
     * gameElements.contains(ownPositionDisplay)     == true;
     * gameElements.contains(tileSorterControl)      == true;
     * gameElements.contains(notificationDisplay)    == true;
     * gameElements.contains(gameTimer)              == true;
     * gameElements.contains(scoreBoard)             == true;
     *
     * gameMessageSender    != null;
     * gameMessageReceiver  != null;
     * gameResultsSubmitter != null;
     *
     * normalHideDisabled     = false;
     * shown                  = false;
     *
     * The interrelationships between the GameElements and
     * non-screen management objects has been established
     *
     * tileSorterControl     .mpBar                  == mpBar;
     * tileSorterControl     .participantCoordinator == participantCoordinator;
     * mpBar                 .powerupButtonManager   == powerupButtonManager;
     * powerupButtonManager  .mpBar                  == mpBar;
     * powerupButtonManager  .participantCoordinator == participantCoordinator;
     * powerupButtonManager  .powerupActivator       == powerupActivator;
     * powerupActivator      .tileSorterControl      == tileSorterControl;
     * gameTimer             .tileSorterControl      == tileSorterControl;
     * gameTimer             .participantCoordinator == participantCoordinator;
     * participantCoordinator.ownPositionDisplay     == ownPositionDisplay;
     * participantCoordinator.powerupActivator       == powerupActivator;
     * participantCoordinator.notificationDisplay    == notificationDisplay;
     * scoreBoard            .gameScreen             == this;
     * gameTimer             .scoreBoard             == scoreBoard;
     * gameTimer             .mpBar                  == mpBar;
     * participantCoordinator.gameMessageSender      == gameMessageSender;
     * participantCoordinator.gameResultsSubmitter   == gameResultsSubmitter;
     * gameMessageReceiver   .participantCoordinator == participantCoordinator;
     * gameMessageSender     .gameScreen             == this;
     * gameResultsSubmitter  .gameScreen             == this;
     *
     */
    public GameScreen(RelativeLayout relativeLayout, MainActivity mainActivity) {
        this.relativeLayout = relativeLayout;
        this.mainActivity   = mainActivity;

        participantCoordinator = new ParticipantCoordinator(relativeLayout, mainActivity);
        powerupActivator       = new PowerupActivator      (relativeLayout, mainActivity);
        mpBar                  = new MPBar                 (relativeLayout, mainActivity);
        powerupButtonManager   = new PowerupButtonManager  (relativeLayout, mainActivity);
        ownPositionDisplay     = new OwnPositionDisplay    (relativeLayout, mainActivity);
        tileSorterControl      = new TileSorterControl     (relativeLayout, mainActivity);
        notificationDisplay    = new NotificationDisplay   (relativeLayout, mainActivity);
        gameTimer              = new GameTimer             (relativeLayout, mainActivity);
        scoreBoard             = new ScoreBoard            (relativeLayout, mainActivity);

        gameElements = new ArrayList<GameElement>();
        gameElements.add(participantCoordinator);
        gameElements.add(powerupActivator);
        gameElements.add(mpBar);
        gameElements.add(powerupButtonManager);
        gameElements.add(ownPositionDisplay);
        gameElements.add(tileSorterControl);
        gameElements.add(notificationDisplay);
        gameElements.add(gameTimer);
        gameElements.add(scoreBoard);

        //registering of game components with each other
        tileSorterControl     .registerMPBar                 (mpBar);
        tileSorterControl     .registerParticipantCoordinator(participantCoordinator);
        mpBar                 .registerPowerupButtonManager  (powerupButtonManager);
        powerupButtonManager  .registerMPBar                 (mpBar);
        powerupButtonManager  .registerParticipantCoordinator(participantCoordinator);
        powerupButtonManager  .registerPowerupActivator      (powerupActivator);
        powerupActivator      .registerTileSorterControl     (tileSorterControl);
        gameTimer             .registerTileSorterControl     (tileSorterControl);
        gameTimer             .registerParticipantCoordinator(participantCoordinator);
        participantCoordinator.registerOwnPositionDisplay    (ownPositionDisplay);
        participantCoordinator.registerPowerupActivator      (powerupActivator);
        participantCoordinator.registerNotificationDisplay   (notificationDisplay);
        scoreBoard            .registerGameScreen            (this);
        gameTimer             .registerScoreBoard            (scoreBoard);
        gameTimer             .registerMPBar                 (mpBar);

        // initializing the non-screen objects
        gameMessageSender    = new GameMessageSender   ();
        gameMessageReceiver  = new GameMessageReceiver ();
        gameResultsSubmitter = new GameResultsSubmitter(mainActivity);

        participantCoordinator.registerGameMessageSender     (gameMessageSender);
        participantCoordinator.registerGameResultsSubmitter  (gameResultsSubmitter);
        gameMessageReceiver   .registerParticipantCoordinator(participantCoordinator);
        gameMessageSender     .registerGameScreen            (this);
        gameResultsSubmitter  .registerGameScreen            (this);

    }

    /**
     * Registers the MessageSender, which all outgoing game messages will be passed on to
     * Without the SecurityMessageLayer, this would be the MainAcitivty, which implements
     * all the required functions to broadcast messages to the Google Game Services API
     * @param messageSender The MessageSender
     */
    public void registerMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Called when the MainActivity switches to the GameScreen
     * It will call setupAndAppearForGame, since the game screen is then
     * required to prepare all its GameElements for the game and appear on screen
     */
    @Override
    public void show() {

        // Sets the shown flag to true, will enables all game messages to
        // be passed on to the ParticipantCoordinator
        shown = true;

        setupAndAppearForGame();
    }

    /**
     * Registers the required information needed to start a new game
     * @param participants   An ArrayList containing the participants
     * @param ownId          A String representing one's own participant ID
     * @param hideIdentities Whether the ParticipantCoordinator should later broadcast as message
     *                       announcing one's own Participant Name and image URL
     */
    public void registerGameInfo(ArrayList<Participant> participants, String ownId, boolean hideIdentities) {
        participantCoordinator.registerGameInfo(participants, ownId, hideIdentities);
    }

    /**
     * Register the participants that have been disconnected form the current game room
     * @param participantIds  A List of the Participant IDs of those who have disconnected
     */
    public void registerDisconnectedParticipants(List<String> participantIds) {
        participantCoordinator.registerDisconnectedParticipants(participantIds);
    }

    /**
     * Calls all the GameElements in the screen to setup and appear for the game
     */
    public void setupAndAppearForGame() {
        enableNormalHide();

        // Enables the disconnected error, so that an error dialog will be shown
        // to the user if the game room closes or disconnects
        mainActivity.enableDisconnectedError();

        for (GameElement gameElement:gameElements)
            gameElement.setupAndAppearForGame();
    }

    /**
     * Hides the game elements for the ending of a game. It differs from the normal
     * hide in that it uses special animations and calls the required methods
     * to end a game.
     */
    public void hideForGameEnd() {

        // Must be called before leave room,
        // Leaving a room will trigger hide(), which will disable the fading blur effect
        disableNormalHide();

        mainActivity.leaveRoom();

        // Sets the shown flag to false, which blocks all game messages
        // from being passed on to the ParticipantCoordinator
        shown = false;

        for (GameElement gameElement:gameElements)
            gameElement.hideForGameEnd();
    }

    /**
     * Disables the MainActivity from showing a disconnected error for the
     * end of a game, since the game room will disconnect after all the
     * players except one's own self has left
     */
    public void disableDisconnectedErrorForGameEnd(){
        mainActivity.disableDisconnectedError();
    }

    /**
     * Disables the normal hide() method from hiding all the GameElements.
     * This is needed to be called at the end of a game, since the MainAcitivty
     * would call the normal hide() function when switching to the MainScreen.
     */
    public void disableNormalHide(){
        normalHideDisabled = true;
    }

    /**
     * Enables the normal hide() method.
     * The normal hide method needs to be enabled for time when the game is still
     * active, so that the MainActivity can instantly hide the GameScreen
     * when the room disconnnects or when the player leaves
     */
    public void enableNormalHide(){
        normalHideDisabled = false;
    }

    @Override
    public void hide(){
        if (normalHideDisabled) return;
        shown = false;
        for (GameElement gameElement:gameElements)
            gameElement.hide();
    }

    /**
     * Calls upon the MainActivity to increment the counter for an Achievement
     * @param achievementId     The ID of the achievement
     * @param timesToIncrement  The amount to increment
     */
    public void incrementAchievement(String achievementId, int timesToIncrement) {
        mainActivity.incrementAchievement(achievementId, timesToIncrement);
    }

    /**
     * Calls upon the MainActivity to unlock an non-incremental achievement
     * @param achievementId  The ID of the achievement
     */
    public void unlockAchievement(String achievementId) {
        mainActivity.unlockAchievement(achievementId);
    }

    /**
     * Calls upon the MainActivity to submit one's own score to the leader boards
     * @param leaderboardId  The ID of the leader board
     * @param score          The score to be submitted
     */
    public void submitScoreToLeaderboard(String leaderboardId, int score) {
        mainActivity.submitScoreToLeaderboard(leaderboardId, score);
    }

    /**
     * Registers an outgoing message with the MainAcitivty to be send to the
     * Google Game Services API
     */
    @Override
    public void registerMessage(String fromParticipantId, byte[] message) {
        // Blocks any message from being registered with the game receiver if
        // the shown flag is false.
        if (shown==false) {

            // Forces the MainActivity to leave the room. In rare cases,
            // a user can be in a game room on the Google Game Services Server,
            // when in actuality, he is in the MainScreen, and will continue to
            // messages from other participants. This will call
            // MainActivity to send a leave room request to the server again.
            mainActivity.leaveRoom();

            return;
        }
        gameMessageReceiver.registerMessage(fromParticipantId, message);
    }

    @Override
    public void broadcastReliableMessageToId(byte[] message, String participantId) {
        messageSender.broadcastMessageToId(message, participantId, true);
    }

    @Override
    public void broadcastUnreliableMessageToId(byte[] message, String participantId) {
        messageSender.broadcastMessageToId(message, participantId, false);
    }

    @Override
    public void broadcastMessageToId(byte[] message, String participantId, boolean reliable) {
        messageSender.broadcastMessageToId(message, participantId, reliable);
    }

    @Override
    public void broadcastReliableMessageToAll(byte[] message,
            HashSet<String> excludedParticipantsIds) {
        messageSender.broadcastMessageToAll(message, excludedParticipantsIds, true);
    }

    @Override
    public void broadcastUnreliableMessageToAll(byte[] message,
            HashSet<String> excludedParticipantsIds) {
        messageSender.broadcastMessageToAll(message, excludedParticipantsIds, false);
    }

    @Override
    public void broadcastMessageToAll(byte[] message,
            HashSet<String> excludedParticipantsIds, boolean reliable) {
        messageSender.broadcastMessageToAll(message, excludedParticipantsIds, reliable);
    }

}
