package com.vengestudios.sortme.game;

import com.vengestudios.sortme.R;

import android.content.Context;

/**
 * Submits the results of one's own game to the leaderboards,
 * as well as attempt to unlock achievements
 */
public class GameResultsSubmitter {

	private Context    context;
    private GameScreen gameScreen;

    /**
     * Constructor
     * @param context The context of the application, for retrieval of 
     *                the achievement IDs from the XML resources
     */
    public GameResultsSubmitter(Context context) {
    	this.context = context;
    }

    /**
     * Register the GameScreen
     * @param gameScreen
     */
    public void registerGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    /**
     * Register the ParticipantData representing one's own performance
     * after a game. It will then submit the ParticipantData to be analyzed to
     * see which achievements it is eligible for, and also submit the score
     * to the leader boards
     * 
     * @param participantData
     */
    public void registerOwnParticipantData(ParticipantData participantData) {
    	assert gameScreen != null;
    	submitForArmorOfGod         (participantData);
    	submitForEngorgio           (participantData);
    	submitForEverydayImShufflin (participantData);
    	submitForHydropump          (participantData);
    	submitForJackOfAllTrades    (participantData);
    	submitForMainLeaderboard    (participantData);
    	submitForNobelPeacePrize    (participantData);
    	submitForONSquared          (participantData);
    }

    /**
     * Submits the score to the leader board
     * @param participantData
     */
    private void submitForMainLeaderboard(ParticipantData participantData) {
    	gameScreen.submitScoreToLeaderboard(context.getString(R.string.leaderboards_id),
    			participantData.getScore());
    }

    /**
     * Analyzes the ParticipantData and attempt to submit it for ONSquared
     * @param participantData
     */
    private void submitForONSquared(ParticipantData participantData) {
    	if (participantData.getPosition()==0) {
    		gameScreen.incrementAchievement(context.getString(R.string.achievement_on2), 1);
    	}
    }

    /**
     * Analyzes the ParticipantData and attempt to submit it for JackOfAllTrades
     * @param participantData
     */
    private void submitForJackOfAllTrades(ParticipantData participantData) {
    	if (participantData.getTotalTimesBlockSuccessful()==0) return;
    	if (participantData.getTimesAttackSuccessful(PowerupType.BUBBLETIZE)==0) return;
    	if (participantData.getTimesAttackSuccessful(PowerupType.RANDOMIZE) ==0) return;
    	if (participantData.getTimesAttackSuccessful(PowerupType.UPSIZE)    ==0) return;
    	gameScreen.incrementAchievement(context.getString(R.string.achievement_jack_of_all_trades), 1);
    }

    /**
     * Analyzes the ParticipantData and attempt to submit it for
     * @param participantData
     */
    private void submitForArmorOfGod(ParticipantData participantData) {
    	gameScreen.incrementAchievement(context.getString(R.string.achievement_armor_of_god),
    			participantData.getTotalTimesBlockSuccessful());
    }

    /**
     * Analyzes the ParticipantData and attempt to submit it for HydroPump
     * @param participantData
     */
    private void submitForHydropump(ParticipantData participantData) {
    	gameScreen.incrementAchievement(context.getString(R.string.achievement_hydropump),
    			participantData.getTimesAttackSuccessful(PowerupType.BUBBLETIZE));
    }

    /**
     * Analyzes the ParticipantData and attempt to submit it for EverydayimShuffin
     * @param participantData
     */
    private void submitForEverydayImShufflin(ParticipantData participantData) {
    	gameScreen.incrementAchievement(context.getString(R.string.achievement_everyday_im_shufflin),
    			participantData.getTimesAttackSuccessful(PowerupType.RANDOMIZE));
    }

    /**
     * Analyzes the ParticipantData and attempt to submit it for Engorgio
     * @param participantData
     */
    private void submitForEngorgio(ParticipantData participantData) {
    	gameScreen.incrementAchievement(context.getString(R.string.achievement_engorgio),
    			participantData.getTimesAttackSuccessful(PowerupType.UPSIZE));
    }

    /**
     * Analyzes the ParticipantData and attempt to submit it for NobelPeacePrize
     * @param participantData
     */
    private void submitForNobelPeacePrize(ParticipantData participantData) {
    	if (participantData.getUsedPowerup()==false)
    		gameScreen.incrementAchievement(context.getString(R.string.achievement_nobel_peace_price), 1);
    }
}
