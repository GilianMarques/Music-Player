package gilianmarques.dev.musicplayer.mediaplayer.service_and_related;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.Objects;

import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.utils.App;

import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;

/**
 * Criado por Gilian Marques em 22/05/2018 as 21:22:37.
 */
public class MyReceiver extends BroadcastReceiver {
    private Intent mIntent;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        mIntent = intent;
        mContext = context;
        Log.d("MyReceiver", "onReceive: " + intent.getAction() + " " + intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT));

        switch (Objects.requireNonNull(intent.getAction())) {
            case Intent.ACTION_MEDIA_BUTTON:
                handleMediaButton();

                break;
            case Intent.ACTION_BOOT_COMPLETED:
                handleBootCompleted();

                break;
            case Intent.ACTION_USER_PRESENT:
                handleUserPresent();

                break;
            case Intent.ACTION_HEADSET_PLUG:
                verifyHeadsetPlugged();

                break;

        }
    }

    private void handleUserPresent() {
        App.binder.get().registerReceiver(this,
                                          new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    private void verifyHeadsetPlugged() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        Intent iStatus = mContext.registerReceiver(null, iFilter);
        if (iStatus == null) return;
        boolean isConnected = iStatus.getIntExtra("state", 0) == 1;

        if (Prefs.getBoolean(c.auto_start_when_earphone_plug_in, true) && isConnected && MusicService.binder == null) {
            new MusicServiceStarter().start();
            if (Prefs.getBoolean(c.start_playing_when_earphone_plug_in, false))
                new MusicServiceStarter() {
                    @Override public void onServiceStarted() {
                        MusicService.binder.getPlayer().toogle();
                        super.onServiceStarted();
                    }
                }.start();

        }
    }

    private void handleBootCompleted() {
    }

    private void handleMediaButton() {


        KeyEvent mEvent = mIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        MusicService.BindService binder = MusicService.binder;
        MusicService musicService = null;
        if (binder != null) {
            musicService = binder.getService();
        }


        if (mEvent.getKeyCode() == MusicService.KILL_APP_IF_DISMISS_NOTIFICATION || mEvent.getKeyCode() == KEYCODE_MEDIA_STOP) {
            if (musicService != null) {
                musicService.stopSelf();
            }

        } else if (musicService == null) {

            new MusicServiceStarter() {
                @Override public void onServiceStarted() {
                    MusicService.binder.getPlayer().toogle();
                    super.onServiceStarted();
                }
            }.start();


        } else {
              /* analiza o o evento na intent e chama o CallbackI na MyMediaSession
            se foi um botão de midia clicado que disparou este broadcastReceiver*/
            MusicService.binder.getService().handleMediButton(mIntent);
        }

        //  Log.d("MyReceiver", "onReceive: new command received " + mIntent.getAction() + " : " + mEvent.getKeyCode());

    }

}
