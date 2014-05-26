package com.vengestudios.sortme.game;

import java.util.List;

import com.vengestudios.sortme.helpers.ui.Effects;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Used to display notifications about the statuses of other participants
 */
public class NotificationDisplay implements GameElement {

    // UI and animation constants
    private static final float  FONT_SIZE                   = 13.f;
    private static final float  FADE_IN_TEXT_VIEW_MOVE_Y    = -15.f;
    private static final int    FADE_IN_TEXT_VIEW_DURATION  = 300;
    private static final float  SCREEN_Y_PERCENTAGE         = 0.02f;
    private static final int    MAX_NAME_CHARACTER_COUNT    = 16;

    private static final String DEFAULT_TEXT = "The game has started...";

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
    private RelativeLayout     relativeLayout;
    @SuppressWarnings("unused")
    private Context            context;

    // UI Elements
    private TextView           textView;

    // Whether the NotificationDisplay is visible
    private boolean         hidden;

    /**
     * Constructor
     *
     * Initializes and positions the required UI elements
     * and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements
     * @param context        The context of the application (usually the MainActivity)
     */
    public NotificationDisplay(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context = context;

        int   screenWidth = ScreenDimensions.getWidth(context);
        float screenTopPadding = SCREEN_Y_PERCENTAGE*ScreenDimensions.getHeight(context);

        textView = new TextView(context);
        textView.setTextSize(FONT_SIZE);
        textView.setGravity(Gravity.CENTER|Gravity.TOP);
        textView.setWidth(screenWidth);
        textView.setText(DEFAULT_TEXT);
        textView.setY(screenTopPadding);
        relativeLayout.addView(textView);

        hide();
    }

    @Override
    public void hide() {
        hidden = true;
        textView.clearAnimation();
        textView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideForGameEnd() {
        hide();
    }

    @Override
    public void setupAndAppearForGame() {
        hidden = false;
        textView.setText(DEFAULT_TEXT);
        textView.clearAnimation();
        textView.setVisibility(View.VISIBLE);
    }

    /**
     * Cast a transition effect on the textview
     */
    private void castTransitionEffect(){
        Effects.castFadeInEffect(textView, 0.f, -FADE_IN_TEXT_VIEW_MOVE_Y, 0.f, 0.f, FADE_IN_TEXT_VIEW_DURATION, true);
    }

    /**
     * Displays an announcement
     * @param htmlString  A HTML formatted String representing the announcement
     */
    public void announce(String htmlString) {
        if (hidden) return;
        textView.setText(Html.fromHtml(htmlString));
        castTransitionEffect();
    }

    /**
     * Gets a HTML String formatted with bold text
     * @param string  The String to format
     * @return        The formatted String
     */
    private String getBoldHTMLString(String string) {
        return "<b>"+string+"</b>";
    }

    /**
     * Announce that another participant has attacked another participant
     * @param intiatorName The name of the participant who started the attack
     * @param victimName   The name of the participant being successfully attacked
     * @param powerupType  The PowerupType of the attack
     */
    public void announceOtherToOtherAttack(String intiatorName, String victimName, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        intiatorName = getResizedName(intiatorName);
        victimName   = getResizedName(victimName);
        if (powerupType==PowerupType.UPSIZE)
            announce(getBoldHTMLString(intiatorName)+" has "
                    +getBoldHTMLString(getColoredHTMLString(
                            PowerupType.UPSIZE.pastTenseVerb, PowerupType.UPSIZE.darkerColorHexString))+" "
                    +getBoldHTMLString(victimName)+"'s next deck");
        else
            announce(getBoldHTMLString(intiatorName)+" has "
                    +getBoldHTMLString(getColoredHTMLString(powerupType.pastTenseVerb, powerupType.darkerColorHexString))
                    +" "+getBoldHTMLString(victimName));
    }

    /**
     * Announce that the user has been successfully attacked by another participant
     * @param intiatorName The name of the participant who started the attack
     * @param powerupType  The PowerupType of the attack
     */
    public void announceOtherToSelfAttack(String initiatorName, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        initiatorName = getResizedName(initiatorName);
        if (powerupType==PowerupType.UPSIZE)
            announce(getBoldHTMLString(initiatorName)+" has "
                    +getBoldHTMLString(getColoredHTMLString(
                            PowerupType.UPSIZE.pastTenseVerb, PowerupType.UPSIZE.darkerColorHexString))
                    +" your next deck!");
        else
            announce(getBoldHTMLString(initiatorName)+" has "
                    +getBoldHTMLString(getColoredHTMLString(powerupType.pastTenseVerb, powerupType.darkerColorHexString))
                    +" you!");
    }

    /**
     * Announce that the user has blocked an attack by another participant
     * @param intiatorName The name of the participant who started the attack
     * @param powerupType  The PowerupType of the attack
     */
    public void announceOtherToSelfBlock(String initiatorName, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        initiatorName = getResizedName(initiatorName);
        announce("You have "+getBlockedFormatHTMLString()+" "+powerupType.prepositionString+" "
                +getBoldHTMLString(getColoredHTMLString(powerupType.name, powerupType.darkerColorHexString))
                +" attempt from "+getBoldHTMLString(initiatorName)+"!");
    }

    /**
     * Announce that the user has successfully attacked another participant
     * @param targetedName The name of the participant being attacked
     * @param powerupType  The PowerupType of the attack
     */
    public void announcePersonalAttackSucceeded(String targetedName, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        targetedName = getResizedName(targetedName);
        if (powerupType==PowerupType.UPSIZE)
            announce("You have successfully "
                    +getBoldHTMLString(getColoredHTMLString(
                            PowerupType.UPSIZE.pastTenseVerb, PowerupType.UPSIZE.darkerColorHexString))+" "
                    +getBoldHTMLString(targetedName)+"'s next deck!");
        else
        announce("You have successfully "
                +getBoldHTMLString(getColoredHTMLString(powerupType.pastTenseVerb, powerupType.darkerColorHexString))
                +" "+getBoldHTMLString(targetedName)+"!");
    }

    /**
     * Announce that the user's attack on another participant has been blocked
     * @param targetedName The name of the participant being targeted
     * @param powerupType  The PowerupType of the attack
     */
    public void announcePersonalAttackBlocked(String targetedName, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        targetedName = getResizedName(targetedName);
        announce(getBoldHTMLString(targetedName)+" has "
                +getBlockedFormatHTMLString()+" your "
                +getBoldHTMLString(getColoredHTMLString(powerupType.name, powerupType.darkerColorHexString))
                +" attempt!");
    }

    /**
     * Announce that another participant has blocked another participant
     * @param initiatorName The name of the participant who started the attack
     * @param blockerName   The name of the participant who blocked the attack
     * @param powerupType   The PowerupType of the attack
     */
    public void announceOtherToOtherBlock(String initiatorName, String blockerName, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        initiatorName = getResizedName(initiatorName);
        blockerName         = getResizedName(blockerName);
        announce(getBoldHTMLString(blockerName)+" has "+getBlockedFormatHTMLString()+" "+powerupType.prepositionString+" "
                +getBoldHTMLString(getColoredHTMLString(powerupType.name, powerupType.darkerColorHexString))
                +" attempt from "+getBoldHTMLString(initiatorName));
    }

    /**
     * Announce that one or more participants have disconnected from the current game
     * @param particpantNames  A List of the names of the disconnected participants
     */
    public void announceDisconnected(List<String> particpantNames) {
        if (particpantNames.size()==1) {
            String particpantName = getResizedName(particpantNames.get(0));
            announce(getBoldHTMLString(particpantName)+" has disconnected from the game.");
        } else {
            StringBuilder particpantNamesJoined = new StringBuilder();
            for (int i=0; i<particpantNames.size(); ++i) {
                particpantNamesJoined.append(particpantNames.get(i));
                if (i<particpantNames.size()-2)
                    particpantNamesJoined.append(", ");
                else if (i<particpantNames.size()-1)
                    particpantNamesJoined.append(" and ");
            }
            announce(getBoldHTMLString(particpantNamesJoined.toString())+" have disconnected.");
        }
    }

    /**
     * Gets a HTML formatted String for the color
     * @param string       The String to format
     * @param colorHexCode The Hex code for the color
     * @return             The formatted String
     */
    private String getColoredHTMLString(String string, String colorHexCode) {
        return "<font color=\""+colorHexCode+"\">"+string+"</font>";
    }

    /**
     * @return A HTML formatted String representing "Blocked"
     */
    private String getBlockedFormatHTMLString() {
        return getBoldHTMLString(getColoredHTMLString("blocked", "#777777"));
    }

    /**
     * Truncates the name if it might be too long to be displayed in the NotificationDisplay
     * @param name  The name
     * @return      The resized name
     */
    public String getResizedName(String name) {
        if (name.length()>MAX_NAME_CHARACTER_COUNT)
            return name.substring(0, MAX_NAME_CHARACTER_COUNT)+"...";
        return name;
    }

}
