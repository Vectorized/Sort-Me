package com.vengestudios.sortme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.vengestudios.sortme.game.GameScreen;
import com.vengestudios.sortme.security.SecurityMessageLayer;
import com.vengestudios.sortme.security.SecurityProtocolChooser;
import com.vengestudios.sortme.security.SecurityProtocolType;

/**
 * This is the main and starting activity of the Android App.
 *
 * It subclasses from BaseGameActivity and implements the required interfaces
 * to allow communication between the client and the Google Game Services Server.
 *
 * Responsible for:
 *
 *  - Sending and Receiving real time messages between the client and
 *    Google Game Services Online API
 *
 *  - Setting up the SecurityMessageLayer for secure message sending
 *    and injecting it as a layer between the Google Game Services API
 *    and the Game Logic (handled by GameScreen)
 *
 *  - Setting up, hiding and showing the different screens
 *
 *  - Registering high scores to the leader board
 *
 *  - Registering the progress of achievements or unlocking achievements
 *
 *  - Signing the user in and out of Google game Services
 *
 *  - Launching the view invitation UI, invite friends UI, leader boards UI,
 *    achievements UI
 *
 *  - Providing garbage collection scheduling
 *
 *  - Managing the Android Application
 */
public class MainActivity extends BaseGameActivity
        implements RealTimeMessageReceivedListener,
        RoomStatusUpdateListener,
        RoomUpdateListener,
        OnInvitationReceivedListener,
        MessageSender {

    // Used to schedule garbage collection before/after a memory intensive game
    // or other memory intensive tasks
    private static final Handler  GARBAGE_COLLECT_HANDLER = new Handler();
    private static final Runnable GARBAGE_COLLECT_RUNNABLE =
            new Runnable() { @Override public void run() { System.gc(); } };

    // An enum to determine the return results for intents to launch
    // the Google Play Services Activities
    private enum ActivityResult{
        SELECT_PLAYERS,
        VIEW_INVITATION_INBOX,
        VIEW_WAITING_ROOM,
        VIEW_ACHIEVEMENTS,
        VIEW_LEADERBOARDS
    }

    // Some booleans to quickly enable/disable features that might be removed upon final release
    private static final boolean SHOULD_HIDE_PARTICIPANTS_IDENTITIES = false;
    private static final boolean SECURITY_ENABLED                    = true;
    private static final boolean ABLE_TO_SET_SECURITY_PROTOCOL       = true;
    private static final boolean SET_NO_SECURITY_PROTOCOL            = true;

    // Fields to hold the information of the room received from Google Game Services
    private String                 roomId         = null;
    private ArrayList<Participant> participants   = null;
    private String                 ownId          = null;

    // An enum to denote the different types of error that can be displayed
    // in a popup dialog
    private enum RoomErrorType{
        ROOM_DISCONNECTED_ERROR  ("Sorry, the other player has disconnected from the game.\n\n" +
                                 "There is not enough players to continue the game."),
        ROOM_CREATION_ERROR      ("Sorry, there is a problem creating the room.\n\n" +
                                 "Check if you are connected to the Internet."),
        ROOM_CONNECTION_ERROR    ("Sorry, there is a problem connecting to the current room."),
        ROOM_JOIN_ERROR          ("Sorry, there is a problem joining the room");

        public String errorMessage;
        RoomErrorType(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    };

    // Used to toggle the room disconnection detection on/off
    // Needs to be turned off upon ending a game, so that closure of the game room
    // will not trigger and error message
    private boolean       showDisconnectedError;

    // The various Screens that can be displayed
    private MainScreen    mainScreen;
    private WaitScreen    waitScreen;
    private GameScreen    gameScreen;
    private SignInScreen  signInScreen;
    private Screen        transitScreen;

    // Incoming messages from Google Game Services will be passed on to it
    private MessageReceiver messageReceiver;

    // Objects responsible to the encryption protocols
    private SecurityMessageLayer    securityMessageLayer;

    // A enum variable to denote the current screen being shown
    private ScreenType   currentScreenType;

    // A HashMap to hold references to the different screens
    private HashMap<ScreenType, Screen> screenMap;

    /**
     * Gets called upon the activity being created for the 1st time
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showDisconnectedError = true;

        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root_layout);

        screenMap = new HashMap<ScreenType, Screen>();

        mainScreen    = new MainScreen(rootLayout, this);
        waitScreen    = new WaitScreen(rootLayout, this);
        signInScreen  = new SignInScreen(rootLayout, this);
        gameScreen    = new GameScreen(rootLayout, this);
        transitScreen = mainScreen.getSnapshotScreen();

        screenMap.put(ScreenType.MAIN_SCREEN,    mainScreen);
        screenMap.put(ScreenType.WAIT_SCREEN,    waitScreen);
        screenMap.put(ScreenType.GAME_SCREEN,    gameScreen);
        screenMap.put(ScreenType.SIGN_IN_SCREEN, signInScreen);
        screenMap.put(ScreenType.TRANSIT_SCREEN, transitScreen);

        if (SECURITY_ENABLED) {
            try {
                securityMessageLayer = new SecurityMessageLayer(this, gameScreen);
                gameScreen.registerMessageSender(securityMessageLayer);
                messageReceiver = securityMessageLayer;

                if (ABLE_TO_SET_SECURITY_PROTOCOL)
                    new SecurityProtocolChooser(
                            this,
                            securityMessageLayer,
                            mainScreen);

                if (SET_NO_SECURITY_PROTOCOL)
                    securityMessageLayer.setSecurityProtocolType(
                            SecurityProtocolType.NONE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            messageReceiver = gameScreen;
            gameScreen.registerMessageSender(this);
        }
    }

    /**
     * Called by the base class (BaseGameActivity) when sign-in has failed. For
     * example, because the user hasn't authenticated yet. We react to this by
     * showing the sign-in button.
     */
    @Override
    public void onSignInFailed() {
        switchToScreen(ScreenType.SIGN_IN_SCREEN);
    }

    /**
     * Called by the base class (BaseGameActivity) when sign-in succeeded. We
     * react by going to our main screen.
     */
    @Override
    public void onSignInSucceeded() {
        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(getApiClient(), this);

        // if we received an invite via notification, accept it; otherwise, go to main screen
        if (getInvitationId() != null) {
            acceptInviteToRoom(getInvitationId());
            return;
        }
        switchToMainScreen();
    }

    // ---------------------------------------------------------------------------------
    // Functions to handle the various buttons being clicked from the MainScreen - START
    // ---------------------------------------------------------------------------------

    public void signInButtonClicked(){
        beginUserInitiatedSignIn();
    }

    public void signOutButtonClicked() {
        signOut();
        switchToScreen(ScreenType.SIGN_IN_SCREEN);
    }

    public void inviteFriendsButtonClicked(){
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(getApiClient(), 1, 3);
        switchToScreen(ScreenType.WAIT_SCREEN);
        startActivityForResult(intent, ActivityResult.SELECT_PLAYERS.ordinal());
    }

    public void seeInvitationsButtonClicked(){
        Intent intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
        switchToScreen(ScreenType.WAIT_SCREEN);
        startActivityForResult(intent, ActivityResult.VIEW_INVITATION_INBOX.ordinal());
    }

    public void acceptPopupInvitationButtonClicked(){
        Intent intent = Games.Invitations.getInvitationInboxIntent(getApiClient());
        switchToScreen(ScreenType.WAIT_SCREEN);
        startActivityForResult(intent, ActivityResult.VIEW_INVITATION_INBOX.ordinal());
    }

    public void quickGameButtonClicked(){
        startQuickGame();
    }

    public void achievementsButtonClicked(){
        switchToScreen(ScreenType.WAIT_SCREEN);
        startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
                ActivityResult.VIEW_ACHIEVEMENTS.ordinal());
    }

    public void leaderboardsButtonClicked(){
        Intent intent = Games.Leaderboards.getLeaderboardIntent(getApiClient(), getString(R.string.leaderboards_id));
        switchToScreen(ScreenType.WAIT_SCREEN);
        startActivityForResult(intent, ActivityResult.VIEW_LEADERBOARDS.ordinal());
    }

    public void instructionsButtonClicked(){
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }

    // -------------------------------------------------------------------------------
    // Functions to handle the various buttons being clicked from the MainScreen - END
    // -------------------------------------------------------------------------------

    /**
     * Called when the user chooses to get auto-matched to a game room
     */
    public void startQuickGame() {
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 3;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        switchToScreen(ScreenType.WAIT_SCREEN);
        keepScreenOn();
        Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
    }

    /**
     * Used to increment an incremental
     * Achievement stored on Google Game Services for the user
     * @param achievementId The id of the Achievement
     * @param timesToIncrement How many times to increment the Achievement by
     */
    public void incrementAchievement(String achievementId, int timesToIncrement) {
        if (timesToIncrement>0)
            Games.Achievements.increment(getApiClient(), achievementId, timesToIncrement);
    }

    /**
     * Used to unlock an Achievement stored on Google Game Services for the user
     * @param achievementId The id of the Achievement
     */
    public void unlockAchievement(String achievementId) {
        Games.Achievements.unlock(getApiClient(), achievementId);
    }

    /**
     * Used to submit the score from a game to the leaderboard on Google Game Services
     * @param leaderboardId The id of the leaderboard
     * @param score The score
     */
    public void submitScoreToLeaderboard(String leaderboardId, int score) {
        Games.Leaderboards.submitScore(getApiClient(), leaderboardId, score);
    }

    /**
     * Launches the Google Game Services Activities such as the HighScores
     * Achievements etc, upon receiving of an Activity result
     * Also used to start the Game or leave an existing game room.
     */
    @Override
    public void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        if (requestCode==ActivityResult.SELECT_PLAYERS.ordinal()) {
            handleSelectPlayersResult(responseCode, intent);
        } else if (requestCode==ActivityResult.VIEW_INVITATION_INBOX.ordinal()) {
            handleInvitationInboxResult(responseCode, intent);
        } else if (requestCode==ActivityResult.VIEW_ACHIEVEMENTS.ordinal()) {
            handleViewAchievementsResult(responseCode, intent);
        } else if (requestCode==ActivityResult.VIEW_LEADERBOARDS.ordinal()) {
            handleViewLeaderboardsResult(requestCode, intent);
        } else if (requestCode==ActivityResult.VIEW_WAITING_ROOM.ordinal()) {
            if (responseCode == Activity.RESULT_OK) {
                startGame();
            } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                leaveRoom();
            } else if (responseCode == Activity.RESULT_CANCELED) {
                leaveRoom();
            }
        }

    }


    /**
     * Handle the result of the "Select players UI" we launched when the user clicked the
     * "Invite friends" button. We react by creating a room with those players.
     * @param response The response code for the Intent
     * @param data The intent for the selection of players UI
     */
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            switchToMainScreen();
            return;
        }

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        }

        // create the room
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }
        switchToScreen(ScreenType.WAIT_SCREEN);
        keepScreenOn();
        Games.RealTimeMultiplayer.create(getApiClient(), rtmConfigBuilder.build());
    }

    /**
     * Handle the result of the invitation inbox UI, where the player can pick an invitation
     * to accept. We react by accepting the selected invitation, if any.
     *
     * @param response  The response code from invoking the intent
     * @param data      The intent for the invitation inbox UI
     */
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            switchToMainScreen();
            return;
        }

        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    /**
     * Handle the result of the view Achievements UI.
     * Shows the Achievement UI or goes back to the main screen.
     *
     * @param response  The response code from invoking the intent
     * @param data      The intent for the Achievements UI
     */
    private void handleViewAchievementsResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            switchToMainScreen();
            return;
        }
    }

    /**
     * Handle the result of the view Leaderboards UI.
     * Shows the Leaderboards UI or goes back to the main screen.
     *
     * @param response  The response code from invoking the intent
     * @param data      The intent for the Leaderboards UI
     */
    private void handleViewLeaderboardsResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            switchToMainScreen();
            return;
        }
    }

    /**
     * Accept the given invitation.
     * @param invId  The invitation Id
     */
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(ScreenType.WAIT_SCREEN);
        keepScreenOn();
        Games.RealTimeMultiplayer.join(getApiClient(), roomConfigBuilder.build());
    }

    /**
     * Activity is going to the background. We have to leave the current room.
     */
    @Override
    public void onStop() {
        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        switchToScreen(ScreenType.WAIT_SCREEN);

        postGarbageCollect(0);
        super.onStop();
    }

    /**
     * Activity just got to the foreground. We switch to the wait screen because we will now
     * go through the sign-in flow (remember that, yes, every time the Activity comes back to the
     * foreground we go through the sign-in flow -- but if the user is already authenticated,
     * this flow simply succeeds and is imperceptible).
     */
    @Override
    public void onStart() {
        switchToScreen(ScreenType.WAIT_SCREEN);
        super.onStart();
    }

    /**
     * Used to handle the back button being pressed
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && currentScreenType == ScreenType.GAME_SCREEN) {
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    /**
     * Leaves the current game room the user is in.
     */
    public void leaveRoom() {
        stopKeepingScreenOn();
        if (roomId != null) {
            Games.RealTimeMultiplayer.leave(getApiClient(), this, roomId);
            roomId = null;
            switchToScreen(ScreenType.TRANSIT_SCREEN);
            postGarbageCollect(2000);
        } else {
            switchToMainScreen();
            postGarbageCollect(2000);
        }
        if (SECURITY_ENABLED) {
            securityMessageLayer.prepareForNextSession();
        }
    }

    /**
     * Show the waiting room UI to track the progress of other players as they enter the
     * room and get connected.
     * @param room  The waiting room
     */
    public void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(getApiClient(), room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, ActivityResult.VIEW_WAITING_ROOM.ordinal());
    }

    /**
     * Part of the Google Game Services OnInvitationReceived Interface
     * Used for detecting invitations.
     * However, it is currently not 100% effectively detected,
     * so we shall leave it for Google to roll up a newer update
     */
    @Override
    public void onInvitationReceived(Invitation invitation) {}

    /**
     * Part of the Google Game Services OnInvitationReceived Interface
     * Used for detecting invitations.
     * However, it is currently not 100% effectively detected,
     * so we shall leave it for Google to roll up a newer update
     */
    @Override
    public void onInvitationRemoved(String invitationId) {}

    /**
     * Called when we are connected to the current room. We're not ready to play yet! (maybe not everybody
     * is connected yet).
     */
    @Override
    public void onConnectedToRoom(Room room) {
        // get room ID, participants and my ID:
        roomId       = room.getRoomId();
        participants = room.getParticipants();
        ownId        = room.getParticipantId(Games.Players.getCurrentPlayerId(getApiClient()));
    }

    /**
     * Called when we've successfully left the room (this happens a result of voluntarily leaving
     * via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
     */
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        // we have left the room; return to main screen.
        switchToMainScreen();
    }

    /**
     * Called when we get disconnected from the room. We return to the main screen.
     */
    @Override
    public void onDisconnectedFromRoom(Room room) {
        roomId = null;
        showGameError(RoomErrorType.ROOM_DISCONNECTED_ERROR);
    }

    /**
     * Show error message about game being cancelled and return to main screen.
     * @param errorType The error to show
     */
    public void showGameError(RoomErrorType errorType) {
        if (errorType == RoomErrorType.ROOM_DISCONNECTED_ERROR) {
            if (showDisconnectedError) {
                showAlert(errorType.errorMessage);
                switchToMainScreen();
            }
        } else {
            showAlert(errorType.errorMessage);
            switchToMainScreen();
        }
    }

    /**
     * Enables showing of the disconnected from room error
     */
    public void enableDisconnectedError(){
        showDisconnectedError = true;
    }

    /**
     * Disables showing of the disconnected from room error
     */
    public void disableDisconnectedError(){
        showDisconnectedError = false;
    }

    /**
     * Called when room has been created
     */
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            showGameError(RoomErrorType.ROOM_CREATION_ERROR);
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    /**
     * Called when room is fully connected.
     */
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            showGameError(RoomErrorType.ROOM_CONNECTION_ERROR);
            return;
        }
        updateParticipants(room);
    }

    /**
     * Called when the user joins a game room via an invitation
     */
    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            showGameError(RoomErrorType.ROOM_JOIN_ERROR);
            return;
        }
        showWaitingRoom(room);
    }

    /**
     * Called when another player has declined the invitation to the current room
     */
    @Override
    public void onPeerDeclined(Room room, List<String> arg1) {
        updateParticipants(room);
    }

    /**
     * Called when a player has been invited a room
     */
    @Override
    public void onPeerInvitedToRoom(Room room, List<String> arg1) {
        updateParticipants(room);
    }

    @Override
    public void onP2PDisconnected(String participant) {}

    @Override
    public void onP2PConnected(String participant) {}

    /**
     * Called when a invited player has joined the room
     */
    @Override
    public void onPeerJoined(Room room, List<String> arg1) {
        updateParticipants(room);
    }

    /**
     * Called when a player has disconnected in the middle of a game
     */
    @Override
    public void onPeerLeft(Room room, List<String> peersWhoLeft) {
        updateParticipants(room);
        gameScreen.registerDisconnectedParticipants(peersWhoLeft);
    }

    /**
     * Called when the player is trying to get auto-matched the current room
     */
    @Override
    public void onRoomAutoMatching(Room room) {
        updateParticipants(room);
    }

    /**
     * Called when the player is connecting the current room
     */
    @Override
    public void onRoomConnecting(Room room) {
        updateParticipants(room);
    }

    /**
     * Called when players have connected to the current room
     */
    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        updateParticipants(room);
    }

    /**
     * Called when players have disconnected from a currently running game room
     * The list of disconnected players will have their participant IDs passed
     * into the game screen to show that they have disconnected
     */
    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        updateParticipants(room);
        gameScreen.registerDisconnectedParticipants(peers);
    }

    /**
     * Update the list of participants in the current room
     * @param room The current room
     */
    void updateParticipants(Room room) {
        if (room != null) {
            participants = room.getParticipants();
        }
    }

    /**
     * Schedule a garbage collection
     * @param delay The number of milliseconds to the garbage collection
     */
    public void postGarbageCollect(long delay) {
        GARBAGE_COLLECT_HANDLER.removeCallbacks(GARBAGE_COLLECT_RUNNABLE);
        if (delay==0)
            System.gc();
        else
            GARBAGE_COLLECT_HANDLER.postDelayed(GARBAGE_COLLECT_RUNNABLE, delay);
    }

    /**
     * Start the game-play phase of the game.
     */
    public void startGame() {
        postGarbageCollect(0);

        gameScreen.registerGameInfo(participants, ownId,
                SHOULD_HIDE_PARTICIPANTS_IDENTITIES);

        ArrayList<String> participantIds = new ArrayList<String>();
        for (Participant p:participants)
            participantIds.add(p.getParticipantId());

        if (SECURITY_ENABLED) {
            securityMessageLayer.registerIdsForNewSession(participantIds);
        }

        switchToScreen(ScreenType.GAME_SCREEN);
    }

    /**
     * Invoked upon receiving a real time message from a participant in the current room
     * from the Google Game Services API.
     */
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] message = rtm.getMessageData();
        String fromParticipantId = rtm.getSenderParticipantId();
        messageReceiver.registerMessage(fromParticipantId, message);
    }

    /**
     * Broadcasts a reliable message to a participant in the current room
     * via the Google Game Services API.
     * Reliable messages have mechanisms implemented to guarantee a
     * successful delivery
     */
    @Override
    public void broadcastReliableMessageToId(byte[] message, String participantId) {
        broadcastMessageToId(message, participantId, true);
    }

    /**
     * Broadcasts an unreliable message to a participant in the current room
     * via the Google Game Services API.
     * Unreliable messages have lower data overheads, but they do not have
     * mechanisms implemented to guarantee the success of their delivery.
     */
    @Override
    public void broadcastUnreliableMessageToId(byte[] message, String participantId) {
        broadcastMessageToId(message, participantId, false);
    }

    /**
     * Broadcasts a message to a certain participant in the current room
     * via the Google Game Services API.
     */
    @Override
    public void broadcastMessageToId(byte[] message, String participantId, boolean reliable) {
        for (Participant p : participants) {
            if (p.getParticipantId().equals(ownId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (p.getParticipantId().equals(participantId)) {
                if (reliable)
                    Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, message,
                            roomId, p.getParticipantId());
                else
                    Games.RealTimeMultiplayer.sendUnreliableMessage(getApiClient(), message, roomId,
                            p.getParticipantId());
            }
        }
    }

    /**
     * Broadcasts a reliable message to all participants in the current room
     * via the Google Game Services API.
     * Reliable messages have mechanisms implemented to guarantee a
     * successful delivery
     */
    @Override
    public void broadcastReliableMessageToAll(byte[] message,
            HashSet<String> excludedParticipantsIds) {
        broadcastMessageToAll(message, excludedParticipantsIds, true);
    }

    /**
     * Broadcasts an unreliable message to all participants in the current room
     * via the Google Game Services API.
     * Unreliable messages have lower data overheads, but they do not have
     * mechanisms implemented to guarantee the success of their delivery.
     */
    @Override
    public void broadcastUnreliableMessageToAll(byte[] message,
            HashSet<String> excludedParticipantsIds) {
        broadcastMessageToAll(message, excludedParticipantsIds, false);
    }

    /**
     * Broadcasts a message to all the participants in the current room
     * via the Google Game Services API.
     */
    @Override
    public void broadcastMessageToAll(byte[] message,
            HashSet<String> excludedParticipantsIds, boolean reliable) {
        for (Participant p : participants) {
            String participantId = p.getParticipantId();
            if (excludedParticipantsIds!=null)
                if (excludedParticipantsIds.contains(participantId))
                    continue;
            if (participantId.equals(ownId))
                continue;
            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;
            if (reliable)
                Games.RealTimeMultiplayer.sendReliableMessage(getApiClient(), null, message,
                        roomId, participantId);
            else
                // it's an interim score notification, so we can use unreliable
                Games.RealTimeMultiplayer.sendUnreliableMessage(getApiClient(), message, roomId,
                        participantId);
        }
    }

    /**
     * Switches to the screen that needs to be displayed.
     * All the other screens except the one being displayed will be hidden.
     * @param screenToSwitchTo
     */
    public void switchToScreen(ScreenType screenToSwitchTo) {
        for (Map.Entry<ScreenType, Screen> entry : screenMap.entrySet()) {
            ScreenType screenType = entry.getKey();
            Screen     screen     = entry.getValue();
            if (screenType==screenToSwitchTo)
                screen.show();
            else
                screen.hide();
        }
        currentScreenType = screenToSwitchTo;
    }

    /**
     * Switches to the main screen or the signed in screen depending on
     * whether the user has been signed in
     */
    public void switchToMainScreen() {
        switchToScreen(isSignedIn() ? ScreenType.MAIN_SCREEN : ScreenType.SIGN_IN_SCREEN);
    }


    /**
     * Sets the flag to keep this screen on. It's recommended to do that during
     * the handshake when setting up a game, because if the screen turns off, the
     * game will be cancelled.
     */
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Clears the flag that keeps the screen on.
     */
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
