package gilianmarques.dev.musicplayer.movement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques
 * Segunda-feira, 16 de Julho de 2018  as 21:38:46.
 *
 * @Since 0.1 Beta
 */
public class DragUpDown implements View.OnTouchListener {
    private final float bottomMov;
    private final int topMov;
    private float initialTouchY, initialPositionYofView;
    private final float minYtoClose;
    private View targertView;
    private final callback callback;

    //Physics
    private long initialClickTimeStamp;
    // End physics

    /**
     * @param bottomPos the min movement of bottom. The place where the bottom of targetView
     *                  will be sit down. Use the height of screen to sit targetview on bottom of it
     * @param maxTopMov max movement of targertView's top. When user drag the view it will not rise above the value of this variable
     *                  Use the initial Y value of targertView.
     * @param callback  callbacks
     */
    public DragUpDown(float bottomPos, int maxTopMov, DragUpDown.callback callback) {
        this.bottomMov = bottomPos;
        this.topMov = maxTopMov;
        this.callback = callback;
        minYtoClose = (bottomPos - maxTopMov) / 2; // have to move 50% to close
    }

    @Override public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        maybeInitVars(v);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleDownAction(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveAction(event);
                break;
            case MotionEvent.ACTION_UP:
                handleUpAction();
                break;

        }


        return true;
    }

    private void maybeInitVars(View v) {
        if (targertView == null) {
            targertView = v;
            initialPositionYofView = targertView.getY();
        }
    }

    private void handleMoveAction(MotionEvent event) {
        float movement = event.getY() - initialTouchY;
        float finalMov = targertView.getY() + movement;

        if (finalMov <= bottomMov & finalMov >= topMov) {
            targertView.setY(finalMov);
        }


    }

    private void handleUpAction() {

        int dragDistance = (int) (targertView.getY() - initialPositionYofView) + 1;// avoid divide per 0 error

        int animDur = (int) getAnimDur(dragDistance);
        animDur = animDur > 800 ? 800 : animDur;

        final boolean show = (dragDistance <= minYtoClose);
        ValueAnimator anim;

        if (show)
            anim = ValueAnimator.ofFloat(targertView.getY(), topMov);
        else
            anim = ValueAnimator.ofFloat(targertView.getY(), bottomMov);

        anim.setInterpolator(new FastOutSlowInInterpolator());

        anim.setDuration(animDur);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                targertView.setY((Float) animation.getAnimatedValue());
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (!show) callback.onTargetDismissed();
                super.onAnimationEnd(animation);
            }

            @Override public void onAnimationStart(Animator animation) {
                if (!show) callback.onTargetDismissing();
                super.onAnimationStart(animation);
            }
        });
        anim.start();


        initialTouchY = 0;


    }

    private float getAnimDur(int dragDistance) {
        if (dragDistance == 0) dragDistance++;
        int dragDur = (int) (System.currentTimeMillis() - initialClickTimeStamp);
        float dragVelocityPerPixels = new BigDecimal(dragDur).divide(new BigDecimal(dragDistance), 2, RoundingMode.HALF_EVEN).floatValue();
        float distanceRemainingInPixels = (dragDistance <= minYtoClose) ? targertView.getY() - topMov : bottomMov - targertView.getY();
        int animDur = (int) (dragVelocityPerPixels * distanceRemainingInPixels);
        return (animDur < 0 ? animDur * -1 : animDur);
    }

    private void handleDownAction(MotionEvent event) {
        initialTouchY = event.getY();
        initialClickTimeStamp = System.currentTimeMillis();
    }

    public static class callback {
        public void onTargetDismissed() {
        }

        public void onTargetDismissing() {
        }
    }
}
