package gilianmarques.dev.musicplayer.mediaplayer.structure;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import gilianmarques.dev.musicplayer.BuildConfig;
import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.mediaplayer.service_and_related.MusicService;
import gilianmarques.dev.musicplayer.utils.Utils;

/**
 * Usada para salvar o estado do player e fecha-lo em seguida
 */
public class PlayerStateService extends Service {

    private NotificationManager notificationManager;
    private final String NAME = Utils.getString(R.string.Progresso_da_persistencia_do_estado_do_player);
    private final String CHANNEL_ID = BuildConfig.APPLICATION_ID.concat(NAME);
    private int notificationId = 462791583;

    public PlayerStateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Utils.putOnForeground(this, notificationId);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        new Thread(new Runnable() {
            @Override
            public void run() {
                addNotification();
                saveState();
            }
        }).start();
        super.onCreate();
    }

    private void saveState() {

        MusicPlayer musicPlayer = MusicService.binder.getPlayer();
        MusicService.binder = null; // se n anularele retorna a versao do player q ja chamou releae se o usuario reabrir o app antes da vm fechar ele completamente
        musicPlayer.saveState();
        musicPlayer.release(PlayerStateService.this);
        PlayerStateService.this.stopForeground(true);
        PlayerStateService.this.stopSelf();
        Log.d("PlayerStateService", "search: Player saved and killed!");
    }

    private void addNotification() {

//Set notification information:
        Notification.Builder notificationBuilder = new Notification.Builder(getApplicationContext());
        notificationBuilder.setOngoing(true)
                .setContentTitle(getString(R.string.Salvando_estado))
                .setContentText(getString(R.string.Por_favor_aguarde))
                .setProgress(100, 0, true)
                .setDefaults(0)
                .setSmallIcon(R.drawable.notification_icon)
                .setVibrate(new long[]{0L}); // Passing null here silently fails


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager == null) return;

            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, NAME, importance);
            mChannel.enableVibration(false);
            mChannel.enableLights(false);
            mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(mChannel);
            notificationBuilder.setChannelId(CHANNEL_ID);
        }


        Notification notification = notificationBuilder.build();
        startForeground(notificationId, notification);

    }
}
