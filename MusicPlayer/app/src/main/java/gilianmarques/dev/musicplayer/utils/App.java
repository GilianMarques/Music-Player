package gilianmarques.dev.musicplayer.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;

import com.bugsnag.android.Bugsnag;
import com.facebook.stetho.Stetho;
import com.pixplicity.easyprefs.library.Prefs;
import com.wonderkiln.blurkit.BlurKit;

import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.library.LibraryActivity;
import gilianmarques.dev.musicplayer.activities.playing_now.PlayingNowActivity;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Criado por Gilian Marques em 01/05/2018 as 17:19:38.
 */
public class App extends Application {

    public static final String DIE_ACTIVITIES_DIE = "Time_to_die!";
    public static final String REFRESH_LIB = "shuaaaaa!";
    public static final String CHANGE_THEME = "skadush!";
    public static BindApplication binder;
    public static String myFuckingUniqueTAG = "USUCK: ";
    private Activity onScreenActivity;

    public void reboot() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DIE_ACTIVITIES_DIE));
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                getApplicationContext().startActivity(new Intent(App.this, LibraryActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        };
        new Handler().postDelayed(mRunnable, 500);

    }

    @Override
    public void onCreate() {
        init();
        super.onCreate();
    }

    private void init() {
        UIRealm.init(this);
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                              .setDefaultFontPath("fonts/Product Sans Regular.ttf")
                                              .setFontAttrId(R.attr.fontPath)
                                              .build()
        );

        BlurKit.init(this);


        Bugsnag.init(this);

        // TODO: 24/05/2019 error screen
      /*  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                handleUncaughtErrors(thread, throwable);
            }
        });*/

        binder = appBinder;


        if (BuildConfig.DEBUG) Stetho.initialize(Stetho.newInitializerBuilder(this)
                                                         .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                                                         .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))

                                                         .build());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


    }


    public void onForeground(Activity currentActivity) {
        onScreenActivity = currentActivity;
    }

    public Activity getActivity() {
        return onScreenActivity;
    }

    public void finishActivities() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DIE_ACTIVITIES_DIE));
    }


    private final BindApplication appBinder = new BindApplication() {
        @Override
        public App get() {
            return App.this;
        }
    };

    public void changeTheme() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CHANGE_THEME));

    }

    public void goToPlayingNow(Context activity) {
        Intent playingNowIntent = new Intent(activity, PlayingNowActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(playingNowIntent);
    }

    public void refreshLibrary() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DIE_ACTIVITIES_DIE));

    }

    public interface BindApplication {
        App get();
    }


    public static void runOnUiThread(Runnable runnable) {
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(runnable);
    }

}


/*
* Here are the details of your new API account.

Application name	MusicPlayer
API key	44654a85aad069d45944a0b043ebb2f2
Shared secret	e3b48eaa97c3b069f7cdfcf58d9de617
Registered to	GilianMarques

*/

/*
* Here are the details of your new API account.

Application name	MusicPlayer
API key	d54d471945d645f432427efc2162aab7
Shared secret	45fba90baa06834437a8dd7edf4f7344
Registered to	GilianMarques2

https://smartmockups.com/mockup/digital_psdg_5

*/