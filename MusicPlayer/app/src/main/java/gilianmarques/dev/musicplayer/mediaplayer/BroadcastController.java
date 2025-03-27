package gilianmarques.dev.musicplayer.mediaplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.media.session.MediaButtonReceiver;
import android.util.Log;
import android.view.KeyEvent;

import com.pixplicity.easyprefs.library.Prefs;

import java.util.Objects;

import gilianmarques.dev.musicplayer.persistence.Keys;
import gilianmarques.dev.musicplayer.utils.App;

import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;

/**
 * Criado por Gilian Marques em 22/05/2018 as 21:22:37.
 */
public class BroadcastController extends BroadcastReceiver {
    private Intent mIntent;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        mIntent = intent;
        mContext = context;

        Log.d("BroadcastController", "onReceive: " + intent.getAction() + " " + intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT));

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

        if (Prefs.getBoolean(Keys.auto_start_when_earphone_plug_in, true) && isConnected) {
            // TODO: 02/01/2020 planejar isso

        }
    }

    private void handleBootCompleted() {
    }

    private void handleMediaButton() {


        KeyEvent mEvent = mIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);


        if (mEvent.getKeyCode() == MyMediaSession.KILL_APP_IF_DISMISS_NOTIFICATION || mEvent.getKeyCode() == KEYCODE_MEDIA_STOP) {
            MusicPlayer.getInstance().stop();
        } else {
              /* analiza o o evento na intent e chama o CallbackI na MyMediaSession
            se foi um botão de midia clicado que disparou este broadcastReceiver*/
            MediaButtonReceiver.handleIntent(MusicPlayer.getInstance().getMediaSession(), mIntent);

        }

        //  Log.d("BroadcastController", "onReceive: new command received " + mIntent.getAction() + " : " + mEvent.getKeyCode());

    }

}
