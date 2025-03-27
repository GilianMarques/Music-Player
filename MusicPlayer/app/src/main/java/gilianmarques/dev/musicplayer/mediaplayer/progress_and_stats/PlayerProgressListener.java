package gilianmarques.dev.musicplayer.mediaplayer.progress_and_stats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Objects;

import gilianmarques.dev.musicplayer.mediaplayer.structure.Const;
import gilianmarques.dev.musicplayer.models.Track;
import gilianmarques.dev.musicplayer.utils.App;
import gilianmarques.dev.musicplayer.utils.IdUtils;

/**
 * Criado por Gilian Marques em 24/05/2018 as 19:12:17.
 */
public class PlayerProgressListener extends BroadcastReceiver {
    private final LocalBroadcastManager broadcastManager;
    private final String LISTENER_ID;
    public static final String BROADCASTED_FOR_ALL = "bfa!";
    private boolean receiveing = false;
    private String className;

    @Override
    public final void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("lid");


        //this ensures that only the caller listener receive the return when launch the FORCE_NOTIFY broadcast to
        // receive initial data from player, this saves the device to reload data from all listeners  unnecessarily
        if (!id.equals(BROADCASTED_FOR_ALL) && !id.equals(LISTENER_ID)) return;

        //  Log.d(App.myFuckingUniqueTAG + "PlayerProgressListener", "onReceive:  LISTENER CALLED: "+id+" (id: "+LISTENER_ID+") action: "+intent.getAction());


        switch (Objects.requireNonNull(intent.getAction())) {
            case Const.PLAY_PAUSE:
                playPauseChanged(intent.getBooleanExtra("play", false));
                break;
            case Const.PROGRESS:
                String m = intent.getStringExtra("millis");
                long millis = m == null ? 0 : Long.parseLong(m);
                progressChanged(intent.getFloatExtra("percent", 50), intent.getStringExtra("timer"), millis);
                break;
            case Const.TRACK_CHANGED:
                trackChanged((Track) intent.getSerializableExtra("track"));
                break;
            case Const.STOP:
                onPlayerStop();
                break;
            case Const.RELEASING:
                releasing();
                break;
            case Const.TRACK_NOT_FOUND:
                trackNotFound();
                break;
            case Const.TRACK_ENDED:
                trackEnded();
                break;
        }
     //   Log.d(App.myFuckingUniqueTAG + "PlayerProgressListener", "callback received: " + className + ": " + intent.getAction());

    }

    private void trackNotFound() {
        Log.d("PlayerProgressListener", "trackNotFound: ");
    }


    public PlayerProgressListener(String className) {
        this.className = className;
        Context mContext = App.binder.get();
        broadcastManager = LocalBroadcastManager.getInstance(mContext);
        LISTENER_ID = IdUtils.createStringObjectId();
    }

    protected void trackChanged(final Track newTrack) {

    }

    /**
     * Only broadcasted when a track is over
     */
    protected void trackEnded() {

    }


    protected void progressChanged(final float percent, final String timer, long millis) {
        // Log.d("PlayerProgressListener", "progressChanged: playback percent: " + percent + "%, timer: " + timer);
    }

    protected void playPauseChanged(final boolean play) {
        Log.d("PlayerProgressListener", "playPauseChanged: "+ " ("+ className+") " + ((play ? "REPRODUZINDO" : "PAUSADO")));
    }

    protected void onPlayerStop() {
         Log.d("PlayerProgressListener",  " ("+ className+") " +"onPlayerStop: STOPPED");
    }

    private void releasing() {
        stopReceiving();
        //     Log.d("PlayerProgressListener", "releasing: CALLBACK UNREGISTERED");
    }

    public final void startReceiving() {
        if (receiveing) return;
        receiveing = true;
        broadcastManager.registerReceiver(this, new IntentFilter(Const.PLAY_PAUSE));
        broadcastManager.registerReceiver(this, new IntentFilter(Const.PROGRESS));
        broadcastManager.registerReceiver(this, new IntentFilter(Const.TRACK_CHANGED));
        broadcastManager.registerReceiver(this, new IntentFilter(Const.STOP));
        broadcastManager.registerReceiver(this, new IntentFilter(Const.RELEASING));
        broadcastManager.registerReceiver(this, new IntentFilter(Const.TRACK_ENDED));
        broadcastManager.registerReceiver(this, new IntentFilter(Const.TRACK_NOT_FOUND));
        broadcastManager.sendBroadcast(new Intent(Const.FORCE_NOTIFY).putExtra("lid", LISTENER_ID));
        // Log.d(App.myFuckingUniqueTAG + "PlayerProgressListener", "startReceiving: START RECEIVING CALL IN "+LISTENER_ID);
    }

    public final void stopReceiving() {

        broadcastManager.unregisterReceiver(this);
        broadcastManager.unregisterReceiver(this);
        broadcastManager.unregisterReceiver(this);
        broadcastManager.unregisterReceiver(this);
        broadcastManager.unregisterReceiver(this);
        broadcastManager.unregisterReceiver(this);
        broadcastManager.unregisterReceiver(this);
        broadcastManager.unregisterReceiver(this);
        //  Log.d("PlayerProgressListener", "stopReceiving: STOP RECEIVING CALLS");
        receiveing = false;
    }


}
