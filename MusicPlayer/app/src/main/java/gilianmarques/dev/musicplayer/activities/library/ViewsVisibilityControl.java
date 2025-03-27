package gilianmarques.dev.musicplayer.activities.library;

import android.animation.ObjectAnimator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.arlib.floatingsearchview.FloatingSearchView;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.utils.App;

/**
 * Criado por Gilian Marques
 * Domingo, 19 de Maio de 2019  as 18:39:22.
 * <p>
 * Controls playingNowView searchView and BottomBar visibility
 */
public class ViewsVisibilityControl {


    private FastOutSlowInInterpolator mInterpolator = new FastOutSlowInInterpolator();
    private int dur = 350;

    private float rootYInit;
    private float rootYHiddenBottomBar;
    private float rootYHiddenPNowView;
    private float playingNowViewSize;
    private float bottomBarSize;
    private float searchViewInitY;

    private View rootView;
    private FloatingSearchView mSearchView;

    private boolean isBbarHidden;
    private boolean isPnowViewHidden;
    private boolean isSviewHidden;
    private int initCount = 0;

    public ViewsVisibilityControl(final View include, final FloatingSearchView mSearchView) {
        this.rootView = include;
        this.mSearchView = mSearchView;
        final View playingNowView = include.findViewById(R.id.PlayingNowView);
        final View bottomBar = include.findViewById(R.id.bottom_navigation);


        rootView.post(new Runnable() {
            @Override public void run() {
                rootYInit = rootView.getY();
                initCount++;
                //
                playingNowView.post(new Runnable() {
                    @Override public void run() {
                        playingNowViewSize = playingNowView.getMeasuredHeight();
                        initCount++;
                        //
                        bottomBar.post(new Runnable() {
                            @Override public void run() {
                                bottomBarSize = bottomBar.getMeasuredHeight();
                                initCount++;
                                //
                                mSearchView.post(new Runnable() {
                                    @Override public void run() {
                                        searchViewInitY = mSearchView.getY();
                                        initCount++;

                                        rootYHiddenBottomBar = rootYInit + bottomBarSize;
                                        rootYHiddenPNowView = rootView.getBottom();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });


    }

    public void hideBottomBar() {
        if (isBbarHidden) return;
        else isBbarHidden = true;

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "y", rootView.getY(), rootYHiddenBottomBar);
        anim.setInterpolator(mInterpolator);
        anim.setDuration(dur);
        anim.start();

        //Log.d(App.myFuckingUniqueTAG + "ViewsVisibilityControl", "hideBottomBar: ");
    }

    public void showBottomBar() {
        if (!isBbarHidden) return;
        else {
            isBbarHidden = false;
            isPnowViewHidden = false;
        }

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "y", rootView.getY(), rootYInit);
        anim.setInterpolator(mInterpolator);
        anim.setDuration(dur);
        anim.start();
        //Log.d(App.myFuckingUniqueTAG + "ViewsVisibilityControl", "showBottomBar: ");
    }

    public void showPlayingNowView() {
        if (!isPnowViewHidden) return;
        else isPnowViewHidden = false;

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "y", rootView.getY(), rootYHiddenBottomBar);
        anim.setInterpolator(mInterpolator);
        anim.setDuration(dur);
        anim.start();
        //Log.d(App.myFuckingUniqueTAG + "ViewsVisibilityControl", "showPlayingNowView: ");
    }

    public void hidePlayingNowView() {

        if (isPnowViewHidden) return;
        else {
            isPnowViewHidden = true;
            isBbarHidden = true;
        }

        //  Log.d(App.myFuckingUniqueTAG + "ViewsVisibilityControl", "hidePlayingNowView: ");

        ObjectAnimator anim = ObjectAnimator.ofFloat(rootView, "y", rootView.getY(),rootYHiddenPNowView);
        anim.setInterpolator(mInterpolator);
        anim.setDuration(dur);
        anim.start();
    }

    public void showSearchView() {
        if (!isSviewHidden) return;
        else isSviewHidden = false;

        Log.d(App.myFuckingUniqueTAG + "ViewsVisibilityControl", "showSearchView: " + mSearchView.getY() + ">" + searchViewInitY);

        ObjectAnimator anim = ObjectAnimator.ofFloat(mSearchView, "y", mSearchView.getY(), searchViewInitY);
        anim.setInterpolator(mInterpolator);
        anim.setDuration(dur);
        anim.start();

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setInterpolator(mInterpolator);
        alphaAnimation.setDuration(dur);
        alphaAnimation.setFillAfter(true);
        mSearchView.startAnimation(alphaAnimation);
    }

    public void hideSearchView() {

        if (isSviewHidden) return;
        else isSviewHidden = true;
        Log.d(App.myFuckingUniqueTAG + "ViewsVisibilityControl", "hideSearchView: " + mSearchView.getY() + ">" + mSearchView.getY() * -1);

        ObjectAnimator anim = ObjectAnimator.ofFloat(mSearchView, "y", mSearchView.getY(), -100);
        anim.setInterpolator(mInterpolator);
        anim.setDuration(dur);
        anim.start();

        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setInterpolator(mInterpolator);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setDuration(dur);
        mSearchView.startAnimation(alphaAnimation);
    }


}
