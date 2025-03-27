package gilianmarques.dev.musicplayer.activities.sync_lyrics;

import android.animation.ValueAnimator;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.playing_now.lyrics.MyLayoutManager;
import gilianmarques.dev.musicplayer.lyrics.LyricsUtils;
import gilianmarques.dev.musicplayer.lyrics.models.Lyric;
import gilianmarques.dev.musicplayer.lyrics.models.Phrase;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;

public class SyncLyric extends MyActivity implements View.OnTouchListener {
    private Lyric lyric;
    private RecyclerView mRecyclerView;
    private SyncAdapter mAdapter;
    private ImageView ivDown;
    private int currPhraseIndex;
    private long currTrackProgressMillis;
    private LinearLayoutManager lManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_lyric);
        Track track = MusicService.binder.getPlayer().getCurrentTrack();
        LyricsUtils.getLyric(track, this, new LyricsUtils.Callback() {
            @Override public void done(Lyric lyric) {
                SyncLyric.this.lyric = lyric;

                if (lyric == null) {
                    Runnable mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    };
                    new Handler().postDelayed(mRunnable, 1000);
                }
                init();
            }
        });
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    private void init() {
        ivDown = findViewById(R.id.ivDown);
        lManager = new MyLayoutManager(this, 100);
        mRecyclerView = findViewById(R.id.scroll);
        mRecyclerView.setLayoutManager(lManager);
        mRecyclerView.setHasFixedSize(true);
        ArrayList<Phrase> phrases = lyric.getOriginal();
        mAdapter = new SyncAdapter(phrases, this);
        mRecyclerView.setAdapter(mAdapter);
        ivDown.setOnTouchListener(this);
        playerProgressListener.startReceiving();
        MusicService.binder.getPlayer().restartTrack();
    }


    @Override public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            lyric.getOriginal().get(currPhraseIndex).setStart(currTrackProgressMillis);
            mRecyclerView.smoothScrollToPosition(currPhraseIndex + 1);
            focus(true, (TextView) lManager.findViewByPosition(currPhraseIndex+1));
            Log.d(App.myFuckingUniqueTAG + "SyncLyric", "onTouch: ACTION_DOWN" + " ph: " + lyric.getOriginal().get(currPhraseIndex).getText() + " | " + lyric.getOriginal().get(currPhraseIndex).getStart() + " | " + lyric.getOriginal().get(currPhraseIndex).getEnd());
        } else if (action == MotionEvent.ACTION_UP) {
            lyric.getOriginal().get(currPhraseIndex).setEnd(currTrackProgressMillis);
            focus(false, (TextView) lManager.findViewByPosition(currPhraseIndex+1));

            Log.d(App.myFuckingUniqueTAG + "SyncLyric", "onTouch: ACTION_UP" + " ph: " + lyric.getOriginal().get(currPhraseIndex).getText() + " | " + lyric.getOriginal().get(currPhraseIndex).getStart() + " | " + lyric.getOriginal().get(currPhraseIndex).getEnd());
            if (currPhraseIndex + 1 == mAdapter.getItemCount() - 1) saveLyric();
            else {
                currPhraseIndex++;

            }
        }


        return true;
    }


    private PlayerProgressListener playerProgressListener = new PlayerProgressListener(getClass().getSimpleName()) {
        @Override protected void trackEnded() {

            saveLyric();
            super.trackEnded();
        }

        @Override
        protected void progressChanged(float percent, String timer, long millis) {
            currTrackProgressMillis = millis;
            super.progressChanged(percent, timer, millis);
        }
    };

    @Override protected void onStop() {
        playerProgressListener.stopReceiving();
        super.onStop();
    }

    private void saveLyric() {
        lyric.setOriginalSynced(true);

        if (new LyricsUtils().writeLyric(MusicService.binder.getPlayer().getCurrentTrack().getId(), lyric))
            Toasty.success(this, "Letra sincronizada com sucesso!", Toasty.LENGTH_LONG).show();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        new Handler().postDelayed(mRunnable, 500);

    }


    private void focus(boolean focused, final TextView currView) {
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

        //  pulse

        float a = (focused ? 1 : 1.09f), b = (focused ? 1.09f : 1f);

        Animation anim = new ScaleAnimation(
                a, b, // Start and end values for the X axis scaling
                a, b, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(focused ? 300 : 600);
        anim.setInterpolator(new FastOutSlowInInterpolator());
       currView.startAnimation(anim);
    }
}
