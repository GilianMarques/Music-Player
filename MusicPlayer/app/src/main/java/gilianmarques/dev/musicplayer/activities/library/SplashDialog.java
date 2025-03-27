package gilianmarques.dev.musicplayer.activities.library;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.activities.MyActivity;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicServiceStarter;
import gilianmarques.dev.musicplayer.persistence.UIRealm;
import gilianmarques.dev.musicplayer.spotify.helpers.AppTokenHelper;
import gilianmarques.dev.musicplayer.spotify.objects.WebSpotifyCache;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Criado por Gilian Marques
 * Sábado, 11 de Maio de 2019  as 19:49:52.
 */
public class SplashDialog extends AlertDialog implements DialogInterface.OnShowListener {


    private Activity mActivity;
    private Callback callback;
    private View splash;
    private long showTime;
    private boolean canDismiss = false;

    SplashDialog(@NonNull LibraryActivity mActivity, Callback callback) {
        super(mActivity, MyActivity.darkTheme ? R.style.AppThemeDark_Translucent_Full : R.style.AppThemeLight_Translucent_Full);
        this.mActivity = mActivity;
        this.callback = callback;
        splash = mActivity.getLayoutInflater().inflate(R.layout.splash_layout, null);
        setCancelable(false);
        setView(splash);
        setOnShowListener(this);
    }


    public void show(boolean fromLauncher) {
        if (!fromLauncher) splash.setVisibility(View.INVISIBLE);
        showTime = System.currentTimeMillis();
        Window window = getWindow();
        if (window != null) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        show();

    }

    @Override public void onShow(final DialogInterface dialog) {
        Log.d(App.myFuckingUniqueTAG + "SplashDialog", "onShow: ");

        AppTokenHelper.init(null);
        final View parent = splash.findViewById(R.id.parentLayout);
        parent.post(new Runnable() {
            @Override public void run() {
                float bottom = parent.getBottom();// represents the full size of screen
                float screen = Utils.screenHeight;
                Utils.hasNavBar = bottom < screen;
                canDismiss = true;
                Log.d(App.myFuckingUniqueTAG + "SplashDialog", "run: " + bottom + " > " + screen);
            }
        });

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        String rationale = mActivity.getString(R.string.Sem_permissao_de_leitura_nao_e_possivel);
        Permissions.Options options = new Permissions.Options().setRationaleDialogTitle(mActivity.getString(R.string.Aviso));

        Permissions.check(mActivity/*context*/, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                if (MusicService.binder == null) new MusicServiceStarter() {
                    @Override public void onServiceStarted() {

                        Log.d(App.myFuckingUniqueTAG + "SplashDialog", "onServiceStarted: ");
                        callback.permissionGranted();
                        super.onServiceStarted();
                    }
                }.start();
                else {
                    showTime = -1;
                    callback.permissionGranted();
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                onShow(dialog);
            }
        });


    }


    private void checkSpotifyToken() {

        //clear web api cache
        UIRealm.get().executeTransactionAsync(new Realm.Transaction() {
            @Override public void execute(Realm realm) {

                RealmResults<WebSpotifyCache> cache = Realm.getDefaultInstance().where(WebSpotifyCache.class).findAll();
                for (WebSpotifyCache c : cache)
                    if (c.isExpired()) c.deleteFromRealm();
            }
        });

        AppTokenHelper.init(null);

        if (AppTokenHelper.mAppAuth == null||AppTokenHelper.mAppAuth.isExpired()) {
               //must get kay any way... if (Utils.canConnect())
                    AppTokenHelper.refreshToken(new AppTokenHelper.CallbackI() {
                        @Override public void done(final boolean success) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    if (BuildConfig.DEBUG)
                                        Toasty.info(App.binder.get(), success ? "New spotify token acquired" : "Fail fetching new token from spotify", 2000).show();
                                }
                            });

                        }
                    });


            dismiss();

        } else dismiss();

    }


    @Override public void dismiss() {
        Log.d(App.myFuckingUniqueTAG + "SplashDialog", "dismiss: ");
        // view invisible means dialog should not appear and was called only to call activity methods (activity was started from launcher)
        if (splash.getVisibility() == View.INVISIBLE) SplashDialog.super.dismiss();

            /* showTime ==-1 means music service is already started when dialog was shown, so app
             is already open and dialog should not apper. (activity was started from launcher and service is already running)*/
        else if (showTime == -1) {
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    AlphaAnimation anim = new AlphaAnimation(1, 0);
                    anim.setDuration(200);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override public void onAnimationStart(Animation animation) {

                        }

                        @Override public void onAnimationEnd(Animation animation) {
                            SplashDialog.super.dismiss();
                        }

                        @Override public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    anim.setFillAfter(true);
                    splash.startAnimation(anim);

                }
            }, 600);
        }

        // minimum of 2 seconds until show
        else if (canDismiss && System.currentTimeMillis() - showTime > 2000) {
            AlphaAnimation anim = new AlphaAnimation(1, 0);
            anim.setDuration(200);
            anim.setFillAfter(true);
            splash.startAnimation(anim);

            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    SplashDialog.super.dismiss();
                }
            }, 300);
        } else new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                dismiss();
            }
        }, 150);

    }

    /**
     * Call from activity after load all stuff
     */
    void continueToLoading() {
        checkSpotifyToken();
    }


    interface Callback {
        void permissionGranted();
    }

}
