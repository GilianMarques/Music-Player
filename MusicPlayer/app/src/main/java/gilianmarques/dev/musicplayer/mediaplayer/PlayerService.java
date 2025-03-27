
package gilianmarques.dev.musicplayer.mediaplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import gilianmarques.dev.musicplayer.utils.App;


@SuppressWarnings("FieldCanBeLocal")
public class PlayerService extends Service {
    public PlayerService() {
    }

    private MusicPlayer instanceHolder;
    public static boolean serviceIsRunning;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(App.myFuckingUniqueTAG + "PlayerService", "onCreate: running...");
        instanceHolder = MusicPlayer.getInstance();
        MyMediaSession mSession = instanceHolder.getMediaSession();
        startForeground(MyMediaSession.notificationId, mSession.mNotification);
        serviceIsRunning = true;

        StateListener stateListener = new StateListener(this) {
            @Override
            protected void onPlayerStop() {
                super.onPlayerStop();
                Log.d(App.myFuckingUniqueTAG + "PlayerService", "onPlayerStop: _________________________________________________");
                instanceHolder = null;
                stopForeground(true);
                stopSelf();
                serviceIsRunning = false;

            }
        };
        instanceHolder.registerListener(stateListener);


    }

}
