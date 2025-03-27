package gilianmarques.dev.musicplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.pixplicity.easyprefs.library.Prefs;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.album_details.AlbumDetailsActivity;
import gilianmarques.dev.musicplayer.activities.artist_details.ArtistDetailsActivity;
import gilianmarques.dev.musicplayer.activities.library.LibraryActivity;
import gilianmarques.dev.musicplayer.activities.library.fragment_playlists.ViewPlaylistActivity;
import gilianmarques.dev.musicplayer.activities.playing_now.PlayingNowActivity;
import gilianmarques.dev.musicplayer.activities.playing_now.PlayingNowQueue;
import gilianmarques.dev.musicplayer.activities.sync_lyrics.SyncLyric;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.tag_editor.Tagger;
import gilianmarques.dev.musicplayer.tag_editor.TaggerAlbuns;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static gilianmarques.dev.musicplayer.utils.App.CHANGE_THEME;
import static gilianmarques.dev.musicplayer.utils.App.DIE_ACTIVITIES_DIE;

/**
 * Criado por Gilian Marques em 01/05/2018 as 19:23:17.
 */
public abstract class MyActivity extends AppCompatActivity {

    public static boolean darkTheme;
    public static int statusBarHeight = -1;
    public static int navigationHeight = -1;
    private String thisClassName;
    private int backPressedTimes;
    private Window mWindow;
    private View decorView;
    private BroadcastReceiver killerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Log.d(App.myFuckingUniqueTAG + "MyActivity", "onReceive: " + intent.getAction());
            if (CHANGE_THEME.equals(intent.getAction())) {
                recreate();
            } else if (DIE_ACTIVITIES_DIE.equals(intent.getAction())) {
                finish();
            }
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected  void applyTheme(Window mWindow, View decorView){}

    protected void onCreate(Bundle savedInstanceState) {
        App.binder.get().onForeground(this);
        setTheme();
        overridePendingTransition();
        changeWindowFeaturesBySdk();
        changeWindowFeatures();

        super.onCreate(savedInstanceState);

        backPressedTimes = 0;


        if (statusBarHeight == -1) {
            // adicionando padding e margins ao layout de acordo com o status e navigation bar do dispositivo
            int statusBarId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            int navigationId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");

            statusBarHeight = getResources().getDimensionPixelSize(statusBarId);
            navigationHeight = getResources().getDimensionPixelSize(navigationId);

        }


        Utils.screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels + navigationHeight;
        Utils.screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        LocalBroadcastManager.getInstance(this).registerReceiver(killerReceiver, new IntentFilter(App.DIE_ACTIVITIES_DIE));
        LocalBroadcastManager.getInstance(this).registerReceiver(killerReceiver, new IntentFilter(CHANGE_THEME));
        Log.d(App.myFuckingUniqueTAG + "MyActivity", "onCreate: darkTheme? " + darkTheme);
    }

    protected void setTheme() {

        darkTheme = Prefs.getBoolean(c.dark_theme, true);
        thisClassName = this.getClass().getName();

        if (thisClassName.equals(ViewPlaylistActivity.class.getName())) {
            setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        } else if (thisClassName.equals(SyncLyric.class.getName())) {
            setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        } else if (thisClassName.equals(PlayingNowActivity.class.getName())) {
            setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        } else if (thisClassName.equals(PlayingNowQueue.class.getName())) {
            setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        } else if (thisClassName.equals(PlayExternalAudioActivity.class.getName())) {
            setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        } else if (thisClassName.equals(Tagger.class.getName())) {
            setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        } else if (thisClassName.equals(TaggerAlbuns.class.getName())) {
            setTheme(darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        } else setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);

    }

    private void overridePendingTransition() {
        if (thisClassName.equals(TaggerAlbuns.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else if (thisClassName.equals(ViewPlaylistActivity.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else if (thisClassName.equals(SettingsActivity.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else if (thisClassName.equals(PlayingNowQueue.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else if (thisClassName.equals(PlayExternalAudioActivity.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else overridePendingTransition(R.anim.fade_in_down, R.anim.fade_out_up);

    }

    /**
     * this method is used to override the multiples styles.xml existing in this ptoject
     */
    private void changeWindowFeaturesBySdk() {
        mWindow = getWindow();
        decorView = mWindow.getDecorView();

        // in light theme
        if (!darkTheme) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) // makes status bar icons darker
            {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                mWindow.setStatusBarColor(Utils.fetchColorFromReference(android.R.attr.windowBackground));
            } else  // below sdk 23 cant get status bar icons darker, so make background ltgray
                mWindow.setStatusBarColor(ContextCompat.getColor(this, R.color.stat_nav_bar_light_sdk23_below));


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)  // makes nav bar buttons darker
            {
                mWindow.setNavigationBarColor(Utils.fetchColorFromReference(android.R.attr.windowBackground));
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

            } else  // below sdk 26 cant get nav bar icons darker, so make background ltgray
                mWindow.setNavigationBarColor(ContextCompat.getColor(this, R.color.stat_nav_bar_light_sdk23_below));
        }

    }

    private void changeWindowFeatures() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (thisClassName.equals(ViewPlaylistActivity.class.getName())) {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        //////////////////////////////////////////////////////////////////
        else if (thisClassName.equals(ArtistDetailsActivity.class.getName())) {
            mWindow.setStatusBarColor(0);
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //////////////////////////////////////////////////////////////////
        else if (thisClassName.equals(LibraryActivity.class.getName())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                mWindow.setStatusBarColor(0);
            }
        }
        //////////////////////////////////////////////////////////////////
        else if (thisClassName.equals(AlbumDetailsActivity.class.getName())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mWindow.setStatusBarColor(0);
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            //status bar must have white icons
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        //////////////////////////////////////////////////////////////////
        else if (thisClassName.equals(PlayingNowActivity.class.getName())) {

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

        //////////////////////////////////////////////////////////////////

        else if (thisClassName.equals(PlayingNowQueue.class.getName())) {

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mWindow.addFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);


        } else if (thisClassName.equals(TaggerAlbuns.class.getName())) {
            mWindow.setStatusBarColor(0);
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            //status bar must have white icons
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        } else if (thisClassName.equals(Tagger.class.getName())) {
            mWindow.setStatusBarColor(0);
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            //status bar must have white icons
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressedTimes = 0;
        /*precisa ficar aqui para que a instancia em Utils seja da activity ques esta na tela*/
        App.binder.get().onForeground(this);
    }

    @Override protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(killerReceiver);
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (backPressedTimes == 0) {
            super.onBackPressed();
            Log.d("MyActivity", "onBackPressed: Backing");
        }
        backPressedTimes++;

        if (thisClassName.equals(ViewPlaylistActivity.class.getName())) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        if (thisClassName.equals(PlayingNowQueue.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        if (thisClassName.equals(PlayExternalAudioActivity.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        if (thisClassName.equals(SettingsActivity.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        if (thisClassName.equals(TaggerAlbuns.class.getName())) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else overridePendingTransition(R.anim.fade_in_down, R.anim.fade_out_up);


    }

}
