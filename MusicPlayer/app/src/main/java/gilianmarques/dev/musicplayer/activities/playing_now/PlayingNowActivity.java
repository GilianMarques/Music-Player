package gilianmarques.dev.musicplayer.activities.playing_now;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.activities.library.LibraryActivity;
import gilianmarques.dev.musicplayer.activities.playing_now.lyrics.LyricsFragment;
import gilianmarques.dev.musicplayer.lyrics.LyricsUtils;
import gilianmarques.dev.musicplayer.lyrics.models.Lyric;
import gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats.PlayerProgressListener;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicServiceStarter;
import gilianmarques.dev.musicplayer.models.Track;

/**
 * Criado por Gilian Marques em 01/05/2018 as 17:19:38.
 */

// TODO: 01/05/2018 visitar aqui para animar https://material.io/guidelines/motion/material-motion.html#material-motion-how-does-material-move

public class PlayingNowActivity extends MyActivity {
    private PlayingNowFragment nowFragment;
    private LyricsFragment lrcFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // pode acontecer qd n sei pq o serviço fecha mas a notificação fica aberta lá
        super.onCreate(savedInstanceState);

        if (MusicService.binder == null) {
            initService();
        } else init();


    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);

        if (mWindow != null) {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

            // it doesnt matter the theme, in PlayingNowActivity i cant have these flags
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);


        }
    }

    private void init() {
        setContentView(R.layout.activity_paying_now);
        final View rootView = findViewById(android.R.id.content);
        rootView.setVisibility(View.INVISIBLE);
        nowFragment = new PlayingNowFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, nowFragment).commit();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(PlayingNowActivity.this, R.anim.fade_in);
                animation.setDuration(300);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {
                        rootView.setVisibility(View.VISIBLE);

                    }

                    @Override public void onAnimationEnd(Animation animation) {

                    }

                    @Override public void onAnimationRepeat(Animation animation) {

                    }
                });
                rootView.startAnimation(animation);
            }
        };
        new Handler().postDelayed(mRunnable, 600);
    }


    private void initService() {
        Toasty.info(this, getString(R.string.Iniciando_serviço), Toast.LENGTH_SHORT).show();
        new MusicServiceStarter() {
            @Override public void onServiceStarted() {
                init();
                super.onServiceStarted();
            }
        }.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        nowFragment.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nowFragment != null) nowFragment.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (lrcFragment != null) {
            playerListener.stopReceiving();
            getSupportFragmentManager().beginTransaction().remove(lrcFragment).commit();
            lrcFragment = null;
            return;
        }

        if (nowFragment != null && nowFragment.isShowLyrics()) {
            nowFragment.toogleLrcs(true);
            return;
        }


        super.onBackPressed();
        // reabre a activity se estiver aberta ao nves de criar uma nova
        startActivity(new Intent(this, LibraryActivity.class).putExtra("show_dialog", false).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_playing_now, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.play_list:
                nowFragment.showPlaylist();
                break;

            case R.id.show_lrcs:
                nowFragment.toogleLrcs(true);
                break;

        }


        return super.onOptionsItemSelected(item);
    }


    public void showFullLyrics() {
        playerListener.startReceiving();
        if (lrcFragment == null) lrcFragment = LyricsFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.lrcContainer, lrcFragment).commit();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                View root;
                do {
                    root = lrcFragment.getRootView();
                } while (root == null);
                final View finalRoot = root;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        finalRoot.setPadding(0, MyActivity.statusBarHeight, 0, 0);
                        finalRoot.findViewById(R.id.syncMask).setPadding(0, 0, 0, MyActivity.navigationHeight);
                    }
                });
            }
        };
        new Thread(mRunnable).start();
    }


    private final PlayerProgressListener playerListener = new PlayerProgressListener(getClass().getSimpleName()) {
        @Override protected void trackChanged(Track newTrack) {
            super.trackChanged(newTrack);
            if (lrcFragment != null)
                LyricsUtils.getLyric(newTrack, PlayingNowActivity.this, new LyricsUtils.Callback() {
                    @Override public void done(Lyric lyric) {
                        if (lrcFragment != null) lrcFragment.setLyric(lyric);
                    }
                });
        }
    };
}
