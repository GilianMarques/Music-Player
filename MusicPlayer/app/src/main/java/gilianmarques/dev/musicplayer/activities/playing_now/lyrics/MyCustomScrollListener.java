package gilianmarques.dev.musicplayer.activities.playing_now.lyrics;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import java.util.Objects;

/**
 * Criado por Gilian Marques
 * Sábado, 04 de Maio de 2019  as 12:44:21.
 * <p>
 * This implementation focus on view at center of recyclerview making it bigger and yellow
 */
public class MyCustomScrollListener extends RecyclerView.OnScrollListener {

    private TextView focusedView;
    private TextView lastFocusedView;


    @Override public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        lastFocusedView = focusedView;
        focusedView = (TextView) recyclerView.findChildViewUnder(recyclerView.getMeasuredWidth() / 2, recyclerView.getMeasuredHeight() / 2);
        if (!Objects.equals(lastFocusedView, focusedView)) scroll();
        super.onScrolled(recyclerView, dx, dy);
    }

    private void scroll() {
        changeColorAlpha(false, lastFocusedView);
        changeColorAlpha(true, focusedView);
        scaleView(lastFocusedView, false);
        scaleView(focusedView, true);


    }

    private void changeColorAlpha(boolean focused, final TextView currView) {
        if (currView == null) return;
        ValueAnimator animator;

        int focusedColor = Color.YELLOW;
        if (focused) animator = ValueAnimator.ofArgb(Color.WHITE, focusedColor);
        else animator = ValueAnimator.ofArgb(focusedColor, Color.WHITE);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator animation) {
                currView.setTextColor((Integer) animation.getAnimatedValue());
            }
        });
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(focused ? 300 : 900);
        animator.start();
    }

    private void scaleView(View v, boolean increase/*/decrease*/) {
        if (v == null) return;

        float a = (increase ? 1 : 1.09f), b = (increase ? 1.09f : 1f);

        Animation anim = new ScaleAnimation(
                a, b, // Start and end values for the X axis scaling
                a, b, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(increase ? 300 : 600);
        anim.setInterpolator(new FastOutSlowInInterpolator());
        v.startAnimation(anim);
    }

}
