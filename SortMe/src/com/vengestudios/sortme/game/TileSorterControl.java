package com.vengestudios.sortme.game;

import java.util.ArrayList;
import java.util.Comparator;

import com.vengestudios.sortme.R;
import com.vengestudios.sortme.helpers.logic.CustomSorts;
import com.vengestudios.sortme.helpers.logic.Randomizer;
import com.vengestudios.sortme.helpers.ui.Effects;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;
import com.vengestudios.sortme.sound.SoundPlayer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * The row of tiles on the GameScreen
 *
 * Responsible For:
 *
 *  - Providing the user with rows(decks) of Tiles to sort in ascending order
 *
 *  - Keeping track of the PowerUp statuses of the user
 *    > Bubbletized
 *    > Shielded
 *    > Has next puzzle UpSized
 *
 *  - Implementing the game mechanics for the various PowerUp statuses
 *
 *  - Incrementing the user's score for successfully sorting each row
 *
 *  - Increasing the difficulty (size of row) and scores rewarded for each row
 *    being sorted
 *
 *  - Calling upon SoundPlayer to play sounds for the different PowerUps
 */
public class TileSorterControl implements OnTouchListener, GameElement {

	// UI, animation, and game mechanics constants
    private static final int   MAX_NO_OF_TILES              = 15;
    private static final int   MIN_NO_OF_TILES              = 2;
    private static final int   MAX_STANDARD_NO_OF_TILES     = 10;
    private static final int   UPSIZE_NO_OF_ADDED_TILES     = 2;
    private static final int   STARTING_NO_OF_TILES         = 5;
    private static final float SCREEN_WIDTH_PERCENTAGE      = 0.88f;
    private static final float SCREEN_HEIGHT_PERCENTAGE     = 0.19f;
    private static final float SCREEN_Y_PERCENTAGE          = 0.73f;
    private static final int   MIN_TILE_VALUE               = 0;
    private static final int   MAX_TILE_VALUE               = 99;
    private static final int   INTERSECT_VERTICAL_TOLERANCE = 50;
    private static final int   SWAP_ANIMATION_TIME          = 300;
    private static final int   SORTED_ANIMATION_MOVE_Y      = -150;
    private static final int   SORTED_ANIMATION_TIME        = 1000;
    private static final float SORTED_ANIMATION_STRENGTH    = 1.5f;
    private static final int   AUTO_CHECK_IS_SORTED_DELAY   = 100;

    private static final float SORTED_MP_REWARD             = 200.f;

    private static final int   UPSIZED_TILE_TEXT_COLOR      = Color.rgb(150, 150, 150);
    private static final int   DISABLED_TILE_TEXT_COLOR     = Color.rgb(150, 150, 150);
    private static final int   TILE_TEXT_COLOR              = Color.rgb(44, 62, 80);

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
	private RelativeLayout  relativeLayout;
    private Context         context;

    // UI Elements and fields
    private RelativeLayout  tilesLayout;
    private ImageView       sortedAfterImageView;
    private Rect            sortedAfterImageCropRect;

    private Tile []         tiles;
    private Tile []         visibleTiles;
    private Tile []         tilesAfterImages;

    private int screenHeight;
    private int screenWidth;
    private int tilesTotalWidth;
    private int tilesTotalHeight;
    private int tilesLeftPadding;
    private int tilesTopPadding;

    private int   tileWidth;
    private float tileWidthHalf;
    private int   noOfTiles;

    private boolean needsOldAnimationListener;

    // PowerUp statuses management fields
    private boolean bubbletized;
    private boolean disabledForBubbletized;

    private boolean shielded;

    private boolean unlocked;

    private boolean nextPuzzleUpsized;

    private Handler  checkIsSortedHandler;
    private Runnable checkIsSortedRunnable;

    private boolean  hidden;

    // GameElement Dependencies
    private MPBar                   mpBar;
    private ParticipantCoordinator  participantCoordinator;

    // Variables for Touch handling
    private static final int STOP_DRAGGING         = 0;
    private static final int CURRENTLY_DRAGGING = 1;
    private static final int START_DRAGGING     = 2;

    private Rect tilesTotalRect;

    private Tile currentTile;
    private int  currentTilePosition = -1;

    private Tile finalIntersectedTile;
    private int  finalIntersectedTilePosition = -1;

    private Tile  leftmostTile;
    private float leftmostTileXCoor;
    private Tile  rightmostTile;
    private float rightmostTileXCoor;

    private int   dragStatus;
    private float initialX;
    private float initialY;

    private boolean readyToTestForSorted;

    /**
     * Constructor
     *
     * Initializes and positions the required UI elements
     * and adds them to the RelativeLayout
     *
     * Loads the sounds needed into SoundPlayer
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements
     * @param context        The context of the application (usually the MainActivity)
     */
    public TileSorterControl(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout     = relativeLayout;
        this.context            = context;

        dragStatus                = STOP_DRAGGING;
        needsOldAnimationListener = (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN);
        checkIsSortedHandler      = new Handler();

        screenHeight = ScreenDimensions.getHeight(context);
        screenWidth  = ScreenDimensions.getWidth(context);

        tilesTotalHeight = (int)(SCREEN_HEIGHT_PERCENTAGE*screenHeight);
        tilesTotalWidth  = (int)(SCREEN_WIDTH_PERCENTAGE *screenWidth);
        tilesLeftPadding = (int)((1.f-SCREEN_WIDTH_PERCENTAGE)*.5*screenWidth);
        tilesTopPadding  = (int)(SCREEN_Y_PERCENTAGE*screenHeight);
        tilesTotalRect   = new Rect(tilesLeftPadding-tileWidth/2,
                                tilesTopPadding-tilesTotalHeight-INTERSECT_VERTICAL_TOLERANCE,
                                tilesLeftPadding+tilesTotalWidth,
                                tilesTopPadding+tilesTotalHeight+INTERSECT_VERTICAL_TOLERANCE);
        tiles            = new Tile[MAX_NO_OF_TILES];
        tilesAfterImages = new Tile[MAX_NO_OF_TILES];

        tilesLayout      = new RelativeLayout(context);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        tilesLayout.setLayoutParams(rlp);
        relativeLayout.addView(tilesLayout);

        sortedAfterImageView = new ImageView(context);
        sortedAfterImageView.setLayoutParams(rlp);
        relativeLayout.addView(sortedAfterImageView);
        sortedAfterImageView.setVisibility(View.INVISIBLE);
        sortedAfterImageCropRect = new Rect(tilesLeftPadding,
                                        tilesTopPadding,
                                        tilesLeftPadding+tilesTotalWidth,
                                        tilesTopPadding+tilesTotalHeight);

        for (int i=0; i<MAX_NO_OF_TILES; ++i) {
            Tile tileAfterImage = new Tile(context);
            tilesAfterImages[i] = tileAfterImage;
            tileAfterImage.setVisibility(View.INVISIBLE);
            tileAfterImage.setAlpha(.5f);
            tileAfterImage.setHeight(tilesTotalHeight);
            tilesLayout.addView(tileAfterImage);
        }
        for (int i=0; i<MAX_NO_OF_TILES; ++i) {
            Tile tile = new Tile(context);
            tile.setOnTouchListener(this);
            tiles[i] = tile;
            tile.setVisibility(View.INVISIBLE);
            tile.setHeight(tilesTotalHeight);
            tilesLayout.addView(tile);
        }

        hide();

        SoundPlayer.loadSound(PowerupType.BUBBLETIZE, context, R.raw.bubble1);
        SoundPlayer.loadSound(PowerupType.RANDOMIZE,  context, R.raw.randomize1);
        SoundPlayer.loadSound(PowerupType.UPSIZE,     context, R.raw.add1);
        SoundPlayer.loadSound(PowerupType.SHIELD,     context, R.raw.shield1);

    }

    @Override
    public void hide(){
        hidden = true;
        for (int i=0; i<MAX_NO_OF_TILES; ++i) {
            tiles[i].setVisibility(View.INVISIBLE);
            tilesAfterImages[i].setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void hideForGameEnd() {
        hide();
    }

    /**
     * Register the MPBar
     * @param mpBar
     */
    public void registerMPBar(MPBar mpBar) {
        this.mpBar = mpBar;
    }

    /**
     * Register the ParticipantCoordinator
     * @param participantCoordinator
     */
    public void registerParticipantCoordinator(ParticipantCoordinator participantCoordinator) {
        this.participantCoordinator = participantCoordinator;
    }

    @Override
    public void setupAndAppearForGame() {
        hidden = false;
        makePuzzle(getStandardNoOfTiles());
    }

    /**
     * Makes a new puzzle, formatting the tiles
     * and sets up the fields for touch handing as required
     *
     * @param puzzleTileCount The number of tiles
     */
    private void makePuzzle (int puzzleTileCount) {
        if (hidden) return;
        puzzleTileCount = Math.max(MIN_NO_OF_TILES, Math.min(MAX_NO_OF_TILES, puzzleTileCount));

        Tile tile, tileAfterImage;
        tileWidth       = tilesTotalWidth/puzzleTileCount;
        tileWidthHalf   = tileWidth>>1;
        noOfTiles       = puzzleTileCount;
        visibleTiles    = new Tile[puzzleTileCount];

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(tileWidth, tilesTotalHeight);

        int tileValues   [] = new int[puzzleTileCount];
        int xCoordinates [] = new int[puzzleTileCount];

        for (int i=0; i<puzzleTileCount; ++i)
            tileValues[i] = Randomizer.randInt(MIN_TILE_VALUE, MAX_TILE_VALUE);
        CustomSorts.insertionSortAsec(tileValues);

        for (int i=0; i<puzzleTileCount; ++i) {
            int x = tilesLeftPadding+i*tileWidth;

            tile = tiles[i];
            tile.setVisibility(View.VISIBLE);
            tile.setLayoutParams(layoutParams);
            tile.setText(Integer.toString(tileValues[i]));
            tile.setY(tilesTopPadding);
            xCoordinates[i] = x;

            tileAfterImage = tilesAfterImages[i];
            tileAfterImage.setVisibility(View.INVISIBLE);
            tileAfterImage.setLayoutParams(layoutParams);
            tileAfterImage.setX(x);
            tileAfterImage.setY(tilesTopPadding);

            visibleTiles[i] = tile;
        }
        Randomizer.shuffleArray(xCoordinates);
        for (int i=0; i<puzzleTileCount; ++i) {
            tile = tiles[i];
            int xCoor = xCoordinates[i];
            tile.setX(xCoor);
            tile.position = (xCoor-tilesLeftPadding)/tileWidth;
        }
        for (int i=puzzleTileCount; i<MAX_NO_OF_TILES; ++i) {
            tile = tiles[i];
            tile.setVisibility(View.INVISIBLE);
        }
        tilesTotalRect = new Rect(tilesLeftPadding-tileWidth/2,
                            tilesTopPadding-tilesTotalHeight-INTERSECT_VERTICAL_TOLERANCE,
                            tilesLeftPadding+tilesTotalWidth,
                            tilesTopPadding+tilesTotalHeight+INTERSECT_VERTICAL_TOLERANCE);

        if (isSorted(false)) makePuzzle(puzzleTileCount);
    }

    /**
     * @return Whether the user is shielded
     */
    public boolean getShielded() {
        return shielded;
    }

    /**
     * Activates the shield
     */
    public void shield() {
        SoundPlayer.play(PowerupType.SHIELD, context, 0, 1.f);
        shielded = true;
    }

    /**
     * Deactivates the shield
     */
    public void unshield() {
        shielded = false;
    }

    /**
     * Attempts to Bubbletize the user
     * @return Whether the attempt is successful (not blocked)
     */
    public boolean bubbletize() {
        if (shielded) return false;
        SoundPlayer.play(PowerupType.BUBBLETIZE, context, 1, 1.f);
        bubbletized = true;
        return true;
    }

    /**
     * Turns off the bubbletize status for the user
     */
    public void unBubbletize(){
        bubbletized = false;
    }

    /**
     * Attempts to UpSize the user's next row
     * @return Whether the attempt is successful (not blocked)
     */
    public boolean upsize() {
        if (shielded) return false;
        nextPuzzleUpsized = true;
        return true;
    }

    /**
     * Sets all the status for PowerUps to false
     */
    private void resetStatusAilments() {
    	bubbletized       = false;
    	shielded          = false;
    	nextPuzzleUpsized = false;
    }

    /**
     * Allows the user to start sorting. Called at the start of a game.
     */
    public void unlock() {
    	resetStatusAilments();
        unlocked          = true;
    }

    /**
     * Stops the user from sorting. Called at the end of a game.
     */
    public void lock() {
        unlocked = false;
    }

    /**
     * Schedule a check on to see if they are sorted
     * @param delayInMilliseconds  The duration to delay
     */
    public void autoCheckIsSorted(int delayInMilliseconds) {
        if (checkIsSortedRunnable!=null) {
            checkIsSortedHandler.removeCallbacks(checkIsSortedRunnable);
        }
        checkIsSortedRunnable = new Runnable() {
            @Override
            public void run() {
                if (isSorted(false)) makeNextPuzzle();
            }
        };
        checkIsSortedHandler.postDelayed(checkIsSortedRunnable, delayInMilliseconds);
    }

    /**
     * Attempts to Randomize the user's tiles by re-shuffling them
     * with some animations
     * @return Whether the attempt is successful (not blocked)
     */
    public boolean randomize() {
        if (shielded) return false;

        ArrayList<Tile> originalTiles = new ArrayList<Tile>(noOfTiles);
        ArrayList<Tile> swappedTiles  = new ArrayList<Tile>(noOfTiles);
        for (int i=0; i<noOfTiles; ++i) {
            Tile tile  = tiles[i];
            int tilePosition = tile.position;
            if (tilePosition!=currentTilePosition &&
                tilePosition<noOfTiles) {
                originalTiles.add(tile);
                swappedTiles .add(tile);
            }
        }
        Randomizer.shuffleArrayList(swappedTiles);
        float swappedXCoor, swappedYCoor;
        Tile originalTile, swappedTile;
        for (int i=0; i<swappedTiles.size(); ++i) {
            originalTile = originalTiles.get(i);
            swappedTile  = swappedTiles .get(i);
            swappedXCoor = swappedTile  .getX();
            swappedYCoor = swappedTile  .getY();
            TranslateAnimation animation = new TranslateAnimation(0, 0,
                    TranslateAnimation.ABSOLUTE, swappedXCoor - originalTile.getX(), 0, 0,
                    TranslateAnimation.ABSOLUTE, swappedYCoor - originalTile.getY());
            animation.setDuration(SWAP_ANIMATION_TIME);
            if (needsOldAnimationListener)
                originalTile.prepareForRandomizeAnimation((int)swappedXCoor, (int)swappedYCoor, swappedTile.position);
            else
                animation.setAnimationListener(new TileAnimationListener(originalTile, swappedXCoor, swappedYCoor, swappedTile.position));
            originalTile.clearAnimation();
            originalTile.startAnimation(animation);
        }
        for (int i=0; i<swappedTiles.size(); ++i) {
            originalTile = originalTiles.get(i);
            originalTile.position = noOfTiles<<1;
        }
        autoCheckIsSorted(SWAP_ANIMATION_TIME+AUTO_CHECK_IS_SORTED_DELAY);

        SoundPlayer.play(PowerupType.RANDOMIZE, context, 0, 1.f);

        return true;
    }

    /**
     * Casts an animation of a tile being swapped
     * @param tile   The Tile
     * @param xCoor  The current X coordinate pixel of the tile
     * @param yCoor  The current Y coordinate pixel of the tile
     * @param nextId The next position the tile will be at in the row
     */
    private void animateTileSwapTo(Tile tile, int xCoor, int yCoor, int nextId) {
        TranslateAnimation animation = new TranslateAnimation(0, 0,
                TranslateAnimation.ABSOLUTE, xCoor - tile.getX(), 0, 0,
                TranslateAnimation.ABSOLUTE, yCoor - tile.getY());
        animation.setDuration(SWAP_ANIMATION_TIME);
        if (needsOldAnimationListener)
            tile.prepareForSwapAnimation(xCoor, yCoor, nextId);
        else
            animation.setAnimationListener(new TileAnimationListenerWithSortedCheck(tile, xCoor, yCoor, nextId));
        tile.position = noOfTiles<<1;
        tile.clearAnimation();
        tile.startAnimation(animation);
    }

    /**
     * Disable a tile indicating that it cannot be swapped with.
     * The tile will change its looks upon disabling.
     *
     * Used for Bubbletize.
     */
    private void disableForBubbletized() {
        if (disabledForBubbletized) return;
        disabledForBubbletized = true;
        for (int i=0; i<noOfTiles; ++i) {
            Tile tile = tiles[i];
            int tilePosition  = tile.position;
            if (tilePosition>noOfTiles) tilePosition>>=1;
            if (tilePosition<currentTilePosition-1 || tilePosition>currentTilePosition+1) {
                tile.setEnabled(false);
            }
        }
    }

    /**
     * Undos the disabling of a tile, so that it looks like a normal tile
     * again.
     *
	 * However, if the user is still being Bubbletized, the next time
	 * he/she drags a tile, all required tiles will be disabled again.
     */
    private void undoDisableForBubbletized(){
        if (!disabledForBubbletized) return;
        disabledForBubbletized = false;
        for (int i=0; i<noOfTiles; ++i) {
            Button tile = tiles[i];
            tile.setEnabled(true);
        }
    }

    /**
     * Calculates the leftmost and rightmost tile currently shown on the screen
     * in the row.
     */
    private void calculateLeftmostAndRightmostTile(){
        float maxXCoor = Float.MIN_VALUE;
        float minXCoor = Float.MAX_VALUE;
        for (int i=0; i<noOfTiles; ++i) {
            Tile tile = tiles[i];
            float xCoor = tile.getX();
            if (xCoor<minXCoor) {
                minXCoor = xCoor;
                leftmostTile = tile;
            } else if (xCoor>maxXCoor) {
                maxXCoor = xCoor;
                rightmostTile = tile;
            }
        }
        leftmostTileXCoor  = minXCoor;
        rightmostTileXCoor = maxXCoor;
    }

    /**
     * Calculates if two tiles have intersected and swaps them if they are.
     * Else, the tile that has been dragged out of position will simply be animated
     * back to its orginal position
     * @param tileXCoor The current X coordinate pixel of the tile being dragged
     * @param tileYCoor The current Y coordinate pixel of the tile being dragged
     */
    private void swapIfIntersect(float tileXCoor, float tileYCoor) {
        if (tilesTotalRect.top<=tileYCoor && tileYCoor<tilesTotalRect.bottom){
            boolean intersected = false;
            Tile intersectedTile = null;
            for (int i=0; i<noOfTiles; ++i) {
                intersectedTile = tiles[i];
                if (intersectedTile!=currentTile) {
                    float xDiff = tileXCoor-intersectedTile.getX();
                    if (xDiff<tileWidthHalf && xDiff>-tileWidthHalf)  {
                        intersected = intersectedTile.isEnabled();
                        break;
                    }
                }
            }
            if (!intersected && !bubbletized) {
                if (tileXCoor<leftmostTileXCoor && leftmostTile.position<currentTilePosition) {
                    intersectedTile = leftmostTile;
                    intersected = true;
                } else if (tileXCoor>rightmostTileXCoor && rightmostTile.position>currentTilePosition) {
                    intersectedTile = rightmostTile;
                    intersected = true;
                }
            }
            if (intersected && intersectedTile.position<noOfTiles) {
                Button tileAfterImage = tilesAfterImages[currentTilePosition];
                tileAfterImage.setVisibility(View.VISIBLE);
                tileAfterImage.setText(intersectedTile.getText());
                finalIntersectedTile = intersectedTile;
                finalIntersectedTilePosition = intersectedTile.position;
            } else {
                tilesAfterImages[currentTilePosition].setVisibility(View.INVISIBLE);
                finalIntersectedTile = null;
                finalIntersectedTilePosition = -1;
            }

        } else {
            tilesAfterImages[currentTilePosition].setVisibility(View.INVISIBLE);
            finalIntersectedTile = null;
            finalIntersectedTilePosition = -1;
        }
    }

    /**
     * Checks if the tiles have been sorted
     * @param isFromSwapAnimation  Whether this method is called from the swapping animation
     * @return True if sorted, else False
     */
    private boolean isSorted(boolean isFromSwapAnimation) {
        if (isFromSwapAnimation) {
            if (readyToTestForSorted==false) {
                readyToTestForSorted = true;
                return false;
            }
        }
        int previousTileValue     = Integer.MIN_VALUE;
        CustomSorts.insertionSort(visibleTiles, new Comparator<Tile>() {
            @Override
            public int compare(Tile lhs, Tile rhs) {
                return lhs.position-rhs.position;
            }
        });
        for (int i=0; i<noOfTiles; ++i) {
            int currentTileValue = Integer.parseInt(((Button)visibleTiles[i]).getText().toString());
            if (currentTileValue<previousTileValue) return false;
            previousTileValue=currentTileValue;
        }
        return true;
    }

    /**
     * Generates the next row of tiles, factoring into account whether the next row
     * has been UpSized.
     *
     * Casts the animation for successful sorting.
     *
     * Increments the MP and user's scores accordingly.
     */
    private void makeNextPuzzle() {
        castSortedEffect();
        incrementMPBar();
        incrementOwnScore();

        int nextPuzzleNoOfTiles = getStandardNoOfTiles();
        if (nextPuzzleUpsized) {
            SoundPlayer.play(PowerupType.UPSIZE, context, 0, 1.f);

            nextPuzzleNoOfTiles+=UPSIZE_NO_OF_ADDED_TILES;
            makePuzzle(nextPuzzleNoOfTiles);
            nextPuzzleUpsized = false;
            int upsizedTilesIndexes[] = new int[nextPuzzleNoOfTiles];
            for (int i=0; i<UPSIZE_NO_OF_ADDED_TILES; ++i)
                upsizedTilesIndexes[i] = 1;
            Randomizer.shuffleArray(upsizedTilesIndexes);
            for (int i=0; i<nextPuzzleNoOfTiles; ++i)
                if (upsizedTilesIndexes[i]==1)
                    tiles[i].setTextColor(UPSIZED_TILE_TEXT_COLOR);
                else
                    tiles[i].setTextColor(TILE_TEXT_COLOR);

        } else {
            makePuzzle(nextPuzzleNoOfTiles);
            for (int i=0; i<nextPuzzleNoOfTiles; ++i)
                tiles[i].setTextColor(TILE_TEXT_COLOR);
        }
    }

    /**
     * @return The number of tiles for the row according to how many rows have been sorted.
     *         It does not factor into account whether the next row is UpSized.
     */
    private int getStandardNoOfTiles() {
        if (participantCoordinator!=null)
            return Math.min(STARTING_NO_OF_TILES+participantCoordinator.getOwnLinesSorted()/5,
            		MAX_STANDARD_NO_OF_TILES);
        else
            return STARTING_NO_OF_TILES;
    }

    /**
     * Increments the user's score.
     */
    private void incrementOwnScore(){
        if (participantCoordinator!=null) {
            int standardNoOfTiles = getStandardNoOfTiles();
            participantCoordinator.incrementOwnScoreAndLinesSorted(standardNoOfTiles*standardNoOfTiles);
        }
    }

    /**
     * Increments the user's MP
     */
    private void incrementMPBar(){
        if (mpBar!=null)
            mpBar.incrementMP(SORTED_MP_REWARD);
    }

    /**
     * Casts the sorted effect for a succesfully sorted row
     */
    private void castSortedEffect(){
        Effects.castFadeAwayAfterImageEffect(tilesLayout, sortedAfterImageView,
                sortedAfterImageCropRect,
                0.f, 0.f,
                1.f, 1.f,
                1.f,
                0.f, SORTED_ANIMATION_MOVE_Y,
                SORTED_ANIMATION_STRENGTH, SORTED_ANIMATION_TIME);
    }

    /**
     * Provides the dragging mechanism for the tiles.
     *
     * It calls upon all other methods used for the swapping mechanism,
     * depending on the status of the dragging action.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        boolean pressed = false;
        if (unlocked==false) return true;
        if (currentTile==null || currentTile==v){
            currentTilePosition = ((Tile)v).position;
            if (currentTilePosition>noOfTiles) return true;
            if (action==MotionEvent.ACTION_DOWN) {
                dragStatus = START_DRAGGING;
                currentTile = (Tile)v;
                finalIntersectedTile = null;
            }
            if (currentTile==null) return true;
            if (action==MotionEvent.ACTION_CANCEL||action==MotionEvent.ACTION_UP) {
                dragStatus = STOP_DRAGGING;
                readyToTestForSorted = false;
                if (finalIntersectedTile!=null) {
                    animateTileSwapTo(finalIntersectedTile, tilesLeftPadding+currentTilePosition*tileWidth,
                            tilesTopPadding, currentTilePosition);
                    animateTileSwapTo(currentTile, tilesLeftPadding+finalIntersectedTilePosition*tileWidth,
                            tilesTopPadding, finalIntersectedTilePosition);
                } else {
                    animateTileSwapTo(currentTile, tilesLeftPadding+currentTilePosition*tileWidth, tilesTopPadding, currentTilePosition);
                }
                undoDisableForBubbletized();
                tilesAfterImages[currentTilePosition].setVisibility(View.INVISIBLE);
                currentTile = null;
                currentTilePosition = -1;
            } else if (event.getAction()==MotionEvent.ACTION_MOVE) {
                if (dragStatus==START_DRAGGING) {
                    dragStatus = CURRENTLY_DRAGGING;
                    initialX = event.getX();
                    initialY = event.getY();
                    calculateLeftmostAndRightmostTile();
                } else if (dragStatus==CURRENTLY_DRAGGING) {
                    float xCoor = v.getX();
                    float yCoor = v.getY();
                    swapIfIntersect(xCoor, yCoor);
                    v.setX(event.getX()-initialX+xCoor);
                    v.setY(event.getY()-initialY+yCoor);
                    pressed = true;
                }
                if (bubbletized) disableForBubbletized();
                else              undoDisableForBubbletized();
            }
        }
        v.setPressed(pressed);
        return true;
    }

    /**
     * An AnimationListener to exclude the tile for being check for
     * intersection with a dragged tile during the animation
     */
    private class TileAnimationListener implements AnimationListener {
        Tile tile; float xCoor, yCoor; int nextPosition;
        public TileAnimationListener(Tile tile, float xCoor, float yCoor, int nextPosition) {
            this.tile = tile; this.xCoor = xCoor; this.yCoor = yCoor; this.nextPosition = nextPosition;
        }
        @Override public void onAnimationEnd(Animation animation) {
            tile.setX(xCoor); tile.setY(yCoor);    tile.position = nextPosition; tile.clearAnimation();
        }
        @Override public void onAnimationRepeat(Animation arg0) {}
        @Override public void onAnimationStart(Animation arg0) {}
    }

    /**
     * Extends on TileAnimationListener to schedule a check on
     * whether the row is sorted upon the end of a swapping animation
     */
    private class TileAnimationListenerWithSortedCheck extends TileAnimationListener {
        public TileAnimationListenerWithSortedCheck(Tile tile, float xCoor, float yCoor, int nextPosition) {
            super(tile, xCoor, yCoor, nextPosition);
        }
        @Override public void onAnimationEnd(Animation animation) {
            super.onAnimationEnd(animation);
            if (isSorted(true)) makeNextPuzzle();
        }
    }

    /**
     * A subclass of Button with added functionality to facilitate
     * the swapping mechanism.
     *
     * Also defines the visual look for the Tile.
     */
    private class Tile extends Button {
        private static final int RANDOMIZE_ANIMATION = 0;
        private static final int SWAP_ANIMATION      = 1;
        public  int position;
        private int nextXCoor, nextYCoor, nextPosition, animationType;
        public Tile(Context context) {
            super(context);

            setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            setTextColor(TILE_TEXT_COLOR);
            setPadding(0, 0, 0, 0);
            setBackgroundResource(R.drawable.tile_9_slice);
        }
        public void prepareForSwapAnimation(int nextXCoor, int nextYCoor, int nextPosition) {
            setAnimationVariables(nextXCoor, nextYCoor, nextPosition);
            animationType = SWAP_ANIMATION;
        }
        public void prepareForRandomizeAnimation(int nextXCoor, int nextYCoor, int nextPosition) {
            setAnimationVariables(nextXCoor, nextYCoor, nextPosition);
            animationType = RANDOMIZE_ANIMATION;
        }
        private void setAnimationVariables(int nextXCoor, int nextYCoor, int nextPosition) {
            this.nextXCoor = nextXCoor; this.nextYCoor = nextYCoor; this.nextPosition = nextPosition;
        }
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                setTextColor(TILE_TEXT_COLOR);
                setAlpha(1.f);
            } else {
                setTextColor(DISABLED_TILE_TEXT_COLOR);
                setAlpha(.8f);
            }
        }
        @Override
        protected void onAnimationEnd() {
            super.onAnimationEnd();
            position = nextPosition;
            setX(nextXCoor); setY(nextYCoor); clearAnimation();
            if (animationType==SWAP_ANIMATION)
                if (isSorted(true))
                    makeNextPuzzle();
        }
    }

}
