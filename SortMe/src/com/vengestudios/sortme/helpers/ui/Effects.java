package com.vengestudios.sortme.helpers.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * A helper class containing functions to cast animations
 */
public class Effects {

	/**
	 * Casts an after-image effect that fades away and translates (moves)
	 *
	 * @param view              The View to cast the effect on
	 * @param afterImageView    The assistant View used for drawing the after-image onto
	 * @param initialXOffset    The initial X Offset of the after-image in pixels
	 * @param initialYOffset    The initial Y Offset of the after-image in pixels
	 * @param effectScaleX      The X scale of the after-image
	 * @param effectScaleY      The Y scale of the after-image
	 * @param startingAlpha     The starting alpha of the after-image
	 * @param xMoveDistance     The X distance to translate the after-image in pixels
	 * @param yMoveDistance     The Y distance to translate the after-image in pixels
	 * @param animationStrength The easing strength of the animation
	 * @param duration          The duration of the animation
	 */
    public static void castFadeAwayAfterImageEffect(View view, ImageView afterImageView,
            float initialXOffset, float initialYOffset,
            float effectScaleX,   float effectScaleY,
            float startingAlpha,
            float xMoveDistance,  float yMoveDistance,
            float animationStrength, int duration) {
        castFadeAwayAfterImageEffect(view, afterImageView,
                new Rect(0, 0, view.getWidth(), view.getHeight()),
                initialXOffset, initialYOffset,
                effectScaleX, effectScaleY,
                startingAlpha,
                xMoveDistance, yMoveDistance,
                animationStrength, duration);
    }

    public static void castFadeAwayAfterImageEffect(View view, ImageView afterImageView,
            Rect cropRect,
            float initialXOffset, float initialYOffset,
            float effectScaleX,   float effectScaleY,
            float startingAlpha,
            float xMoveDistance,  float yMoveDistance,
            float animationStrength, int duration) {
        int width = cropRect.width();
        int height = cropRect.height();
        int location[] = new int[2];
        view.getLocationInWindow(location);
        int xCoor = location[0];
        int yCoor = location[1];

        view.setDrawingCacheEnabled(true);
        Bitmap b = Bitmap.createBitmap(view.getDrawingCache(), cropRect.left, cropRect.top, width, height);
        view.setDrawingCacheEnabled(false);

        afterImageView.setImageBitmap(b);
        afterImageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        afterImageView.setX(xCoor+cropRect.left+initialXOffset);
        afterImageView.setY(yCoor+cropRect.top+initialYOffset);
        afterImageView.setScaleX(effectScaleX);
        afterImageView.setScaleY(effectScaleY);
        AnimationSet effectAnimationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(startingAlpha, 0.f);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0,
                TranslateAnimation.ABSOLUTE, xMoveDistance, 0, 0,
                TranslateAnimation.ABSOLUTE, yMoveDistance);
        effectAnimationSet.setInterpolator(new DecelerateInterpolator(animationStrength));
        effectAnimationSet.setDuration(duration);
        effectAnimationSet.addAnimation(alphaAnimation);
        effectAnimationSet.addAnimation(translateAnimation);
        effectAnimationSet.setAnimationListener(new HideViewAfterAnimationListener(afterImageView));

        afterImageView.startAnimation(effectAnimationSet);
    }
    public static void castExpandingAfterImageEffect(View view, ImageView effectImageView,
            float startingAlpha,
            float endScaleX, float endScaleY,
            int duration){
        int width = view.getWidth();
        int height = view.getHeight();
        int location[] = new int[2];
        view.getLocationInWindow(location);
        int xCoor = location[0];
        int yCoor = location[1];

        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.setDrawingCacheEnabled(true);
        c.drawBitmap(view.getDrawingCache(), 0, 0, null);
        view.setDrawingCacheEnabled(false);
        effectImageView.setImageBitmap(b);
        effectImageView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        effectImageView.setX(xCoor);
        effectImageView.setY(yCoor);

        AnimationSet effectAnimationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(startingAlpha, 0.f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.f, endScaleX, 1.f,
                endScaleY, Animation.ABSOLUTE, xCoor+.5f*width, Animation.ABSOLUTE, yCoor+.5f*height);
        effectAnimationSet.setInterpolator(new LinearInterpolator());
        effectAnimationSet.setDuration(duration);
        effectAnimationSet.addAnimation(alphaAnimation);
        effectAnimationSet.addAnimation(scaleAnimation);
        effectAnimationSet.setAnimationListener(new HideViewAfterAnimationListener(effectImageView));
        effectImageView.startAnimation(effectAnimationSet);
    }

    public static class HideViewAfterAnimationListener implements AnimationListener {
        private View viewToHide;
        public HideViewAfterAnimationListener(View viewToHide) {
            this.viewToHide = viewToHide;
        }
        @Override public void onAnimationStart(Animation animation) {
            viewToHide.setVisibility(View.VISIBLE);
        }
        @Override public void onAnimationRepeat(Animation animation) {}
        @Override public void onAnimationEnd(Animation animation) {
            viewToHide.clearAnimation();
            viewToHide.setVisibility(View.INVISIBLE);
        }
    }

    public static void castFadeInEffect(View view,
            float initialXOffset, float initialYOffset,
            float xMoveDistance, float yMoveDistance,
            int duration, boolean fillAfter) {
        castAlphaAndTranslateEffect(view, 0.f, 1.f,
                initialXOffset, initialYOffset,
                xMoveDistance, yMoveDistance,
                duration, fillAfter);
    }

    public static void castFadeOutEffect(View view,
            float initialXOffset, float initialYOffset,
            float xMoveDistance, float yMoveDistance,
            int duration, boolean fillAfter) {
        castAlphaAndTranslateEffect(view, 1.f, 0.f,
                initialXOffset, initialYOffset,
                xMoveDistance, yMoveDistance,
                duration, fillAfter);
    }

    public static void castAlphaAndTranslateEffect(View view,
            float initialAlpha, float endAlpha,
            float initialXOffset, float initialYOffset,
            float xMoveDistance, float yMoveDistance,
            int duration, boolean fillAfter) {
        view.clearAnimation();
        view.setVisibility(View.VISIBLE);

        AnimationSet effectAnimationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(initialAlpha, endAlpha);
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, initialXOffset,
                TranslateAnimation.ABSOLUTE, xMoveDistance,
                TranslateAnimation.ABSOLUTE, initialYOffset,
                TranslateAnimation.ABSOLUTE, yMoveDistance);
        effectAnimationSet.setInterpolator(new LinearInterpolator());
        effectAnimationSet.setDuration(duration);
        effectAnimationSet.addAnimation(alphaAnimation);
        effectAnimationSet.addAnimation(translateAnimation);
        effectAnimationSet.setFillAfter(true);

        view.startAnimation(effectAnimationSet);
    }

    public static void castFadeOutEffect(View view,
            int duration, boolean fillAfter, boolean hideViewAfter) {
        view.clearAnimation();
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.f, 0.f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(fillAfter);
        if (hideViewAfter)
            alphaAnimation.setAnimationListener(new HideViewAfterAnimationListener(view));
        view.startAnimation(alphaAnimation);
    }

    public static void castFadeInEffect(View view,
            float endAlpha,
            int duration, boolean fillAfter) {
        view.clearAnimation();
        view.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.f, endAlpha);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(fillAfter);
        view.startAnimation(alphaAnimation);
    }

    public static void castBlinkEffect(View view,
            int timesToBlink, int duration, boolean hideViewAfter) {
        new BlinkEffectHandlerRunnable(view, timesToBlink, duration, hideViewAfter);
    }

    public static class BlinkEffectHandlerRunnable extends Handler implements Runnable {
        private View view;
        private boolean hideViewAfter;
        private int timesToChangeVisibility;
        private int timesChangedVisibility;
        private int timeBetweenChangeVisibility;
        public BlinkEffectHandlerRunnable(View view, int timesToBlink, int duration, boolean hideViewAfter) {
            this.view = view;
            this.hideViewAfter = hideViewAfter;
            timesToChangeVisibility     = Math.max(1, timesToBlink)*2;
            timeBetweenChangeVisibility = Math.max(16, duration/timesToChangeVisibility);
            view.setVisibility(View.VISIBLE);
            postDelayed(this, timeBetweenChangeVisibility);
        }
        @Override
        public void run() {
            if (timesChangedVisibility<timesToChangeVisibility) {
                view.setVisibility((view.getVisibility()==View.VISIBLE)?View.INVISIBLE:View.VISIBLE);
                ++timesChangedVisibility;
                postDelayed(this, timeBetweenChangeVisibility);
            } else  {
                if (hideViewAfter) view.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void castOvershotTranslateEffect(View view,
            float initialXOffset, float initialYOffset,
            float xMoveDistance, float yMoveDistance,
            float tension, int duration, boolean fillAfter, boolean hideAfter) {
        castTranslateEffect(view,
                initialXOffset, initialYOffset,
                xMoveDistance, yMoveDistance,
                new OvershootInterpolator(tension), duration, fillAfter, hideAfter);
    }

    public static void castAccelerateTranslateEffect(View view,
            float initialXOffset, float initialYOffset,
            float xMoveDistance, float yMoveDistance,
            float strength, int duration, boolean fillAfter, boolean hideAfter) {
        castTranslateEffect(view,
                initialXOffset, initialYOffset,
                xMoveDistance, yMoveDistance,
                new AccelerateInterpolator(strength), duration, fillAfter, hideAfter);
    }

    public static void castTranslateEffect(View view,
            float initialXOffset, float initialYOffset,
            float xMoveDistance, float yMoveDistance,
            Interpolator interpolator, int duration, boolean fillAfter, boolean hideAfter) {
        view.clearAnimation();
        view.setVisibility(View.VISIBLE);

        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, initialXOffset,
                TranslateAnimation.ABSOLUTE, xMoveDistance,
                TranslateAnimation.ABSOLUTE, initialYOffset,
                TranslateAnimation.ABSOLUTE, yMoveDistance);
        translateAnimation.setInterpolator(new LinearInterpolator());
        translateAnimation.setDuration(duration);
        translateAnimation.setInterpolator(interpolator);
        translateAnimation.setFillAfter(fillAfter);
        if (hideAfter)
            translateAnimation.setAnimationListener(new HideViewAfterAnimationListener(view));

        view.startAnimation(translateAnimation);
    }

    public static class PulsulationAnimator {
    	private static final float PI_TWO             = 6.28318f;
    	private static final float PI                 = 3.14159f;
    	private static final float PI_HALF            = 1.27323f;
    	private static final float FAST_SIN_C1        = 0.4053f;
    	private static final float FAST_SIN_C2        = 0.225f;
    	private static final long  DURATION_PER_FRAME = 16;

    	private float pulseDelta;
    	private float pulse;
    	private View  view;
    	private float middleAlpha;
    	private float alphaAmplitude;

    	private Handler  pulsulationAnimationHandler;
    	private Runnable pulsulationAnimationRunnable;

    	public PulsulationAnimator(View view,
    			float lowerAlphaLimit, float upperAlphaLimit,
    			float periodDuration) {
    		this.view = view;
    		alphaAmplitude = .5f*(upperAlphaLimit-lowerAlphaLimit);
    		middleAlpha    = upperAlphaLimit-alphaAmplitude;
    		pulseDelta     = PI*DURATION_PER_FRAME/periodDuration;
    		pulse          = -PI;

    		pulsulationAnimationHandler = new Handler();
    		pulsulationAnimationRunnable = new PulsulationAnimationRunnable();
    	}

    	private class PulsulationAnimationRunnable implements Runnable {
    		@Override
    		public void run() {
    			pulse += pulseDelta;
    			if (pulse>PI_TWO)
    				pulse -= PI_TWO;
    			view.setAlpha(middleAlpha+fastSin(pulse)*alphaAmplitude);
    			pulsulationAnimationHandler.postDelayed(pulsulationAnimationRunnable, DURATION_PER_FRAME);
    		}
    		private float fastSin(float x) {
    			if (x<PI) {
    				x *= PI_HALF - FAST_SIN_C1 * x;
    				return FAST_SIN_C2*(x*x-x)+x;
    			} else {
    				x -= PI;
    				x *=  PI_HALF - FAST_SIN_C1 * x;
    				return -FAST_SIN_C2*(x*x-x)-x;
    			}
    		}
        }

    	public void start() {
			pulsulationAnimationHandler.postDelayed(pulsulationAnimationRunnable, DURATION_PER_FRAME);
    	}

    	public void stop() {
    		pulsulationAnimationHandler.removeCallbacks(pulsulationAnimationRunnable);
    	}
    }


}
