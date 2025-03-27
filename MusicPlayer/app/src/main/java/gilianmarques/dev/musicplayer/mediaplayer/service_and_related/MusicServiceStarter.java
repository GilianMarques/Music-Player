package gilianmarques.dev.musicplayer.mediaplayer.service_and_related;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Abril de 2019  as 17:41:50.
 */
public class MusicServiceStarter extends BroadcastReceiver {
    public static final String SERVICE_STARTED = BuildConfig.APPLICATION_ID.concat("service_media_session_and_player_stated_SUCCESSFULLY");


    @Override public final void onReceive(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(App.binder.get()).unregisterReceiver(this);
        onServiceStarted();
    }


    /**
     * This method is called only once
     */
    public void onServiceStarted() {
    }

    public void start() {
        LocalBroadcastManager.getInstance(App.binder.get()).registerReceiver(this, new IntentFilter(SERVICE_STARTED));
        Utils.startService(App.binder.get(), MusicService.class);

    }
}
