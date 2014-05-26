package com.vengestudios.sortme.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.android.gms.games.multiplayer.Participant;
import com.vengestudios.sortme.helpers.logic.CustomSorts;
import com.vengestudios.sortme.helpers.logic.Randomizer;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;


/**
 * This is the core of the GameScreen, managing the most of the game logic
 * and the redirection of messages
 *
 * Responsible for:
 *
 *  - Calculating the positions of the participants
 *
 *  - Redirecting incoming messages from the GameMessageReceiver to the
 *    other GameElements
 *
 *  - Sending attacks and announcements to other participants
 */
public class ParticipantCoordinator implements GameElement{

	// UI and game logic constants
    private static final float  SCREEN_WIDTH_PERCENTAGE     = .695f;
    private static final float  SCREEN_Y_PERCENTAGE         = .148f-0.04f;
    private static final int    SWITCH_PLAYER_INTERVAL      = 3000;
    private static final int    BROADCAST_OWN_INFO_DELAY    = 1000;

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
	private RelativeLayout       relativeLayout;
    private Context              context;

    // UI Elements and fields
    private TableLayout          tableLayout;
    private TableRow             tableLayoutRow;

    private RelativeLayout       scoreEffectRelativeLayout;

    private int layoutWidth;

    // Game logic management objects and fields
    private HashMap<String, ParticipantDisplay> participantDisplays;
    private ParticipantDisplay   ownDisplay;
    @SuppressWarnings("unused")
	private String               ownId;

    private Handler              switchTargetHandler;
    private Runnable             switchTargetRunnable;

    // GameElement Dependencies
    private OwnPositionDisplay   ownPositionDisplay;
    private PowerupActivator     powerupActivator;
    private GameMessageSender    gameMessageSender;
    private NotificationDisplay  notificationDisplay;
    private GameResultsSubmitter gameResultsSubmitter;

    // Handlers and Runnables
    private Handler              broadcastOwnInfoHandler;
    private Runnable             broadcastOwnInfoRunnable;

    /**
     * Constructor
     *
     * Initializes and positions the UI Elements and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements into
     * @param context The context of the application (usually MainActivity)
     */
    public ParticipantCoordinator(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context        = context;

        tableLayout = new TableLayout(context);

        int screenWidth = ScreenDimensions.getWidth(context);
        int screenHeight = ScreenDimensions.getHeight(context);
        int layoutWidth = (int)(screenWidth*SCREEN_WIDTH_PERCENTAGE);
        int layoutHeight = screenHeight;
        float layoutLeftPadding = .5f*(1.0f-SCREEN_WIDTH_PERCENTAGE)*screenWidth;
        float layoutTopPadding = layoutHeight*SCREEN_Y_PERCENTAGE;

        tableLayout.setLayoutParams(new LinearLayout.LayoutParams(layoutWidth, TableLayout.LayoutParams.WRAP_CONTENT));
        tableLayoutRow = new TableRow(context);
        TableRow.LayoutParams tableRowParams
            = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRowParams.gravity = Gravity.CENTER_HORIZONTAL;
        tableLayoutRow.setLayoutParams(tableRowParams);
        tableLayout.addView(tableLayoutRow);

        relativeLayout.addView(tableLayout);
        tableLayout.setX(layoutLeftPadding);
        tableLayout.setY(layoutTopPadding);

        scoreEffectRelativeLayout = new RelativeLayout(context);
        relativeLayout.addView(scoreEffectRelativeLayout);
        scoreEffectRelativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenHeight));

        switchTargetHandler   = new Handler();
        switchTargetRunnable  = new SwitchTargetRunnable();

        broadcastOwnInfoHandler = new Handler();

        this.layoutWidth = layoutWidth;

        hide();
    }

    @Override
    public void hide(){
        stopSwitchingTarget();
        broadcastOwnInfoHandler.removeCallbacks(broadcastOwnInfoRunnable);
        tableLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideForGameEnd() {
        hide();
    }

    @Override
    public void setupAndAppearForGame() {
        tableLayout.setVisibility(View.VISIBLE);
        stopSwitchingTarget();
    }

    /**
     * A Runnable used to switch the targeted participant
     * in fixed intervals.
     *
     * The targeted participant will be marked by an indicator on-screen.
     *
     * Any offensive PowerUps activated will be sent as attacks to the targeted participant
     */
    private class SwitchTargetRunnable implements Runnable {
        @Override
        public void run() {
            ArrayList<ParticipantDisplay> opponentDisplays = new ArrayList<ParticipantDisplay>();
            for (ParticipantDisplay participantDisplay:participantDisplays.values()) {
                if (participantDisplay!=ownDisplay && participantDisplay.getConnected())
                    opponentDisplays.add(participantDisplay);
            }

            if (opponentDisplays.size()==0) return;

            ParticipantDisplay randomOpponentDisplay
                = opponentDisplays.get(Randomizer.randInt(0, opponentDisplays.size()-1));
            for (ParticipantDisplay participantDisplay:opponentDisplays) {
                if (participantDisplay==randomOpponentDisplay) {
                    participantDisplay.setTargeted(true);
                } else {
                    participantDisplay.setTargeted(false);
                }
            }
            if (opponentDisplays.size()>=2)
                switchTargetHandler.postDelayed(switchTargetRunnable, SWITCH_PLAYER_INTERVAL);
        }
    }

    /**
     * Start switching the targeted participant
     */
    public void startSwitchingTarget() {
        switchTargetHandler.removeCallbacks(switchTargetRunnable);
        switchTargetHandler.postDelayed(switchTargetRunnable, 0);
    }

    /**
     * Stop switching the targeted participant
     */
    public void stopSwitchingTarget() {
        switchTargetHandler.removeCallbacks(switchTargetRunnable);
        if (participantDisplays!=null)
            for (ParticipantDisplay participantDisplay:participantDisplays.values())
                participantDisplay.setTargeted(false);
    }

    /**
     * Register a list of participants IDs of those who have disconnected
     * from the current game room
     * @param participantIds
     */
    public void registerDisconnectedParticipants(List<String> participantIds) {
    	if (participantIds!=null){
    		ArrayList<String> disconnectedNames = new ArrayList<String>();
    		for (String participantId:participantIds) {
    			Log.e("Participant Id", participantId);
    			ParticipantDisplay participantDisplay = participantDisplays.get(participantId);
    			if (participantDisplay!=null) {
    				participantDisplay.setConnected(false);
    				participantDisplay.setTargeted(false);
    				disconnectedNames.add(participantDisplay.getParticipantName());
    			}
    		}
    		if (disconnectedNames.size()>0) {
    			assert notificationDisplay!=null;
    			notificationDisplay.announceDisconnected(disconnectedNames);
    		}
    		switchTargetRunnable.run();
    	}
    }

    /**
     * Registers the NotificationDisplay
     * @param notificationDisplay
     */
    public void registerNotificationDisplay(NotificationDisplay notificationDisplay) {
        this.notificationDisplay = notificationDisplay;
    }

    /**
     * Registers the PowerupActivator
     * @param powerupActivator
     */
    public void registerPowerupActivator(PowerupActivator powerupActivator) {
        this.powerupActivator = powerupActivator;
    }

    /**
     * Registers the GameMessageSender
     * @param gameMessageSender
     */
    public void registerGameMessageSender(GameMessageSender gameMessageSender) {
        this.gameMessageSender = gameMessageSender;
    }

    /**
     * Registers the OwnPositionDisplay
     * @param ownPositionDisplay
     */
    public void registerOwnPositionDisplay(OwnPositionDisplay ownPositionDisplay) {
        this.ownPositionDisplay = ownPositionDisplay;
    }

    /**
     * Registers the GameResultsSubmitter
     * @param gameResultsSubmitter
     */
    public void registerGameResultsSubmitter(GameResultsSubmitter gameResultsSubmitter) {
    	this.gameResultsSubmitter = gameResultsSubmitter;
    }

    /**
     * Register the required information needed to start a new game
     * @param participants   An ArrayList of Participants
     * @param ownId          A String representing one own's participant ID
     * @param hideIdentities Whether a messages announcing one's own Participant Name and image URL
     *                       should be broadcasted
     */
    public void registerGameInfo(ArrayList<Participant> participants, String ownId, boolean hideIdentities) {
    	this.ownId = ownId;

    	tableLayoutRow.removeAllViews();
    	tableLayout.setStretchAllColumns(true);

    	int participantNameWidth = layoutWidth/(participants.size());
    	participantDisplays = new HashMap<String, ParticipantDisplay>();

    	for (Participant participant:participants) {
    		String participantId       = participant.getParticipantId();
    		String participantName     = participant.getDisplayName();
    		String participantImageURL = participant.getIconImageUrl();
            if (participantImageURL!=null)
            	participantImageURL.replaceFirst("https://", "http://");

    		ParticipantDisplay newParticipantDisplay
    		= new ParticipantDisplay(scoreEffectRelativeLayout, context,
    				new ParticipantData(participantId, participantName),
    				participantNameWidth);
    		tableLayoutRow.addView(newParticipantDisplay);
    		participantDisplays.put(participantId, newParticipantDisplay);
    		newParticipantDisplay.setParticipantImageURL(participantImageURL);
    	}
    	ownDisplay = participantDisplays.get(ownId);

    	if (hideIdentities==false) {
    		String ownParticipantName     = ownDisplay.getParticipantName();
    		String ownParticipantImageURL = ownDisplay.getParticipantImageURL();
            broadcastOwnInfoRunnable =
                    new BroadcastOwnInfoRunnable(ownParticipantName, ownParticipantImageURL);
            broadcastOwnInfoHandler.postDelayed(broadcastOwnInfoRunnable, BROADCAST_OWN_INFO_DELAY);
    	}
    }

    /**
     * A runnable to broadcast one own's participant name and image URL
     * Used by BroadcastOwnInfoRunnable to delay the broadcasting for
     * some time
     */
    private class BroadcastOwnInfoRunnable implements Runnable {
        private String ownParticipantName;
        private String ownParticipantImageURIString;
        public BroadcastOwnInfoRunnable(String ownParticipantName, String ownParticipantImageURIString) {
            this.ownParticipantName           = ownParticipantName;
            this.ownParticipantImageURIString = ownParticipantImageURIString;
        }
        @Override
        public void run() {
            gameMessageSender.announceOwnInfo(ownParticipantName, ownParticipantImageURIString);
        }
    }

    /**
     * Registers a personal attack from another participant
     * @param fromParticipantId The participant ID of the attacker
     * @param powerupType       The PowerupType of the attack
     */
    public void receivePersonalAttack(String fromParticipantId, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        else {
            assert notificationDisplay != null;
            assert powerupActivator    != null;
            assert gameMessageSender   != null;
            assert ownDisplay          != null;
            boolean successful = false;
            if (powerupType==PowerupType.BUBBLETIZE)
                successful = powerupActivator.bubbletize();
            else if (powerupType==PowerupType.RANDOMIZE)
                successful = powerupActivator.randomize();
            else if (powerupType==PowerupType.UPSIZE)
                successful = powerupActivator.upsize();

            String initiatorParticipantName = getParticipantName(fromParticipantId);
            HashSet<String> excludedParticipantsIds = new HashSet<String>(1);
            excludedParticipantsIds.add(fromParticipantId);
            if (successful) {
                gameMessageSender.announceOtherToOthersAttack(initiatorParticipantName,
                		excludedParticipantsIds, powerupType);
                gameMessageSender.sendPersonalAttackSucceededReply(fromParticipantId, powerupType);
                notificationDisplay.announceOtherToSelfAttack(initiatorParticipantName, powerupType);
                ownDisplay.incrementTimesAttackedByOthers(powerupType);
            } else {
                gameMessageSender.announceOthersToOthersBlock(initiatorParticipantName,
                		excludedParticipantsIds, powerupType);
                gameMessageSender.sendPersonalAttackBlockedReply(fromParticipantId, powerupType);
                notificationDisplay.announceOtherToSelfBlock(initiatorParticipantName, powerupType);
                ownDisplay.incrementTimesBlockSuccessful(powerupType);
            }
        }
    }

    /**
     * Register that the user's attack on another participant is successful
     * @param fromParticipantId  The participant ID of the victim
     * @param powerupType        The PowerupType of the attack
     */
    public void receivePersonalAttackSucceededReply(String fromParticipantId, PowerupType powerupType) {
        assert notificationDisplay != null;
        assert ownDisplay          != null;
    	String targetedParticipantName = getParticipantName(fromParticipantId);
    	notificationDisplay.announcePersonalAttackSucceeded(targetedParticipantName, powerupType);
    	ownDisplay.incrementTimesAttackSuccessful(powerupType);
    }

    /**
     * Register that the user's attack on another participant is blocked
     * @param fromParticipantId  The participant ID of the blocker
     * @param powerupType        The PowerupType of the attack
     */
    public void receivePersonalAttackBlockedReply(String fromParticipantId, PowerupType powerupType) {
        assert notificationDisplay != null;
        assert ownDisplay          != null;
    	String targetedParticipantName = getParticipantName(fromParticipantId);
    	notificationDisplay.announcePersonalAttackBlocked(targetedParticipantName, powerupType);
    	ownDisplay.incrementTimesBlockedByOthers(powerupType);
    }

    /**
     * Registers an announcement of another participant successfully attacking another participant
     * @param initiatorName  The name of the participant who started the attack
     * @param victimId       The participant ID of the victim
     * @param powerupType    The PowerupType of the attack
     */
    public void announceOtherToOtherAttack(String initiatorName, String victimId, PowerupType powerupType) {
        assert notificationDisplay != null;
        String victimName = getParticipantName(victimId);
        notificationDisplay.announceOtherToOtherAttack(initiatorName, victimName, powerupType);
    }

    /**
     * Registers an announcement of another participant blocking another participant's attack
     * @param initiatorName  The name of the participant who started the attack
     * @param blockerId      The participant ID of the victim
     * @param powerupType    The PowerupType of the attack
     */
    public void announceOtherToOtherBlock(String initiatorName, String blockerId, PowerupType powerupType) {
        assert notificationDisplay != null;
        String blockerName = getParticipantName(blockerId);
        notificationDisplay.announceOtherToOtherBlock(initiatorName, blockerName, powerupType);
    }

    /**
     * Sends an attack to the currently targeted participant
     * @param powerupType The PowerupType of the attack
     */
    public void sendPersonalAttack(PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        else {
            assert gameMessageSender != null;
            for (ParticipantDisplay participantDisplay:participantDisplays.values()) {
                if (participantDisplay.getTargeted()) {
                    gameMessageSender.sendPersonalAttack(participantDisplay.getParticipantId(), powerupType);
                    break;
                }
            }
        }
    }

    /**
     * Sets that the user has used a PowerUp
     */
    public void setOwnUsedPowerupToTrue() {
    	assert ownDisplay != null;
    	ownDisplay.setUsedPowerupToTrue();
    }

    /**
     * Registers the information of a participant
     * @param participantId             The ID of the participant
     * @param participantName           The display name of the participant
     * @param participantImageURIString The image URL of the participant
     */
    public void setParticipantInfo(String participantId, String participantName,
            String participantImageURIString) {
        ParticipantDisplay participantDisplay = getParticipantDisplay(participantId);
        participantDisplay.setParticipantName(participantName);
        participantDisplay.setParticipantImageURL(participantImageURIString);
    }

    /**
     * Gets the participant display of a participant
     * @param participantId The ID of the participant
     * @return              The corresponding ParticipantDisplay
     */
    private ParticipantDisplay getParticipantDisplay(String participantId) {
        assert participantDisplays != null;
        ParticipantDisplay participantDisplay = participantDisplays.get(participantId);
        assert participantDisplay != null;
        return participantDisplay;
    }

    /**
     * Gets the name of a participant
     * @param participantId The ID of the participant
     * @return              The corresponding name of the participant
     */
    private String getParticipantName(String participantId) {
        return getParticipantDisplay(participantId).getParticipantName();
    }

    /**
     * @return The display name of the user
     */
    public String getOwnName() {
        assert ownDisplay != null;
        return ownDisplay.getParticipantName();
    }

    /**
     * @return The number of lines the user has sorted
     */
    public int getOwnLinesSorted(){
        assert ownDisplay != null;
        return ownDisplay.getLinesSorted();
    }

    /**
     * Increment the user's score by the specified amount,
     * and increments the lines sorted by 1
     * @param score  The amount to increment the user's score by
     */
    public void incrementOwnScoreAndLinesSorted(int score) {
        assert ownDisplay != null;
        ownDisplay.incrementScoreAndLinesSorted(score);
        gameMessageSender.announceScoreAndLinesSorted(ownDisplay.getScore(), ownDisplay.getLinesSorted());
        updateParticipantPositions();
        updateOwnPositionDisplay();
    }

    /**
     * Set the score and the number of lines sorted by a participant
     * @param participantId The ID of the participant
     * @param score         The score of the participant
     * @param linesSorted   The number of lines sorted by the participant
     */
    public void setScoreAndLinesSorted(String participantId, int score, int linesSorted) {
        assert participantDisplays != null;
        ParticipantDisplay participantDisplay = participantDisplays.get(participantId);
        assert participantDisplay != null;
        participantDisplay.setScoreAndLinesSorted(score, linesSorted);
        updateParticipantPositions();
        updateOwnPositionDisplay();
    }

    /**
     * Increments the score of a participant by the specified amount,
     * and increments the lines sorted by him/her by 1
     * @param participantId The ID of the participant
     * @param score         The amount to increment the participant's score by
     */
    public void incrementScoreAndLinesSorted(String participantId, int score) {
        assert participantDisplays != null;
        ParticipantDisplay participantDisplay = participantDisplays.get(participantId);
        assert participantDisplay != null;
        participantDisplay.incrementScoreAndLinesSorted(score);
        updateParticipantPositions();
        updateOwnPositionDisplay();
    }

    /**
     * Updates the position of the user displayed in the OwnPositionDisplay
     */
    private void updateOwnPositionDisplay(){
        assert ownPositionDisplay != null;
        ownPositionDisplay.setOwnPosition(ownDisplay.getPosition());
    }

    /**
     * Calculates and updates the positions of all the participants
     */
    private void updateParticipantPositions() {
        assert participantDisplays != null;

        ArrayList<ParticipantDisplay> sortedParticipantDisplays = new ArrayList<ParticipantDisplay>();
        for (ParticipantDisplay participantDisplay:participantDisplays.values())
            sortedParticipantDisplays.add(participantDisplay);
        CustomSorts.insertionSort(sortedParticipantDisplays, new Comparator<ParticipantDisplay>() {
            @Override
            public int compare(ParticipantDisplay arg0, ParticipantDisplay arg1) {
                return arg1.getScore()-arg0.getScore(); // Descending order sort
            }
        });
        int currentScore = sortedParticipantDisplays.get(0).getScore();
        int currentPosition = 0;
        for (int i=0; i<sortedParticipantDisplays.size(); ++i) {
            ParticipantDisplay participantDisplay = sortedParticipantDisplays.get(i);
            if (participantDisplay.getScore()<currentScore) {
                currentPosition = i;
                currentScore = participantDisplay.getScore();
            }
            participantDisplay.setPosition(currentPosition);
        }
    }

    /**
     * Submit a ParticipantData containing the user's performance in a game
     * to the GameResultsSubmitter
     */
    public void submitGameResults() {
    	assert gameResultsSubmitter != null;
    	gameResultsSubmitter.registerOwnParticipantData(
    			ownDisplay.getParticipantDataClone());
    }

    /**
     * @return A copy of the user's ParticipantData
     */
    public ParticipantData getCopyOfOwnParticipantData() {
    	assert ownDisplay != null;
    	return ownDisplay.getParticipantDataClone();
    }

    /**
     * @return An ArrayList of copies of the ParticipantDatas of all the
     *         participants sorted in descending order according to their positions
     */
    public ArrayList<ParticipantData> getCopyOfParticipantDatasWithDsecPositions() {
    	assert participantDisplays != null;

    	updateParticipantPositions();
    	ArrayList<ParticipantData> copyOfParticipantDatas = new ArrayList<ParticipantData>();
    	for (ParticipantDisplay participantDisplay:participantDisplays.values()) {
    		copyOfParticipantDatas.add(participantDisplay.getParticipantDataClone());
    	}

    	CustomSorts.insertionSort(copyOfParticipantDatas, new Comparator<ParticipantData>() {
    		@Override
    		public int compare(ParticipantData arg0, ParticipantData arg1) {
    			return arg1.getScore()-arg0.getScore(); // Descending order sort
    		}
    	});

    	return copyOfParticipantDatas;
    }

}
